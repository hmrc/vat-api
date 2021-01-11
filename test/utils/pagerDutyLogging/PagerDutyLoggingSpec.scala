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

import org.joda.time.DateTime
import support.UnitSpec

class PagerDutyLoggingSpec extends UnitSpec {
  "timeOfDay" should {
    "return TimeOfDay.IN_HOURS" when {
      "date passed in is after 9am" in {
        PagerDutyLogging.timeOfDay(DateTime.parse("1970-01-01T09:00:00Z").getHourOfDay) shouldBe TimeOfDay.IN_HOURS
      }
      "date passed in is before 5pm" in {
        PagerDutyLogging.timeOfDay(DateTime.parse("1970-01-01T16:59:59Z").getHourOfDay) shouldBe TimeOfDay.IN_HOURS
      }
    }
    "return TimeOfDay.OUT_OF_HOURS" when {
      "date passed in is after 9am" in {
        PagerDutyLogging.timeOfDay(DateTime.parse("1970-01-01T08:59:59Z").getHourOfDay) shouldBe TimeOfDay.OUT_OF_HOURS
      }
      "date passed in is before 5pm" in {
        PagerDutyLogging.timeOfDay(DateTime.parse("1970-01-01T17:00:00Z").getHourOfDay) shouldBe TimeOfDay.OUT_OF_HOURS
      }
    }
  }
}
