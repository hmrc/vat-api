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

import play.api.libs.json.{Json, OFormat, Writes}
import utils.DateTimeUtil

case class Metadata(businessId: String = "vat",
                    notableEvent: String = "vat-return",
                    payloadContentType: String = "application/json",
                    payloadSha256Checksum: String,
                    userSubmissionTimestamp: LocalDateTime,
                    identityData: IdentityData,
                    userAuthToken: String,
                    headerData: Map[String, String],
                    searchKeys: SearchKeys)

object Metadata {
  implicit val dateToString: Writes[LocalDateTime] = Writes { date =>
    Json.toJson(date.format(
      DateTimeUtil.dateTimeFormatter
    ))
  }

  implicit val format: OFormat[Metadata] = Json.format[Metadata]
}

