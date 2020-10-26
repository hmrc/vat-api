/*
 * Copyright 2020 HM Revenue & Customs
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

object PagerDutyLogging {

  def timeOfDay(now: Int = DateTime.now().getHourOfDay): TimeOfDay.Value = {
    val beforeStartOfWorkDay: Boolean = now < 9
    val afterEndOfWorkDay: Boolean = now >= 17
    if (beforeStartOfWorkDay || afterEndOfWorkDay) {
      TimeOfDay.OUT_OF_HOURS
    } else {
      TimeOfDay.IN_HOURS
    }
  }

  def logError(loggerMessage: LoggerMessages.Value, status: Int, body: String,
               f: String => Unit, affinityGroup: String): Unit = {
    val message = s"DES error occurred. User type: $affinityGroup\n" + s"Status code: $status\nBody: $body"
    status.toString.head match {
      case '5' => f(s"$message ( ${loggerMessage.toString}_${timeOfDay().toString} )")
      case _   => f(message)
    }
  }
}
