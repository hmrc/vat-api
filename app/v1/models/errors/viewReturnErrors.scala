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

import play.api.libs.json.Json

// Parser Errors
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

// Service Errors
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

object PeriodKeyFormatErrorDes extends MtdError(code = "PERIOD_KEY_INVALID", message = "Invalid period key", None)

object EmptyNotFoundError extends MtdError("MATCHING_RESOURCE_NOT_FOUND", "", None)
