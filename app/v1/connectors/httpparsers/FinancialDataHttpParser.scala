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
          case ("INVALID_CORRELATIONID", BAD_REQUEST)                 => FinancialInvalidCorrelationId
          case ("INVALID_IDTYPE", BAD_REQUEST)                        => FinancialInvalidIdType
          case ("INVALID_IDNUMBER", BAD_REQUEST)                      => FinancialInvalidIdNumber
          case ("INVALID_REGIME_TYPE", BAD_REQUEST)                   => FinancialInvalidRegimeType
          case ("INVALID_DOC_NUMBER", BAD_REQUEST)                    => FinancialInvalidDocNumber
          case ("INVALID_ONLY_OPEN_ITEMS", BAD_REQUEST)               => FinancialInvalidOnlyOpenItems
          case ("INVALID_INCLUDE_LOCKS", BAD_REQUEST)                 => FinancialInvalidIncludeLocks
          case ("INVALID_CALCULATE_ACCRUED_INTEREST", BAD_REQUEST)    => FinancialInvalidIncludeAccruedInterest
          case ("INVALID_CUSTOMER_PAYMENT_INFORMATION", BAD_REQUEST)  => FinancialInvalidIncludeCustomerPaymentInfo
          case ("INVALID_DATE_FROM", BAD_REQUEST)                     => FinancialInvalidDateFrom
          case ("INVALID_DATE_TO", BAD_REQUEST)                       => FinancialInvalidDateTo
          case ("INVALID_DATE_RANGE", BAD_REQUEST)                    => FinancialInvalidDateRange
          case ("INVALID_REMOVE_PAYMENT_ON_ACCOUNT", BAD_REQUEST)     => FinancialInvalidRemovePaymentOnAccount
          case ("INVALID_INCLUDE_STATISTICAL", BAD_REQUEST)           => FinancialInvalidIncludeStats
          case ("INVALID_REQUEST", BAD_REQUEST)                       => FinancialInvalidRequest
          case ("NO_DATA_FOUND", NOT_FOUND) => FinancialNotDataFound
          case ("DUPLICATE_SUBMISSION", CONFLICT) => FinancialDuplicateSubmission
          case ("INVALID_IDTYPE", UNPROCESSABLE_ENTITY) => FinancialInvalidIdTypeUnprocessEntity
          case ("INVALID_ID", UNPROCESSABLE_ENTITY) => FinancialInvalidIdValueUnprocessEntity
          case ("INVALID_REGIME_TYPE", UNPROCESSABLE_ENTITY) => FinancialInvalidRegimeUnprocessEntity
          case ("INVALID_DOC_NUMBER", UNPROCESSABLE_ENTITY) => FinancialInvalidDocNumberUnprocessEntity
          case ("REQUEST_NOT_PROCESSED", UNPROCESSABLE_ENTITY) => FinancialInvalidRequestUnprocessEntity
          case ("SERVICE_UNAVAILABLE", SERVICE_UNAVAILABLE) => FinancialServiceUnavailable
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
