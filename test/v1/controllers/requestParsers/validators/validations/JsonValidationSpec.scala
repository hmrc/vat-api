/*
 * Copyright 2021 HM Revenue & Customs
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

package v1.controllers.requestParsers.validators.validations

import play.api.libs.json.Json
import play.api.mvc.AnyContent
import support.UnitSpec
import v1.models.errors.InvalidJsonError

class JsonValidationSpec extends UnitSpec {

  "validate" should {
    "return no errors" when {
      "valid Json is provided" in {
        val jsonBody: AnyContent = AnyContent(Json.parse(
          """
            |{
            |   "periodKey": "abc",
            |   "vatDueSales": 	9999999999999.99,
            |   "vatDueAcquisitions": 	9999999999999.99,
            |   "totalVatDue": 	9999999999999.99,
            |   "vatReclaimedCurrPeriod": 	9999999999999.99,
            |   "netVatDue": 	99999999999.99,
            |   "totalValueSalesExVAT": 	9999999999999.99,
            |   "totalValuePurchasesExVAT": 	9999999999999.99,
            |   "totalValueGoodsSuppliedExVAT": 	9999999999999.99,
            |   "totalAcquisitionsExVAT": 	9999999999999.99,
            |   "finalised": true
            |}
            |""".stripMargin
        ))

          JsonValidation.validate(jsonBody) shouldBe List()
      }
    }

    "return errors" when {
      "non-valid Json is provided" in {

        val jsonBody: AnyContent = AnyContent(
          """
            |{
            |   "periodKey": abc,
            |   "vatDueSales": 	9999999999999.99,
            |   "vatDueAcquisitions": 	9999999999999.99,
            |   "totalVatDue": 	9999999999999.99,
            |   "vatReclaimedCurrPeriod": 	9999999999999.99,
            |   "netVatDue": 	99999999999.99,
            |   "totalValueSalesExVAT": 	9999999999999.99,
            |   "totalValuePurchasesExVAT": 	9999999999999.99,
            |   "totalValueGoodsSuppliedExVAT": 	9999999999999.99,
            |   "totalAcquisitionsExVAT": 	9999999999999.99,
            |   "finalised": true
            |}
            |""".stripMargin
        )

        JsonValidation.validate(jsonBody) shouldBe List(InvalidJsonError)
      }
    }
  }
}
