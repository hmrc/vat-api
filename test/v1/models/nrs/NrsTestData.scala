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

import java.time.{Instant, LocalDateTime, ZoneId}

import org.joda.time.LocalDate
import play.api.libs.json.{JsObject, JsValue, Json}
import uk.gov.hmrc.auth.core.AffinityGroup.Agent
import uk.gov.hmrc.auth.core.retrieve._
import uk.gov.hmrc.auth.core.{ConfidenceLevel, User}

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
        |    "currentLogin": "2016-11-27T09:00:00.000Z",
        |    "previousLogin": "2016-11-01T12:00:00.000Z"
        |  }
        |}""".stripMargin)

    val correctModel: IdentityData = IdentityData(
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
      loginTimes = IdentityLoginTimes(
        LocalDateTime.ofInstant(Instant.parse("2016-11-27T09:00:00.000Z"), ZoneId.of("UTC")),
        Some(LocalDateTime.ofInstant(Instant.parse("2016-11-01T12:00:00.000Z"), ZoneId.of("UTC")))
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
         |    "payloadSha256Checksum": "426a1c28<snip>d6d363",
         |    "userSubmissionTimestamp": "2018-04-07T12:13:25.156Z",
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
      """.stripMargin)

    val correctModel: Metadata = Metadata(
      payloadSha256Checksum = "426a1c28<snip>d6d363",
      userSubmissionTimestamp = LocalDateTime.ofInstant(Instant.parse("2018-04-07T12:13:25.156Z"), ZoneId.of("UTC")),
      identityData = IdentityDataTestData.correctModel,
      userAuthToken = "Bearer AbCdEf123456...",
      headerData = Map(
        "Gov-Client-Public-IP" -> "127.0.0.0",
        "Gov-Client-Public-Port" -> "12345",
        "Gov-Client-Device-ID" -> "beec798b-b366-47fa-b1f8-92cede14a1ce",
        "Gov-Client-User-ID" -> "alice_desktop",
        "Gov-Client-Timezone" -> "GMT+3",
        "Gov-Client-Local-IP" -> "10.1.2.3",
        "Gov-Client-Screen-Resolution" -> "1920x1080",
        "Gov-Client-Window-Size" -> "1256x803",
        "Gov-Client-Colour-Depth" -> "24"
      ),
      searchKeys = SearchKeys(vrn = Some("123456789"), periodKey = Some("18AA"))
    )
  }

  object FullRequestTestData {
    val correctJson: JsObject = Json.obj(
      "payload" -> "XXX-base64checksum-XXX",
      "metadata" -> MetadataTestData.correctJson
    )

    val correctModel: NRSSubmission = NRSSubmission(
      "XXX-base64checksum-XXX", MetadataTestData.correctModel
    )
  }

}
