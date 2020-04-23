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

import org.joda.time.LocalDate
import play.api.libs.json.{Json, OWrites}
import uk.gov.hmrc.auth.core.retrieve._
import uk.gov.hmrc.auth.core.{AffinityGroup, ConfidenceLevel, CredentialRole}

case class IdentityData(internalId: Option[String],
                        externalId: Option[String],
                        agentCode: Option[String],
                        credentials: Option[Credentials],
                        confidenceLevel: ConfidenceLevel,
                        nino: Option[String],
                        saUtr: Option[String],
                        name: Option[Name],
                        dateOfBirth: Option[LocalDate],
                        email: Option[String],
                        agentInformation: AgentInformation,
                        groupIdentifier: Option[String],
                        credentialRole: Option[CredentialRole],
                        mdtpInformation: Option[MdtpInformation],
                        itmpName: ItmpName,
                        itmpDateOfBirth: Option[LocalDate],
                        itmpAddress: ItmpAddress,
                        affinityGroup: Option[AffinityGroup],
                        credentialStrength: Option[String],
                        loginTimes: LoginTimes)

object IdentityData {
  implicit val credFormat: OWrites[Credentials] = Json.writes[Credentials]
  implicit val nameFormat: OWrites[Name] = Json.writes[Name]
  implicit val agentInfoFormat: OWrites[AgentInformation] = Json.writes[AgentInformation]
  implicit val mdtpInfoFormat: OWrites[MdtpInformation] = Json.writes[MdtpInformation]
  implicit val itmpNameFormat: OWrites[ItmpName] = Json.writes[ItmpName]
  implicit val itmpAddressFormat: OWrites[ItmpAddress] = Json.writes[ItmpAddress]
  implicit val loginTimes: OWrites[LoginTimes] = Json.writes[LoginTimes]

  implicit val writes: OWrites[IdentityData] = Json.writes[IdentityData]
}
