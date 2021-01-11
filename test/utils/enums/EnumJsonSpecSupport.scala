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

package utils.enums

import play.api.libs.json.{Format, Json}
import support.UnitSpec

trait EnumJsonSpecSupport {
  self: UnitSpec =>

  /**
    * Tests round-tripping
    *
    * @param namesAndValues Pairs (name, object) for all the objects in the enumeration under test
    * @tparam A the type of enumeration (sealed trait of objects) being tested
    */

  def testRoundTrip[A: Format](namesAndValues: (String, A)*): Unit =
    "JSON formats" must {
      "support round trip" in {
        namesAndValues.foreach {
          case (name, obj) =>
            val json = Json.parse(s""""$name"""")

            Json.toJson(obj) shouldBe json
            json.as[A] shouldBe obj
        }
      }
    }
}
