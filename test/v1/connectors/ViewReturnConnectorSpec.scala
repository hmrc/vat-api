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

package v1.connectors

import mocks.MockAppConfig
import v1.models.domain.Vrn
import v1.mocks.MockHttpClient
import v1.models.outcomes.ResponseWrapper
import v1.models.request.viewReturn.ViewRequest
import v1.models.response.viewReturn.ViewReturnResponse

import scala.concurrent.Future

class ViewReturnConnectorSpec extends ConnectorSpec {

  private val vrn: String = "123456789"
  private val periodKey: String = "F034"

  private val viewReturnRequest: ViewRequest =
    ViewRequest(
      vrn = Vrn(vrn),
      periodKey = periodKey
    )

  private val viewReturnResponse: ViewReturnResponse =
    ViewReturnResponse(
      periodKey = "F034",
      vatDueSales = 4567.23,
      vatDueAcquisitions = -456675.5,
      totalVatDue = 7756.65,
      vatReclaimedCurrPeriod = -756822354.64,
      netVatDue = 8956743245.12,
      totalValueSalesExVAT = 43556767890.00,
      totalValuePurchasesExVAT = 34556790.00,
      totalValueGoodsSuppliedExVAT = 34556.00,
      totalAcquisitionsExVAT = -68978.00
    )

  class Test extends MockHttpClient with MockAppConfig {

    val connector: ViewReturnConnector =
      new ViewReturnConnector(
        http = mockHttpClient,
        appConfig = mockAppConfig
      )

    MockedAppConfig.desBaseUrl returns baseUrl
    MockedAppConfig.desToken returns "des-token"
    MockedAppConfig.desEnvironment returns "des-environment"
    MockedAppConfig.desEnvironmentHeaders returns Some(allowedDesHeaders)
  }

  "ViewReturnConnector" when {
    "viewing a VAT return" must {
      "return a valid response" in new Test {
        val outcome = Right(ResponseWrapper(correlationId, viewReturnResponse))

        val queryParams: Seq[(String, String)] =
          Seq(
            "period-key" -> periodKey
          )

        MockedHttpClient
          .parameterGet(
            url = s"$baseUrl/vat/returns/vrn/$vrn",
            queryParams = queryParams,
            config = dummyDesHeaderCarrierConfig,
            requiredHeaders = requiredDesHeaders,
            excludedHeaders = Seq("AnotherHeader" -> "HeaderValue")
          ).returns(Future.successful(outcome))

        await(connector.viewReturn(viewReturnRequest)) shouldBe outcome
      }
    }
  }
}