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

import play.api.http.Status
import support.GuiceBox
import uk.gov.hmrc.http.HeaderCarrier
import v1.constants
import v1.constants.FinancialDataHIPConstants
import v1.mocks.MockHttpClient
import v1.models.errors._
import v1.models.request.penalties.FinancialRequestHIP

import scala.concurrent.Future

class FinancialDataHIPConnectorSpec extends GuiceBox with ConnectorSpec with MockHttpClient {

  val testConnector: FinancialDataHIPConnector = new FinancialDataHIPConnector(mockHttpClient, appConfig)

  val request: FinancialRequestHIP = FinancialDataHIPConstants.testFinancialRequestHIP

    "retrieveFinancialDataHIP" when {

      "a valid response is returned" must {

        "return Future(Right(FinancialDataHIPResponse) min" in {

          MockedHttpClient.post(
            url = FinancialDataHIPConstants.financialDataHIPUrl,
            config = dummyHeaderCarrierConfig,
            body = request,
          ).returns(
            Future.successful(Right(FinancialDataHIPConstants.testFinancialDataResponse))
          )

          val result = testConnector.retrieveFinancialDataHIP(FinancialDataHIPConstants.testFinancialRequestHIP)
          val expectedResult = Right(FinancialDataHIPConstants.testFinancialDataResponse)

          await(result) shouldBe expectedResult
        }

        "return Future(Right(FinancialDataHIPResponse) max" in {

          MockedHttpClient.post(
            url = FinancialDataHIPConstants.financialDataHIPUrl,
            config = dummyHeaderCarrierConfig,
            body = request,
          ).returns(
            Future.successful(Right(FinancialDataHIPConstants.testFinancialDataResponse))
          )

          val result = testConnector.retrieveFinancialDataHIP(FinancialDataHIPConstants.testFinancialRequestHIP)
          val expectedResult = Right(FinancialDataHIPConstants.testFinancialDataResponse)

          await(result) shouldBe expectedResult
        }
      }

    "a invalid json response is returned" must {

        "return Future(Left(InvalidJson)" in {

          MockedHttpClient.post(
            url = FinancialDataHIPConstants.financialDataHIPUrl,
            config = dummyHeaderCarrierConfig,
            body = request,
          ).returns(
            Future.successful(Left(InvalidJson))
          )

          val result = testConnector.retrieveFinancialDataHIP(FinancialDataHIPConstants.testFinancialRequestHIP)
          val expectedResult = Left(InvalidJson)

          await(result) shouldBe expectedResult
        }
      }

      "a UnexpectedFailure response is returned" must {

        "return Future(Left(UnexpectedFailure)" in {

          MockedHttpClient.post(
            url = FinancialDataHIPConstants.financialDataHIPUrl,
            config = dummyHeaderCarrierConfig,
            body = request,
          ).returns(
            Future.successful(Left(UnexpectedFailure.mtdError(Status.INTERNAL_SERVER_ERROR, "error")))
          )

          val result = testConnector.retrieveFinancialDataHIP(constants.FinancialDataHIPConstants.testFinancialRequestHIP)
          val expectedResult = Left(UnexpectedFailure.mtdError(Status.INTERNAL_SERVER_ERROR, "error"))

          await(result) shouldBe expectedResult
        }

        "return return a downstream error when backend fails" in {

          MockedHttpClient.post(
            url = FinancialDataHIPConstants.financialDataHIPUrl,
            config = dummyHeaderCarrierConfig,
            body = request,
          ).returns(Future.failed(new Exception("test exception")))

          val result = testConnector.retrieveFinancialDataHIP(FinancialDataHIPConstants.testFinancialRequestHIP)
          val expectedResult = Left(ErrorWrapper(correlationId, MtdError("DOWNSTREAM_ERROR", "test exception")))

          await(result) shouldBe expectedResult
        }
      }
    }

}
