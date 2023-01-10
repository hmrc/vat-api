/*
 * Copyright 2023 HM Revenue & Customs
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

class ObligationSpec extends UnitSpec {

  "Obligation" should {
    "read from the downstream model" when {

      "all fields are present" in {
        val desJson = Json.parse(
          s"""|
              |{
              |  "status": "F",
              |  "inboundCorrespondenceFromDate": "2017-04-06",
              |  "inboundCorrespondenceToDate": "2017-07-05",
              |  "inboundCorrespondenceDateReceived": "2017-08-01",
              |  "inboundCorrespondenceDueDate": "2017-08-05",
              |  "periodKey": "#001"
              |}
              |""".stripMargin)

        val obligation = Obligation(
          periodKey = "#001",
          start = "2017-04-06",
          end = "2017-07-05",
          due = "2017-08-05",
          status = "F",
          received  = Some("2017-08-01")
        )

        desJson.as[Obligation] shouldBe obligation

      }

      "only mandatory fields are present" in {
        val desJson = Json.parse(
          s"""|
              |{
              |  "status": "O",
              |  "inboundCorrespondenceFromDate": "2017-04-06",
              |  "inboundCorrespondenceToDate": "2017-07-05",
              |  "inboundCorrespondenceDueDate": "2017-08-05",
              |  "periodKey": "#001"
              |}
              |""".stripMargin)

        val obligation = Obligation(
          periodKey = "#001",
          start = "2017-04-06",
          end = "2017-07-05",
          due = "2017-08-05",
          status = "O",
          received = None
        )

        desJson.as[Obligation] shouldBe obligation
      }

    }

    "write the correct model" when {

      "all fields are present" in {
        val obligation = Obligation(
          periodKey = "#001",
          start = "2017-04-06",
          end = "2017-07-05",
          due = "2017-08-05",
          status = "F",
          received  = Some("2017-08-01")
        )
        val json = Json.parse(
          s"""{
             | "periodKey": "#001",
             | "start": "2017-04-06",
             | "end": "2017-07-05",
             | "due": "2017-08-05",
             | "status": "F",
             | "received": "2017-08-01"
             |}""".stripMargin)

        Json.toJson(obligation) shouldBe json
      }

      "only mandatory fields are present" in {
        val obligation = Obligation(
          periodKey = "#001",
          start = "2017-04-06",
          end = "2017-07-05",
          due = "2017-08-05",
          status = "F",
          received  = None
        )
        val json = Json.parse(
          s"""{
             | "periodKey": "#001",
             | "start": "2017-04-06",
             | "end": "2017-07-05",
             | "due": "2017-08-05",
             | "status": "F"
             |}""".stripMargin)

        Json.toJson(obligation) shouldBe json
      }
    }
  }
}
