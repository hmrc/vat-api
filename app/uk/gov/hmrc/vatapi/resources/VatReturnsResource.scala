/*
 * Copyright 2018 HM Revenue & Customs
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
import play.api.libs.json.{JsNull, JsValue, Json, OFormat}
import play.api.mvc.{Action, AnyContent, Request}
import uk.gov.hmrc.domain.Vrn
import uk.gov.hmrc.vatapi.audit.AuditEvent
import uk.gov.hmrc.vatapi.audit.AuditService.audit
import uk.gov.hmrc.vatapi.connectors.VatReturnsConnector
import uk.gov.hmrc.vatapi.models.{Errors, VatReturnDeclaration}
import uk.gov.hmrc.vatapi.orchestrators.VatReturnsOrchestrator
import uk.gov.hmrc.vatapi.resources.wrappers.VatReturnResponse

import scala.concurrent.ExecutionContext.Implicits.global

object VatReturnsResource extends BaseResource {

  private val connector = VatReturnsConnector
  private val orchestrator = VatReturnsOrchestrator

  def submitVatReturn(vrn: Vrn): Action[JsValue] = APIAction(vrn).async(parse.json) { implicit request =>
    val receiptId = "Receipt-ID"
    val receiptTimestamp = "Receipt-Timestamp"
    val receiptSignature = "Receipt-Signature"

    logger.debug(s"[VatReturnsResource][submitVatReturn] - Submitting Vat Return")
    fromDes {
      for {
        vatReturn <- validateJson[VatReturnDeclaration](request.body)
        _ <- authorise(vatReturn) { case _ if !vatReturn.finalised => Errors.NotFinalisedDeclaration }
        response <- BusinessResult { orchestrator.submitVatReturn(vrn, vatReturn) }
        _ <-  audit(SubmitVatReturnEvent(vrn, response))
      } yield response
    } onSuccess { response =>
      response.filter { case 200 => response.jsonOrError match {
        case Right(vatReturnDesResponse) =>
          logger.debug(s"[VatReturnsResource][submitVatReturn] - Successfully created ")
          Created(Json.toJson(vatReturnDesResponse)).withHeaders(
            receiptId -> response.nrsData.nrSubmissionId,
            receiptTimestamp -> response.nrsData.timestamp,
            receiptSignature -> response.nrsData.cadesTSignature
          )
      }
      }
    }
  }

  def retrieveVatReturns(vrn: Vrn, periodKey: String): Action[AnyContent] =
    APIAction(vrn).async { implicit request =>
      logger.debug(s"[VatReturnsResource] [retrieveVatReturns] Retrieve VAT returns for the VRN : $vrn")
      fromDes {
        for {
          _ <- validate[String](periodKey) { case _ if periodKey.length != 4 => Errors.InvalidPeriodKey }
          response <- execute { _ => connector.query(vrn, periodKey) }
          _ <-  audit(RetrieveVatReturnEvent(vrn, response))
        } yield response
      } onSuccess { response =>
        response.filter {
          case 200 => response.vatReturnOrError match {
            case Right(vatReturn) => Ok(Json.toJson(vatReturn))
            case Left(error) =>
              logger.error(s"[VatReturnsResource] [retrieveVatReturns] Json format from DES doesn't match the VatReturn model: ${error.msg}")
              InternalServerError
          }
        }
      }
    }

  private case class SubmitVatReturn(vrn: Vrn, httpStatus: Int, requestPayload: JsValue, responsePayload: JsValue)

  private implicit val submitVatReturnFormat: OFormat[SubmitVatReturn] = Json.format[SubmitVatReturn]

  private def SubmitVatReturnEvent(vrn: Vrn, response: VatReturnResponse)(implicit request: Request[JsValue]): AuditEvent[SubmitVatReturn] =
    AuditEvent(
      auditType = "submitVatReturn",
      transactionName = "vat-return-create",
      detail = SubmitVatReturn(vrn, response.status, request.body, response.jsonOrError.right.getOrElse(JsNull))
    )

  private case class RetrieveVatReturn(vrn: Vrn, httpStatus: Int, responsePayload: JsValue)

  private implicit val retrieveVatReturnFormat: OFormat[RetrieveVatReturn] = Json.format[RetrieveVatReturn]

  private def RetrieveVatReturnEvent(vrn: Vrn, response: VatReturnResponse): AuditEvent[RetrieveVatReturn] =
    AuditEvent(
      auditType = "retrieveVatReturns",
      transactionName = "vat-retrieve-vat-returns",
      detail = RetrieveVatReturn(vrn, response.status, response.jsonOrError.right.getOrElse(JsNull))
    )

}
