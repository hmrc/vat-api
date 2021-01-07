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
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import uk.gov.hmrc.domain.Vrn
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.http.logging.RequestId
import v1.controllers.UserRequest
import v1.mocks.MockHttpClient
import v1.models.auth.UserDetails
import v1.models.outcomes.ResponseWrapper
import v1.models.request.viewReturn.ViewRequest
import v1.models.response.viewReturn.ViewReturnResponse

import scala.concurrent.Future

class ViewReturnConnectorSpec extends ConnectorSpec {

  implicit val userRequest: UserRequest[AnyContentAsEmpty.type] = UserRequest(UserDetails("Individual",None,"id"),FakeRequest())
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

    val desRequestHeaders: Seq[(String, String)] =
      Seq(
        "Environment" -> "des-environment",
        "Authorization" -> s"Bearer des-token"
      )

    MockedAppConfig.desBaseUrl returns baseUrl
    MockedAppConfig.desToken returns "des-token"
    MockedAppConfig.desEnvironment returns "des-environment"
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
          .get(
            url = s"$baseUrl/vat/returns/vrn/$vrn",
            queryParams = queryParams,
            requiredHeaders = "Environment" -> "des-environment", "Authorization" -> s"Bearer des-token"
          )
          .returns(Future.successful(outcome))

        await(connector.viewReturn(viewReturnRequest)
        (hc = HeaderCarrier(requestId = Some(RequestId("123"))), ec = ec, userRequest = userRequest, correlationId = correlationId)) shouldBe outcome
      }
    }
  }
}
