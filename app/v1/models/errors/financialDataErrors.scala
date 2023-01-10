/*
 * Copyright 2023 HM Revenue & Customs
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

// Parser errors
object FinancialDataInvalidDateFromError extends MtdError(
  code = "DATE_FROM_INVALID",
  message = "Invalid date from",
  customJson = Some(
    Json.parse(
      """
        |{
        |  "statusCode": 400,
        |  "message": "DATE_FROM_INVALID"
        |}
      """.stripMargin
    )
  )
)

object FinancialDataInvalidDateToError extends MtdError(
  code = "DATE_TO_INVALID",
  message = "Invalid date to",
  customJson = Some(
    Json.parse(
      """
        |{
        |  "statusCode": 400,
        |  "message": "DATE_TO_INVALID"
        |}
      """.stripMargin
    )
  )
)

object FinancialDataInvalidDateRangeError extends MtdError(
  code = "DATE_RANGE_INVALID",
  message = "Invalid date range, must be 365 days or less",
  customJson = Some(
    Json.parse(
      """
        |{
        |    "statusCode": 400,
        |    "message": "DATE_RANGE_INVALID"
        |}
      """.stripMargin
    )
  )
)

// Service Errors
object InvalidDataError extends MtdError("INVALID_DATA", "The provided data is failed validation, contains invalid data")
