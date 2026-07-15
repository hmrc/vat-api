/*
 * Copyright 2026 HM Revenue & Customs
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

package v1.controllers.requestParsers.validators

import play.api.libs.json.{JsValue, Json}
import play.api.mvc.{AnyContent, AnyContentAsJson}
import support.UnitSpec
import v1.models.errors._
import v1.models.request.submit.SubmitRawData

class AssistReturnValidatorSpec extends UnitSpec {

  val validator: AssistReturnValidator = new AssistReturnValidator()
  private val validVrn   = "123456789"
  private val invalidVrn = "thisIsNotAVrn"

  // No "finalised" field
  val validBody: AnyContentAsJson = AnyContentAsJson(Json.parse(
    """
      |{
      |   "periodKey": "AB12",
      |   "vatDueSales": 0.00,
      |   "vatDueAcquisitions": 0.00,
      |   "totalVatDue": 0.00,
      |   "vatReclaimedCurrPeriod": 99999999999.99,
      |   "netVatDue": 99999999999.99,
      |   "totalValueSalesExVAT": 9999999999999,
      |   "totalValuePurchasesExVAT": 9999999999999,
      |   "totalValueGoodsSuppliedExVAT": 9999999999999,
      |   "totalAcquisitionsExVAT": 9999999999999
      |}
      |""".stripMargin
  ))

  val inValidFieldRangeBody: AnyContentAsJson = AnyContentAsJson(Json.parse(
    """
      |{
      |   "periodKey": "AB12",
      |   "vatDueSales": 9999999999999.99,
      |   "vatDueAcquisitions": 10000000000000.00,
      |   "totalVatDue": 99999999999.99,
      |   "vatReclaimedCurrPeriod": 0.00,
      |   "netVatDue": 99999999999.99,
      |   "totalValueSalesExVAT": 9999999999999,
      |   "totalValuePurchasesExVAT": 9999999999999,
      |   "totalValueGoodsSuppliedExVAT": 9999999999999,
      |   "totalAcquisitionsExVAT": 9999999999999
      |}
      |""".stripMargin
  ))

  val inValidMultipleFieldRangeBody: AnyContentAsJson = AnyContentAsJson(Json.parse(
    """
      |{
      |   "periodKey": "AB12",
      |   "vatDueSales": 9999999999999.99,
      |   "vatDueAcquisitions": 10000000000000.00,
      |   "totalVatDue": 9999999999999.99,
      |   "vatReclaimedCurrPeriod": 9999999999999.99,
      |   "netVatDue": 100000000000.00,
      |   "totalValueSalesExVAT": 9999999999999,
      |   "totalValuePurchasesExVAT": 9999999999999,
      |   "totalValueGoodsSuppliedExVAT": 9999999999999,
      |   "totalAcquisitionsExVAT": 9999999999999
      |}
      |""".stripMargin
  ))

  val inValidPeriodKeyBody: AnyContentAsJson = AnyContentAsJson(Json.parse(
    """
      |{
      |   "periodKey": "ABABABABABABABA",
      |   "vatDueSales": 9999999999999.99,
      |   "vatDueAcquisitions": 9999999999999.99,
      |   "totalVatDue": 9999999999999.99,
      |   "vatReclaimedCurrPeriod": 9999999999999.99,
      |   "netVatDue": 99999999999.99,
      |   "totalValueSalesExVAT": 9999999999999,
      |   "totalValuePurchasesExVAT": 9999999999999,
      |   "totalValueGoodsSuppliedExVAT": 9999999999999,
      |   "totalAcquisitionsExVAT": 9999999999999
      |}
      |""".stripMargin
  ))

  val invalidJsonBody: AnyContent = AnyContent(
    """
      |{
      |   "periodKey": abc,
      |   "vatDueSales": 9999999999999.99,
      |   "vatDueAcquisitions": 9999999999999.99,
      |   "totalVatDue": 9999999999999.99,
      |   "vatReclaimedCurrPeriod": 9999999999999.99,
      |   "netVatDue": 99999999999.99,
      |   "totalValueSalesExVAT": 9999999999999,
      |   "totalValuePurchasesExVAT": 9999999999999,
      |   "totalValueGoodsSuppliedExVAT": 9999999999999,
      |   "totalAcquisitionsExVAT": 9999999999999
      |}
      |""".stripMargin
  )

  val periodKeyCustomJson: Option[JsValue] = Some(Json.parse(
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
      |""".stripMargin))

  "running a validation" should {

    "return no errors" when {
      "a valid request is supplied" in {
        validator.validate(SubmitRawData(validVrn, validBody)) shouldBe List()
      }
    }

    "return VrnFormatError" when {
      "an invalid Vrn is supplied" in {
        validator.validate(SubmitRawData(invalidVrn, validBody)) shouldBe List(VrnFormatError)
      }
    }

    "return InvalidJsonError" when {
      "the body is not valid JSON" in {
        validator.validate(SubmitRawData(validVrn, invalidJsonBody)) shouldBe List(InvalidJsonError)
      }
    }

    "return InvalidMonetaryValueError" when {
      "an invalid field range is supplied" in {
        validator.validate(SubmitRawData(validVrn, inValidFieldRangeBody)) shouldBe
          List(InvalidMonetaryValueError.withFieldName("vatDueAcquisitions", BigDecimal(-9999999999999.99), BigDecimal(9999999999999.99)))
      }

      "multiple field ranges are invalid including netVatDue" in {
        validator.validate(SubmitRawData(validVrn, inValidMultipleFieldRangeBody)) shouldBe
          List(
            InvalidMonetaryValueError.withFieldName("vatDueAcquisitions", BigDecimal(-9999999999999.99), BigDecimal(9999999999999.99)),
            InvalidMonetaryValueError.withFieldNameAndNonNegative("netVatDue")
          )
      }
    }

    "return PeriodKeyFormatError" when {
      "an invalid Period Key is supplied" in {
        val result = validator.validate(SubmitRawData(validVrn, inValidPeriodKeyBody))
        result.head.customJson shouldBe periodKeyCustomJson
      }
    }

    "return NumericFormatRuleError" when {
      "an invalid numerical value is supplied" in {
        val jsonBody: JsValue = Json.parse(
          """
            |{
            |   "periodKey": "AB12",
            |   "vatDueSales": "five",
            |   "vatDueAcquisitions": 0.00,
            |   "totalVatDue": 0.00,
            |   "vatReclaimedCurrPeriod": 99999999999.99,
            |   "netVatDue": 99999999999.99,
            |   "totalValueSalesExVAT": 9999999999999,
            |   "totalValuePurchasesExVAT": 9999999999999,
            |   "totalValueGoodsSuppliedExVAT": 9999999999999,
            |   "totalAcquisitionsExVAT": 9999999999999
            |}
            |""".stripMargin)

        validator.validate(SubmitRawData(validVrn, AnyContentAsJson(jsonBody))) shouldBe
          List(NumericFormatRuleError.withFieldName("vatDueSales"))
      }
    }

    "return StringFormatRuleError" when {
      "the period key is not a string" in {
        val jsonBody: JsValue = Json.parse(
          """
            |{
            |   "periodKey": 12,
            |   "vatDueSales": 9999999999999.99,
            |   "vatDueAcquisitions": 9999999999999.99,
            |   "totalVatDue": 9999999999999.99,
            |   "vatReclaimedCurrPeriod": 9999999999999.99,
            |   "netVatDue": 99999999999.99,
            |   "totalValueSalesExVAT": 9999999999999,
            |   "totalValuePurchasesExVAT": 9999999999999,
            |   "totalValueGoodsSuppliedExVAT": 9999999999999,
            |   "totalAcquisitionsExVAT": 9999999999999
            |}
            |""".stripMargin)

        validator.validate(SubmitRawData(validVrn, AnyContentAsJson(jsonBody))) shouldBe List(StringFormatRuleError)
      }
    }

    "return MandatoryFieldRuleError" when {
      "a body with a missing field is supplied" in {
        val jsonBody: JsValue = Json.parse(
          """
            |{
            |   "periodKey": "AB12",
            |   "vatDueAcquisitions": 0.00,
            |   "totalVatDue": 0.00,
            |   "vatReclaimedCurrPeriod": 99999999999.99,
            |   "netVatDue": 99999999999.99,
            |   "totalValueSalesExVAT": 9999999999999,
            |   "totalValuePurchasesExVAT": 9999999999999,
            |   "totalValueGoodsSuppliedExVAT": 9999999999999,
            |   "totalAcquisitionsExVAT": 9999999999999
            |}
            |""".stripMargin
        )

        validator.validate(SubmitRawData(validVrn, AnyContentAsJson(jsonBody))) shouldBe
          List(MandatoryFieldRuleError.withFieldName("vatDueSales"))
      }
    }

    "return VATTotalValueRuleError" when {
      "the total VAT figures do not match" in {
        val jsonBody: JsValue = Json.parse(
          """
            |{
            |   "periodKey": "AB12",
            |   "vatDueSales": 50.00,
            |   "vatDueAcquisitions": 50.00,
            |   "totalVatDue": 101.00,
            |   "vatReclaimedCurrPeriod": -1.00,
            |   "netVatDue": 102.00,
            |   "totalValueSalesExVAT": 9999999999999,
            |   "totalValuePurchasesExVAT": 9999999999999,
            |   "totalValueGoodsSuppliedExVAT": 9999999999999,
            |   "totalAcquisitionsExVAT": 9999999999999
            |}
            |""".stripMargin
        )

        validator.validate(SubmitRawData(validVrn, AnyContentAsJson(jsonBody))) shouldBe List(VATTotalValueRuleError)
      }
    }

    "return VATNetValueRuleError" when {
      "the net VAT figures do not match" in {
        val jsonBody: JsValue = Json.parse(
          """
            |{
            |   "periodKey": "AB12",
            |   "vatDueSales": 0.00,
            |   "vatDueAcquisitions": 100.00,
            |   "totalVatDue": 100.00,
            |   "vatReclaimedCurrPeriod": 0.00,
            |   "netVatDue": 101.00,
            |   "totalValueSalesExVAT": 9999999999999,
            |   "totalValuePurchasesExVAT": 9999999999999,
            |   "totalValueGoodsSuppliedExVAT": 9999999999999,
            |   "totalAcquisitionsExVAT": 9999999999999
            |}
            |""".stripMargin
        )

        validator.validate(SubmitRawData(validVrn, AnyContentAsJson(jsonBody))) shouldBe List(VATNetValueRuleError)
      }
    }

    "return errors in the correct precedence order" when {

      "vrn is invalid (takes precedence over everything)" in {
        validator.validate(SubmitRawData(invalidVrn, invalidJsonBody)) shouldBe List(VrnFormatError)
      }

      "json is invalid (takes precedence over field rules)" in {
        validator.validate(SubmitRawData(validVrn, invalidJsonBody)) shouldBe List(InvalidJsonError)
      }

      "period key format is invalid" in {
        val result = validator.validate(SubmitRawData(validVrn, inValidPeriodKeyBody))
        result.head.customJson shouldBe periodKeyCustomJson
      }

      "total vat is incorrect" in {
        val jsonBody: JsValue = Json.parse(
          """
            |{
            |   "periodKey": "AB12",
            |   "vatDueSales": 9999999999999.99,
            |   "vatDueAcquisitions": 9999999999999.99,
            |   "totalVatDue": 100.00,
            |   "vatReclaimedCurrPeriod": 0.00,
            |   "netVatDue": 100.00,
            |   "totalValueSalesExVAT": 9999999999999,
            |   "totalValuePurchasesExVAT": 9999999999999,
            |   "totalValueGoodsSuppliedExVAT": 9999999999999,
            |   "totalAcquisitionsExVAT": 9999999999999
            |}
            |""".stripMargin
        )

        validator.validate(SubmitRawData(validVrn, AnyContentAsJson(jsonBody))) shouldBe List(VATTotalValueRuleError)
      }
    }
  }
}