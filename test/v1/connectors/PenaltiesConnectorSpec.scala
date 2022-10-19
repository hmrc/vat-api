/*
 * Copyright 2022 HM Revenue & Customs
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
import v1.constants.{FinancialDataConstants, PenaltiesConstants}
import v1.mocks.MockHttpClient
import v1.models.errors.{ErrorWrapper, InvalidJson, MtdError, UnexpectedFailure, VrnFormatError, VrnNotFound}

import scala.concurrent.Future

class PenaltiesConnectorSpec extends GuiceBox with ConnectorSpec with MockHttpClient {

  val testConnector: PenaltiesConnector = new PenaltiesConnector(mockHttpClient, appConfig)

  "PenaltiesConnector" when {

    "retrievePenalties" when {

      "a valid response is returned" must {

        "return Future(Right(PenaltiesResponse) min" in {

          MockedHttpClient.get(
            url = PenaltiesConstants.penaltiesURlWithConfig(),
            config = dummyHeaderCarrierConfig,
            queryParams = Seq()
          ).returns(
            Future.successful(Right(PenaltiesConstants.testPenaltiesResponseMin))
          )

          val result = testConnector.retrievePenalties(PenaltiesConstants.penaltiesRequest)
          val expectedResult = Right(PenaltiesConstants.testPenaltiesResponseMin)

          await(result) shouldBe expectedResult
        }

        "return Future(Right(PenaltiesResponse)) max" in {

          MockedHttpClient.get(
            url = PenaltiesConstants.penaltiesURlWithConfig(),
            config = dummyHeaderCarrierConfig,
            queryParams = Seq()
          ).returns(
            Future.successful(Right(PenaltiesConstants.testPenaltiesResponseMax))
          )

          val result = testConnector.retrievePenalties(PenaltiesConstants.penaltiesRequest)
          val expectedResult = Right(PenaltiesConstants.testPenaltiesResponseMax)

          await(result) shouldBe expectedResult
        }

      }
    }


      "a invalid json response is returned" must {

        "return Future(Left(InvalidJson))" in {

          MockedHttpClient.get(
            url = PenaltiesConstants.penaltiesURlWithConfig(),
            config = dummyHeaderCarrierConfig,
            queryParams = Seq()
          ).returns(
            Future.successful(Left(InvalidJson))
          )

          val result = testConnector.retrievePenalties(PenaltiesConstants.penaltiesRequest)
          val expectedResult = Left(InvalidJson)

          await(result) shouldBe expectedResult
        }
      }

      "a InvalidVrn response is returned" must {

        "return Future(Left(InvalidVrn))" in {

          MockedHttpClient.get(
            url = PenaltiesConstants.penaltiesURlWithConfig(),
            config = dummyHeaderCarrierConfig,
            queryParams = Seq()
          ).returns(
            Future.successful(Left(VrnFormatError))
          )

          val result = testConnector.retrievePenalties(PenaltiesConstants.penaltiesRequest)
          val expectedResult = Left(VrnFormatError)

          await(result) shouldBe expectedResult
        }
      }

      "a VrnNotFound response is returned" must {

        "return Future(Left(VrnNotFound)" in {

          MockedHttpClient.get(
            url = PenaltiesConstants.penaltiesURlWithConfig(),
            config = dummyHeaderCarrierConfig,
            queryParams = Seq()
          ).returns(
            Future.successful(Left(VrnNotFound))
          )

          val result = testConnector.retrievePenalties(PenaltiesConstants.penaltiesRequest)
          val expectedResult = Left(VrnNotFound)

          await(result) shouldBe expectedResult
        }
      }

      "a UnexpectedFailure response is returned" must {

        "return Future(Left(UnexpectedFailure)" in {

          MockedHttpClient.get(
            url = PenaltiesConstants.penaltiesURlWithConfig(),
            config = dummyHeaderCarrierConfig,
            queryParams = Seq()
          ).returns(
            Future.successful(Left(UnexpectedFailure.mtdError(Status.INTERNAL_SERVER_ERROR, "error")))
          )

          val result = testConnector.retrievePenalties(PenaltiesConstants.penaltiesRequest)
          val expectedResult = Left(UnexpectedFailure.mtdError(Status.INTERNAL_SERVER_ERROR, "error"))

          await(result) shouldBe expectedResult
        }
      }
    }


    "retrieveFinancialData" when {

      "a valid response is returned" must {

        "return Future(Right(FinancialDataResponse) min" in {

          MockedHttpClient.get(
            url = FinancialDataConstants.financialDataUrlWithConfig(),
            config = dummyHeaderCarrierConfig,
            queryParams = Seq()
          ).returns(
            Future.successful(Right(FinancialDataConstants.testFinancialDataResponse))
          )

          val result = testConnector.retrieveFinancialData(FinancialDataConstants.financialRequest)
          val expectedResult = Right(FinancialDataConstants.testFinancialDataResponse)

          await(result) shouldBe expectedResult
        }

        "return Future(Right(FinancialDataResponse) max" in {

          MockedHttpClient.get(
            url = FinancialDataConstants.financialDataUrlWithConfig(),
            config = dummyHeaderCarrierConfig,
            queryParams = Seq()
          ).returns(
            Future.successful(Right(FinancialDataConstants.testFinancialDataResponse))
          )

          val result = testConnector.retrieveFinancialData(FinancialDataConstants.financialRequest)
          val expectedResult = Right(FinancialDataConstants.testFinancialDataResponse)

          await(result) shouldBe expectedResult
        }
      }

    "a invalid json response is returned" must {

        "return Future(Left(InvalidJson)" in {

          MockedHttpClient.get(
            url = FinancialDataConstants.financialDataUrlWithConfig(),
            config = dummyHeaderCarrierConfig,
            queryParams = Seq()
          ).returns(
            Future.successful(Left(InvalidJson))
          )

          val result = testConnector.retrieveFinancialData(FinancialDataConstants.financialRequest)
          val expectedResult = Left(InvalidJson)

          await(result) shouldBe expectedResult
        }
      }

      "a InvalidVrn response is returned" must {

        "return Future(Left(InvalidVrn)" in {

          MockedHttpClient.get(
            url = FinancialDataConstants.financialDataUrlWithConfig(),
            config = dummyHeaderCarrierConfig,
            queryParams = Seq()
          ).returns(
            Future.successful(Left(VrnFormatError))
          )

          val result = testConnector.retrieveFinancialData(FinancialDataConstants.financialRequest)
          val expectedResult = Left(VrnFormatError)

          await(result) shouldBe expectedResult
        }
      }

      "a VrnNotFound response is returned" must {

        "return Future(Left(VrnNotFound)" in {

          MockedHttpClient.get(
            url = FinancialDataConstants.financialDataUrlWithConfig(),
            config = dummyHeaderCarrierConfig,
            queryParams = Seq()
          ).returns(
            Future.successful(Left(VrnNotFound))
          )

          val result = testConnector.retrieveFinancialData(FinancialDataConstants.financialRequest)
          val expectedResult = Left(VrnNotFound)

          await(result) shouldBe expectedResult
        }
      }

      "a UnexpectedFailure response is returned" must {

        "return Future(Left(UnexpectedFailure)" in {

          MockedHttpClient.get(
            url = FinancialDataConstants.financialDataUrlWithConfig(),
            config = dummyHeaderCarrierConfig,
            queryParams = Seq()
          ).returns(
            Future.successful(Left(UnexpectedFailure.mtdError(Status.INTERNAL_SERVER_ERROR, "error")))
          )

          val result = testConnector.retrieveFinancialData(FinancialDataConstants.financialRequest)
          val expectedResult = Left(UnexpectedFailure.mtdError(Status.INTERNAL_SERVER_ERROR, "error"))

          await(result) shouldBe expectedResult
        }

        "return return a downstream error when backend fails" in {

          MockedHttpClient.get(
            url = FinancialDataConstants.financialDataUrlWithConfig(),
            config = dummyHeaderCarrierConfig,
            queryParams = Seq()
          ).returns(Future.failed(new Exception("test exception")))

          val result = testConnector.retrieveFinancialData(FinancialDataConstants.financialRequest)
          val expectedResult = Left(ErrorWrapper(correlationId, MtdError("DOWNSTREAM_ERROR", "test exception")))

          await(result) shouldBe expectedResult
        }
      }
    }

}
