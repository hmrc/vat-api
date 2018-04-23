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
import play.api.libs.json._
import uk.gov.hmrc.auth.core.retrieve._
import uk.gov.hmrc.auth.core.{AffinityGroup, ConfidenceLevel, CredentialRole}
import uk.gov.hmrc.domain.Vrn
import uk.gov.hmrc.http.controllers.RestFormats
import uk.gov.hmrc.vatapi.models.isoInstantDateFormat

case class NRSSubmission( payload: String,
                          metadata: Metadata )

object NRSSubmission{
  implicit val mdFormat: OFormat[Metadata] = Metadata.format
  implicit val format: OFormat[NRSSubmission] = Json.format[NRSSubmission]
}

//Identity Data should always be populated, but allow it to be optional for when the authEnabled flag is disabled
case class Metadata(businessId: String,
                    notableEvent: String,
                    payloadContentType: String,
                    payloadSha256Checksum: Option[String],
                    userSubmissionTimestamp: DateTime,
                    identityData: Option[IdentityData],
                    userAuthToken: String,
                    headerData: JsValue,
                    searchKeys: SearchKeys)

object Metadata{
  implicit val idformat: OFormat[IdentityData] = IdentityData.format
  implicit val format: OFormat[Metadata] = Json.format[Metadata]
}

//Todo: match against NRS mandatory fields with what may not be returned from auth.  Appropriate error handling
case class IdentityData(internalId: Option[String] = None,
                        externalId: Option[String] = None,
                        agentCode: Option[String] = None,
                        credentials: Credentials,
                        confidenceLevel: ConfidenceLevel,
                        nino: Option[String] = None,
                        saUtr: Option[String] = None,
                        name: Name,
                        dateOfBirth: Option[LocalDate] = None,
                        email: Option[String] = None,
                        agentInformation: AgentInformation,
                        groupIdentifier: Option[String] = None,
                        credentialRole: Option[CredentialRole],
                        mdtpInformation: Option[MdtpInformation] = None,
                        itmpName: Option[ItmpName],
                        itmpDateOfBirth: Option[LocalDate] = None,
                        itmpAddress: Option[ItmpAddress],
                        affinityGroup: Option[AffinityGroup],
                        credentialStrength: Option[String] = None,
                        loginTimes: LoginTimes)

object IdentityData {
  implicit val localDateFormat: Format[LocalDate] = RestFormats.localDateFormats
  implicit val credFormat: OFormat[Credentials] = Json.format[Credentials]
  implicit val nameFormat: OFormat[Name] = Json.format[Name]
  implicit val agentInfoFormat: OFormat[AgentInformation] = Json.format[AgentInformation]
  implicit val mdtpInfoFormat: OFormat[MdtpInformation] = Json.format[MdtpInformation]
  implicit val itmpNameFormat: OFormat[ItmpName] = Json.format[ItmpName]
  implicit val itmpAddressFormat: OFormat[ItmpAddress] = Json.format[ItmpAddress]
  implicit val loginTimesFormat: OFormat[LoginTimes] = Json.format[LoginTimes]
  implicit val format: OFormat[IdentityData] = Json.format[IdentityData]
}

case class SearchKeys(vrn: Option[Vrn] = None,
                      companyName: Option[String] = None,
                      taxPeriodEndDate: Option[LocalDate] = None,
                      periodKey: Option[String] = None
                     )

object SearchKeys{
  implicit val localDateFormat: Format[LocalDate] = RestFormats.localDateFormats
  implicit val format: OFormat[SearchKeys] = Json.format[SearchKeys]
}