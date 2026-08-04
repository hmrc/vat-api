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

package v1.models.errors

import play.api.libs.json.Json

import java.time.LocalDate

// Parser Errors
object InvalidFromError extends MtdError(
  code = "INVALID_DATE_FROM",
  message = "Invalid date from",
  customJson = Some(
    Json.parse(
      """
        |{
        |   "statusCode": 400,
        |   "message": "INVALID_DATE_FROM"
        |}
      """.stripMargin
    )
  )
)

object InvalidToError extends MtdError(
  code = "INVALID_DATE_TO",
  message = "Invalid date to",
  customJson = Some(
    Json.parse(
      """
        |{
        |   "statusCode": 400,
        |   "message": "INVALID_DATE_TO"
        |}
      """.stripMargin
    )
  )
)

object InvalidStatusError extends MtdError(
  code = "INVALID_STATUS",
  message = "Invalid status",
  customJson = Some(
    Json.parse(
      """
        |{
        |   "statusCode": 400,
        |   "message": "INVALID_STATUS"
        |}
      """.stripMargin
    )
  )
)

// Rule Errors
object RuleDateRangeInvalidError extends MtdError(
  code = "INVALID_DATE_RANGE",
  message = "Invalid date range, must be 366 days or less",
  customJson = Some(
    Json.parse(
      """
        |{
        |    "statusCode": 400,
        |    "message": "INVALID_DATE_RANGE"
        |}
      """.stripMargin
    )
  )
)

object RuleMissingDateRangeError extends MtdError(
  code = "MISSING_DATE_RANGE",
  message = "Missing date range",
  customJson = Some(
    Json.parse(
      """
        |{
        |    "statusCode": 400,
        |    "message": "MISSING_DATE_RANGE"
        |}
      """.stripMargin
    )
  )
)


object RuleOBLDateRangeTooLargeError extends MtdError(
  code = "DATE_RANGE_TOO_LARGE",
  message = "The date of the requested return cannot be further than four years from the current date.",
  customJson = Some(
    Json.parse(
      """
        |{
        |    "code": "DATE_RANGE_TOO_LARGE",
        |    "message": "The date of the requested return cannot be further than four years from the current date."
        |}
      """.stripMargin
    )
  )
)

// Service Errors
object InvalidStatusErrorDes extends MtdError("INVALID_STATUS","The provided data is failed validation, invalid status")

object NoOpenObligation extends MtdError("PERIOD_KEY_INVALID", "The supplied period key does not match an open obligation")

// Internal (TxR assist) obligation matching outcomes
sealed trait OpenObligationMatchError {
  def error: MtdError
  def detail: String
}

object OpenObligationMatchError {

  case class NoMatchingObligation(requested: String, available: Seq[String]) extends OpenObligationMatchError {
    val error: MtdError = NoOpenObligation
    val detail: String  = s"no open obligation matching periodKey : $requested, open periodKeys returned : ${available.mkString(",")}"
  }

  case class PeriodNotEnded(periodKey: String, end: String, today: LocalDate) extends OpenObligationMatchError {
    val error: MtdError = TaxPeriodNotEnded
    val detail: String  = s"periodKey : $periodKey ends $end, which is not before $today"
  }

  case class UnreadableEndDate(periodKey: String, rawEnd: String) extends OpenObligationMatchError {
    val error: MtdError = DownstreamError
    val detail: String  = s"periodKey : $periodKey has an unparseable inboundCorrespondenceToDate : '$rawEnd'"
  }
}
