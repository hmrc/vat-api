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
import v1.models.request.liability.LiabilityRequest
import v1.models.response.common.TaxPeriod
import v1.models.response.liability.{Liability, LiabilityResponse}

import scala.concurrent.Future

class RetrieveLiabilitiesConnectorSpec extends ConnectorSpec {

  private val vrn: String = "123456789"

  private val retrieveLiabilitiesRequest: LiabilityRequest =
    LiabilityRequest(
      Vrn(vrn),"2017-1-1","2017-12-31"
    )

  private val retrieveLiabilitiesResponse: LiabilityResponse =
    LiabilityResponse(Seq(Liability(None,"VAT",1.0,None,None)))

  private val retrieveMultipleLiabilitiesResponse: LiabilityResponse =
    LiabilityResponse(Seq(Liability(None,"VAT",1.0,None,None),Liability(Some(TaxPeriod("2017-1-1","2017-12-31")),"VAT",2.0,Some(1.0),None)))

  class Test extends MockHttpClient with MockAppConfig {

    val connector: RetrieveLiabilitiesConnector =
      new RetrieveLiabilitiesConnector(
        http = mockHttpClient,
        appConfig = mockAppConfig
      )

    val desRequestHeaders: Seq[(String, String)] =
      Seq(
        "Environment" -> "des-environment",
        "Authorization" -> s"Bearer des-token"
      )

    MockedAppConfig.desBaseUrl returns baseUrl
    MockedAppConfig.desToken returns "des-token"
    MockedAppConfig.desEnvironment returns "des-environment"
  }

  "RetrieveLiabilitiesConnector" when {
    "retrieving liabilities" must {
      "return a valid response" in new Test {
        val outcome = Right(ResponseWrapper(correlationId, retrieveLiabilitiesResponse))

        MockedHttpClient
          .get(
            url = s"$baseUrl/enterprise/financial-data/VRN/$vrn/VATC?dateFrom=${retrieveLiabilitiesRequest.from}&dateTo=${retrieveLiabilitiesRequest.to}&onlyOpenItems=false&includeLocks=false&calculateAccruedInterest=true&customerPaymentInformation=true",
            requiredHeaders = "Environment" -> "des-environment", "Authorization" -> s"Bearer des-token"
          )
          .returns(Future.successful(outcome))

        await(connector.retrieveLiabilities(retrieveLiabilitiesRequest)) shouldBe outcome
      }

      "return a valid response for multiple results" in new Test {
        val outcome = Right(ResponseWrapper(correlationId, retrieveMultipleLiabilitiesResponse))

        MockedHttpClient
          .get(
            url = s"$baseUrl/enterprise/financial-data/VRN/$vrn/VATC?dateFrom=${retrieveLiabilitiesRequest.from}&dateTo=${retrieveLiabilitiesRequest.to}&onlyOpenItems=false&includeLocks=false&calculateAccruedInterest=true&customerPaymentInformation=true",
            requiredHeaders = "Environment" -> "des-environment", "Authorization" -> s"Bearer des-token"
          )
          .returns(Future.successful(outcome))

        await(connector.retrieveLiabilities(retrieveLiabilitiesRequest)) shouldBe outcome
      }
    }
  }
}
