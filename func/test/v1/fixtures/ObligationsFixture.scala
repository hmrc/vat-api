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

package v1.fixtures

import play.api.libs.json.{JsValue, Json}
import v1.models.response.obligations.{Obligation, ObligationsResponse}

trait ObligationsFixture {

  val obligationsDesJson: JsValue = Json.parse(
    s"""
       |{
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
       |""".stripMargin
  )
  val obligationsMtdJson: JsValue = Json.parse(
    s"""
       |{
       |  "obligations": [
       |    {
       |      "start": "2017-01-01",
       |      "end": "2017-03-31",
       |      "due": "2017-05-07",
       |      "status": "F",
       |      "periodKey": "18A1",
       |      "received": "2017-05-06"
       |    },
       |    {
       |      "start": "2017-04-01",
       |      "end": "2017-06-30",
       |      "due": "2017-08-07",
       |      "status": "O",
       |      "periodKey": "18A2"
       |    }
       |  ]
       |}
       |""".stripMargin
  )

  val obligationsResponse: ObligationsResponse =
    ObligationsResponse(Seq(
      Obligation(
        start = "2017-01-01",
        end = "2017-03-31",
        due = "2017-05-07",
        status = "F",
        periodKey = "18A1",
        received = Some("2017-05-06")
      ),
      Obligation(
        start = "2017-04-01",
        end =  "2017-06-30",
        due = "2017-08-07",
        status = "O",
        periodKey = "18A2",
        received = None
      )
    )
    )
}
