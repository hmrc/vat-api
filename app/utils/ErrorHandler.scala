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

package utils

import javax.inject._
import play.api._
import play.api.http.Status._
import play.api.libs.json.Json
import play.api.mvc.Results._
import play.api.mvc._
import uk.gov.hmrc.auth.core.AuthorisationException
import uk.gov.hmrc.http._
import uk.gov.hmrc.play.HeaderCarrierConverter
import uk.gov.hmrc.play.audit.http.connector.AuditConnector
import uk.gov.hmrc.play.bootstrap.config.HttpAuditEvent
import uk.gov.hmrc.play.bootstrap.http.JsonErrorHandler
import v1.models.errors._

import scala.concurrent._

@Singleton
class ErrorHandler @Inject()(
                              config: Configuration,
                              auditConnector: AuditConnector,
                              httpAuditEvent: HttpAuditEvent
                            )(implicit ec: ExecutionContext) extends JsonErrorHandler(auditConnector, httpAuditEvent, config) {

  import httpAuditEvent.dataEvent

  override def onClientError(request: RequestHeader, statusCode: Int, message: String): Future[Result] = {

    implicit val headerCarrier: HeaderCarrier = HeaderCarrierConverter.fromHeadersAndSession(request.headers, Some(request.session))

    Logger.warn(s"[ErrorHandler][onClientError] error in version 1, for (${request.method}) [${request.uri}] with status:" +
      s" $statusCode and message: $message")
    statusCode match {
      case BAD_REQUEST =>
        auditConnector.sendEvent(dataEvent("ServerValidationError",
          "Request bad format exception", request))
        Future.successful(BadRequest(Json.toJson(BadRequestError)))
      case NOT_FOUND =>
        auditConnector.sendEvent(dataEvent("ResourceNotFound",
          "Resource Endpoint Not Found", request))
        Future.successful(NotFound(Json.toJson(NotFoundError)))
      case _ =>
        val errorCode = statusCode match {
          case UNAUTHORIZED => UnauthorisedError
          case UNSUPPORTED_MEDIA_TYPE => InvalidBodyTypeError
          case _ => MtdError("INVALID_REQUEST", message)
        }

        auditConnector.sendEvent(
          dataEvent(
            eventType = "ClientError",
            transactionName = s"A client error occurred, status: $statusCode",
            request = request,
            detail = Map.empty
          )
        )

        Future.successful(Status(statusCode)(Json.toJson(errorCode)))
    }
  }

  override def onServerError(request: RequestHeader, ex: Throwable): Future[Result] = {
    implicit val headerCarrier: HeaderCarrier = HeaderCarrierConverter.fromHeadersAndSession(request.headers, Some(request.session))

    Logger.warn(s"[ErrorHandler][onServerError] Internal server error in version 1, for (${request.method}) [${request.uri}] -> ", ex)

    val (status, errorCode, eventType) = ex match {
      case _: NotFoundException => (NOT_FOUND, NotFoundError, "ResourceNotFound")
      case _: AuthorisationException => (UNAUTHORIZED, UnauthorisedError, "ClientError")
      case _: JsValidationException => (BAD_REQUEST, BadRequestError, "ServerValidationError")
      case e: HttpException => (e.responseCode, BadRequestError, "ServerValidationError")
      case e: Upstream4xxResponse => (e.reportAs, BadRequestError, "ServerValidationError")
      case e: Upstream5xxResponse => (e.reportAs, DownstreamError, "ServerInternalError")
      case _ => (INTERNAL_SERVER_ERROR, DownstreamError, "ServerInternalError")
    }

    auditConnector.sendEvent(
      dataEvent(
        eventType = eventType,
        transactionName = "Unexpected error",
        request = request,
        detail = Map("transactionFailureReason" -> ex.getMessage)
      )
    )

    Future.successful(Status(status)(Json.toJson(errorCode)))
  }
}

