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
}
