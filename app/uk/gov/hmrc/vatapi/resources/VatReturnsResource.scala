/*
 * Copyright 2020 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.gov.hmrc.vatapi.resources

import cats.implicits._
import javax.inject.{Inject, Singleton}
import play.api.libs.json._
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import uk.gov.hmrc.domain.Vrn
import uk.gov.hmrc.vatapi.audit.AuditEvents
import uk.gov.hmrc.vatapi.connectors.VatReturnsConnector
import uk.gov.hmrc.vatapi.models.des.VatReturnsDES
import uk.gov.hmrc.vatapi.models.{Errors, VatReturnDeclaration}
import uk.gov.hmrc.vatapi.orchestrators.VatReturnsOrchestrator
import uk.gov.hmrc.vatapi.resources.wrappers.Response
import uk.gov.hmrc.vatapi.services.{AuditService, AuthorisationService}
import uk.gov.hmrc.vatapi.utils.pagerDutyLogging.Endpoint

import scala.concurrent.ExecutionContext

@Singleton
class VatReturnsResource @Inject()(
                                    connector: VatReturnsConnector,
                                    orchestrator: VatReturnsOrchestrator,
                                    val authService: AuthorisationService,
                                    auditService: AuditService,
                                    cc: ControllerComponents
                                  )(implicit ec: ExecutionContext) extends BaseResource(cc) {

  def submitVatReturn(vrn: Vrn): Action[JsValue] = APIAction(vrn, nrsRequired = true).async(parse.json) { implicit request =>
    val receiptId = "Receipt-ID"
    val receiptTimestamp = "Receipt-Timestamp"
    val receiptSignature = "Receipt-Signature"

    logger.debug(s"[VatReturnsResource][submitVatReturn] - Submitting Vat Return")
    val arn = getArn
    val clientId = getClientId

    implicit val endpoint: Endpoint = Endpoint.SubmitReturn

    def audit(vatResult: VatResult, correlationId: String, nrsId: Option[String]) =
      auditService.audit(AuditEvents.submitVatReturn(correlationId,
        request.authContext.affinityGroup, nrsId, clientId, arn, vatResult.auditResponse))

    val result = fromDes {

      for {
        vatReturn <- validateJson[VatReturnDeclaration](request.body)
        _ <- authorise(vatReturn) { case _ if !vatReturn.finalised => Errors.NotFinalisedDeclaration }
        response <- BusinessResult {
          orchestrator.submitVatReturn(vrn, vatReturn, arn)
        }
      } yield response
    }.map {
      case Right(response) =>
        val result = response.filter {
          case OK => response.vatSubmissionReturnOrError match {
            case Right(vatReturnDesResponse) =>
              def successResponse(vatReturn: VatReturnsDES) = {
                logger.debug(s"[VatReturnsResource][submitVatReturn] - Successfully created ")

                VatResult.Success(CREATED, vatReturn)
                  .withHeaders(
                    receiptId -> response.nrsData.nrSubmissionId,
                    receiptTimestamp -> response.nrsData.timestamp,
                    receiptSignature -> response.nrsData.cadesTSignature)
              }

              vatReturnDesResponse.validate[VatReturnsDES] match {
                case JsSuccess(vatReturn, _) => successResponse(vatReturn)
                case JsError(errs) =>
                  logger.warn(s"[VatReturnsResource] [submitVatReturn] Could not read response from DES as a Vat Return $errs")
                  VatResult.Failure(INTERNAL_SERVER_ERROR, Errors.InternalServerError)
              }
          }
        }
        audit(result, response.getCorrelationId, Option(response.nrsData).map(_.nrSubmissionId))
        result

      case Left(errorResult) =>
        logger.warn(s"[VatReturnsResource] [submitVatReturn] Unexpected downstream error $errorResult")
        val result = handleErrors(errorResult)
        audit(result, Response.defaultCorrelationId, None)
        result
    }

    result.recover {
      case ex =>
        logger.warn(s"[VatReturnsResource] [submitVatReturn] Unexpected downstream error thrown ${ex.getMessage}")
        val result = VatResult.Failure(INTERNAL_SERVER_ERROR, Errors.InternalServerError)
        audit(result, Response.defaultCorrelationId, None)
        result
    }.map(_.result)
  }

  def retrieveVatReturns(vrn: Vrn, periodKey: String): Action[AnyContent] =
    APIAction(vrn).async { implicit request =>
      logger.debug(s"[VatReturnsResource] [retrieveVatReturns] Retrieve VAT returns for VRN : $vrn")

      val arn = getArn
      val clientId = getClientId

      implicit val endpoint: Endpoint = Endpoint.RetrieveReturns

      def audit(vatResult: VatResult, correlationId: String) =
        auditService.audit(AuditEvents.retrieveVatReturnsAudit(correlationId,
          request.authContext.affinityGroup, arn, clientId, vatResult.auditResponse))

      val result = fromDes {
        for {
          _ <- validate[String](periodKey) { case _ if periodKey.length != 4 =>
            Errors.InvalidPeriodKey }
          response <- execute { _ => connector.query(vrn, periodKey) }
        } yield response
      }.map {
        case Right(response) =>
          val result = response.filter {
            case OK => response.vatReturnOrError match {
              case Right(vatReturn) =>
                logger.debug(s"[VatReturnsResource] [retrieveVatReturns] Successfully retrieved Vat Return from DES")
                VatResult.Success(OK, vatReturn)
              case Left(error) =>
                logger.error(s"[VatReturnsResource] [retrieveVatReturns] Json format from DES doesn't match the VatReturn model: ${error.msg}")
                VatResult.FailureEmptyBody(INTERNAL_SERVER_ERROR, Errors.InternalServerError)
            }
          }

          audit(result, response.getCorrelationId)
          result

        case Left(errorResult) =>
          logger.warn(s"[VatReturnsResource][retrieveVatReturns] Unexpected downstream error $errorResult")
          val result = handleErrors(errorResult)
          audit(result, Response.defaultCorrelationId)
          result
      }

      result.recover {
        case ex =>
          logger.warn(s"[VatReturnsResource][retrieveVatReturns] Unexpected downstream error thrown ${ex.getMessage}")
          val result = VatResult.Failure(INTERNAL_SERVER_ERROR, Errors.InternalServerError)
          audit(result, Response.defaultCorrelationId)
          result
      }.map(_.result)
    }
}
