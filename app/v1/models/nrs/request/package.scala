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

package v1.models.nrs

import java.time.LocalDateTime

import org.joda.time.{DateTime, LocalDate}
import play.api.libs.json._
import uk.gov.hmrc.http.controllers.RestFormats
import utils.DateTimeUtil

package object request {
  implicit val dateFormats: Format[LocalDate] = RestFormats.localDateFormats
  implicit val dateTimeFormat: Format[DateTime] = RestFormats.dateTimeFormats
  val datePattern = "yyyy-MM-dd"

  implicit val dateToString: Writes[LocalDateTime] = Writes {
    date => Json.toJson(date.format(DateTimeUtil.dateTimeFormatter))
  }

  val dateFormat: Format[LocalDate] = Format[LocalDate](
    JodaReads.jodaLocalDateReads(datePattern),
    JodaWrites.jodaLocalDateWrites(datePattern)
  )
}
