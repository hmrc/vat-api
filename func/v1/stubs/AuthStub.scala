/*
 * Copyright 2021 HM Revenue & Customs
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

package v1.stubs

import com.github.tomakehurst.wiremock.stubbing.StubMapping
import play.api.http.Status._
import play.api.libs.json.{JsObject, JsValue, Json}
import support.WireMockMethods

object AuthStub extends WireMockMethods {

  private val authoriseUri: String = "/auth/authorise"

  private val mtdEnrolment: JsObject = Json.obj(
    "key" -> "HMRC-MTD-VAT",
    "identifiers" -> Json.arr(
      Json.obj(
        "key" -> "VRN",
        "value" -> "1234567890"
      )
    )
  )

  def authorised(): StubMapping = {
    when(method = POST, uri = authoriseUri)
      .thenReturn(status = OK, body = successfulAuthResponse(mtdEnrolment))
  }

  def authorisedWithNrs(): StubMapping = {
    when(method = POST, uri = authoriseUri)
      .thenReturn(status = OK, body = successAuthWithNrsResponse)
  }

  def unauthorisedUnsupportedAffinity(): StubMapping = {
    when(method = POST, uri = authoriseUri)
      .thenReturn(
        status = OK,
        body = Json.obj(
          "allEnrolments" -> mtdEnrolment,
          "affinityGroup" -> "Schmindividual"
        )
      )
  }

  def unauthorisedNotLoggedIn(): StubMapping = {
    when(method = POST, uri = authoriseUri)
      .thenReturn(status = UNAUTHORIZED, headers = Map("WWW-Authenticate" -> """MDTP detail="MissingBearerToken""""))
  }

  def unauthorisedOther(): StubMapping = {
    when(method = POST, uri = authoriseUri)
      .thenReturn(status = UNAUTHORIZED, headers = Map("WWW-Authenticate" -> """MDTP detail="InvalidBearerToken""""))
  }

  private def successfulAuthResponse(enrolments: JsObject): JsValue =
    Json.parse(
      s"""
        |{
        |  "agentInformation": {
        |        "agentCode" : "TZRXXV",
        |        "agentFriendlyName" : "Bodgitt & Legget LLP",
        |        "agentId": "BDGL"
        |    },
        |  "allEnrolments" : [${enrolments.toString}],
        |  "affinityGroup": "Individual"
        |}
        """.stripMargin
    )

  private val successAuthWithNrsResponse = Json.parse("""
                                                        |{
                                                        |  "internalId": "some-id",
                                                        |  "externalId": "some-id",
                                                        |  "credentials" : {"providerId":"8124873381064832", "providerType":"GovernmentGateway"},
                                                        |  "confidenceLevel": 200,
                                                        |  "name": { "name": "test", "lastName": "test" },
                                                        |  "dateOfBirth": "1985-01-01",
                                                        |  "postCode":"NW94HD",
                                                        |  "description" : "description",
                                                        |  "agentInformation": {
                                                        |        "agentCode" : "TZRXXV",
                                                        |        "agentFriendlyName" : "Bodgitt & Legget LLP",
                                                        |        "agentId": "BDGL"
                                                        |    },
                                                        |  "groupIdentifier" : "GroupId",
                                                        |  "credentialRole": "admin",
                                                        |  "itmpName" : { "givenName": "test", "middleName": "test", "familyName": "test" },
                                                        |  "itmpDateOfBirth" : "1985-01-01",
                                                        |  "itmpAddress" : {
                                                        |    "line1" : "Line 1",
                                                        |    "line2" : "",
                                                        |    "line3" : "",
                                                        |    "line4" : "",
                                                        |    "line5" : "",
                                                        |    "postCode" :"NW94HD",
                                                        |    "countryName" : "United Kingdom",
                                                        |    "countryCode" : "UK"
                                                        |    },
                                                        |  "affinityGroup": "Organisation",
                                                        |  "loginTimes": {
                                                        |     "currentLogin": "2016-11-27T09:00:00.000Z",
                                                        |     "previousLogin": "2016-11-01T12:00:00.000Z"
                                                        |  },
                                                        |  "allEnrolments": [
                                                        |   {
                                                        |         "key":"HMRC-MTD-VAT",
                                                        |         "identifiers":[
                                                        |            {
                                                        |               "key":"VRN",
                                                        |               "value":"1000051409"
                                                        |            }
                                                        |         ],
                                                        |         "state":"Activated"
                                                        |      }
                                                        |  ]
                                                        |}
                                                      """.stripMargin)

}
