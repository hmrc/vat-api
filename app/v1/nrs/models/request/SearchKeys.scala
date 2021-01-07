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

package v1.nrs.models.request

import org.joda.time.LocalDate
import play.api.libs.json.{Format, Json, OFormat}
import utils.DateUtils

case class SearchKeys(vrn: Option[String] = None,
                      companyName: Option[String] = None,
                      taxPeriodEndDate: Option[LocalDate] = None,
                      periodKey: Option[String] = None
                     )

object SearchKeys {
  implicit val dateFormats: Format[LocalDate] = DateUtils.dateFormat
  implicit val format: OFormat[SearchKeys] = Json.format[SearchKeys]
}
