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

package v1.nrs.models.request

import play.api.libs.json._
import utils.DateUtils
import java.time.{LocalDateTime, OffsetDateTime}

case class Metadata(
                     businessId: String,
                     notableEvent: String,
                     payloadContentType: String,
                     payloadSha256Checksum: String,
                     userSubmissionTimestamp: String,
                     identityData: Option[IdentityData],
                     userAuthToken: String,
                     headerData: JsValue,
                     searchKeys: SearchKeys)

object Metadata {
  implicit val idformat: OFormat[IdentityData] = IdentityData.format
  implicit val dateReads: Reads[OffsetDateTime] = DateUtils.isoInstantDateReads
  implicit val dateWrites: Writes[OffsetDateTime] = DateUtils.isoInstantDateWrites
  implicit val format: OFormat[Metadata] = Json.format[Metadata]
}
