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
import play.api.libs.json.{ JsError, JsSuccess, JsValue }
import uk.gov.hmrc.http.{ HttpReads, HttpResponse }
import utils.Logging
import v1.connectors.Outcome
import v1.models.errors._
import v1.models.outcomes.ResponseWrapper
import v1.models.response.penalties.{ PenaltiesErrorsHIP, PenaltiesResponse }

object PenaltiesHttpParser extends Logging {

  implicit object PenaltiesHttpReads extends HttpReads[Outcome[PenaltiesResponse]] with HttpParser {

    def read(method: String, url: String, response: HttpResponse): Outcome[PenaltiesResponse] = {
      val responseCorrelationId = retrieveCorrelationId(response)
      response.status match {
        case OK =>
          response.json.validate[PenaltiesResponse] match {
            case JsSuccess(model, _) => Right(ResponseWrapper(responseCorrelationId, model))
            case JsError(errors) =>
              errorConnectorLog(s"[PenaltiesHttpParser][read] invalid JSON errors: $errors")(response)
              Left(ErrorWrapper(responseCorrelationId, InvalidJson))
          }
        case FORBIDDEN =>
          errorConnectorLog(s"[PenaltiesHttpParser][read] 403 error: ${response.body}")(response)
          Left(ErrorWrapper(responseCorrelationId, UnauthorisedError))
        case status =>
          val mtdErrors = errorHelper(response.json)
          errorConnectorLog(s"[PenaltiesHttpParser][read] status: $status with Error $mtdErrors")(response)
          Left(ErrorWrapper(responseCorrelationId, mtdErrors))
      }
    }

    def errorHelper(jsonString: JsValue): MtdError = {
      jsonString.validate[PenaltiesErrorsHIP] match {
        case JsSuccess(errorsHIP, _) => convertToMtdErrorsHIP(errorsHIP)
        case JsError(errors) =>
          MtdError("SERVER_ERROR", s"Unable to validate json error response with errors: $errors", Some(jsonString))
      }
    }
  }

  private def convertToMtdErrorsHIP(errorsHIP: PenaltiesErrorsHIP): MtdError = {
    val error = errorsHIP.errors
    error.code match {
      case "002" => DownstreamError // Invalid Tax Regime
      case "003" => DownstreamError // Request could not be processed (ETMP issue)
      case "015" => DownstreamError // Invalid ID Type
      case "016" => PenaltiesInvalidIdValue // Invalid ID Number
      case _     => MtdError(error.code, error.text)
    }
  }

}
