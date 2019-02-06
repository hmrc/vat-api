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

package uk.gov.hmrc.vatapi.utils

import javax.inject.Inject
import play.api.Configuration
import uk.gov.hmrc.play.audit.http.connector.AuditConnector
import uk.gov.hmrc.play.bootstrap.http.JsonErrorHandler

class ErrorHandler {

  // TODO implement error handling:
  /*
  override def onStart(app: Application): Unit = {
    super.onStart(app)
    application = app
  }

  override def onError(request: RequestHeader, ex: Throwable): Future[Result] = {
    super.onError(request, ex).map { result =>
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


  override def onBadRequest(request: RequestHeader, error: String) = {
    super.onBadRequest(request, error).map { result =>
      error match {
        case "ERROR_VRN_INVALID"       => BadRequest(Json.toJson(ErrorBadRequest(ErrorCode.VRN_INVALID, "The provided Vrn is invalid")))
        case "ERROR_INVALID_DATE"      => BadRequest(Json.toJson(ErrorBadRequest(ErrorCode.INVALID_DATE, "The provided date is invalid")))
        case "ERROR_INVALID_FROM_DATE" => BadRequest(Json.toJson(ErrorBadRequest(ErrorCode.INVALID_FROM_DATE, "The provided from date is invalid")))
        case "ERROR_INVALID_TO_DATE"   => BadRequest(Json.toJson(ErrorBadRequest(ErrorCode.INVALID_TO_DATE, "The provided to date is invalid")))
        case _                         => result
      }
    }
  }
  */


}


// play.http.errorHandler = "uk.gov.hmrc.play.bootstrap.http.JsonErrorHandler
