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
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.{Action, AnyContent}
import uk.gov.hmrc.domain.Vrn
import uk.gov.hmrc.vatapi.audit.{AuditEvents, AuditService}
import uk.gov.hmrc.vatapi.config.AppContext
import uk.gov.hmrc.vatapi.connectors.ObligationsConnector
import uk.gov.hmrc.vatapi.models.{Errors, ObligationsQueryParams}
import uk.gov.hmrc.vatapi.services.AuthorisationService

import scala.concurrent.ExecutionContext.Implicits.global

object ObligationsResource extends ObligationsResource {
  override val connector = ObligationsConnector
  override val authService = AuthorisationService
  override val appContext = AppContext
  override val auditService = AuditService
}

trait ObligationsResource extends BaseResource {

  val connector: ObligationsConnector
  val auditService: AuditService

  def retrieveObligations(vrn: Vrn, params: ObligationsQueryParams): Action[AnyContent] = APIAction(vrn).async { implicit request =>
    logger.debug(s"[ObligationsResource][retrieveObligations] - Retrieve Obligations for VRN : $vrn")

    val result = fromDes {
      for {
        response <- execute { _ => connector.get(vrn, params) }
      } yield response
    } onSuccess { response =>
      response.filter {
        case 200 =>
          response.obligations(vrn) match {
            case Right(Some(obligations)) =>
              auditService.audit(AuditEvents.retrieveVatObligationsAudit(response.getCorrelationId(), request.authContext.affinityGroup, getArn))
              Ok(Json.toJson(obligations))
            case Right(None) => NotFound
            case Left(ex) =>
              logger.error(s"[ObligationsResource][retrieveObligations] Json format from DES doesn't match the Obligations model: ${ex.msg}")
              InternalServerError(Json.toJson(Errors.InternalServerError))
          }
      }
    }

    result.recover {
      case ex =>
        logger.warn(s"[ObligationsResource][retrieveObligations] Unexpected downstream error thrown ${ex.getMessage}")
        InternalServerError(Json.toJson(Errors.InternalServerError))
    }
  }

  private case class RetrieveVatObligations(vrn: Vrn, httpStatus: Int, responsePayload: JsValue)

  private implicit val retrieveVatObligationFormat = Json.format[RetrieveVatObligations]

}
