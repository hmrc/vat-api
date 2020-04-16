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
import v1.models.request.payments.PaymentsRequest
import v1.models.response.common.TaxPeriod
import v1.models.response.payments.PaymentsResponse.Payment
import v1.models.response.payments.{PaymentItem, PaymentsResponse}

import scala.concurrent.Future

class PaymentsConnectorSpec extends ConnectorSpec {

  private val vrn: String = "123456789"

  private val retrievePaymentsRequest: PaymentsRequest =
    PaymentsRequest(vrn = Vrn(vrn), from = "2017-1-1", to = "2017-12-31")

  private val retrievePaymentsResponse: PaymentsResponse =
    PaymentsResponse(
      payments = Seq(
        Payment(
          taxPeriod = Some(TaxPeriod(from = "2017-1-1", to = "2017-12-31")),
          `type` = "VAT Return Debit Charge",
          paymentItems = Some(Seq(
            PaymentItem(amount = Some(200.00), received = Some("2017-03-12"))
          ))
        )
      )
    )

  private val retrieveMultipleLiabilitiesResponse: PaymentsResponse =
    PaymentsResponse(
      payments = Seq(
        Payment(
          taxPeriod = Some(TaxPeriod(from = "2017-1-1", to = "2017-12-31")),
          `type` = "VAT Return Debit Charge",
          paymentItems = Some(Seq(
            PaymentItem(amount = Some(200.00), received = Some("2017-03-12"))
          ))
        ),
        Payment(
          taxPeriod = Some(TaxPeriod(from = "2018-1-1", to = "2018-12-31")),
          `type` = "VAT Return Debit Charge",
          paymentItems = Some(Seq(
            PaymentItem(amount = Some(2375.23), received = Some("2018-03-12"))
          ))
        )
      )
    )

  class Test extends MockHttpClient with MockAppConfig {

    val connector: PaymentsConnector =
      new PaymentsConnector(
        http = mockHttpClient,
        appConfig = mockAppConfig
      )

    val desRequestHeaders: Seq[(String, String)] =
      Seq(
        "Environment" -> "des-environment",
        "Authorization" -> s"Bearer des-token"
      )

    val queryParams: Seq[(String, String)] = Seq(
      ("dateFrom" , retrievePaymentsRequest.from),
      ("dateTo" , retrievePaymentsRequest.to),
      ("onlyOpenItems" , "false"),
      ("includeLocks" , "false"),
      ("calculateAccruedInterest" , "true"),
      ("customerPaymentInformation" , "true")
    )

    MockedAppConfig.desBaseUrl returns baseUrl
    MockedAppConfig.desToken returns "des-token"
    MockedAppConfig.desEnvironment returns "des-environment"
  }

  "RetrievePaymentsConnector" when {
    "retrieving payments" must {
      "return a valid response" in new Test {
        val outcome = Right(ResponseWrapper(correlationId, retrievePaymentsResponse))

        MockedHttpClient
          .get(
            url = s"$baseUrl/enterprise/financial-data/VRN/$vrn/VATC",
            queryParams = queryParams,
            requiredHeaders = "Environment" -> "des-environment", "Authorization" -> s"Bearer des-token",
          )
          .returns(Future.successful(outcome))

        await(connector.retrievePayments(retrievePaymentsRequest)) shouldBe outcome
      }

      "return a valid response for multiple results" in new Test {
        val outcome = Right(ResponseWrapper(correlationId, retrieveMultipleLiabilitiesResponse))

        MockedHttpClient
          .get(
            url = s"$baseUrl/enterprise/financial-data/VRN/$vrn/VATC",
            queryParams = queryParams,
            requiredHeaders = "Environment" -> "des-environment", "Authorization" -> s"Bearer des-token"
          )
          .returns(Future.successful(outcome))

        await(connector.retrievePayments(retrievePaymentsRequest)) shouldBe outcome
      }
    }
  }
}
