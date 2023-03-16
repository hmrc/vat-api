package config

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

import config.FeatureSwitch.FinancialDataRamlFeature
import play.api.http.Status
import play.api.libs.json.{JsValue, Json}
import play.api.libs.ws.WSResponse
import support.IntegrationBaseSpec

class DocumentationControllerISpec extends IntegrationBaseSpec with FeatureToggleSupport {


  val apiDefinitionJson: JsValue = Json.parse(
    """
      |{
      |  "scopes":[
      |    {
      |      "key":"read:vat",
      |      "name":"View your VAT information",
      |      "description":"Allow read access to VAT data"
      |    },
      |    {
      |      "key":"write:vat",
      |      "name":"Change your VAT information",
      |      "description":"Allow write access to VAT data"
      |    }
      |  ],
      |  "api":{
      |    "name":"VAT (MTD)",
      |    "description":"An API for providing VAT data",
      |    "context":"organisations/vat",
      |    "categories":["VAT_MTD"],
      |    "versions":[
      |      {
      |        "version":"1.0",
      |        "status":"BETA",
      |        "endpointsEnabled":true
      |      }
      |    ]
      |  }
      |}
    """.stripMargin)

  "GET /api/definition" should {
    "return a 200 with the correct response body" in {
      val response: WSResponse = await(buildRequest("/api/definition").get())
      response.status shouldBe Status.OK
      Json.parse(response.body) shouldBe apiDefinitionJson
    }
  }

}
