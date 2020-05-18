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
import org.joda.time.DateTime
import uk.gov.hmrc.domain.Vrn
import v1.mocks.MockHttpClient
import v1.models.outcomes.ResponseWrapper
import v1.models.request.submit.{SubmitRequest, SubmitRequestBody}
import v1.models.response.submit.SubmitResponse

import scala.concurrent.Future

class SubmitReturnConnectorSpec extends ConnectorSpec {

  private val vrn: String = "123456789"

  private val submitReturnRequest: SubmitRequest =
    SubmitRequest(
      vrn = Vrn(vrn),
      body = SubmitRequestBody(
        periodKey = Some("F034"),
        vatDueSales = Some(4567.23),
        vatDueAcquisitions = Some(-456675.5),
        totalVatDue = Some(7756.65),
        vatReclaimedCurrPeriod = Some(-756822354.64),
        netVatDue = Some(8956743245.12),
        totalValueSalesExVAT = Some(43556767890.00),
        totalValuePurchasesExVAT = Some(34556790.00),
        totalValueGoodsSuppliedExVAT = Some(34556.00),
        totalAcquisitionsExVAT = Some(-68978.00),
        finalised = Some(true),
        receivedAt = None,
        agentReference = None
      )
    )

  val submitReturnResponse: SubmitResponse = SubmitResponse(processingDate = new DateTime("2018-01-16T08:20:27.895+0000"),
    paymentIndicator = Some("BANK"),
    formBundleNumber = "256660290587",
    chargeRefNumber = Some("aCxFaNx0FZsCvyWF"))

  class Test extends MockHttpClient with MockAppConfig {

    val connector: SubmitReturnConnector =
      new SubmitReturnConnector(
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

  "submitReturnConnector" should {
      "return a valid response" when {
        "a valid VAT return is submitted" in new Test {
        val outcome = Right(ResponseWrapper(correlationId, submitReturnResponse))

        MockedHttpClient
          .post(
            url = s"$baseUrl/enterprise/return/vat/$vrn",
            body = submitReturnRequest.body,
            requiredHeaders = "Environment" -> "des-environment",
                              "Authorization" -> s"Bearer des-token",
                              "Accept" -> "application/json",
                              "OriginatorID" -> "MDTP"
          )
          .returns(Future.successful(outcome))

        await(connector.submitReturn(submitReturnRequest)) shouldBe outcome
      }
    }
  }
}
