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

package utils

import org.joda.time.DateTime
import play.api.libs.json.{Format, JodaReads, JodaWrites}

object DateUtils {

  val isoInstantDatePattern = "yyyy-MM-dd'T'HH:mm:ss'Z'"
  val dateTimePattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"

  val dateTimeFormat: Format[DateTime] = Format[DateTime](
    JodaReads.jodaDateReads(dateTimePattern),
    JodaWrites.jodaDateWrites(dateTimePattern)
  )

  val defaultDateTimeFormat: Format[DateTime] = Format[DateTime](
    JodaReads.jodaDateReads(isoInstantDatePattern),
    JodaWrites.jodaDateWrites(dateTimePattern)
  )
}
