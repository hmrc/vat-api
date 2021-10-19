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

package utils.pagerDutyLogging

import support.UnitSpec
import play.api.http.Status._

class PagerDutyLoggingSpec extends UnitSpec {
  "generateMessage" should {
    "generate a message without the LoggerMessages value" when {
      "inputted status is not 5xx" in {
        PagerDutyLogging.generateMessage(LoggerMessages.SUBMIT_RETURN_500, NOT_FOUND, "DEF", "ABC") shouldBe
          s"DES error occurred. User type: ABC\n" + s"Status code: 404\nBody: DEF"
      }
    }
    "generate a message with the LoggerMessages value" when {
      "inputted status is 5xx" in {
        PagerDutyLogging.generateMessage(LoggerMessages.SUBMIT_RETURN_500, INTERNAL_SERVER_ERROR, "DEF", "ABC") shouldBe
          s"DES error occurred. User type: ABC\n" + s"Status code: 500\nBody: DEF ( SUBMIT_RETURN_500 )"
      }
    }
  }
}
