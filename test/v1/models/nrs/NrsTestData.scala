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

import org.joda.time.{DateTime, DateTimeZone, LocalDate}
import play.api.libs.json.{JsObject, JsValue, Json}
import uk.gov.hmrc.auth.core.AffinityGroup.Agent
import uk.gov.hmrc.auth.core.retrieve._
import uk.gov.hmrc.auth.core.{ConfidenceLevel, User}
import utils.HashUtil
import v1.models.nrs.request._
import v1.models.nrs.response.NrsResponse

object NrsTestData {

  object IdentityDataTestData {

    val correctJson: JsValue = Json.parse(
      """{
        |  "internalId": "some-id",
        |  "externalId": "some-id",
        |  "agentCode": "TZRXXV",
        |  "credentials": {"providerId": "12345-credId",
        |  "providerType": "GovernmentGateway"},
        |  "confidenceLevel": 200,
        |  "nino": "DH00475D",
        |  "saUtr": "Utr",
        |  "name": { "name": "test", "lastName": "test" },
        |  "dateOfBirth": "1985-01-01",
        |  "email":"test@test.com",
        |  "agentInformation": {
        |    "agentId": "BDGL",
        |    "agentCode" : "TZRXXV",
        |    "agentFriendlyName" : "Bodgitt & Legget LLP"
        |  },
        |  "groupIdentifier" : "GroupId",
        |  "credentialRole": "User",
        |  "mdtpInformation" : {"deviceId" : "DeviceId",
        |    "sessionId": "SessionId" },
        |  "itmpName" : {},
        |  "itmpAddress" : {},
        |  "affinityGroup": "Agent",
        |  "credentialStrength": "strong",
        |  "loginTimes": {
        |    "currentLogin": "2016-11-27T09:00:00Z",
        |    "previousLogin": "2016-11-01T12:00:00Z"
        |  }
        |}
      """.stripMargin
    )

    val correctModel: IdentityData = request.IdentityData(
      internalId = Some("some-id"),
      externalId = Some("some-id"),
      agentCode = Some("TZRXXV"),
      credentials = Some(Credentials("12345-credId", "GovernmentGateway")),
      confidenceLevel = ConfidenceLevel.L200,
      nino = Some("DH00475D"),
      saUtr = Some("Utr"),
      name = Some(Name(Some("test"), Some("test"))),
      dateOfBirth = Some(LocalDate.parse("1985-01-01")),
      email = Some("test@test.com"),
      agentInformation = AgentInformation(agentCode = Some("TZRXXV"), agentFriendlyName = Some("Bodgitt & Legget LLP"), agentId = Some("BDGL")),
      groupIdentifier = Some("GroupId"),
      credentialRole = Some(User),
      mdtpInformation = Some(MdtpInformation("DeviceId", "SessionId")),
      itmpName = ItmpName(None, None, None),
      itmpDateOfBirth = None,
      itmpAddress = ItmpAddress(None, None, None, None, None, None, None, None),
      affinityGroup = Some(Agent),
      credentialStrength = Some("strong"),
      loginTimes = LoginTimes(
        DateTime.parse("2016-11-27T09:00:00Z").withZone(DateTimeZone.UTC),
        Some(DateTime.parse("2016-11-01T12:00:00Z").withZone(DateTimeZone.UTC))
      )
    )
  }

  object MetadataTestData {
    val correctJson: JsValue = Json.parse(
      s"""
         |{
         |    "businessId": "vat",
         |    "notableEvent": "vat-return",
         |    "payloadContentType": "application/json",
         |    "payloadSha256Checksum":"2c98a3e52aed1f06728e35e4f47699bd4af6f328c3dabfde998007382dba86ce",
         |    "userSubmissionTimestamp": "2018-04-07T12:13:25Z",
         |    "identityData": ${IdentityDataTestData.correctJson},
         |    "userAuthToken": "Bearer AbCdEf123456...",
         |    "headerData": {
         |      "Gov-Client-Public-IP": "127.0.0.0",
         |      "Gov-Client-Public-Port": "12345",
         |      "Gov-Client-Device-ID": "beec798b-b366-47fa-b1f8-92cede14a1ce",
         |      "Gov-Client-User-ID": "alice_desktop",
         |      "Gov-Client-Timezone": "GMT+3",
         |      "Gov-Client-Local-IP": "10.1.2.3",
         |      "Gov-Client-Screen-Resolution": "1920x1080",
         |      "Gov-Client-Window-Size": "1256x803",
         |      "Gov-Client-Colour-Depth": "24"
         |    },
         |    "searchKeys": {
         |      "vrn": "123456789",
         |      "periodKey": "18AA"
         |    }
         |}
      """.stripMargin
    )

    val correctModel: Metadata = Metadata(
      businessId = "vat",
      notableEvent = "vat-return",
      payloadContentType = "application/json",
      payloadSha256Checksum = HashUtil.getHash("XXX-base64checksum-XXX"),
      userSubmissionTimestamp = DateTime.parse("2018-04-07T12:13:25Z"),
      identityData = Some(IdentityDataTestData.correctModel),
      userAuthToken = "Bearer AbCdEf123456...",
      headerData = Json.toJson(Map(
        "Gov-Client-Public-IP" -> "127.0.0.0",
        "Gov-Client-Public-Port" -> "12345",
        "Gov-Client-Device-ID" -> "beec798b-b366-47fa-b1f8-92cede14a1ce",
        "Gov-Client-User-ID" -> "alice_desktop",
        "Gov-Client-Timezone" -> "GMT+3",
        "Gov-Client-Local-IP" -> "10.1.2.3",
        "Gov-Client-Screen-Resolution" -> "1920x1080",
        "Gov-Client-Window-Size" -> "1256x803",
        "Gov-Client-Colour-Depth" -> "24"
      )),
      searchKeys =
        SearchKeys(
          vrn = Some("123456789"),
          companyName = None,
          periodKey = Some("18AA"),
          taxPeriodEndDate = None
        )
    )
  }

  object SearchKeysTestData {
    val correctJson: JsObject = Json.obj(
      "vrn" -> "vrn",
      "companyName" -> "Good, Bad & Ugly Ltd",
      "taxPeriodEndDate" -> "2018-06-04",
      "periodKey" -> "period key"
    )

    val correctModel: SearchKeys =
      SearchKeys(
        vrn = Some("vrn"),
        companyName = Some("Good, Bad & Ugly Ltd"),
        taxPeriodEndDate = Some(LocalDate.parse("2018-06-04")),
        periodKey = Some("period key")
      )
  }

  object FullRequestTestData {
    val correctJson: JsObject = Json.obj(
      "payload" -> "XXX-base64checksum-XXX",
      "metadata" -> MetadataTestData.correctJson
    )

    val correctModel: NrsSubmission = NrsSubmission(
      "XXX-base64checksum-XXX", MetadataTestData.correctModel
    )
  }

  object NrsResponseTestData {

    val correctJson: JsValue = Json.parse(
      """
        |{
        |  "nrSubmissionId": "anID",
        |  "cadesTSignature": "aSignature",
        |  "timestamp": "aTimeStamp"
        |}
    """.stripMargin
    )

    val correctModel: NrsResponse =
      NrsResponse(
        nrSubmissionId = "anID",
        cadesTSignature = "This has been deprecated - DO NOT USE",
        timestamp = ""
      )
  }

}
