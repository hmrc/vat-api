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

package v1.controllers.requestParsers

import play.api.libs.json.{JsValue, Json}
import play.api.mvc.AnyContentAsJson
import support.UnitSpec
import v1.models.domain.Vrn
import v1.mocks.validators.MockSubmitReturnValidator
import v1.models.errors.{ErrorWrapper, InvalidMonetaryValueError, PeriodKeyFormatError, VrnFormatError}
import v1.models.request.submit.{SubmitRawData, SubmitRequest, SubmitRequestBody}

class SubmitReturnRequestParserSpec extends UnitSpec {

  private val validVrn = "AA111111A"
  private val invalidVrn = "notAVrn"
  implicit val correlationId: String = "a1e8057e-fbbc-47a8-a8b4-78d9f015c253"

  val validBodyJson: JsValue = Json.parse(
    """
      |{
      |   "periodKey": "AB12",
      |   "vatDueSales": 	9999999999999.99,
      |   "vatDueAcquisitions": 	9999999999999.99,
      |   "totalVatDue": 	9999999999999.99,
      |   "vatReclaimedCurrPeriod": 	9999999999999.99,
      |   "netVatDue": 	999999999999.99,
      |   "totalValueSalesExVAT": 	9999999999999.99,
      |   "totalValuePurchasesExVAT": 	9999999999999.99,
      |   "totalValueGoodsSuppliedExVAT": 	9999999999999.99,
      |   "totalAcquisitionsExVAT": 	9999999999999.99,
      |   "finalised": true
      |}
      |""".stripMargin
  )

  val invalidPeriodKeyJson: JsValue = Json.parse(
    """
      |{
      |   "periodKey": "thisIsNotAPeriodKey",
      |   "vatDueSales": 	9999999999999.99,
      |   "vatDueAcquisitions": 	9999999999999.9,
      |   "totalVatDue": 	9999999999999.99,
      |   "vatReclaimedCurrPeriod": 	9999999999999.99,
      |   "netVatDue": 	999999999999.99,
      |   "totalValueSalesExVAT": 	9999999999999.99,
      |   "totalValuePurchasesExVAT": 	9999999999999.99,
      |   "totalValueGoodsSuppliedExVAT": 	9999999999999.99,
      |   "totalAcquisitionsExVAT": 	9999999999999.99,
      |   "finalised": true
      |}
      |""".stripMargin
  )

  val invalidMonetaryFormatJson: JsValue = Json.parse(
    """
      |{
      |   "periodKey": "AB12",
      |   "vatDueSales": 	9999999999999.99,
      |   "vatDueAcquisitions": 	999999999999.999,
      |   "totalVatDue": 	9999999999999.99,
      |   "vatReclaimedCurrPeriod": 	9999999999999.99,
      |   "netVatDue": 	999999999999.99,
      |   "totalValueSalesExVAT": 	9999999999999.99,
      |   "totalValuePurchasesExVAT": 	9999999999999.99,
      |   "totalValueGoodsSuppliedExVAT": 	9999999999999.99,
      |   "totalAcquisitionsExVAT": 	9999999999999.99,
      |   "finalised": true
      |}
      |""".stripMargin
  )

  val invalidMonetaryRangeJson: JsValue = Json.parse(
    """
      |{
      |   "periodKey": "AB12",
      |   "vatDueSales": 	9999999999999.99,
      |   "vatDueAcquisitions": 	10000000000000.00,
      |   "totalVatDue": 	9999999999999.99,
      |   "vatReclaimedCurrPeriod": 	9999999999999.99,
      |   "netVatDue": 	999999999999.99,
      |   "totalValueSalesExVAT": 	9999999999999.99,
      |   "totalValuePurchasesExVAT": 	9999999999999.99,
      |   "totalValueGoodsSuppliedExVAT": 	9999999999999.99,
      |   "totalAcquisitionsExVAT": 	9999999999999.99,
      |   "finalised": true
      |}
      |""".stripMargin
  )

  val validBodyModel: SubmitRequestBody = SubmitRequestBody(periodKey = Some("AB12"),
                                                            vatDueSales = Some(9999999999999.99),
                                                            vatDueAcquisitions = Some(9999999999999.99),
                                                            totalVatDue = Some(9999999999999.99),
                                                            vatReclaimedCurrPeriod = Some(9999999999999.99),
                                                            netVatDue = Some(999999999999.99),
                                                            totalValueSalesExVAT = Some(9999999999999.99),
                                                            totalValuePurchasesExVAT = Some(9999999999999.99),
                                                            totalValueGoodsSuppliedExVAT = Some(9999999999999.99),
                                                            totalAcquisitionsExVAT = Some(9999999999999.99),
                                                            finalised = Some(true),
                                                            receivedAt = None,
                                                            agentReference = None)


  trait Test extends MockSubmitReturnValidator {
    lazy val parser = new SubmitReturnRequestParser(mockValidator)
  }

  "parsing an submit vat returns request" should {
    "produce a valid request" when {
      "the data supplied by the vendor is valid" in  new Test {

        MockSubmitReturnsValidator.validate(SubmitRawData(validVrn, AnyContentAsJson(validBodyJson)))
          .returns(Nil)

        parser.parseRequest(SubmitRawData(validVrn, AnyContentAsJson(validBodyJson))) shouldBe
          Right(SubmitRequest(Vrn(validVrn), validBodyModel))
      }

    }

    "return BadRequest wrapped error" when {
      "invalid vrn is provided" in new Test {
        MockSubmitReturnsValidator.validate(SubmitRawData(invalidVrn, AnyContentAsJson(validBodyJson)))
          .returns(List(VrnFormatError))

        parser.parseRequest(SubmitRawData(invalidVrn, AnyContentAsJson(validBodyJson))) shouldBe
          Left(ErrorWrapper(correlationId, VrnFormatError, None))
      }
    }

    "return BadRequest wrapped error" when {
      "invalid period key is provided" in new Test {
        MockSubmitReturnsValidator.validate(SubmitRawData(validVrn, AnyContentAsJson(invalidPeriodKeyJson)))
          .returns(List(PeriodKeyFormatError))

        parser.parseRequest(SubmitRawData(validVrn, AnyContentAsJson(invalidPeriodKeyJson))) shouldBe
          Left(ErrorWrapper(correlationId, PeriodKeyFormatError, None))
      }
    }

    "return BadRequest wrapped error" when {
      "invalid monetary format is provided" in new Test {
        MockSubmitReturnsValidator.validate(SubmitRawData(validVrn, AnyContentAsJson(invalidMonetaryFormatJson)))
          .returns(List(InvalidMonetaryValueError.withFieldName("vatDueAcquisitions", BigDecimal(-9999999999999.99), BigDecimal(9999999999999.99))))

        parser.parseRequest(SubmitRawData(validVrn, AnyContentAsJson(invalidMonetaryFormatJson))) shouldBe
          Left(ErrorWrapper(correlationId, InvalidMonetaryValueError.withFieldName("vatDueAcquisitions", BigDecimal(-9999999999999.99), BigDecimal(9999999999999.99)), None))
      }
    }

    "return BadRequest wrapped error" when {
      "invalid monetary range is provided" in new Test {
        MockSubmitReturnsValidator.validate(SubmitRawData(invalidVrn, AnyContentAsJson(invalidMonetaryRangeJson)))
          .returns(List(InvalidMonetaryValueError.withFieldName("vatDueAcquisitions", BigDecimal(-9999999999999.99), BigDecimal(9999999999999.99))))

        parser.parseRequest(SubmitRawData(invalidVrn, AnyContentAsJson(invalidMonetaryRangeJson))) shouldBe
          Left(ErrorWrapper(correlationId, InvalidMonetaryValueError.withFieldName("vatDueAcquisitions", BigDecimal(-9999999999999.99), BigDecimal(9999999999999.99)), None))
      }
    }

    "return only single error" when {
      "invalid body and vrn is provided" in new Test {
        MockSubmitReturnsValidator.validate(SubmitRawData(invalidVrn, AnyContentAsJson(invalidMonetaryRangeJson)))
          .returns(List(VrnFormatError))

        parser.parseRequest(SubmitRawData(invalidVrn, AnyContentAsJson(invalidMonetaryRangeJson))) shouldBe
          Left(ErrorWrapper(correlationId, VrnFormatError, None))
      }
    }
  }
}
