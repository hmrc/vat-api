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

package utils

import play.api.libs.json.Json
import support.UnitSpec

import java.time.{LocalDate, OffsetDateTime}

class DateUtilsSpec extends UnitSpec {

  "in dateUtils" when {

    "dateTime reads and writes are called" should {

      val readJson = Json.parse(""" "2021-04-12T16:34:21.123Z" """)

      val writeJson = Json.parse(""" "2021-04-12T16:34:21.123Z" """)

      val dateTime: OffsetDateTime = OffsetDateTime.parse("2021-04-12T16:34:21.123Z")

      "return a SubmitRequestBody model" when {
        "valid json is provided" in {

          readJson.as[OffsetDateTime] shouldBe dateTime
        }
      }

      "write valid Json" when {
        "a valid model is provided" in {

          Json.toJson(dateTime) shouldBe writeJson
        }
      }
    }

    "isoInstantDate reads and writes" should {

      val readJson = Json.parse(""" "2021-04-12T16:34:21Z" """)

      val writeJson = Json.parse(""" "2021-04-12T16:34:21Z" """)

      val dateTime: OffsetDateTime = OffsetDateTime.parse("2021-04-12T16:34:21Z")

      "return a SubmitRequestBody model" when {
        "valid json is provided" in {

          readJson.as[OffsetDateTime] shouldBe dateTime
        }
      }

      "write valid Json" when {
        "a valid model is provided" in {

          Json.toJson(dateTime) shouldBe writeJson
        }
      }
    }

      "defaultDateTime reads and writes" should {

        val readJson = Json.parse(""" "2021-04-12T16:34:21Z" """)

        val writeJson = Json.parse(""" "2021-04-12T16:34:21Z" """)

        val dateTime: OffsetDateTime = OffsetDateTime.parse("2021-04-12T16:34:21Z")

        "return a SubmitRequestBody model" when {
          "valid json is provided" in {

            readJson.as[OffsetDateTime] shouldBe dateTime
          }
        }

        "write valid Json" when {
          "a valid model is provided" in {

            Json.toJson(dateTime) shouldBe writeJson
          }
        }
      }

      "date reads and writes" should {

        val readJson = Json.parse(""" "2021-04-12" """)

        val writeJson = Json.parse(""" "2021-04-12" """)

        val dateTime = LocalDate.parse("2021-04-12")

        "return a SubmitRequestBody model" when {
          "valid json is provided" in {

            readJson.as[LocalDate] shouldBe dateTime
          }
        }

        "write valid Json" when {
          "a valid model is provided" in {

            Json.toJson(dateTime) shouldBe writeJson
          }
        }
      }
  }
}