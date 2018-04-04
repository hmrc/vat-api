/*
 * Copyright 2018 HM Revenue & Customs
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

package nrs.models

import org.joda.time.{DateTime, LocalDate}
import play.api.libs.functional.syntax._
import play.api.libs.json.{Writes, __, _}
import uk.gov.hmrc.auth.core.ConfidenceLevel
import uk.gov.hmrc.domain.{Nino, SaUtr, Vrn}
import uk.gov.hmrc.http.controllers.RestFormats

case class NRSSubmission(payload: String,
                      metadata: Metadata)

object NRSSubmission{
  implicit val mdFormat: OFormat[Metadata] = Metadata.format
  implicit val format: OFormat[NRSSubmission] = Json.format[NRSSubmission]
}

case class Metadata(businessId: String,
                    notableEvent: String,
                    payloadContentType: String,
                    payloadSha256Checksum: Option[String],
                    userSubmissionTimestamp: DateTime,
                    identityData: IdentityData,
                    userAuthToken: String,
                    headerData: HeaderData,
                    searchKeys: SearchKeys)

object Metadata{
  implicit val dateTimeFormat: Format[DateTime] = RestFormats.dateTimeFormats
  implicit val idformat: OFormat[IdentityData] = IdentityData.format
  implicit val hdWrts: Writes[HeaderData] = HeaderData.writes
  implicit val hdRds: Reads[HeaderData] = HeaderData.reads
  implicit val format: OFormat[Metadata] = Json.format[Metadata]
}

case class IdentityData(internalId: Option[String] = None,
                        externalId: Option[String] = None,
                        agentCode: Option[String] = None,
                        credentials: Option[Credentials] = None,
                        confidenceLevel: Option[ConfidenceLevel] = None,
                        nino: Option[Nino] = None,
                        saUtr: Option[SaUtr] = None,
                        name: Option[Name] = None,
                        dateOfBirth: Option[LocalDate] = None,
                        email: Option[String] = None,
                        agentInformation: Option[AgentInformation] = None,
                        groupIdentifier: Option[String] = None,
                        credentialRole: Option[String] = None,
                        mdtpInformation: Option[MdtpInformation] = None,
                        itmpName: Option[ItmpName] = None,
                        itmpDateOfBirth: Option[LocalDate] = None,
                        itmpAddress: Option[ItmpAddress] = None,
                        affinityGroup: Option[String] = None,
                        credentialStrength: Option[String] = None,
                        loginTimes: Option[LoginTimes] = None)

object IdentityData {
  implicit val localDateFormat: Format[LocalDate] = RestFormats.localDateFormats
  implicit val dateTimeReads: Format[DateTime] = RestFormats.dateTimeFormats
  implicit val format: OFormat[IdentityData] = Json.format[IdentityData]
}

case class HeaderData(publicIp: Option[String] = None,
                      port: Option[String] = None,
                      deviceId: Option[String] = None,
                      userId: Option[String] = None,
                      timeZone: Option[String] = None,
                      localIp: Option[String] = None,
                      screenResolution: Option[String] = None,
                      windowSize: Option[String] = None,
                      colourDepth: Option[String] = None)

object HeaderData {
  implicit val writes: Writes[HeaderData] = (
    (__ \ "Gov-Client-Public-IP").writeNullable[String] and
      (__ \ "Gov-Client-Public-Port").writeNullable[String] and
      (__ \ "Gov-Client-Device-ID").writeNullable[String] and
      (__ \ "Gov-Client-User-ID").writeNullable[String] and
      (__ \ "Gov-Client-Timezone").writeNullable[String] and
      (__ \ "Gov-Client-Local-IP").writeNullable[String] and
      (__ \ "Gov-Client-Screen-Resolution").writeNullable[String] and
      (__ \ "Gov-Client-Window-Size").writeNullable[String] and
      (__ \ "Gov-Client-Colour-Depth").writeNullable[String]
    )(unlift(HeaderData.unapply))

  implicit val reads: Reads[HeaderData] = (
    (__ \ "Gov-Client-Public-IP").readNullable[String] and
      (__ \ "Gov-Client-Public-Port").readNullable[String] and
      (__ \ "Gov-Client-Device-ID").readNullable[String] and
      (__ \ "Gov-Client-User-ID").readNullable[String] and
      (__ \ "Gov-Client-Timezone").readNullable[String] and
      (__ \ "Gov-Client-Local-IP").readNullable[String] and
      (__ \ "Gov-Client-Screen-Resolution").readNullable[String] and
      (__ \ "Gov-Client-Window-Size").readNullable[String] and
      (__ \ "Gov-Client-Colour-Depth").readNullable[String]
    )(HeaderData.apply _)
}

case class SearchKeys(vrn: Vrn,
                      companyName: String,
                      taxPeriodEndDate: LocalDate)

object SearchKeys{
  implicit val localDateFormat: Format[LocalDate] = RestFormats.localDateFormats
  implicit val format: OFormat[SearchKeys] = Json.format[SearchKeys]
}

case class Credentials(providerId: String, providerType: String)

object Credentials {
  implicit val format: Format[Credentials]  = Json.format[Credentials]
}

case class Name(name: Option[String], lastName: Option[String])

object Name {
  implicit val format: Format[Name] = Json.format[Name]
}

case class PostCode(value: String)

object PostCode {
  implicit val format: Format[PostCode] = Json.format[PostCode]
}

case class ItmpName(givenName: Option[String],
                    middleName: Option[String],
                    familyName: Option[String])

object ItmpName {
  implicit val format: Format[ItmpName] = Json.format[ItmpName]
}

case class ItmpAddress(line1: Option[String],
                       line2: Option[String],
                       line3: Option[String],
                       line4: Option[String],
                       line5: Option[String],
                       postCode: Option[String],
                       countryName: Option[String],
                       countryCode: Option[String])

object ItmpAddress {
  implicit val format: Format[ItmpAddress] = Json.format[ItmpAddress]
}

case class MdtpInformation(deviceId: String, sessionId: String)
object MdtpInformation {
  implicit val format: Format[MdtpInformation] = Json.format[MdtpInformation]
}

case class AgentInformation(agentId: Option[String],
                            agentCode: Option[String],
                            agentFriendlyName: Option[String])

object AgentInformation {
  implicit val format: Format[AgentInformation] = Json.format[AgentInformation]
}

case class LoginTimes(currentLogin: DateTime, previousLogin: Option[DateTime])

object LoginTimes {
  implicit val dateTimeReads: Format[DateTime] = RestFormats.dateTimeFormats
  implicit val format: Format[LoginTimes] = Json.format[LoginTimes]
}
