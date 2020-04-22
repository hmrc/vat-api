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

package v1.connectors

import mocks.MockAppConfig
import uk.gov.hmrc.domain.Vrn
import v1.mocks.MockHttpClient
import v1.models.outcomes.ResponseWrapper
import v1.models.request.obligations.ObligationsRequest
import v1.models.response.obligations.{Obligation, ObligationsResponse}

import scala.concurrent.Future

class ObligationsConnectorSpec extends ConnectorSpec {

  val vrn = Vrn("123456789")

  val obligationsResponse: ObligationsResponse =
    ObligationsResponse(Seq(
      Obligation(
        periodKey = "18A2",
        start = "2017-04-01",
        end = "2017-06-30",
        due = "2017-08-07",
        status = "O",
        received = None
      )
    ))

  val outcome = Right(ResponseWrapper(correlationId, obligationsResponse))

  class Test extends MockHttpClient with MockAppConfig {

    val connector: ObligationsConnector = new ObligationsConnector(http = mockHttpClient, appConfig = mockAppConfig)
    val desRequestHeaders: Seq[(String, String)] = Seq("Environment" -> "des-environment", "Authorization" -> s"Bearer des-token")

    MockedAppConfig.desBaseUrl returns baseUrl
    MockedAppConfig.desToken returns "des-token"
    MockedAppConfig.desEnvironment returns "des-environment"
  }

  "ObligationsConnector" when {

    "retrieving obligations" must {

      "return a valid response" in new Test {

        val from = "2017-04-06"
        val to = "2018-04-05"

        val status = "O"

        val queryParams: Seq[(String, String)] =
          Seq(
            "from" -> from,
            "to" -> to,
            "status" -> "O"
          )

        val request: ObligationsRequest = ObligationsRequest(vrn, Some(from), Some(to), Some(status))

        MockedHttpClient
          .get(
            url = s"$baseUrl/enterprise/obligation-data/vrn/$vrn/VATC",
            queryParams = queryParams,
            requiredHeaders = "Environment" -> "des-environment", "Authorization" -> s"Bearer des-token"
          )
          .returns(Future.successful(outcome))

        await(connector.retrieveObligations(request)) shouldBe outcome
      }

      "not add query parameters if not supplied" in new Test {

        val queryParams: Seq[(String, String)] = Seq()

        val request: ObligationsRequest = ObligationsRequest(vrn, from = None, to = None, status = None)

        MockedHttpClient
          .get(
            url = s"$baseUrl/enterprise/obligation-data/vrn/$vrn/VATC",
            queryParams = queryParams,
            requiredHeaders = "Environment" -> "des-environment", "Authorization" -> s"Bearer des-token"
          )
          .returns(Future.successful(outcome))

        await(connector.retrieveObligations(request)) shouldBe outcome
      }

      "only add query parameters supplied" in new Test {

        val queryParams: Seq[(String, String)] = Seq("status" -> "O")

        val request: ObligationsRequest = ObligationsRequest(vrn, from = None, to = None, status = Some("O"))

        MockedHttpClient
          .get(
            url = s"$baseUrl/enterprise/obligation-data/vrn/$vrn/VATC",
            queryParams = queryParams,
            requiredHeaders = "Environment" -> "des-environment", "Authorization" -> s"Bearer des-token"
          )
          .returns(Future.successful(outcome))

        await(connector.retrieveObligations(request)) shouldBe outcome
      }
    }
  }
}
