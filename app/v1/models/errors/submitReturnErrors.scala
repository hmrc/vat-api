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

object InvalidMonetaryValueError extends MtdError("INVALID_REQUEST", "Invalid request") {
  def withFieldName(fieldName: String, minValue: BigDecimal, maxValue: BigDecimal): MtdError = this.copy(customJson = Some(Json.parse(
    s"""
       |{
       |   "code": "INVALID_MONETARY_AMOUNT",
       |   "message": "amount should be a monetary value (to 2 decimal places), between -9,999,999,999,999.99 and 9,999,999,999,999.99",
       |   "path": "/$fieldName"
       |}
       |""".stripMargin)))

  def withFieldNameAndNonNegative(fieldName: String): MtdError = this.copy(customJson = Some(Json.parse(
    s"""
       |{
       |   "code": "INVALID_MONETARY_AMOUNT",
       |   "message": "amount should be a monetary value (to 2 decimal places), between 0 and 99,999,999,999.99",
       |   "path": "/$fieldName"
       |}
       |""".stripMargin)))

  def withFieldName(fieldName: String): MtdError = this.copy(customJson = Some(Json.parse(
    s"""
       |{
       |   "code": "INVALID_MONETARY_AMOUNT",
       |   "message": "The value must be between -9999999999999 and 9999999999999",
       |   "path": "/$fieldName"
       |}
       |""".stripMargin)))
}

object BodyPeriodKeyFormatError extends MtdError(
  code = "PERIOD_KEY_INVALID",
  message = "Invalid period key",
  customJson = Some(
    Json.parse(
      """
        |{
        |  "code": "INVALID_REQUEST",
        |  "message": "Invalid request",
        |  "errors": [
        |    {
        |      "code": "PERIOD_KEY_INVALID",
        |      "message": "period key should be a 4 character string",
        |      "path": "/periodKey"
        |    }
        |  ]
        |}
      """.stripMargin
    )
  )
)

object InvalidJsonError extends MtdError(
  code = "Invalid Json",
  message = "Invalid json, e.g. \"periodKey\": abc",
  customJson = Some(
    Json.parse(
      """
        |{
        |   "statusCode": 400,
        |   "message": "Invalid Json"
        |}
      """.stripMargin
    )
  )
)

object VATNetValueRuleError extends MtdError(
  code = "VAT_NET_VALUE",
  message = "netVatDue should be the difference between the largest and the smallest values among totalVatDue and vatReclaimedCurrPeriod",
  customJson = Some(
    Json.parse(
      """
        |{
        |  "code": "INVALID_REQUEST",
        |  "message": "Invalid request",
        |  "errors": [
        |    {
        |      "code": "VAT_NET_VALUE",
        |      "message": "netVatDue should be the difference between the largest and the smallest values among totalVatDue and vatReclaimedCurrPeriod",
        |      "path": "/netVatDue"
        |    }
        |  ]
        |}
      """.stripMargin
    )
  )
)

object VATTotalValueRuleError extends MtdError(
  code = "VAT_TOTAL_VALUE",
  message = "totalVatDue should be equal to the sum of vatDueSales and vatDueAcquisitions",
  customJson = Some(
    Json.parse(
      """
        |{
        |  "code": "INVALID_REQUEST",
        |  "message": "Invalid request",
        |  "errors": [
        |    {
        |      "code": "VAT_TOTAL_VALUE",
        |      "message": "totalVatDue should be equal to vatDueSales + vatDueAcquisitions",
        |      "path": "/totalVatDue"
        |    }
        |  ]
        |}
      """.stripMargin
    )
  )
)

object FinalisedValueRuleError extends MtdError(
  code = "NOT_FINALISED",
  message = "User has not declared VAT return as final",
  customJson = Some(
    Json.parse(
      """
        |{
        |  "code": "BUSINESS_ERROR",
        |  "message": "Business validation error",
        |  "errors": [
        |    {
        |      "code": "NOT_FINALISED",
        |      "message": "The return cannot be accepted without a declaration it is finalised.",
        |      "path": "/finalised"
        |    }
        |  ]
        |}
      """.stripMargin
    )
  )
)

object StringFormatRuleError extends MtdError(
  code = "INVALID_STRING_VALUE",
  message = "String with invalid type, e.g.\"periodKey\": 1,",
  customJson = Some(
    Json.parse(
      """
        |{
        |  "code": "INVALID_REQUEST",
        |  "message": "Invalid request",
        |  "errors": [
        |    {
        |      "code": "INVALID_STRING_VALUE",
        |      "message": "please provide a string field",
        |      "path": "/periodKey"
        |    }
        |  ]
        |
        |}
      """.stripMargin
    )
  )
)

object UnMappedPlayRuleError extends MtdError(
  code = "UNMAPPED_PLAY_ERROR",
  message = "error.expected.jsboolean",
  customJson = Some(
    Json.parse(
      """
        |{
        |  "code": "INVALID_REQUEST",
        |  "message": "Invalid request",
        |  "errors": [
        |    {
        |      "code": "UNMAPPED_PLAY_ERROR",
        |      "message": "error.expected.jsboolean",
        |      "path": "/finalised"
        |    }
        |  ]
        |
        |}
      """.stripMargin
    )
  )
)

object NumericFormatRuleError extends MtdError("INVALID_REQUEST", "Invalid request") {
  def withFieldName(fieldName: String): MtdError = this.copy(customJson = Some(Json.parse(
    s"""
       |{
       |    "code": "INVALID_NUMERIC_VALUE",
       |    "message": "please provide a numeric field",
       |    "path": "/$fieldName"
       |}
       |""".stripMargin)))
}

object MandatoryFieldRuleError extends MtdError("INVALID_REQUEST", "Invalid request") {
  def withFieldName(fieldName: String): MtdError = this.copy(customJson = Some(Json.parse(
    s"""
       |{
       |    "code": "MANDATORY_FIELD_MISSING",
       |    "message": "a mandatory field is missing",
       |    "path": "/$fieldName"
       |
       |}
      """.stripMargin)))
}


