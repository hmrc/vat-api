/*
 * Copyright 2019 HM Revenue & Customs
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
import uk.gov.hmrc.domain.Vrn
import uk.gov.hmrc.vatapi.UnitSpec
import v2.models.audit.AuditError

class AuditDetailSpec extends UnitSpec {

  private val userType = "Organisation"
  private val agentReferenceNumber = Some("012345678")
  private val vrn: Vrn = generateVrn
  private val `X-CorrelationId` = "X-123"
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
    "passed an audit detail model with success response" should {
      "produce valid json" in {

        val jsonResponse = Json.stringify(vatReturnDeclarationJson)
        
        val json = Json.parse(
          s"""
             |{
             |        "vrn": "$vrn",
             |        "arn": "012345678",
             |        "userType": "Agent",
             |        "X-CorrelationId": "X-123",
             |        "response": {
             |            "httpStatus": 200,
             |            "payload": $jsonResponse
             |        }
             |}
           """.stripMargin)

        val model = AuditDetail("Agent", agentReferenceNumber, vrn.toString(), `X-CorrelationId`, response = responseSuccess)

        Json.toJson(model) shouldBe json
      }
    }

    "passed an audit detail model with errors field" should {
      "produce valid json" in {
        val json = Json.parse(
          s"""
             |{
             |    "vrn": "$vrn",
             |    "userType": "Organisation",
             |    "X-CorrelationId": "X-123",
             |    "response": {
             |     "httpStatus": 400,
             |      "errors": [
             |      {
             |        "errorCode": "VRN_INVALID"
             |      }
             |     ]
             |    }
             |}
           """.stripMargin)

        val model = AuditDetail(userType, None, vrn.toString(), `X-CorrelationId`, responseFail)

        Json.toJson(model) shouldBe json
      }
    }
  }
}
