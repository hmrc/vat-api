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

package v1.models.response.obligations

import play.api.libs.json.Json
import support.UnitSpec

class ObligationResponseSpec extends UnitSpec {

  "ObligationResponse" should {
    "read from the downstream model" when {

      "all fields are present in a single list" in {

        val desJson = Json.parse(
          s"""{
             |   "obligations":[
             |      {
             |         "identification":{
             |            "referenceNumber":"123456789",
             |            "referenceType":"VRN"
             |         },
             |         "obligationDetails":[
             |            {
             |               "status":"F",
             |               "inboundCorrespondenceFromDate":"2017-01-01",
             |               "inboundCorrespondenceToDate":"2017-03-31",
             |               "inboundCorrespondenceDateReceived":"2017-05-06",
             |               "inboundCorrespondenceDueDate":"2017-05-07",
             |               "periodKey":"18A1"
             |            },
             |            {
             |               "status":"O",
             |               "inboundCorrespondenceFromDate":"2017-04-01",
             |               "inboundCorrespondenceToDate":"2017-06-30",
             |               "inboundCorrespondenceDueDate":"2017-08-07",
             |               "periodKey":"18A2"
             |            }
             |         ]
             |      }
             |   ]
             |}
             |""".stripMargin)

        val obligations =
          ObligationsResponse(Seq(
            Obligation(
              periodKey = "18A1",
              start = "2017-01-01",
              end = "2017-03-31",
              due = "2017-05-07",
              status = "F",
              received  = Some("2017-05-06")
            ),
            Obligation(
              periodKey = "18A2",
              start = "2017-04-01",
              end = "2017-06-30",
              due = "2017-08-07",
              status = "O",
              received  = None
            )
          ))

        desJson.as[ObligationsResponse] shouldBe obligations

      }

      "multiple obligations are returned (Hybrid customers)" in {
        val desJson = Json.parse(
          s"""{
             |   "obligations":[
             |      {
             |         "identification":{
             |            "referenceNumber":"123456789",
             |            "referenceType":"VRN"
             |         },
             |         "obligationDetails":[
             |            {
             |               "status":"F",
             |               "inboundCorrespondenceFromDate":"2017-01-01",
             |               "inboundCorrespondenceToDate":"2017-03-31",
             |               "inboundCorrespondenceDateReceived":"2017-05-06",
             |               "inboundCorrespondenceDueDate":"2017-05-07",
             |               "periodKey":"18A1"
             |            }
             |         ]
             |      },
             |      {
             |         "identification":{
             |            "referenceNumber":"123456789",
             |            "referenceType":"VRN"
             |         },
             |         "obligationDetails":[
             |            {
             |               "status":"O",
             |               "inboundCorrespondenceFromDate":"2017-04-01",
             |               "inboundCorrespondenceToDate":"2017-06-30",
             |               "inboundCorrespondenceDueDate":"2017-08-07",
             |               "periodKey":"18A2"
             |            }
             |          ]
             |       }
             |   ]
             |}
             |""".stripMargin)

        val obligations =
          ObligationsResponse(Seq(
            Obligation(
              periodKey = "18A1",
              start = "2017-01-01",
              end = "2017-03-31",
              due = "2017-05-07",
              status = "F",
              received  = Some("2017-05-06")
            ),
            Obligation(
              periodKey = "18A2",
              start = "2017-04-01",
              end = "2017-06-30",
              due = "2017-08-07",
              status = "O",
              received  = None
            )
          ))

        desJson.as[ObligationsResponse] shouldBe
          obligations

      }
    }

    "write the correct model" when {

      "single obligation is returned" in {
        val obligation =
          ObligationsResponse(Seq(
            Obligation (
              periodKey = "18A1",
              start = "2017-01-01",
              end = "2017-03-31",
              due = "2017-05-07",
              status = "F",
              received  = Some("2017-05-06")
            ))
          )


        val json = Json.parse(
          s"""{
             |    "obligations": [
             |        {
             |            "periodKey": "18A1",
             |            "start": "2017-01-01",
             |            "end": "2017-03-31",
             |            "due": "2017-05-07",
             |            "received": "2017-05-06",
             |            "status": "F"
             |        }
             |    ]
             |}
             |""".stripMargin
        )

        Json.toJson(obligation) shouldBe json
      }

      "multiple obligations are returned" in {

        val obligations =
          ObligationsResponse(Seq(
            Obligation(
              periodKey = "18A1",
              start = "2017-01-01",
              end = "2017-03-31",
              due = "2017-05-07",
              status = "F",
              received  = Some("2017-05-06")
            ),
            Obligation(
              periodKey = "18A2",
              start = "2017-04-01",
              end = "2017-06-30",
              due = "2017-08-07",
              status = "O",
              received  = None
            )
          ))

        val json = Json.parse(
          s"""{
             |    "obligations": [
             |        {
             |            "periodKey": "18A1",
             |            "start": "2017-01-01",
             |            "end": "2017-03-31",
             |            "due": "2017-05-07",
             |            "received": "2017-05-06",
             |            "status": "F"
             |        },
             |        {
             |            "periodKey": "18A2",
             |            "start": "2017-04-01",
             |            "end": "2017-06-30",
             |            "due": "2017-08-07",
             |            "status": "O"
             |        }
             |    ]
             |}""".stripMargin
        )

        Json.toJson(obligations) shouldBe json
      }

    }

    "return empty model" when {

      "DES response contains no obligationDetails" in {
        val desJson = Json.parse(
          s"""{
             |   "obligations":[
             |      {
             |         "identification":{
             |            "referenceNumber":"123456789",
             |            "referenceType":"VRN"
             |         },
             |         "obligationDetails":[
             |         ]
             |      }
             |   ]
             |}
             |""".stripMargin)

        val obligations = ObligationsResponse(Seq())

        desJson.as[ObligationsResponse] shouldBe obligations
      }

      "response written with empty ObligationsResponse object" in {
        val obligations = ObligationsResponse(Seq())

        val json = Json.parse(
          s"""{
             |   "obligations":[]
             |}
             |""".stripMargin
        )

        Json.toJson(obligations) shouldBe json
      }

    }
  }
}
