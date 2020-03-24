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

package v1.models.response

import support.UnitSpec
import play.api.libs.json.{JsValue, Json}
import v1.models.response.viewReturn.ViewReturnResponse

class ViewReturnResponseSpec extends UnitSpec {

  val desViewReturn: JsValue = Json.parse(
    """
      |{
      |    "periodKey": "A001",
      |    "vatDueSales": 2119210210.23,
      |    "vatDueAcquisitions": -9876543210912.87,
      |    "vatDueTotal": 929201123.23,
      |    "vatReclaimedCurrPeriod": -1234567890122.23,
      |    "vatDueNet": 2345678901.12,
      |    "totalValueSalesExVAT": 93732432923.00,
      |    "totalValuePurchasesExVAT": 34853948.00,
      |    "totalValueGoodsSuppliedExVAT": 82390428304.00,
      |    "totalAllAcquisitionsExVAT": -204832482.00
      |}
      |""".stripMargin)

  val mtdViewReturn: ViewReturnResponse = ViewReturnResponse(
    periodKey = "A001",
    vatDueSales = 2119210210.23,
    vatDueAcquisitions = -9876543210912.87,
    totalVatDue = 929201123.23,
    vatReclaimedCurrPeriod = -1234567890122.23,
    vatDueNet = 2345678901.12,
    totalValueSalesExVAT = 93732432923.00,
    totalValuePurchasesExVAT = 34853948.00,
    totalValueGoodsSuppliedExVAT = 82390428304.00,
    totalAcquisitionsExVAT = -204832482.00)

  "ViewReturnResponse" when {
    "read from valid JSON" should {
      "produce the expected ViewReturnResponse object for a return" in {
        desViewReturn.as[ViewReturnResponse] shouldBe mtdViewReturn
      }
    }
  }
}
