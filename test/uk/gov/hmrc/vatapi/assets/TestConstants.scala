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

package uk.gov.hmrc.vatapi.assets

import nrs.models._
import org.joda.time.DateTime
import play.api.libs.json.Json
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.auth.core.retrieve._
import uk.gov.hmrc.domain.Vrn
import uk.gov.hmrc.vatapi.auth.{Agent, AuthContext, Individual, Organisation}
import uk.gov.hmrc.vatapi.httpparsers.NRSData
import uk.gov.hmrc.vatapi.models.des.PaymentIndicator
import uk.gov.hmrc.vatapi.models.{VatReturnDeclaration, des}

object TestConstants {

  object Auth {

    val orgIdentityData  = IdentityData(
      internalId = Some("Int-a7688cda-d983-472d-9971-ddca5f124641"),
      externalId = Some("Ext-c4ebc935-ac7a-4cc2-950a-19e6fac91f2a"),
      agentCode = None,
      credentials = retrieve.Credentials(
        providerId = "8124873381064832",
        providerType = "GovernmentGateway"
      ),
      confidenceLevel = ConfidenceLevel.L200,
      name = Name(
        name = Some("TestUser"),
        lastName = None
      ),
      email = Some("user@test.com"),
      agentInformation = AgentInformation(
        agentCode = None,
        agentFriendlyName = None,
        agentId = None
      ),
      groupIdentifier = Some("testGroupId-840cf4e3-c8ad-48f4-80fd-ea267f916be5"),
      credentialRole = Some(User),
      itmpName = Some(ItmpName(
        givenName = Some("a"),
        middleName = Some("b"),
        familyName = Some("c")
      )),
      itmpAddress = Some(ItmpAddress(
        line1 = Some("1"),
        line2 = Some("2"),
        line3 = Some("3"),
        line4 = Some("4"),
        line5 = Some("5"),
        postCode = Some("cw93nm"),
        countryName = Some("uk"),
        countryCode = Some("uk")
      )),
      affinityGroup = Some(AffinityGroup.Organisation),
      credentialStrength = Some("strong"),
      loginTimes = LoginTimes(
        currentLogin = DateTime.parse("2018-04-16T11:00:55Z"),
        previousLogin = None
      )
    )

    val indIdentityData: IdentityData = orgIdentityData.copy(affinityGroup = Some(AffinityGroup.Individual))

    val agentIdentityData: IdentityData = orgIdentityData.copy(affinityGroup = Some(AffinityGroup.Agent)).copy(agentInformation = AgentInformation(
      agentCode = Some("AGENT007"),
      agentFriendlyName = Some("James"),
      agentId = Some("JB007")
    ))

    val orgAuthContextWithNrsData: AuthContext = Organisation(Some(orgIdentityData))
    val indAuthContextWithNrsData: AuthContext = Individual(Some(indIdentityData))
    val agentAuthContextWithNrsData: AuthContext = Agent(Some("AGENT007"), Some("JB007"), Some(agentIdentityData))

    val orgAuthContext: AuthContext = Organisation(None)
    val indAuthContext: AuthContext = Individual(None)
    val agentAuthContext: AuthContext = Agent(Some("AGENT007"), Some("JB007"), None)
  }

  object VatReturn {
    val vatReturnDeclaration = VatReturnDeclaration(
      periodKey = "#001",
      vatDueSales = -3600.15,
      vatDueAcquisitions = 12000.05,
      totalVatDue = 8399.90,
      vatReclaimedCurrPeriod = 124.15,
      netVatDue = 8275.75,
      totalValueSalesExVAT = 1000,
      totalValuePurchasesExVAT = 200,
      totalValueGoodsSuppliedExVAT = 100,
      totalAcquisitionsExVAT = 540,
      finalised = true
    )

    val desVatReturnDeclaration : DateTime =>  des.VatReturnDeclaration = time => vatReturnDeclaration.toDes().copy(receivedAt = time)

    val desVatReturnDeclarationAsJsonString : des.VatReturnDeclaration => String = desVatReturnDeclaration =>
      desVatReturnDeclaration.toJsonString

    val vatReturnsDes = des.VatReturnsDES(
      processingDate = DateTime.parse("2018-06-30T01:20"),
      paymentIndicator = PaymentIndicator.DirectDebit,
      formBundleNumber = "123456789012",
      chargeRefNumber = Some("SKDJGFH9URGT")
    )
  }

  object NRSResponse {

    val timestamp: DateTime = DateTime.parse("2018-02-14T09:32:15Z")

    //Examples taken from NRS Spec
    val nrsSubmission: NRSSubmission = NRSSubmission(
      payload = "payload",
      metadata = Metadata(
        businessId = "vat",
        notableEvent = "vat-return",
        payloadContentType = "application/json",
        payloadSha256Checksum = None,
        userSubmissionTimestamp = timestamp,
        identityData = Some(Auth.orgIdentityData),
        userAuthToken = "",
        headerData = Json.toJson(
          s"""
             |{
             |"Gov-Client-Public-IP":"198.51.100.0",
             |"Gov-Client-Public-Port":"12345",
             |"Gov-Client-Device-ID":"beec798b-b366-47fa-b1f8-92cede14a1ce",
             |"Gov-Client-User-ID":"alice_desktop",
             |"Gov-Client-Timezone":"GMT+3",
             |"Gov-Client-Local-IP":"10.1.2.3",
             |"Gov-Client-Screen-Resolution":"1920x1080",
             |"Gov-Client-Window-Size":"1256x803",
             |"Gov-Client-Colour-Depth":"24"
             |}
           """.stripMargin),
        searchKeys = SearchKeys(
          vrn = Some(Vrn("123456789")),
          periodKey = Some("AA34")
        )
      )
    )

    val nrsData: NRSData = NRSData(
      nrSubmissionId = "2dd537bc-4244-4ebf-bac9-96321be13cdc",
      cadesTSignature = "30820b4f06092a864886f70111111111c0445c464",
      timestamp = timestamp.toString
    )
  }
}
