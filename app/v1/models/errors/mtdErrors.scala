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

package v1.models.errors

import play.api.libs.json.{JsValue, Json, Writes}

case class MtdError(code: String, message: String, customJson: Option[JsValue] = None){
  lazy val toJson: JsValue = Json.obj(
    "code" -> this.code,
    "message" -> this.message
  )
}

object MtdError {
  implicit val writes: Writes[MtdError] = {
    case o@MtdError(_, _, None) => o.toJson
    case MtdError(_, _, Some(customJson)) => customJson
  }
}

// Format Errors
object VrnFormatError extends MtdError("VRN_INVALID", "The provided Vrn is invalid")
object VrnFormatErrorDes extends MtdError("VRN_INVALID", "The provided VRN is invalid")

object InvalidFromError extends MtdError("INVALID_DATE_FROM", "Invalid date from", Some(Json.parse(
  """
    |{
    |   "statusCode": 400,
    |   "message": "INVALID_DATE_FROM"
    |}
    |""".stripMargin)))

object InvalidToError extends MtdError("INVALID_DATE_TO", "Invalid date to", Some(Json.parse(
  """
    |{
    |   "statusCode": 400,
    |   "message": "INVALID_DATE_TO"
    |}
    |""".stripMargin)))

object InvalidStatusError extends MtdError("INVALID_STATUS", "Invalid status", Some(Json.parse(
  """
    |{
    |   "statusCode": 400,
    |   "message": "INVALID_STATUS"
    |}
    |""".stripMargin)))

// Rule Errors
object RuleIncorrectOrEmptyBodyError extends MtdError("RULE_INCORRECT_OR_EMPTY_BODY_SUBMITTED", "An empty or non-matching body was submitted")

object RuleDateRangeInvalidError extends MtdError("INVALID_DATE_RANGE", "Invalid date range, must be 366 days or less", Some(Json.parse(
  """
    |{
    |    "statusCode": 400,
    |    "message": "INVALID_DATE_RANGE"
    |}
    |""".stripMargin)))

object RuleMissingDateRangeError extends MtdError("MISSING_DATE_RANGE", "Missing date range", Some(Json.parse(
  """
    |{
    |    "statusCode": 400,
    |    "message": "MISSING_DATE_RANGE"
    |}
    |""".stripMargin)))

// Standard Errors
object NotFoundError extends MtdError("MATCHING_RESOURCE_NOT_FOUND", "Matching resource not found")

object DownstreamError extends MtdError("INTERNAL_SERVER_ERROR", "An internal server error occurred")

object BadRequestError extends MtdError("INVALID_REQUEST", "Invalid request")

object BVRError extends MtdError("BUSINESS_ERROR", "Business validation error")

object ServiceUnavailableError extends MtdError("SERVICE_UNAVAILABLE", "Internal server error")

// Authorisation Errors
object UnauthorisedError extends MtdError("CLIENT_OR_AGENT_NOT_AUTHORISED", "The client and/or agent is not authorised")

object InvalidBearerTokenError extends MtdError("UNAUTHORIZED", "Bearer token is missing or not authorized")

// Accept header Errors
object InvalidAcceptHeaderError extends MtdError("ACCEPT_HEADER_INVALID", "The accept header is missing or invalid")

object UnsupportedVersionError extends MtdError("NOT_FOUND", "The requested resource could not be found")

object InvalidBodyTypeError extends MtdError("INVALID_BODY_TYPE", "Expecting text/json or application/json body")

// Custom VAT errors
object PeriodKeyFormatError extends MtdError(
  code = "PERIOD_KEY_INVALID",
  message = "Invalid period key",
  customJson = Some(
    Json.parse(
      """
        |{
        |  "code": "INVALID_REQUEST",
        |  "message": "Invalid request",
        |  "errors": [
        |      {
        |        "code": "PERIOD_KEY_INVALID",
        |        "message": "Invalid period key"
        |      }
        |    ]
        |}
      """.stripMargin
    )
  )
)

object PeriodKeyFormatErrorDes extends MtdError(code = "PERIOD_KEY_INVALID", message = "Invalid period key", None)

object PeriodKeyFormatErrorDesNotFound extends MtdError(
  code = "PERIOD_KEY_INVALID",
  message = "Invalid period key",
  customJson = Some(
    Json.parse(
      """
        |{
        |  "code": "PERIOD_KEY_INVALID",
        |  "message": "Invalid period key"
        |}
      """.stripMargin
    )
  )
)

object EmptyNotFoundError extends MtdError("MATCHING_RESOURCE_NOT_FOUND", "", None)

object LegacyInvalidDateFromError extends MtdError(
  code = "INVALID_DATE_FROM",
  message = "Invalid date from",
  customJson = Some(
    Json.parse(
      """
        |{
        |  "statusCode": 400,
        |  "message": "INVALID_DATE_FROM"
        |}
        |""".stripMargin
    )
  )
)

object LegacyInvalidDateToError extends MtdError(
  code = "INVALID_DATE_TO",
  message = "Invalid date to",
  customJson = Some(
    Json.parse(
      """
        |{
        |  "statusCode": 400,
        |  "message": "INVALID_DATE_TO"
        |}
        |""".stripMargin
    )
  )
)

object LegacyInvalidDateRangeError extends MtdError(
  code = "INVALID_DATE_RANGE",
  message = "Invalid date to",
  customJson = Some(
    Json.parse(
      """
        |{
        |  "statusCode": 400,
        |  "message": "INVALID_DATE_RANGE"
        |}
        |""".stripMargin
    )
  )
)

object LegacyInvalidStatusError extends MtdError(
  code = "INVALID_STATUS",
  message = "Invalid status",
  customJson = Some(
    Json.parse(
      """
        |{
        |  "statusCode": 400,
        |  "message": "INVALID_STATUS"
        |}
        |""".stripMargin
    )
  )
)

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

object InvalidInputDataError extends MtdError(
  code = "DATE_RANGE_TOO_LARGE",
  message = "The date of the requested return cannot be further than four years from the current date.",
  customJson = Some(
    Json.parse(
      """
        |{
        |  "code": "INVALID_REQUEST",
        |  "message": "Invalid request"
        |}
      """.stripMargin
    )
  )
)

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

object LegacyUnauthorisedError extends MtdError("CLIENT_OR_AGENT_NOT_AUTHORISED", "The client and/or agent is not authorised.")

object InvalidDataError extends MtdError("INVALID_DATA", "The provided data has failed validation, contains invalid data")

object InvalidDateToError extends MtdError("DATE_TO_INVALID", "The provided to date is invalid")

object InvalidDateFromError extends MtdError("DATE_FROM_INVALID", "The provided from date is invalid")

object InvalidDesStatusError extends MtdError("INVALID_STATUS","The provided data is failed validation, invalid status")

