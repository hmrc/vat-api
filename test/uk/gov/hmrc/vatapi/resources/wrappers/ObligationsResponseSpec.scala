/*
 * Copyright 2018 HM Revenue & Customs
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

package uk.gov.hmrc.vatapi.resources.wrappers

import org.joda.time.LocalDate
import play.api.libs.json.{JsValue, Json}
import uk.gov.hmrc.domain.Vrn
import uk.gov.hmrc.http.HttpResponse
import uk.gov.hmrc.vatapi.UnitSpec

class ObligationsResponseSpec extends UnitSpec {
  val vrn = Vrn("123456789")

  val emptyJson: JsValue = Json.parse("""{ "obligations": [] }""")

  val invalidTypeJson : JsValue = Json.parse(
    s"""
     |{
     |  "obligations": [
     |  {
     |    "identification": {
     |        "incomeSourceType": "A",
     |        "referenceNumber": "123456789",
     |        "referenceType": "UnknownType"
     |    },
     |    "details": [
     |    {
     |      "status": "F",
     |      "inboundCorrespondenceFromDate": "2017-01-01",
     |      "inboundCorrespondenceToDate": "2017-03-31",
     |      "inboundCorrespondenceDateReceived": "2017-04-25",
     |      "inboundCorrespondenceDueDate": "2017-04-30",
     |      "periodKey": "001"
     |    },
     |    {
     |      "status": "O",
     |      "inboundCorrespondenceFromDate": "2017-04-01",
     |      "inboundCorrespondenceToDate": "2017-06-30",
     |      "inboundCorrespondenceDueDate": "2017-07-30",
     |      "periodKey": "002"
     |    },
     |    {
     |      "status": "O",
     |      "inboundCorrespondenceFromDate": "2017-07-01",
     |      "inboundCorrespondenceToDate": "2017-09-30",
     |      "inboundCorrespondenceDueDate": "2017-10-30",
     |      "periodKey": "003"
     |    },
     |    {
     |      "status": "O",
     |      "inboundCorrespondenceFromDate": "2017-10-01",
     |      "inboundCorrespondenceToDate": "2017-12-01",
     |      "inboundCorrespondenceDueDate": "2018-01-01",
     |      "periodKey": "004"
     |    }
     |    ]
     |  }
     |  ]
     |}
    """.stripMargin)

  val obligationJson: JsValue = Json.parse(
    s"""
       |{
       |  "obligations": [
       |  {
       |    "identification": {
       |        "incomeSourceType": "A",
       |        "referenceNumber": "123456789",
       |        "referenceType": "VRN"
       |    },
       |    "obligationDetails": [
       |    {
       |      "status": "F",
       |      "inboundCorrespondenceFromDate": "2017-01-01",
       |      "inboundCorrespondenceToDate": "2017-03-31",
       |      "inboundCorrespondenceDateReceived": "2017-04-25",
       |      "inboundCorrespondenceDueDate": "2017-04-30",
       |      "periodKey": "001"
       |    },
       |    {
       |      "status": "O",
       |      "inboundCorrespondenceFromDate": "2017-04-01",
       |      "inboundCorrespondenceToDate": "2017-06-30",
       |      "inboundCorrespondenceDueDate": "2017-07-30",
       |      "periodKey": "002"
       |    },
       |    {
       |      "status": "O",
       |      "inboundCorrespondenceFromDate": "2017-07-01",
       |      "inboundCorrespondenceToDate": "2017-09-30",
       |      "inboundCorrespondenceDueDate": "2017-10-30",
       |      "periodKey": "003"
       |    },
       |    {
       |      "status": "O",
       |      "inboundCorrespondenceFromDate": "2017-10-01",
       |      "inboundCorrespondenceToDate": "2017-12-01",
       |      "inboundCorrespondenceDueDate": "2018-01-01",
       |      "periodKey": "004"
       |    }
       |    ]
       |  }
       |  ]
       |}
    """.stripMargin)


  "ObligationResponse" should {
    "wrap empty response" in {
      val response = ObligationsResponse(HttpResponse(200, Some(emptyJson)))

      val obligations = response.obligations(vrn)
      obligations.right.get shouldBe None
    }


    "wrap invalid json response" in {
      val response = ObligationsResponse(HttpResponse(200, Some(invalidTypeJson)))

      val obligations = response.obligations(vrn)
      obligations.left.get.msg shouldBe "The response from DES does not match the expected format"
    }

    "wrap valid response" in {
      val response = ObligationsResponse(HttpResponse(200, Some(obligationJson)))

      val obligations = response.obligations(vrn)
      obligations.right.get.get.obligations.find(o => o.received.get == new LocalDate("2017-04-25")) shouldBe defined
    }
  }
}