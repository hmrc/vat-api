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

package uk.gov.hmrc.vatapi.models.audit

import play.api.http.Status
import play.api.libs.json.Json
import uk.gov.hmrc.vatapi.UnitSpec
import v2.models.audit.AuditError

class AuditResponseSpec extends UnitSpec {

  private val vatReturnDeclarationJson = Json.parse(
    """
      |{
      |      "periodKey":"#001",
      |      "vatDueSales":7000,
      |      "vatDueAcquisitions":3000,
      |      "totalVatDue":10000,
      |      "vatReclaimedCurrPeriod":1000,
      |      "netVatDue":9000,
      |      "totalValueSalesExVAT":1000,
      |      "totalValuePurchasesExVAT":200,
      |      "totalValueGoodsSuppliedExVAT":100,
      |      "totalAcquisitionsExVAT":540
      |      }
    """.stripMargin)

  private val responseSuccess = AuditResponse(Status.OK, None, Some(vatReturnDeclarationJson))
  private val responseFail = AuditResponse(Status.BAD_REQUEST, Some(Seq(AuditError("VRN_INVALID"))), None)

  "writes" when {
    "passed an audit response model with success tax calculation field" should {
      "produce valid json" in {

        val json = Json.parse(
          s"""
             |{
             |   "httpStatus": 200,
             |   "payload": $vatReturnDeclarationJson
             |}
           """.stripMargin)

        Json.toJson(responseSuccess) shouldBe json
      }
    }

    "passed an audit response model with error field" should {
      "produce valid json" in {
        val json = Json.parse(
          s"""
             |{
             |    "httpStatus": 400,
             |    "errors": [
             |      {
             |        "errorCode": "VRN_INVALID"
             |      }
             |     ]
             |}
           """.stripMargin)

        Json.toJson(responseFail) shouldBe json
      }
    }
  }
}
