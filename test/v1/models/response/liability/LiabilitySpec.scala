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

package v1.models.response.liability

import play.api.libs.json.Json
import support.UnitSpec
import v1.models.response.common.TaxPeriod

class LiabilitySpec extends UnitSpec {

  "Liability" should {
    "read from the downstream model" when {

      "all fields are present in a single list" in {

        val desJson = Json.parse(
          s"""
             |{
             |            "chargeType": "VAT",
             |            "mainType": "2100",
             |            "periodKey": "13RL",
             |            "periodKeyDescription": "abcde",
             |            "taxPeriodFrom": "2017-01-01",
             |            "taxPeriodTo": "2017-04-05",
             |            "businessPartner": "6622334455",
             |            "contractAccountCategory": "02",
             |            "contractAccount": "D",
             |            "contractObjectType": "ABCD",
             |            "contractObject": "00000003000000002757",
             |            "sapDocumentNumber": "1040000872",
             |            "sapDocumentNumberItem": "XM00",
             |            "chargeReference": "XM002610011594",
             |            "mainTransaction": "1234",
             |            "subTransaction": "5678",
             |            "originalAmount": 463872,
             |            "outstandingAmount": 463872,
             |            "accruedInterest": 10000,
             |            "items": [{
             |                "subItem": "001",
             |                "dueDate": "2017-03-08",
             |                "amount": 463872
             |            }]
             |}
             |""".stripMargin)

        val liability = Liability(
            taxPeriod = Some(
              TaxPeriod(
                from = "2017-01-01",
                to = "2017-04-05"
              )
            ),
            `type` = "VAT",
            originalAmount = 463872,
            outstandingAmount = Some(463872),
            due = Some("2017-03-08")
          )

        desJson.as[Liability] shouldBe liability
      }

      "one liability is returned without items:dueDate" in {

        val desJson = Json.parse(
          s"""
             |{
             |            "chargeType": "VAT",
             |            "mainType": "2100",
             |            "periodKey": "13RL",
             |            "periodKeyDescription": "abcde",
             |            "taxPeriodFrom": "2017-01-01",
             |            "taxPeriodTo": "2017-04-05",
             |            "businessPartner": "6622334455",
             |            "contractAccountCategory": "02",
             |            "contractAccount": "D",
             |            "contractObjectType": "ABCD",
             |            "contractObject": "00000003000000002757",
             |            "sapDocumentNumber": "1040000872",
             |            "sapDocumentNumberItem": "XM00",
             |            "chargeReference": "XM002610011594",
             |            "mainTransaction": "1234",
             |            "subTransaction": "5678",
             |            "originalAmount": 463872,
             |            "outstandingAmount": 463872,
             |            "accruedInterest": 10000
             |}
             |""".stripMargin)

        val liability = Liability(
            taxPeriod = Some(
              TaxPeriod(
                from = "2017-01-01",
                to = "2017-04-05"
              )
            ),
            `type` = "VAT",
            originalAmount = 463872,
            outstandingAmount = Some(463872),
            due = None
          )

        desJson.as[Liability] shouldBe liability
      }

      "not parse incorrect json" in {

        val badJson = Json.parse(
          s"""
             |{
             |    "idType": "VRN"
             |}
             |""".stripMargin)

        badJson.asOpt[Liability] shouldBe None
      }

    }

    "use the writes format correctly" in {

      val liabilityJson = Json.parse(
        s"""
           |{
           |		"taxPeriod": {
           |			"from": "2017-01-01",
           |			"to": "2017-04-05"
           |		},
           |		"type": "VAT",
           |		"originalAmount": 463872,
           |		"outstandingAmount": 463872,
           |		"due": "2017-03-08"
           |	}
           |""".stripMargin)

      val liability = Liability(
        taxPeriod = Some(
          TaxPeriod(
            from = "2017-01-01",
            to = "2017-04-05"
          )
        ),
        `type` = "VAT",
        originalAmount = 463872,
        outstandingAmount = Some(463872),
        due = Some("2017-03-08")
      )

      Json.toJson(liability) shouldBe liabilityJson
    }
  }
}
