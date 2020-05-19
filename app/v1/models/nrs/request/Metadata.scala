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

package v1.models.nrs.request

import org.joda.time.DateTime
import play.api.libs.json.{Json, OWrites, Writes}
import uk.gov.hmrc.http.controllers.RestFormats

case class Metadata(businessId: String,
                    notableEvent: String,
                    payloadContentType: String,
                    payloadSha256Checksum: Option[String],
                    userSubmissionTimestamp: DateTime,
                    identityData: IdentityData,
                    userAuthToken: String,
                    headerData: Map[String, String],
                    searchKeys: SearchKeys)

object Metadata {
  implicit val dateFormats: Writes[DateTime] = RestFormats.dateTimeWrite
  implicit val writes: OWrites[Metadata] = Json.writes[Metadata]
}
