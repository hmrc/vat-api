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

package uk.gov.hmrc.vatapi.connectors

import org.scalatestplus.play.OneAppPerSuite
import play.api.libs.json.JsValue
import play.mvc.Http.{HeaderNames, MimeTypes}
import uk.gov.hmrc.domain.Vrn
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}
import uk.gov.hmrc.vatapi.UnitSpec
import uk.gov.hmrc.vatapi.config.WSHttp
import uk.gov.hmrc.vatapi.mocks.MockHttp
import uk.gov.hmrc.vatapi.mocks.config.MockAppContext
import uk.gov.hmrc.vatapi.models.ObligationsQueryParams
import uk.gov.hmrc.vatapi.resources.Jsons
import uk.gov.hmrc.vatapi.resources.wrappers.ObligationsResponse

import scala.concurrent.Future

class ObligationsConnectorSpec extends UnitSpec with OneAppPerSuite
  with MockHttp
  with MockAppContext {

  class Setup {
    val testObligationsConnector = new ObligationsConnector {
      override val http: WSHttp = mockHttp
      override val appContext = mockAppContext
    }
    MockAppContext.desUrl returns desBaseUrl
    MockAppContext.desToken returns desToken
    MockAppContext.desEnv returns desEnvironment
  }

  lazy val desToken = "test-token"
  lazy val desEnvironment = "test-env"
  lazy val desBaseUrl = "des-base-url"

  val vrn: Vrn = generateVrn
  val queryParams = ObligationsQueryParams(now.minusDays(7), now, "O")
  val queryString = s"from=${queryParams.from}&to=${queryParams.to}&status=${queryParams.status}"
  val desUrl = s"$desBaseUrl/enterprise/obligation-data/vrn/$vrn/VATC?$queryString"
  val desObligationsJson: JsValue = Jsons.Obligations.desResponse(vrn)

  val desHttpResponse = HttpResponse(200, Some(desObligationsJson))

  implicit val hc = HeaderCarrier()

  "get" should {
    "have the DES headers in the request" in new Setup {
      MockHttp.GET[HttpResponse](desUrl)
        .returns(Future.successful(desHttpResponse))

      await(testObligationsConnector.get(vrn, queryParams))

      val headers = MockHttp.fetchHeaderCarrier.headers.toMap
      headers("Accept") shouldBe MimeTypes.JSON
      headers("Environment") shouldBe desEnvironment
      headers("Authorization") shouldBe s"Bearer $desToken"
      headers("Originator-Id") shouldBe "DA_SDI"
    }

    "return an ObligationsResponse" when {
      "DES returns a 200 response" in new Setup {
        MockHttp.GET[HttpResponse](desUrl)
          .returns(Future.successful(desHttpResponse))

        val response = await(testObligationsConnector.get(vrn, queryParams))
        response shouldBe ObligationsResponse(desHttpResponse)
      }
    }
  }
}
