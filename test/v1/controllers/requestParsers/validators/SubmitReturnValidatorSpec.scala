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

package v1.controllers.requestParsers.validators

import play.api.libs.json.{JsValue, Json}
import play.api.mvc.{AnyContent, AnyContentAsJson}
import support.UnitSpec
import v1.models.errors._
import v1.models.request.submit.SubmitRawData

class SubmitReturnValidatorSpec extends UnitSpec {

  val validator: SubmitReturnValidator = new SubmitReturnValidator()
  private val validVrn = "123456789"
  private val invalidVrn = "thisIsNotAVrn"
  val validBody: AnyContentAsJson = AnyContentAsJson(Json.parse(
    """
      |{
      |   "periodKey": "AB12",
      |   "vatDueSales": 	0.00,
      |   "vatDueAcquisitions": 	0.00,
      |   "totalVatDue": 	0.00,
      |   "vatReclaimedCurrPeriod": 		99999999999.99,
      |   "netVatDue": 	99999999999.99,
      |   "totalValueSalesExVAT": 	9999999999999,
      |   "totalValuePurchasesExVAT": 	9999999999999,
      |   "totalValueGoodsSuppliedExVAT": 	9999999999999,
      |   "totalAcquisitionsExVAT": 	9999999999999,
      |   "finalised": true
      |}
      |""".stripMargin
  ))

  val inValidFieldFormatBody: AnyContentAsJson = AnyContentAsJson(Json.parse(
    """
      |{
      |   "periodKey": "AB12",
      |   "vatDueSales": 	9999999999999.99,
      |   "vatDueAcquisitions": 	999999999999.999,
      |   "totalVatDue": 	9999999999999.99,
      |   "vatReclaimedCurrPeriod": 	9999999999999.99,
      |   "netVatDue": 	99999999999.99,
      |   "totalValueSalesExVAT": 	9999999999999,
      |   "totalValuePurchasesExVAT": 	9999999999999,
      |   "totalValueGoodsSuppliedExVAT": 	9999999999999,
      |   "totalAcquisitionsExVAT": 	9999999999999,
      |   "finalised": true
      |}
      |""".stripMargin
  ))

  val inValidPeriodKeyBody: AnyContentAsJson = AnyContentAsJson(Json.parse(
    """
      |{
      |   "periodKey": "ABABABABABABABA",
      |   "vatDueSales": 	9999999999999.99,
      |   "vatDueAcquisitions": 	9999999999999.99,
      |   "totalVatDue": 	9999999999999.99,
      |   "vatReclaimedCurrPeriod": 	9999999999999.99,
      |   "netVatDue": 	99999999999.99,
      |   "totalValueSalesExVAT": 	9999999999999,
      |   "totalValuePurchasesExVAT": 	9999999999999,
      |   "totalValueGoodsSuppliedExVAT": 	9999999999999,
      |   "totalAcquisitionsExVAT": 	9999999999999,
      |   "finalised": false
      |}
      |""".stripMargin
  ))


  val inValidFieldRangeBody: AnyContentAsJson = AnyContentAsJson(Json.parse(
    """
      |{
      |   "periodKey": "AB12",
      |   "vatDueSales": 	9999999999999.99,
      |   "vatDueAcquisitions": 	10000000000000.00,
      |   "totalVatDue": 99999999999.99,
      |   "vatReclaimedCurrPeriod": 	0.00,
      |   "netVatDue": 	99999999999.99,
      |   "totalValueSalesExVAT": 	9999999999999,
      |   "totalValuePurchasesExVAT": 	9999999999999,
      |   "totalValueGoodsSuppliedExVAT": 	9999999999999,
      |   "totalAcquisitionsExVAT": 	9999999999999,
      |   "finalised": true
      |}
      |""".stripMargin
  ))

  val inValidMultiFieldRangeBody: AnyContentAsJson = AnyContentAsJson(Json.parse(
    """
      |{
      |   "periodKey": "AB12",
      |   "vatDueSales": 	10000000000000.00,
      |   "vatDueAcquisitions": 	10000000000000.00,
      |   "totalVatDue": 	0.00,
      |   "vatReclaimedCurrPeriod": 	99999999999.99,
      |   "netVatDue": 	99999999999.99,
      |   "totalValueSalesExVAT": 	9999999999999,
      |   "totalValuePurchasesExVAT": 	9999999999999,
      |   "totalValueGoodsSuppliedExVAT": 	9999999999999,
      |   "totalAcquisitionsExVAT": 	9999999999999,
      |   "finalised": true
      |}
      |""".stripMargin
  ))

  val inValidMultipleFieldRangeBody: AnyContentAsJson = AnyContentAsJson(Json.parse(
    """
      |{
      |   "periodKey": "AB12",
      |   "vatDueSales": 	9999999999999.99,
      |   "vatDueAcquisitions": 	10000000000000.00,
      |   "totalVatDue": 	9999999999999.99,
      |   "vatReclaimedCurrPeriod": 	9999999999999.99,
      |   "netVatDue": 	100000000000.00,
      |   "totalValueSalesExVAT": 	9999999999999,
      |   "totalValuePurchasesExVAT": 	9999999999999,
      |   "totalValueGoodsSuppliedExVAT": 	9999999999999,
      |   "totalAcquisitionsExVAT": 	9999999999999,
      |   "finalised": true
      |}
      |""".stripMargin
  ))

  val InvalidJsonBody: AnyContent = AnyContent(
    """
      |{
      |   "periodKey": abc,
      |   "vatDueSales": 	9999999999999.99,
      |   "vatDueAcquisitions": 	9999999999999.99,
      |   "totalVatDue": 	9999999999999.99,
      |   "vatReclaimedCurrPeriod": 	9999999999999.99,
      |   "netVatDue": 	99999999999.99,
      |   "totalValueSalesExVAT": 	9999999999999,
      |   "totalValuePurchasesExVAT": 	9999999999999,
      |   "totalValueGoodsSuppliedExVAT": 	9999999999999,
      |   "totalAcquisitionsExVAT": 	9999999999999,
      |   "finalised": true
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
      "a valid request" in {
        validator.validate(SubmitRawData(validVrn, validBody)) shouldBe List()
      }
    }

    "return VrnFormatError error" when {
      "an invalid Vrn is supplied" in {
        validator.validate(SubmitRawData(invalidVrn, validBody)) shouldBe List(VrnFormatError)
      }
    }

    "return Invalid Json error" when {
      "an invalid period key format is supplied" in {
        validator.validate(SubmitRawData(validVrn, InvalidJsonBody)) shouldBe List(InvalidJsonError)
      }
    }


    "return InvalidMonetaryValueError error" when {
      "a invalid field range is supplied" in {
        validator.validate(SubmitRawData(validVrn, inValidFieldRangeBody)) shouldBe
          List(InvalidMonetaryValueError.withFieldName("vatDueAcquisitions", BigDecimal(-9999999999999.99), BigDecimal(9999999999999.99)))
      }

      "multiple field ranges are invalid including netVatDue" in {
        validator.validate(SubmitRawData(validVrn, inValidMultipleFieldRangeBody)) shouldBe
          List(InvalidMonetaryValueError.withFieldName("vatDueAcquisitions", BigDecimal(-9999999999999.99), BigDecimal(9999999999999.99)),
            InvalidMonetaryValueError.withFieldNameAndNonNegative("netVatDue"))
      }
    }

    "return PeriodKeyFormatError error" when {
      "an invalid Period Key is supplied" in {
        val result = validator.validate(SubmitRawData(validVrn, inValidPeriodKeyBody))

        result.head.customJson shouldBe periodKeyCustomJson
      }
    }

    "return InvalidNumericValue error" when {
      "an invalid numerical value is supplied" in {
        val jsonBody: JsValue = Json.parse(
          """
            |{
            |   "periodKey": "AB12",
            |   "vatDueSales": 	"five",
            |   "vatDueAcquisitions": 	0.00,
            |   "totalVatDue": 	0.00,
            |   "vatReclaimedCurrPeriod": 		99999999999.99,
            |   "netVatDue": 	99999999999.99,
            |   "totalValueSalesExVAT": 	9999999999999,
            |   "totalValuePurchasesExVAT": 	9999999999999,
            |   "totalValueGoodsSuppliedExVAT": 	9999999999999,
            |   "totalAcquisitionsExVAT": 	9999999999999,
            |   "finalised": true
            |}
            |""".stripMargin)

        validator.validate(SubmitRawData(validVrn, AnyContentAsJson(jsonBody))) shouldBe List(NumericFormatRuleError.withFieldName("vatDueSales"))
      }
    }

    "return INVALID_STRING_VALUE error" when {
      "an invalid Period Key is supplied" in {
        val jsonBody: JsValue = Json.parse(
          """
            |{
            |   "periodKey": 12,
            |   "vatDueSales": 	9999999999999.99,
            |   "vatDueAcquisitions": 	9999999999999.99,
            |   "totalVatDue": 	9999999999999.99,
            |   "vatReclaimedCurrPeriod": 	9999999999999.99,
            |   "netVatDue": 	99999999999.99,
            |   "totalValueSalesExVAT": 	9999999999999,
            |   "totalValuePurchasesExVAT": 	9999999999999,
            |   "totalValueGoodsSuppliedExVAT": 	9999999999999,
            |   "totalAcquisitionsExVAT": 	9999999999999,
            |   "finalised": true
            |}
            |""".stripMargin)

        validator.validate(SubmitRawData(validVrn, AnyContentAsJson(jsonBody))) shouldBe List(StringFormatRuleError)
      }
    }

    "return MANDATORY_FIELD_MISSING error" when {
      "an body with a missing field is supplied" in {
        val jsonBody: JsValue = Json.parse(
          """
            |{
            |   "periodKey": "AB12",
            |   "vatDueAcquisitions": 	0.00,
            |   "totalVatDue": 	0.00,
            |   "vatReclaimedCurrPeriod": 		99999999999.99,
            |   "netVatDue": 	99999999999.99,
            |   "totalValueSalesExVAT": 	9999999999999,
            |   "totalValuePurchasesExVAT": 	9999999999999,
            |   "totalValueGoodsSuppliedExVAT": 	9999999999999,
            |   "totalAcquisitionsExVAT": 	9999999999999,
            |   "finalised": true
            |}
            |""".stripMargin
        )

        validator.validate(SubmitRawData(validVrn, AnyContentAsJson(jsonBody))) shouldBe
          List(MandatoryFieldRuleError.withFieldName("vatDueSales"))
      }
    }

    "return UNMAPPED_PLAY_ERROR error" when {
      "finalised is not a boolean" in {
        val jsonBody: JsValue = Json.parse(
          """
            |{
            |   "periodKey": "AB12",
            |   "vatDueSales": 	0.00,
            |   "vatDueAcquisitions": 	0.00,
            |   "totalVatDue": 	0.00,
            |   "vatReclaimedCurrPeriod": 		99999999999.99,
            |   "netVatDue": 	99999999999.99,
            |   "totalValueSalesExVAT": 	9999999999999,
            |   "totalValuePurchasesExVAT": 	9999999999999,
            |   "totalValueGoodsSuppliedExVAT": 	9999999999999,
            |   "totalAcquisitionsExVAT": 	9999999999999,
            |   "finalised": 1
            |}
            |""".stripMargin
        )

        validator.validate(SubmitRawData(validVrn, AnyContentAsJson(jsonBody))) shouldBe
          List(UnMappedPlayRuleError)
      }
    }

    "return VAT_TOTAL_VALUE error" when {
      "the figures do not match" in {
        val jsonBody: JsValue = Json.parse(
          """
            |{
            |   "periodKey": "AB12",
            |   "vatDueSales": 50.00,
            |   "vatDueAcquisitions": 	50.00,
            |   "totalVatDue": 	101.00,
            |   "vatReclaimedCurrPeriod": 	-1.00,
            |   "netVatDue": 	102.00,
            |   "totalValueSalesExVAT": 	9999999999999,
            |   "totalValuePurchasesExVAT": 	9999999999999,
            |   "totalValueGoodsSuppliedExVAT": 	9999999999999,
            |   "totalAcquisitionsExVAT": 	9999999999999,
            |   "finalised": true
            |}
            |""".stripMargin
        )

        validator.validate(SubmitRawData(validVrn, AnyContentAsJson(jsonBody))) shouldBe List(VATTotalValueRuleError)
      }
    }

    "return VAT_NET_VALUE error" when {
      "the figures do not match" in {
        val jsonBody: JsValue = Json.parse(
          """
            |{
            |   "periodKey": "AB12",
            |   "vatDueSales": 	0.00,
            |   "vatDueAcquisitions": 	100.00,
            |   "totalVatDue": 	100.00,
            |   "vatReclaimedCurrPeriod": 	0.00,
            |   "netVatDue": 	101.00,
            |   "totalValueSalesExVAT": 	9999999999999,
            |   "totalValuePurchasesExVAT": 	9999999999999,
            |   "totalValueGoodsSuppliedExVAT": 	9999999999999,
            |   "totalAcquisitionsExVAT": 	9999999999999,
            |   "finalised": true
            |}
            |""".stripMargin
        )

        validator.validate(SubmitRawData(validVrn, AnyContentAsJson(jsonBody))) shouldBe List(VATNetValueRuleError)
      }
    }


    "return NOT_FINALISED error" when {
      "the finalised is not true" in {
        val jsonBody: JsValue = Json.parse(
          """
            |{
            |   "periodKey": "AB12",
            |   "vatDueSales": 	0.00,
            |   "vatDueAcquisitions": 	0.00,
            |   "totalVatDue": 	0.00,
            |   "vatReclaimedCurrPeriod": 		99999999999.99,
            |   "netVatDue": 	99999999999.99,
            |   "totalValueSalesExVAT": 	9999999999999,
            |   "totalValuePurchasesExVAT": 	9999999999999,
            |   "totalValueGoodsSuppliedExVAT": 	9999999999999,
            |   "totalAcquisitionsExVAT": 	9999999999999,
            |   "finalised": false
            |}
            |""".stripMargin
        )

        validator.validate(SubmitRawData(validVrn, AnyContent(jsonBody))) shouldBe List(FinalisedValueRuleError)
      }
    }

    "return errors in the correct order" when {
      "vrn, Json is invalid, period key format is invalid, total vat is incorrect and is not finalised" in {
        val jsonBody: AnyContent = AnyContent(
          """
            |{
            |   "periodKey": abc,
            |   "vatDueSales": 	9999999999999.99,
            |   "vatDueAcquisitions": 	9999999999999.99,
            |   "totalVatDue": 	9999999999999.99,
            |   "vatReclaimedCurrPeriod": 	9999999999999.99,
            |   "netVatDue": 	99999999999.99,
            |   "totalValueSalesExVAT": 	9999999999999,
            |   "totalValuePurchasesExVAT": 	9999999999999,
            |   "totalValueGoodsSuppliedExVAT": 	9999999999999,
            |   "totalAcquisitionsExVAT": 	9999999999999,
            |   "finalised": false
            |}
            |""".stripMargin
        )

        validator.validate(SubmitRawData(invalidVrn, jsonBody)) shouldBe List(VrnFormatError)
      }

      "Json is invalid, period key format is invalid, total vat is incorrect and is not finalised" in {
        val jsonBody: AnyContent = AnyContent(
          """
            |{
            |   "periodKey": abc,
            |   "vatDueSales": 	9999999999999.99,
            |   "vatDueAcquisitions": 	9999999999999.99,
            |   "totalVatDue": 	9999999999999.99,
            |   "vatReclaimedCurrPeriod": 	9999999999999.99,
            |   "netVatDue": 	99999999999.99,
            |   "totalValueSalesExVAT": 	9999999999999,
            |   "totalValuePurchasesExVAT": 	9999999999999,
            |   "totalValueGoodsSuppliedExVAT": 	9999999999999,
            |   "totalAcquisitionsExVAT": 	9999999999999,
            |   "finalised": false
            |}
            |""".stripMargin
        )

        validator.validate(SubmitRawData(validVrn, jsonBody)) shouldBe List(InvalidJsonError)
      }

      "period key format is invalid, total vat is incorrect and is not finalised" in {
        val jsonBody: JsValue = Json.parse(
          """
            |{
            |   "periodKey": "ABABABABABABABA",
            |   "vatDueSales": 	9999999999999.99,
            |   "vatDueAcquisitions": 	9999999999999.99,
            |   "totalVatDue": 	9999999999999.99,
            |   "vatReclaimedCurrPeriod": 	9999999999999.99,
            |   "netVatDue": 	99999999999.99,
            |   "totalValueSalesExVAT": 	9999999999999,
            |   "totalValuePurchasesExVAT": 	9999999999999,
            |   "totalValueGoodsSuppliedExVAT": 	9999999999999,
            |   "totalAcquisitionsExVAT": 	9999999999999,
            |   "finalised": false
            |}
            |""".stripMargin
        )

        val result = validator.validate(SubmitRawData(validVrn, AnyContent(jsonBody)))

        result.head.customJson shouldBe periodKeyCustomJson
      }

      "total vat is incorrect and is not finalised" in {
        val jsonBody: JsValue = Json.parse(
          """
            |{
            |   "periodKey": "AB12",
            |   "vatDueSales": 	9999999999999.99,
            |   "vatDueAcquisitions": 	9999999999999.99,
            |   "totalVatDue": 	100.00,
            |   "vatReclaimedCurrPeriod": 	0.00,
            |   "netVatDue": 	100.00,
            |   "totalValueSalesExVAT": 	9999999999999,
            |   "totalValuePurchasesExVAT": 	9999999999999,
            |   "totalValueGoodsSuppliedExVAT": 	9999999999999,
            |   "totalAcquisitionsExVAT": 	9999999999999,
            |   "finalised": false
            |}
            |""".stripMargin
        )

        validator.validate(SubmitRawData(validVrn, AnyContentAsJson(jsonBody))) shouldBe List(VATTotalValueRuleError)
      }
    }
  }
}
