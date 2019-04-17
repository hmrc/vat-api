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

package uk.gov.hmrc.vatapi.utils

import uk.gov.hmrc.vatapi.UnitSpec

class JsonErrorSanitiserSpec extends UnitSpec {

  "JsonErrorSanitiser" should {

    "return an empty string for an empty string" in {

      val inputString = ""
      val expectedString = ""

      val result = JsonErrorSanitiser.sanitise(inputString)
      result shouldBe expectedString

    }

    "return the original non-sensitive string with spaces trimmed" in {

      val inputString = "this data is not sensitive \n      "
      val expectedString = "this data is not sensitive"

      val result = JsonErrorSanitiser.sanitise(inputString)
      result shouldBe expectedString

    }

    "return the original sensitive string sanitised with spaces trimmed" in {

      val inputString = "Invalid Json: Unexpected character (''' (code 39)): expected a valid value (number, String, array, object, 'true', 'false' or 'null')     "
      val expectedString = "Invalid Json"

      val result = JsonErrorSanitiser.sanitise(inputString)
      result shouldBe expectedString

    }

  }
}
