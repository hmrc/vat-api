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

package utils

import support.UnitSpec

class JsonErrorSanitiserSpec extends UnitSpec {

  "JsonErrorSanitiser" should {

    "return an empty string for an empty string" in {
      JsonErrorSanitiser.sanitise("") shouldBe ""
    }

    "return the original non-sensitive string with spaces trimmed" in {
      JsonErrorSanitiser.sanitise("  \n this data is not sensitive \n      ") shouldBe "this data is not sensitive"
    }

    "return the original sensitive string sanitised with spaces trimmed" in {
      JsonErrorSanitiser.sanitise("Invalid Json: sensitive information  ") shouldBe "Invalid Json"
    }

    "return a non-sentitive prefix" in {
      JsonErrorSanitiser.sanitise("Some not sensitive message. Invalid Json: sensitive information") shouldBe
        "Some not sensitive message."
    }

  }
}
