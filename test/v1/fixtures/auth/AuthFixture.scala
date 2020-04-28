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

package v1.fixtures.auth

import org.joda.time.DateTime
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.auth.core.retrieve._
import v1.models.auth.UserDetails
import v1.models.nrs.IdentityData

import scala.collection.Seq

object AuthFixture {

  val vatEnrolments: Enrolments =
    Enrolments(
      enrolments = Set(
        Enrolment(
          key = "HMRC-MTD-VAT",
          identifiers = Seq(
            EnrolmentIdentifier("VRN", "123456789")
          ),
          state = "Active"
        )
      )
    )

  val agentEnrolments: Enrolments =
    Enrolments(
      enrolments = Set(
        Enrolment(
          key = "HMRC-MTD-VAT",
          identifiers = Seq(
            EnrolmentIdentifier("VRN", "123456789")
          ),
          state = "Active"
        ),
        Enrolment(
          key = "HMRC-AS-AGENT",
          identifiers = Seq(
            EnrolmentIdentifier("AgentReferenceNumber", "987654321")
          ),
          state = "Active"
        )
      )
    )

  val identityData: IdentityData = IdentityData(
    internalId = Some("Int-a7688cda-d983-472d-9971-ddca5f124641"),
    externalId = Some("Ext-c4ebc935-ac7a-4cc2-950a-19e6fac91f2a"),
    agentCode = None,
    credentials = Some(retrieve.Credentials(
      providerId = "8124873381064832",
      providerType = "GovernmentGateway"
    )),
    confidenceLevel = ConfidenceLevel.L200,
    name = Some(Name(
      name = Some("TestUser"),
      lastName = None
    )),
    email = Some("user@test.com"),
    agentInformation = AgentInformation(
      agentCode = None,
      agentFriendlyName = None,
      agentId = None
    ),
    groupIdentifier = Some("testGroupId-840cf4e3-c8ad-48f4-80fd-ea267f916be5"),
    credentialRole = Some(User),
    itmpName = ItmpName(
      givenName = None,
      middleName = None,
      familyName = None
    ),
    itmpAddress = ItmpAddress(
      line1 = None,
      line2 = None,
      line3 = None,
      line4 = None,
      line5 = None,
      postCode = None,
      countryName = None,
      countryCode = None
    ),
    affinityGroup = Some(AffinityGroup.Organisation),
    credentialStrength = Some("strong"),
    loginTimes = LoginTimes(
      currentLogin = DateTime.parse("2018-04-16T11:00:55Z"),
      previousLogin = None
    )
  )

  val indIdentityData: IdentityData = identityData.copy(affinityGroup = Some(AffinityGroup.Individual))

  val agentIdentityData: IdentityData = identityData.copy(affinityGroup = Some(AffinityGroup.Agent)).copy(agentInformation = AgentInformation(
    agentCode = Some("AGENT007"),
    agentFriendlyName = Some("James"),
    agentId = Some("987654321")
  ))

  val organisationResponse =
    new ~(new ~(new ~(new ~(new ~(new ~(new ~(new ~(new ~(new ~(new ~(new ~(new ~(new ~(new ~(new ~(new ~(
      Option(AffinityGroup.Organisation),
      vatEnrolments),
      identityData.internalId),
      identityData.externalId),
      None),
      identityData.credentials),
      identityData.confidenceLevel),
      None),
      None),
      identityData.name)
      , None),
      identityData.email),
      identityData.agentInformation),
      identityData.groupIdentifier),
      identityData.credentialRole),
      None),
      identityData.credentialStrength),
      identityData.loginTimes
    )

  val individualResponse =
    new ~(new ~(new ~(new ~(new ~(new ~(new ~(new ~(new ~(new ~(new ~(new ~(new ~(new ~(new ~(new ~(new ~(
      Option(AffinityGroup.Individual),
      vatEnrolments),
      identityData.internalId),
      identityData.externalId),
      None),
      identityData.credentials),
      identityData.confidenceLevel),
      None),
      None),
      identityData.name)
      , None),
      identityData.email),
      identityData.agentInformation),
      identityData.groupIdentifier),
      identityData.credentialRole),
      None),
      identityData.credentialStrength),
      identityData.loginTimes
    )

  val agentResponse =
    new ~(new ~(new ~(new ~(new ~(new ~(new ~(new ~(new ~(new ~(new ~(new ~(new ~(new ~(new ~(new ~(new ~(
      Option(AffinityGroup.Agent),
      agentEnrolments),
      agentIdentityData.internalId),
      agentIdentityData.externalId),
      Some("AGENT007")),
      agentIdentityData.credentials),
      agentIdentityData.confidenceLevel),
      None),
      None),
      agentIdentityData.name)
      , None),
      agentIdentityData.email),
      agentIdentityData.agentInformation),
      agentIdentityData.groupIdentifier),
      agentIdentityData.credentialRole),
      None),
      agentIdentityData.credentialStrength),
      agentIdentityData.loginTimes
    )

  val userDetails: (AffinityGroup, AgentInformation) => UserDetails = (Individual, agentInformation) => UserDetails(userType = Individual.toString,
    agentReferenceNumber = agentInformation.agentId,
    clientId = "",
    identityData = Option(
      IdentityData(
        internalId = Some("Int-a7688cda-d983-472d-9971-ddca5f124641"),
        externalId = Some("Ext-c4ebc935-ac7a-4cc2-950a-19e6fac91f2a"),
        agentCode = agentInformation.agentCode,
        credentials = Some(Credentials("8124873381064832", "GovernmentGateway")),
        confidenceLevel = ConfidenceLevel.L200,
        nino = None,
        saUtr = None,
        name = Some(
          Name(
            name = Some("TestUser"),
            lastName = None)),
        dateOfBirth = None,
        email = Some("user@test.com"),
        agentInformation = agentInformation,
        groupIdentifier = Some("testGroupId-840cf4e3-c8ad-48f4-80fd-ea267f916be5"),
        credentialRole = Some(User),
        mdtpInformation = None,
        itmpName = ItmpName(
          givenName = None,
          familyName = None,
          middleName = None),
        itmpDateOfBirth = None,
        itmpAddress = ItmpAddress(
          line1 = None,
          line2 = None,
          line3 = None,
          line4 = None,
          line5 = None,
          postCode = None,
          countryName = None,
          countryCode = None),
        affinityGroup = Some(Individual),
        credentialStrength = Some("strong"),
        loginTimes = LoginTimes(
          currentLogin = DateTime.parse("2018-04-16T11:00:55.000Z"),
          previousLogin = None))))
}
