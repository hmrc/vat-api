/*
 * Copyright 2024 HM Revenue & Customs
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

import play.api._
import play.api.http.DefaultHttpErrorHandler
import play.api.libs.json.Json
import play.api.mvc.Results.{BadRequest, NotImplemented}
import play.api.mvc.{RequestHeader, Result}
import play.api.routing.Router
import uk.gov.hmrc.http.NotImplementedException
import v1.models.errors._

import javax.inject._
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class LegacyErrorHandler @Inject()(env: Environment,
                                   config: Configuration,
                                   sourceMapper: OptionalSourceMapper,
                                   router: Provider[Router]
                                  )(implicit ec: ExecutionContext) extends DefaultHttpErrorHandler(env, config, sourceMapper, router) with Logging {

  override def onServerError(request: RequestHeader, ex: Throwable): Future[Result] = {
    super.onServerError(request, ex).map { result =>
      ex match {
        case _ =>
          ex.getCause match {
            case _: NotImplementedException =>
              NotImplemented(Json.toJson(DownstreamError))
            case _ =>
              logger.warn(s"[LegacyErrorHandler][onServerError] uncaught 5xx Exception")
              result
          }
      }
    }
  }

  override protected def onBadRequest(request: RequestHeader, error: String): Future[Result] = {
    super.onBadRequest(request, error).map { _ =>
      error match {
        case "ERROR_VRN_INVALID" => BadRequest(Json.toJson(VrnFormatError))
        case "ERROR_INVALID_FROM_DATE" => BadRequest(Json.toJson(InvalidDateFromErrorDes))
        case "ERROR_INVALID_TO_DATE" => BadRequest(Json.toJson(InvalidDateToErrorDes))
        case "INVALID_STATUS" | "INVALID_DATE_RANGE" => BadRequest(Json.toJson(Json.obj("statusCode" -> 400, "message" -> error)))
        case unmatchedError =>
          logger.warn(s"[LegacyErrorHandler][onBadRequest] - Received unmatched error: '$unmatchedError'")
          BadRequest(Json.toJson(Json.obj("statusCode" -> 400, "message" -> JsonErrorSanitiser.sanitise(unmatchedError))))
      }
    }
  }

  override protected def onDevServerError(request: RequestHeader, ex: UsefulException): Future[Result] = {
    super.onDevServerError(request, ex).map { result =>
      ex match {
        case _ =>
          ex.getCause match {
            case _: NotImplementedException =>
              NotImplemented(Json.toJson(LegacyNotFoundError))
            case _ => result
          }
      }
    }
  }
}