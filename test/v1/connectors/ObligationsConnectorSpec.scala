/*
 * Copyright 2026 HM Revenue & Customs
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

package v1.connectors

import mocks.MockAppConfig
import v1.mocks.MockHttpClient
import v1.models.domain.Vrn
import v1.models.outcomes.ResponseWrapper
import v1.models.request.obligations.ObligationsRequest
import v1.models.response.obligations.{ Obligation, ObligationsResponse }

import scala.concurrent.Future

class ObligationsConnectorSpec extends ConnectorSpec {

  val vrn: Vrn = Vrn("123456789")

  val obligationsResponse: ObligationsResponse =
    ObligationsResponse(
      Seq(
        Obligation(
          periodKey = "18A2",
          start = "2017-04-01",
          end = "2017-06-30",
          due = "2017-08-07",
          status = "O",
          received = None
        )
      ))

  private val outcome = Right(ResponseWrapper(correlationId, obligationsResponse))

  class Test extends MockHttpClient with MockAppConfig {

    val connector: ObligationsConnector = new ObligationsConnector(http = mockHttpClient, appConfig = mockAppConfig)

    MockedAppConfig.desBaseUrl returns baseUrl
    MockedAppConfig.desToken returns "des-token"
    MockedAppConfig.desEnvironment returns "des-environment"
    MockedAppConfig.desEnvironmentHeaders returns Some(allowedDesHeaders)
  }

  "ObligationsConnector" when {
    "retrieving obligations" must {
      "return a success response from the HttpClient" when {
        "query parameters are provided" in new Test {
          val from                               = "2017-04-06"
          val to                                 = "2018-04-05"
          val status                             = "O"
          val queryParams: Seq[(String, String)] = Seq("from" -> from, "to" -> to, "status" -> status)

          val request: ObligationsRequest = ObligationsRequest(vrn, Some(from), Some(to), Some(status))

          MockedHttpClient
            .get(
              url = s"$baseUrl/enterprise/obligation-data/vrn/$vrn/VATC",
              queryParams = queryParams,
              config = dummyDesHeaderCarrierConfig,
              requiredHeaders = requiredDesHeaders,
              excludedHeaders = Seq("AnotherHeader" -> "HeaderValue")
            )
            .returns(Future.successful(outcome))

          await(connector.retrieveObligations(request)) shouldBe outcome
        }

        "no query parameters are provided" in new Test {
          val queryParams: Seq[(String, String)] = Seq()

          val request: ObligationsRequest = ObligationsRequest(vrn, from = None, to = None, status = None)

          MockedHttpClient
            .get(
              url = s"$baseUrl/enterprise/obligation-data/vrn/$vrn/VATC",
              queryParams = queryParams,
              config = dummyDesHeaderCarrierConfig,
              requiredHeaders = requiredDesHeaders,
              excludedHeaders = Seq("AnotherHeader" -> "HeaderValue")
            )
            .returns(Future.successful(outcome))

          await(connector.retrieveObligations(request)) shouldBe outcome
        }
      }

      "return a failure response from the HttpClient" in new Test {
        val request: ObligationsRequest = ObligationsRequest(vrn, from = None, to = None, status = None)

        MockedHttpClient
          .get(
            url = s"$baseUrl/enterprise/obligation-data/vrn/$vrn/VATC",
            queryParams = Seq(),
            config = dummyDesHeaderCarrierConfig,
            requiredHeaders = requiredDesHeaders,
            excludedHeaders = Seq("AnotherHeader" -> "HeaderValue")
          )
          .returns(Future.failed(new Exception("Test Exception")))

        private val catchResult = intercept[Exception] {
          await(connector.retrieveObligations(request))
        }

        catchResult.getMessage shouldBe "Test Exception"
      }
    }
  }

}
