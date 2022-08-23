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

package v1.models.errors

import play.api.libs.json.{JsObject, JsValue, Json, Reads, Writes, OWrites}

case class MtdError(code: String, message: String, customJson: Option[JsValue] = None){
  lazy val toJson: JsValue = Json.obj(
    "code" -> this.code,
    "message" -> this.message
  )
}

object MtdError {
  implicit val writes: Writes[MtdError] = {
    case o@MtdError(_, _, None) => o.toJson
    case MtdError("INVALID_REQUEST", "Invalid request penalties", Some(customJson)) => BadRequestErrorPenalties.toJson.as[JsObject] + ("errors" -> Json.toJson(customJson))
    case MtdError("INVALID_REQUEST", _, Some(customJson)) => BadRequestError.toJson.as[JsObject] + ("errors" -> Json.toJson(Seq(customJson)))
    case MtdError(_, _, Some(customJson)) => customJson
  }

  implicit def genericWrites[T <: MtdError]: Writes[T] =
    writes.contramap[T](c => c: MtdError)

  implicit val reads: Reads[MtdError] = Json.reads[MtdError]
}

case class MtdErrorWrapper(code: String, message: String, path: Option[String], errors: Option[Seq[MtdErrorWrapper]] = None)

object MtdErrorWrapper {
  implicit val writes: OWrites[MtdErrorWrapper] = Json.writes[MtdErrorWrapper]

  implicit def genericWrites[T <: MtdErrorWrapper]: OWrites[T] =
    writes.contramap[T](c => c: MtdErrorWrapper)

  implicit val reads: Reads[MtdErrorWrapper] = Json.reads[MtdErrorWrapper]
}

//NRS error
object NrsError extends MtdError("NRS_SUBMISSION_FAILURE", "The submission to NRS from MDTP failed")

// Format Errors
object VrnFormatError extends MtdError("VRN_INVALID", "The provided Vrn is invalid")
object VrnFormatErrorDes extends MtdError("VRN_INVALID", "The provided VRN is invalid")
object VrnNotFound extends MtdError("VRN_NOT_FOUND", "The provided VRN was not found")

//Penalties Errors
object PenaltiesInvalidIdType extends MtdError("INVALID_IDTYPE", "Invalid Id Type")
object PenaltiesInvalidIdValue extends MtdError("INVALID_IDVALUE", "Invalid Id value")
object PenaltiesInvalidDataLimit extends MtdError("INVALID_DATELIMIT", "Invalid Date Limit")
object PenaltiesInvalidCorrelationId extends MtdError("INVALID_CORRELATIONID", "Invalid correlation ID")
object PenaltiesNotDataFound extends MtdError("NO_DATA_FOUND", "No data found")
object PenaltiesDuplicateSubmission extends MtdError("DUPLICATE_SUBMISSION", "Duplicate Submission")
object PenaltiesInvalidIdTypeUnprocessEntity extends MtdError("INVALID_IDTYPE", "Invalid Id Type")
object PenaltiesInvalidIdValueUnprocessEntity extends MtdError("INVALID_ID", "Invalid Id value")
object PenaltiesRequestNotProcessedUnprocessEntity extends MtdError("REQUEST_NOT_PROCESSED", "Request not processed")
object PenaltiesServiceUnavailable extends MtdError("SERVICE_UNAVAILABLE", "Service Unavailable")



//Financial Data Errors
object FinancialInvalidCorrelationId extends MtdError("INVALID_CORRELATIONID", "Invalid Correlation Id")
object FinancialInvalidIdType extends MtdError("INVALID_IDTYPE", "Invalid Id type")
object FinancialInvalidIdNumber extends MtdError("INVALID_IDNUMBER", "Invalid Id Number")
object FinancialInvalidRegimeType extends MtdError("INVALID_REGIME_TYPE", "Invalid Regime Type")
object FinancialInvalidDocNumber extends MtdError("INVALID_DOC_NUMBER", "Invalid Doc Number")
object FinancialInvalidOnlyOpenItems extends MtdError("INVALID_ONLY_OPEN_ITEMS", "Invalid Only Open Items")
object FinancialInvalidIncludeLocks extends MtdError("INVALID_INCLUDE_LOCKS", "Invalid Include Locks")
object FinancialInvalidIncludeAccruedInterest extends MtdError("INVALID_CALCULATE_ACCRUED_INTEREST", "Invalid Calculate Accrued Interest")
object FinancialInvalidIncludeCustomerPaymentInfo extends MtdError("INVALID_CUSTOMER_PAYMENT_INFORMATION", "Invalid Customer Payment Information")
object FinancialInvalidDateFrom extends MtdError("INVALID_DATE_FROM", "Invalid Date From")
object FinancialInvalidDateTo extends MtdError("INVALID_DATE_TO", "Invalid Date to")
object FinancialInvalidDateRange extends MtdError("INVALID_DATE_RANGE", "Invalid Date Range")
object FinancialInvalidRemovePaymentOnAccount extends MtdError("INVALID_REMOVE_PAYMENT_ON_ACCOUNT", "Invalid Remove Payment On Account")
object FinancialInvalidIncludeStats extends MtdError("INVALID_INCLUDE_STATISTICAL", "Invalid Include Statistical")
object FinancialInvalidRequest extends MtdError("INVALID_REQUEST", "Invalid Request")
object FinancialNotDataFound extends MtdError("NO_DATA_FOUND", "No data found")
object FinancialDuplicateSubmission extends MtdError("DUPLICATE_SUBMISSION", "Duplicate Submission")
object FinancialInvalidIdTypeUnprocessEntity extends MtdError("INVALID_IDTYPE", "Invalid Id Type")
object FinancialInvalidIdValueUnprocessEntity extends MtdError("INVALID_ID", "Invalid Id value")
object FinancialInvalidRegimeUnprocessEntity extends MtdError("INVALID_REGIME_TYPE", "Invalid Regime type")
object FinancialInvalidDocNumberUnprocessEntity extends MtdError("INVALID_DOC_NUMBER", "Invalid Doc Number")
object FinancialInvalidRequestUnprocessEntity extends MtdError("REQUEST_NOT_PROCESSED", "Request Not Processed")
object FinancialServiceUnavailable extends MtdError("SERVICE_UNAVAILABLE", "Service Unavailable")

// Rule Errors
object RuleIncorrectOrEmptyBodyError extends MtdError("RULE_INCORRECT_OR_EMPTY_BODY_SUBMITTED", "An empty or non-matching body was submitted")
object RuleInsolventTraderError extends MtdError("RULE_INSOLVENT_TRADER", "The remote endpoint has indicated that the Trader is insolvent")

// Standard Errors
object NotFoundError extends MtdError("MATCHING_RESOURCE_NOT_FOUND", "Matching resource not found")
object DownstreamError extends MtdError("INTERNAL_SERVER_ERROR", "An internal server error occurred")
object BadRequestError extends MtdError("INVALID_REQUEST", "Invalid request")
object BadRequestErrorPenalties extends MtdError("INVALID_REQUEST", "Invalid request penalties")
object BVRError extends MtdError("BUSINESS_ERROR", "Business validation error")
object ServiceUnavailableError extends MtdError("SERVICE_UNAVAILABLE", "Internal server error")
object InvalidJson extends MtdError("INVALID_JSON", "Invalid JSON received")
object UnexpectedFailure {
  def mtdError(status: Int, body: String): MtdError = MtdError("UNEXPECTED_FAILURE", s"Unexpected failure. Status $status, body $body")
}

// Authorisation Errors
object UnauthorisedError extends MtdError("CLIENT_OR_AGENT_NOT_AUTHORISED", "The client and/or agent is not authorised")
object InvalidBearerTokenError extends MtdError("UNAUTHORIZED", "Bearer token is missing or not authorized")

// Legacy Authorisation Errors
object LegacyUnauthorisedError extends MtdError("CLIENT_OR_AGENT_NOT_AUTHORISED", "The client and/or agent is not authorised.")

object ForbiddenDownstreamError extends MtdError(
  code = "INTERNAL_SERVER_ERROR",
  message = "An internal server error occurred",
  customJson = Some(
    Json.parse(
      """
        |{
        |  "code": "INTERNAL_SERVER_ERROR",
        |  "message": "An internal server error occurred"
        |}
      """.stripMargin
    )
  )
)

// Accept header Errors
object InvalidAcceptHeaderError extends MtdError("ACCEPT_HEADER_INVALID", "The accept header is missing or invalid")
object UnsupportedVersionError extends MtdError("NOT_FOUND", "The requested resource could not be found")
object InvalidBodyTypeError extends MtdError("INVALID_BODY_TYPE", "Expecting text/json or application/json body")

// Custom VAT errors
object LegacyNotFoundError extends MtdError("NOT_FOUND", "The remote endpoint has indicated that no data can be found")

object RuleDateRangeTooLargeError extends MtdError(
  code = "DATE_RANGE_TOO_LARGE",
  message = "The date of the requested return cannot be further than four years from the current date.",
  customJson = Some(
    Json.parse(
      """
        |{
        |  "code": "BUSINESS_ERROR",
        |  "message": "Business validation error",
        |  "errors": [
        |      {
        |        "code": "DATE_RANGE_TOO_LARGE",
        |        "message": "The date of the requested return cannot be further than four years from the current date."
        |      }
        |  ]
        |}
      """.stripMargin
    )
  )
)

object InvalidDateToErrorDes extends MtdError("DATE_TO_INVALID", "The provided to date is invalid")
object InvalidDateFromErrorDes extends MtdError("DATE_FROM_INVALID", "The provided from date is invalid")
object TaxPeriodNotEnded extends MtdError("TAX_PERIOD_NOT_ENDED", "The remote endpoint has indicated that the submission is for a tax period that has not ended")

object DuplicateVatSubmission extends MtdError(
  code = "DUPLICATE_SUBMISSION",
  message = "The VAT return was already submitted for the given period.",
  customJson = Some(
    Json.parse(
      """
        |{
        |  "code": "BUSINESS_ERROR",
        |  "message": "Business validation error",
        |  "errors": [
        |      {
        |        "code": "DUPLICATE_SUBMISSION",
        |        "message": "The VAT return was already submitted for the given period."
        |      }
        |  ]
        |}
      """.stripMargin
    )
  ))