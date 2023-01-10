/*
 * Copyright 2023 HM Revenue & Customs
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
import v1.models.domain.Vrn
import v1.mocks.MockHttpClient
import v1.models.errors.{DesErrorCode, DesErrors}
import v1.models.outcomes.ResponseWrapper
import v1.models.request.liabilities.LiabilitiesRequest
import v1.models.response.common.TaxPeriod
import v1.models.response.liabilities.{LiabilitiesResponse, Liability}

import scala.concurrent.Future

class LiabilitiesConnectorSpec extends ConnectorSpec {

  private val vrn: String = "123456789"

  private val retrieveLiabilitiesRequest: LiabilitiesRequest =
    LiabilitiesRequest(vrn = Vrn(vrn),from = "2017-1-1",to = "2017-12-31")

  private val retrieveLiabilitiesResponse: LiabilitiesResponse =
    LiabilitiesResponse(
      liabilities = Seq(
        Liability(
          taxPeriod = None,
          `type` = "VAT",
          originalAmount = 1.0,
          outstandingAmount = None,
          due = None
        )
      )
    )

  private val retrieveMultipleLiabilitiesResponse: LiabilitiesResponse =
    LiabilitiesResponse(
      liabilities = Seq(
        Liability(
          taxPeriod = None,
          `type` = "VAT",
          originalAmount = 1.0,
          outstandingAmount = None,
          due = None
        ),
        Liability(
          taxPeriod = Some(TaxPeriod(from = "2017-1-1", to = "2017-12-31")),
          `type` = "VAT",
          originalAmount = 2.0,
          outstandingAmount = Some(1.0),
          due = None
        )
      )
    )

  class Test extends MockHttpClient with MockAppConfig {

    val connector: LiabilitiesConnector =
      new LiabilitiesConnector(
        http = mockHttpClient,
        appConfig = mockAppConfig
      )

    val queryParams: Seq[(String, String)] = Seq(
      ("dateFrom" , retrieveLiabilitiesRequest.from),
      ("dateTo" , retrieveLiabilitiesRequest.to),
      ("onlyOpenItems" , "false"),
      ("includeLocks" , "false"),
      ("calculateAccruedInterest" , "true"),
      ("customerPaymentInformation" , "true")
    )

    MockedAppConfig.desBaseUrl returns baseUrl
    MockedAppConfig.desToken returns "des-token"
    MockedAppConfig.desEnvironment returns "des-environment"
    MockedAppConfig.desEnvironmentHeaders returns Some(allowedDesHeaders)
  }

  "RetrieveLiabilitiesConnector" when {
    "retrieving liabilities" must {
      "return a valid response" in new Test {
        val outcome = Right(ResponseWrapper(correlationId, retrieveLiabilitiesResponse))

        MockedHttpClient
          .get(
            url = s"$baseUrl/enterprise/financial-data/VRN/$vrn/VATC",
            queryParams = queryParams,
            config = dummyDesHeaderCarrierConfig,
            requiredHeaders = requiredDesHeaders,
            excludedHeaders = Seq("AnotherHeader" -> "HeaderValue")
          ).returns(Future.successful(outcome))

        await(connector.retrieveLiabilities(retrieveLiabilitiesRequest)) shouldBe outcome
      }

      "return a valid response for multiple results" in new Test {
        val outcome = Right(ResponseWrapper(correlationId, retrieveMultipleLiabilitiesResponse))

        MockedHttpClient
          .get(
            url = s"$baseUrl/enterprise/financial-data/VRN/$vrn/VATC",
            queryParams = queryParams,
            config = dummyDesHeaderCarrierConfig,
            requiredHeaders = requiredDesHeaders,
            excludedHeaders = Seq("AnotherHeader" -> "HeaderValue")
          ).returns(Future.successful(outcome))

        await(connector.retrieveLiabilities(retrieveLiabilitiesRequest)) shouldBe outcome
      }

      "return a downstream error when the backend fails" in new Test {
        val outcome = Left(ResponseWrapper(correlationId, DesErrors(List(DesErrorCode("DOWNSTREAM_ERROR")))))

        MockedHttpClient
          .get(
            url = s"$baseUrl/enterprise/financial-data/VRN/$vrn/VATC",
            queryParams = queryParams,
            config = dummyDesHeaderCarrierConfig,
            requiredHeaders = requiredDesHeaders,
            excludedHeaders = Seq("AnotherHeader" -> "HeaderValue")
          ).returns(Future.failed(new Exception("test exception")))

        await(connector.retrieveLiabilities(retrieveLiabilitiesRequest)) shouldBe outcome
      }
    }
  }
}