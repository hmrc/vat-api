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

package v1.services

import play.api.http.Status
import support.GuiceBox
import uk.gov.hmrc.http.HeaderCarrier
import v1.models.errors.{InvalidJson, UnexpectedFailure, VrnFormatError, VrnNotFound}
import v1.constants.{FinancialDataConstants, PenaltiesConstants}
import v1.constants.PenaltiesConstants.{correlationId, userRequest}
import v1.mocks.connectors.MockPenaltiesConnector

import scala.concurrent.ExecutionContext

class PenaltiesServiceSpec extends GuiceBox with MockPenaltiesConnector {

  object TestService extends PenaltiesService(mockPenaltiesConnector)
  implicit val hc: HeaderCarrier = HeaderCarrier()
  implicit val ec: ExecutionContext = app.injector.instanceOf[ExecutionContext]

  "PenaltiesService" when {

    "retrievePenalties" when {

      "a valid response is returned" must {

        "return Future(Right(PenaltiesResponse)" in {

          mockRetrievePenalties(
            PenaltiesConstants.penaltiesRequest,
            Right(PenaltiesConstants.wrappedPenaltiesResponse())
          )

          val result = TestService.retrievePenalties(PenaltiesConstants.penaltiesRequest)
          val expectedResult = Right(PenaltiesConstants.wrappedPenaltiesResponse())

          await(result) shouldBe expectedResult
        }
      }

      "a invalid json response is returned" must {

        "return Future(Left(PenaltiesConstants.errorWrapper(InvalidJson))" in {

          mockRetrievePenalties(
            PenaltiesConstants.penaltiesRequest,
            Left(PenaltiesConstants.errorWrapper(InvalidJson))
          )

          val result = TestService.retrievePenalties(PenaltiesConstants.penaltiesRequest)
          val expectedResult = Left(PenaltiesConstants.errorWrapper(InvalidJson))

          await(result) shouldBe expectedResult
        }
      }

      "a VrnFormatError response is returned" must {

        "return Future(Left(VrnFormatError)" in {

          mockRetrievePenalties(
            PenaltiesConstants.penaltiesRequest,
            Left(PenaltiesConstants.errorWrapper(VrnFormatError))
          )

          val result = TestService.retrievePenalties(PenaltiesConstants.penaltiesRequest)
          val expectedResult = Left(PenaltiesConstants.errorWrapper(VrnFormatError))

          await(result) shouldBe expectedResult
        }
      }

      "a VrnNotFound response is returned" must {

        "return Future(Left(VrnNotFound)" in {

          mockRetrievePenalties(
            PenaltiesConstants.penaltiesRequest,
            Left(PenaltiesConstants.errorWrapper(VrnNotFound))
          )

          val result = TestService.retrievePenalties(PenaltiesConstants.penaltiesRequest)
          val expectedResult = Left(PenaltiesConstants.errorWrapper(VrnNotFound))

          await(result) shouldBe expectedResult
        }
      }

      "a UnexpectedFailure response is returned" must {

        "return Future(Left(UnexpectedFailure)" in {

          mockRetrievePenalties(
            PenaltiesConstants.penaltiesRequest,
            Left(PenaltiesConstants.errorWrapper(UnexpectedFailure.mtdError(Status.INTERNAL_SERVER_ERROR, "error")))
          )

          val result = TestService.retrievePenalties(PenaltiesConstants.penaltiesRequest)
          val expectedResult = Left(PenaltiesConstants.errorWrapper(UnexpectedFailure.mtdError(Status.INTERNAL_SERVER_ERROR, "error")))

          await(result) shouldBe expectedResult
        }
      }
    }


    "retrieveFinancialData" when {

      "a valid response is returned" must {

        "return Future(Right(FinancialDataResponse)" in {

          mockRetrieveFinancialData(
            FinancialDataConstants.financialRequest,
            Right(FinancialDataConstants.wrappedFinancialDataResponse())
          )

          val result = TestService.retrieveFinancialData(FinancialDataConstants.financialRequest)
          val expectedResult = Right(FinancialDataConstants.wrappedFinancialDataResponse())

          await(result) shouldBe expectedResult
        }
      }

      "a invalid json response is returned" must {

        "return Future(Left(PenaltiesConstants.errorWrapper(InvalidJson))" in {

          mockRetrieveFinancialData(
            FinancialDataConstants.financialRequest,
            Left(FinancialDataConstants.errorWrapper(InvalidJson))
          )

          val result = TestService.retrieveFinancialData(FinancialDataConstants.financialRequest)
          val expectedResult = Left(FinancialDataConstants.errorWrapper(InvalidJson))

          await(result) shouldBe expectedResult
        }
      }

      "a VrnFormatError response is returned" must {

        "return Future(Left(VrnFormatError)" in {

          mockRetrieveFinancialData(
            FinancialDataConstants.financialRequest,
            Left(FinancialDataConstants.errorWrapper(VrnFormatError))
          )

          val result = TestService.retrieveFinancialData(FinancialDataConstants.financialRequest)
          val expectedResult = Left(FinancialDataConstants.errorWrapper(VrnFormatError))

          await(result) shouldBe expectedResult
        }
      }

      "a VrnNotFound response is returned" must {

        "return Future(Left(VrnNotFound)" in {

          mockRetrieveFinancialData(
            FinancialDataConstants.financialRequest,
            Left(FinancialDataConstants.errorWrapper(VrnNotFound))
          )

          val result = TestService.retrieveFinancialData(FinancialDataConstants.financialRequest)
          val expectedResult = Left(FinancialDataConstants.errorWrapper(VrnNotFound))

          await(result) shouldBe expectedResult
        }
      }

      "a UnexpectedFailure response is returned" must {

        "return Future(Left(UnexpectedFailure)" in {

          mockRetrieveFinancialData(
            FinancialDataConstants.financialRequest,
            Left(FinancialDataConstants.errorWrapper(UnexpectedFailure.mtdError(Status.INTERNAL_SERVER_ERROR, "error")))
          )

          val result = TestService.retrieveFinancialData(FinancialDataConstants.financialRequest)
          val expectedResult = Left(FinancialDataConstants.errorWrapper(UnexpectedFailure.mtdError(Status.INTERNAL_SERVER_ERROR, "error")))

          await(result) shouldBe expectedResult
        }
      }
    }
  }

}
