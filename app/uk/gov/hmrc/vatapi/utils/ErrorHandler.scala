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

package uk.gov.hmrc.vatapi.utils

import javax.inject._
import play.api._
import play.api.http.Status._
import play.api.libs.json.Json
import play.api.mvc.Results._
import play.api.mvc._
import uk.gov.hmrc.http._
import uk.gov.hmrc.play.HeaderCarrierConverter
import uk.gov.hmrc.play.audit.http.connector.AuditConnector
import uk.gov.hmrc.play.bootstrap.config.HttpAuditEvent
import uk.gov.hmrc.play.bootstrap.http.JsonErrorHandler
import uk.gov.hmrc.vatapi.models.{ErrorBadRequest, ErrorCode, ErrorNotImplemented}

import scala.concurrent._

@Singleton
class ErrorHandler @Inject()(
                              config: Configuration,
                              auditConnector: AuditConnector,
                              httpAuditEvent: HttpAuditEvent
                            )(implicit ec: ExecutionContext) extends JsonErrorHandler(auditConnector, httpAuditEvent, config) {

  override def onClientError(request: RequestHeader, statusCode: Int, message: String): Future[Result] = {

    implicit val headerCarrier: HeaderCarrier = HeaderCarrierConverter.fromHeadersAndSession(request.headers, Some(request.session))

    Logger.warn(s"[ErrorHandler][onClientError] error in version 1, for (${request.method}) [${request.uri}] with status:" +
      s" $statusCode and message: $message")
    statusCode match {
      case BAD_REQUEST =>
        Future { message match {
          case "ERROR_VRN_INVALID" => BadRequest(Json.toJson(ErrorBadRequest(ErrorCode.VRN_INVALID, "The provided Vrn is invalid")))
          case "ERROR_INVALID_DATE" => BadRequest(Json.toJson(ErrorBadRequest(ErrorCode.INVALID_DATE, "The provided date is invalid")))
          case "ERROR_INVALID_FROM_DATE" => BadRequest(Json.toJson(ErrorBadRequest(ErrorCode.INVALID_FROM_DATE, "The provided from date is invalid")))
          case "ERROR_INVALID_TO_DATE" => BadRequest(Json.toJson(ErrorBadRequest(ErrorCode.INVALID_TO_DATE, "The provided to date is invalid")))
          case "INVALID_STATUS" | "INVALID_DATE_RANGE" => BadRequest(Json.toJson(Json.obj("statusCode" -> 400, "message" -> message)))
          case unmatchedError => {
            Logger.warn(s"[ErrorHandler][onBadRequest] - Received unmatched error: '$unmatchedError'")
            BadRequest(Json.toJson(Json.obj("statusCode" -> 400, "message" -> JsonErrorSanitiser.sanitise(unmatchedError))))
          }}
        }
    }
  }

  override def onServerError(request: RequestHeader, ex: Throwable): Future[Result] = {
    super.onServerError(request, ex).map { result =>
      ex match {
        case _ =>
          ex.getCause match {
            case ex: NotImplementedException =>
              NotImplemented(Json.toJson(ErrorNotImplemented))
            case _ => result
          }
      }
    }
  }

}

