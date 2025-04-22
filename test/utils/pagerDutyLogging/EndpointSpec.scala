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

package utils.pagerDutyLogging

import support.UnitSpec
import utils.pagerDutyLogging.Endpoint._

import scala.concurrent.ExecutionContext.Implicits.global

class EndpointSpec extends UnitSpec {

  case class ExpectedEndpointMessages(endPoint: Endpoint,
                                      expectedToLoggerMessage: String,
                                      expectedRequestFailedMessage: String
                                     )

  val expectedEndpointMessages: Seq[ExpectedEndpointMessages] = Seq(
    ExpectedEndpointMessages(RetrieveObligations, "RETRIEVE_OBLIGATIONS_500", "RETRIEVE_OBLIGATIONS_REQUEST_FAILED"),
    ExpectedEndpointMessages(SubmitReturn, "SUBMIT_RETURN_500", "SUBMIT_RETURNS_REQUEST_FAILED"),
    ExpectedEndpointMessages(RetrieveReturns, "RETRIEVE_RETURN_500", "RETRIEVE_RETURN_REQUEST_FAILED"),
    ExpectedEndpointMessages(RetrieveLiabilities, "RETRIEVE_LIABILITIES_500", "RETRIEVE_LIABILITIES_REQUEST_FAILED"),
    ExpectedEndpointMessages(RetrievePayments, "RETRIEVE_PAYMENTS_500", "RETRIEVE_PAYMENTS_REQUEST_FAILED"),
    ExpectedEndpointMessages(RetrievePenalties, "RETRIEVE_PENALTIES_500", "RETRIEVE_PENALTIES_REQUEST_FAILED"),
    ExpectedEndpointMessages(RetrieveFinancialData, "RETRIEVE_FINANCIAL_DATA_500", "RETRIEVE_FINANCIAL_DATA_REQUEST_FAILED"),
    ExpectedEndpointMessages(RetrieveCustomerInfo, "RETRIEVE_CUSTOMER_INFO_500", "RETRIEVE_CUSTOMER_INFO_REQUEST_FAILED")
  )

  endpointMessagesTests(expectedEndpointMessages)

  def endpointMessagesTests(expectedmessages: Seq[ExpectedEndpointMessages]): Unit = {

    expectedmessages.foreach { endpoint =>

      s"${endpoint.endPoint.toString}" must {

        "return the correct toLoggerMessage" in {

          endpoint.endPoint.toLoggerMessage.toString shouldBe endpoint.expectedToLoggerMessage
        }

        "return the correct requestFailedMessage" in {

          endpoint.endPoint.requestFailedMessage.toString shouldBe endpoint.expectedRequestFailedMessage
        }
      }
    }
  }

}
