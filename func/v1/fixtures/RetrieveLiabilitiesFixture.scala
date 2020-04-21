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

package v1.fixtures

import play.api.libs.json.{JsValue, Json}
import v1.models.response.common.TaxPeriod
import v1.models.response.liabilities.{LiabilitiesResponse, Liability}

trait RetrieveLiabilitiesFixture {

  val desJson: JsValue = Json.parse(
    """
      |{
      |    "idType": "VRN",
      |    "idNumber": "123456789",
      |    "regimeType": "VATC",
      |    "processingDate": "2017-03-07T09:30:00.000Z",
      |    "financialTransactions": [{
      |            "chargeType": "VAT",
      |            "mainType": "2100",
      |            "periodKey": "13RL",
      |            "periodKeyDescription": "abcde",
      |            "taxPeriodFrom": "2017-02-01",
      |            "taxPeriodTo": "2017-11-01",
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
      |                "dueDate": "2017-11-11",
      |                "amount": 463872
      |            }]
      |        }
      |    ]
      |}
    """.stripMargin
  )

  val mtdJson: JsValue = Json.parse(
    s"""
       |{
       |	"liabilities": [{
       |		"taxPeriod": {
       |			"from": "2017-02-01",
       |			"to": "2017-11-01"
       |		},
       |		"type": "VAT",
       |		"originalAmount": 463872,
       |		"outstandingAmount": 463872,
       |		"due": "2017-11-11"
       |	}]
       |}
    """.stripMargin
  )

  val liabilityResponse: LiabilitiesResponse =
    LiabilitiesResponse(
      Seq(
        Liability(
          Some(TaxPeriod("2017-01-01", "2017-12-01")),
          "VAT",
          1.0,
          Some(1.0),
          Some("2017-11-11")
        )
      )
    )
}
