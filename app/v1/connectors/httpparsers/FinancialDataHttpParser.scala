/*
 * Copyright 2022 HM Revenue & Customs
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
import play.api.libs.json.{JsError, JsSuccess, JsValue, Json}
import uk.gov.hmrc.http.{HttpReads, HttpResponse}
import utils.Logging
import v1.connectors.Outcome
import v1.models.errors._
import v1.models.outcomes.ResponseWrapper
import v1.models.response.financialData.{FinancialDataErrors, FinancialDataResponse}

object FinancialDataHttpParser extends Logging {

  implicit object FinancialDataHttpReads extends HttpReads[Outcome[FinancialDataResponse]] with HttpParser {

    //TODO change content after user research
    def errorHelper(jsonString: JsValue): MtdError = {
      val financialDataErrors = jsonString.as[FinancialDataErrors]
      val mtdErrorsConvert = financialDataErrors.failures.map { error =>
        (error.code) match {
          case ("INVALID_IDNUMBER")                              => FinancialInvalidIdNumber
          case ("INVALID_SEARCH_ITEM")                           => FinancialInvalidSearchItem
          case ("NO_DATA_FOUND")                                 => FinancialNotDataFound
          case ("INVALID_CORRELATIONID")                         => DownstreamError
          case ("INVALID_IDTYPE")                                => DownstreamError
          case ("INVALID_REGIME_TYPE")                           => DownstreamError
          case ("INVALID_SEARCH_TYPE")                           => DownstreamError
          case ("INVALID_SEARCH_ITEM")                           => DownstreamError
          case ("INVALID_DATE_FROM")                             => DownstreamError
          case ("INVALID_DATE_TO")                               => DownstreamError
          case ("INVALID_DATE_TYPE")                             => DownstreamError
          case ("INVALID_DATE_RANGE")                            => DownstreamError
          case ("INVALID_INCLUDE_CLEARED_ITEMS")                 => DownstreamError
          case ("INVALID_INCLUDE_STATISTICAL_ITEMS")             => DownstreamError
          case ("INVALID_INCLUDE_PAYMENT_ON_ACCOUNT")            => DownstreamError
          case ("INVALID_ADD_REGIME_TOTALISATION")               => DownstreamError
          case ("INVALID_ADD_LOCK_INFORMATION")                  => DownstreamError
          case ("INVALID_ADD_PENALTY_DETAILS")                   => DownstreamError
          case ("INVALID_ADD_POSTED_INTEREST_DETAILS")           => DownstreamError
          case ("INVALID_ADD_ACCRUING_INTEREST_DETAILS")         => DownstreamError
          case ("INVALID_REQUEST")                               => DownstreamError
          case ("INVALID_TARGETED_SEARCH")                       => DownstreamError
          case ("INVALID_SELECTION_CRITERIA")                    => DownstreamError
          case ("INVALID_DATA_ENRICHMENT")                       => DownstreamError
          case ("DUPLICATE_SUBMISSION")                          => DownstreamError
          case ("INVALID_ID")                                    => DownstreamError
          case ("INVALID_DOC_NUMBER_OR_CHARGE_REFERENCE_NUMBER") => FinancialInvalidSearchItem
          case ("REQUEST_NOT_PROCESSED")                         => DownstreamError
          case ("INVALID_DATA_TYPE")                             => DownstreamError
          case ("INVALID_DATE_RANGE")                            => DownstreamError
          case ("SERVER_ERROR")                                  => DownstreamError
          case ("SERVICE_UNAVAILABLE")                           => DownstreamError
          case _ => MtdError(error.code, error.reason)
        }
      }

      val head = mtdErrorsConvert.head
      val error = if (mtdErrorsConvert.tail.isEmpty) {
        head
      } else if (mtdErrorsConvert.contains(DownstreamError)) {
        DownstreamError
      } else {
        MtdError("INVALID_REQUEST", "Invalid request financial details", Some(Json.toJson(mtdErrorsConvert)))
      }
      error
    }

    //TODO more error handling can be added once scenarios confirmed by Penalties team
    def read(method: String, url: String, response: HttpResponse): Outcome[FinancialDataResponse] = {
      val responseCorrelationId = retrieveCorrelationId(response)
      response.status match {
        case OK => response.json.validate[FinancialDataResponse] match {
          case JsSuccess(model, _) => Right(ResponseWrapper(responseCorrelationId, model))
          case JsError(errors) =>
            logger.error(s"[FinancialDataResponseReads][read] invalid JSON errors: $errors")
            Left(ErrorWrapper(responseCorrelationId, InvalidJson))
        }
        case status =>
          val mtdErrors = errorHelper(response.json)
          logger.error(s"[FinancialDataHttpParser][read] status: ${status} with Error ${mtdErrors}")
          Left(ErrorWrapper(responseCorrelationId, mtdErrors))
      }
    }
  }

}
