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

package v1.models.request

import play.api.libs.json.{JsValue, Json}
import support.UnitSpec
import v1.models.request.submit.SubmitRequestBody

class SubmitRequestBodySpec extends UnitSpec {

  val vendorRequest: JsValue = Json.parse(
    """
      |{
      |   "periodKey": "AB12",
      |   "vatDueSales": 	0.00,
      |   "vatDueAcquisitions": 	0.00,
      |   "totalVatDue": 	0.00,
      |   "vatReclaimedCurrPeriod": 	99999999999.99,
      |   "netVatDue": 	99999999999.99,
      |   "totalValueSalesExVAT": 	9999999999999,
      |   "totalValuePurchasesExVAT": 	9999999999999,
      |   "totalValueGoodsSuppliedExVAT": 	9999999999999,
      |   "totalAcquisitionsExVAT": 	9999999999999,
      |   "finalised": true
      |}
    """.stripMargin)

  val toDesJson: JsValue = Json.parse(
    """
      |{
      |   "periodKey": "AB12",
      |   "vatDueSales": 	0.00,
      |   "vatDueAcquisitions": 	0.00,
      |   "vatDueTotal": 	0.00,
      |   "vatReclaimedCurrPeriod": 	99999999999.99,
      |   "vatDueNet": 	99999999999.99,
      |   "totalValueSalesExVAT": 	9999999999999.0,
      |   "totalValuePurchasesExVAT": 	9999999999999.0,
      |   "totalValueGoodsSuppliedExVAT": 	9999999999999.0,
      |   "totalAllAcquisitionsExVAT": 	9999999999999.0,
      |   "finalised": true,
      |   "receivedAt": "2020-05-05T12:00:00Z"
      |}
    """.stripMargin)

  val submitRequestBody: SubmitRequestBody = SubmitRequestBody(Some("AB12"), Some(BigDecimal(0.00)),
    Some(BigDecimal(0.00)), Some(BigDecimal(0.00)), Some(BigDecimal(99999999999.99)),
    Some(BigDecimal(99999999999.99)), Some(BigDecimal(9999999999999.0)), Some(BigDecimal(9999999999999.0)),
    Some(BigDecimal(9999999999999.0)), Some(BigDecimal(9999999999999.0)), Some(true), None, None)

  val submitRequestToDesBody: SubmitRequestBody = SubmitRequestBody(Some("AB12"), Some(BigDecimal(0.00)),
    Some(BigDecimal(0.00)), Some(BigDecimal(0.00)), Some(BigDecimal(99999999999.99)),
    Some(BigDecimal(99999999999.99)), Some(BigDecimal(9999999999999.0)), Some(BigDecimal(9999999999999.0)),
    Some(BigDecimal(9999999999999.0)), Some(BigDecimal(9999999999999.0)), Some(true), Some("2020-05-05T12:00:00Z"), None)

  "Submit request body" should {
    "return a SubmitRequestBody model" when {
      "valid json is provided" in {

        vendorRequest.as[SubmitRequestBody] shouldBe submitRequestBody
      }
    }

    "write valid Json" when {
      "a valid model is provided" in {

        Json.toJson(submitRequestToDesBody) shouldBe toDesJson
      }
    }
  }
}
