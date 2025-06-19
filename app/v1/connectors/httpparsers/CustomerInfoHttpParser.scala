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

package v1.connectors.httpparsers

import play.api.http.Status._
import play.api.libs.json.{JsError, JsSuccess, JsValue}
import uk.gov.hmrc.http.{HttpReads, HttpResponse}
import utils.Logging
import v1.connectors.Outcome
import v1.models.errors._
import v1.models.outcomes.ResponseWrapper
import v1.models.response.information.{CustomerInfoDataError, CustomerInfoResponse}

object CustomerInfoHttpParser extends Logging {

  implicit object CustomerInfoHttpReads extends HttpReads[Outcome[CustomerInfoResponse]] with HttpParser {

    def errorHelper(jsonString: JsValue): MtdError = {
      val customerInfoDataError = jsonString.as[CustomerInfoDataError]
      MtdError(customerInfoDataError.code, customerInfoDataError.reason , None)
    }

    def read(method: String, url: String, response: HttpResponse): Outcome[CustomerInfoResponse] = {
      val responseCorrelationId = retrieveCorrelationId(response)
      response.status match {
        case OK => response.json.validate[CustomerInfoResponse] match {
          case JsSuccess(model, _) => Right(ResponseWrapper(responseCorrelationId, model))
          case JsError(errors) =>
            errorConnectorLog(s"[CustomerInfoResponseHttpParser][read] invalid JSON errors: $errors")(response)
            Left(ErrorWrapper(responseCorrelationId, InvalidJson))
        }
        case BAD_REQUEST =>
            errorConnectorLog(s"[CustomerInfoResponseHttpParser][read] CustomerInfoInvalidIdValue JSON errors: $BAD_REQUEST")(response)
            Left(ErrorWrapper(responseCorrelationId, CustomerInfoInvalidIdValue))
        case NOT_FOUND =>
          errorConnectorLog(s"[CustomerInfoResponseHttpParser][read]  CustomerInfoNotDataFound errors: $NOT_FOUND")(response)
          Left(ErrorWrapper(responseCorrelationId, CustomerInfoNotDataFound))
        case _ =>
          errorConnectorLog(s"[CustomerInfoResponseHttpParser][read] status: ${response.status} with Error ${response}")(response)
          Left(ErrorWrapper(responseCorrelationId, DownstreamError))
      }
    }
  }

}
