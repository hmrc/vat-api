/*
 * Copyright 2019 HM Revenue & Customs
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
import play.api.libs.json.{JsValue, Json, OFormat}
import play.api.mvc.{Action, AnyContent, Result}
import uk.gov.hmrc.domain.Vrn
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.vatapi.audit.AuditEvents
import uk.gov.hmrc.vatapi.config.AppContext
import uk.gov.hmrc.vatapi.connectors.ObligationsConnector
import uk.gov.hmrc.vatapi.models.audit.AuditResponse
import uk.gov.hmrc.vatapi.models.{Errors, ObligationsQueryParams}
import uk.gov.hmrc.vatapi.resources.wrappers.ObligationsResponse
import uk.gov.hmrc.vatapi.services.{AuditService, AuthorisationService}
import v2.models.audit.AuditError

import scala.concurrent.ExecutionContext.Implicits.global

@Singleton
class ObligationsResource @Inject()(
                                     connector: ObligationsConnector,
                                     override val authService: AuthorisationService,
                                     override val appContext: AppContext,
                                     auditService: AuditService
                                   ) extends BaseResource {

  def retrieveObligations(vrn: Vrn, params: ObligationsQueryParams): Action[AnyContent] = APIAction(vrn).async { implicit request =>

    def audit(response: ObligationsResponse, result: Result, userType: String, arn: Option[String])
             (implicit hc: HeaderCarrier, request: AuthRequest[_]) = {
      result.header.status match {
        case OK =>
          auditService.audit(AuditEvents.submitVatReturn(getCorrelationId(response.underlying),
            userType, None,
            arn, AuditResponse(200, None, retrieveBody(result))))
        case status => auditService.audit(AuditEvents.submitVatReturn(getCorrelationId(response.underlying),
          userType, None, arn, AuditResponse(status, Some(Seq(AuditError(retrieveErrorCode(result)))), None)))
      }
    }

    val arn = getArn
    logger.debug(s"[ObligationsResource][retrieveObligations] - Retrieve Obligations for VRN : $vrn")

    val result = fromDes {
      for {
        response <- execute { _ => connector.get(vrn, params) }
      } yield response
    } onSuccess { desResponse =>
      val result = desResponse.filter {
        case OK =>
          desResponse.obligations(vrn) match {
            case Right(Some(obligations)) =>
              val responseBody = Json.toJson(obligations)
              Ok(responseBody)
            case Right(None) =>
              NotFound
            case Left(ex) =>
              logger.error(s"[ObligationsResource][retrieveObligations] Json format from DES doesn't match the Obligations model: ${ex.msg}")
              InternalServerError(Json.toJson(Errors.InternalServerError))
          }
      }
      audit(desResponse, result, request.authContext.affinityGroup, arn)
      result
    }

    result.recover {
      case ex =>
        logger.warn(s"[ObligationsResource][retrieveObligations] Unexpected downstream error thrown ${ex.getMessage}")
        auditService.audit(AuditEvents.submitVatReturn(defaultCorrelationId,
          request.authContext.affinityGroup, None,
          arn, AuditResponse(INTERNAL_SERVER_ERROR, Some(Seq(AuditError(Errors.InternalServerError.code))), None)))
        InternalServerError(Json.toJson(Errors.InternalServerError))
    }
  }

  private case class RetrieveVatObligations(vrn: Vrn, httpStatus: Int, responsePayload: JsValue)

  private implicit val retrieveVatObligationFormat: OFormat[RetrieveVatObligations] = Json.format[RetrieveVatObligations]

}
