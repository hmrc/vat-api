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
import v1.models.response.financialData.{ FinancialDataErrorsHIP, FinancialDataResponse }

object FinancialDataHttpParser extends Logging {

  implicit object FinancialDataHttpReads extends HttpReads[Outcome[FinancialDataResponse]] with HttpParser {

    def read(method: String, url: String, response: HttpResponse): Outcome[FinancialDataResponse] = {
      val responseCorrelationId = retrieveCorrelationId(response)
      response.status match {
        case OK | CREATED =>
          response.json.validate[FinancialDataResponse] match {
            case JsSuccess(model, _) => Right(ResponseWrapper(responseCorrelationId, model))
            case JsError(errors) =>
              errorConnectorLog(s"[FinancialDataHttpParser][read] invalid JSON errors: $errors")(response)
              Left(ErrorWrapper(responseCorrelationId, InvalidJson))
          }
        case status =>
          val mtdErrors = errorHelper(response.json)
          errorConnectorLog(s"[FinancialDataHttpParser][read] status: $status with Error $mtdErrors")(response)
          Left(ErrorWrapper(responseCorrelationId, mtdErrors))
      }
    }

    def errorHelper(jsonString: JsValue): MtdError = {
      jsonString.validate[FinancialDataErrorsHIP] match {
        case JsSuccess(errorsHIP, _) => convertToMtdErrorsHIP(errorsHIP)
        case JsError(errors) =>
          MtdError("SERVER_ERROR", s"Unable to validate json error response with errors: $errors", Some(jsonString))
      }
    }

    private def convertToMtdErrorsHIP(errorsHIP: FinancialDataErrorsHIP): MtdError = {
      val error = errorsHIP.errors
      error.code match {
        case "002"                                        => DownstreamError // Invalid Tax Regime
        case "003"                                        => DownstreamError // Request could not be processed (ETMP issue)
        case "015"                                        => DownstreamError // Invalid ID Type
        case "016"                                        => FinancialInvalidIdNumber // Invalid ID Number
        case "017" if error.text == "Invalid Search Type" => DownstreamError // Invalid Search Type
        case "017"                                        => FinancialInvalidSearchItem // Invalid Document or Charge Ref
        case "018"                                        => FinancialNotDataFound // No Data Identified
        case "019"                                        => DownstreamError // Invalid Data Type
        case "020"                                        => DownstreamError // Invalid Date Range
        case _                                            => MtdError(error.code, error.text)
      }
    }
  }

}
