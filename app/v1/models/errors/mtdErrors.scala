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

import play.api.libs.json.{JsObject, JsValue, Json, Writes}

case class MtdError(code: String, message: String)

object MtdError {
  implicit val writes: Writes[MtdError] = Json.writes[MtdError]
}

trait CustomError {
  val json: JsValue
}

object CustomError {
  implicit val writes: Writes[CustomError] = (o: CustomError) => o.json
}

// Format Errors
object FormatVrnError extends MtdError("VRN_INVALID", "The provided Vrn is invalid")

// Rule Errors
object RuleIncorrectOrEmptyBodyError extends MtdError("RULE_INCORRECT_OR_EMPTY_BODY_SUBMITTED", "An empty or non-matching body was submitted")

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
object FormatPeriodKeyError extends MtdError("INVALID_REQUEST", "Invalid request") with CustomError {
  val json: JsValue = Json.parse(
    """
      |{
      |  "code": "INVALID_REQUEST",
      |  "message": "Invalid request",
      |  "errors": [
      |      {
      |        "code": "PERIOD_KEY_INVALID",
      |        "message": "Invalid period key"
      |      }
    """.stripMargin
  )
}

object EmptyNotFoundError extends MtdError("MATCHING_RESOURCE_NOT_FOUND", "") with CustomError {
  val json: JsValue = JsObject.empty
}

object RuleDateRangeTooLargeError extends MtdError("BUSINESS_ERROR", "Business validation error") with CustomError {
  val json: JsValue = Json.parse(
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
}