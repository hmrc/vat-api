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
import play.api.libs.json.{JsError, JsSuccess, JsValue}
import uk.gov.hmrc.http.{HttpReads, HttpResponse}
import utils.Logging
import v1.connectors.Outcome
import v1.models.errors._
import v1.models.outcomes.ResponseWrapper
import v1.models.response.financialData.{FinancialDataErrors, FinancialDataResponse}

object FinancialDataHttpParser extends Logging {

  implicit object FinancialDataHttpReads extends HttpReads[Outcome[FinancialDataResponse]] with HttpParser {

    //TODO change content after user research
    def errorHelper(jsonString: JsValue, status: Int): (MtdError, Option[List[MtdError]]) = {
      val financialDataErrors = jsonString.as[FinancialDataErrors]
      val mtdErrorsConvert = financialDataErrors.failures.map{ error =>
        (error.code, status) match {
          case ("INVALID_IDNUMBER", _)                              => FinancialInvalidIdNumber
          case ("INVALID_SEARCH_ITEM", _)                           => FinancialInvalidSearchItem
          case ("NO_DATA_FOUND", _)                                 => FinancialNotDataFound
          case ("INVALID_CORRELATIONID", _)                         => DownstreamError
          case ("INVALID_IDTYPE", _)                                => DownstreamError
          case ("INVALID_REGIME_TYPE", _)                           => DownstreamError
          case ("INVALID_SEARCH_TYPE", _)                           => DownstreamError
          case ("INVALID_DATE_FROM", _)                             => DownstreamError
          case ("INVALID_DATE_TO", _)                               => DownstreamError
          case ("INVALID_DATE_TYPE", _)                             => DownstreamError
          case ("INVALID_INCLUDE_CLEARED_ITEMS", _)                 => DownstreamError
          case ("INVALID_INCLUDE_STATISTICAL_ITEMS", _)             => DownstreamError
          case ("INVALID_INCLUDE_PAYMENT_ON_ACCOUNT", _)            => DownstreamError
          case ("INVALID_ADD_REGIME_TOTALISATION", _)               => DownstreamError
          case ("INVALID_INCLUDE_STATISTICAL", _)                   => DownstreamError
          case ("INVALID_ADD_LOCK_INFORMATION", _)                  => DownstreamError
          case ("INVALID_ADD_PENALTY_DETAILS", _)                   => DownstreamError
          case ("INVALID_ADD_POSTED_INTEREST_DETAILS", _)           => DownstreamError
          case ("INVALID_ADD_ACCRUING_INTEREST_DETAILS", _)         => DownstreamError
          case ("DUPLICATE_SUBMISSION", _)                          => DownstreamError
          case ("INVALID_ID", _)                                    => DownstreamError
          case ("INVALID_IDTYPE", _)                                => DownstreamError
          case ("INVALID_REGIME_TYPE", _)                           => DownstreamError
          case ("INVALID_DOC_NUMBER_OR_CHARGE_REFERENCE_NUMBER", _) => FinancialInvalidSearchItem
          case ("REQUEST_NOT_PROCESSED", _)                         => DownstreamError
          case ("SERVER_ERROR", _)                                  => DownstreamError
          case ("SERVICE_UNAVAILABLE", _)                           => DownstreamError
          case _ => MtdError(error.code, error.reason)
        }
      }

      val head = mtdErrorsConvert.head
      val tail = if(mtdErrorsConvert.tail.isEmpty) None else Some(mtdErrorsConvert.tail)
      (head, tail)
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
          val mtdErrors = errorHelper(response.json, status)
          logger.error(s"[FinancialDataHttpParser][read] status: ${status} with Error ${mtdErrors._1} ${mtdErrors._2}")
          Left(ErrorWrapper(responseCorrelationId, mtdErrors._1, mtdErrors._2))
      }
    }
  }

}
