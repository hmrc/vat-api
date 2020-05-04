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

import play.api.libs.json.Json

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

