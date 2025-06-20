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

package v1.services

import play.api.http.Status
import support.GuiceBox
import uk.gov.hmrc.http.HeaderCarrier
import v1.constants.PenaltiesConstants.correlationId
import v1.constants.{FinancialDataConstants, FinancialDataHIPConstants}
import v1.mocks.connectors.MockFinancialDataHIPConnector
import v1.models.errors.{InvalidJson, UnexpectedFailure}


class FinancialDataHIPServiceSpec extends GuiceBox with MockFinancialDataHIPConnector {

  object TestService extends FinancialDataHIPService(mockFinancialDataHIPConnector)
  implicit val hc: HeaderCarrier = HeaderCarrier()

    "retrieveFinancialDataHIP" when {

      "a valid response is returned" must {

        "return Future(Right(FinancialDataHIPResponse)" in {

          mockRetrieveFinancialDataHIP(
            FinancialDataHIPConstants.financialRequest
          )(
            Right(FinancialDataHIPConstants.wrappedFinancialDataResponse())
          )

          val result = TestService.retrieveFinancialDataHIP(FinancialDataHIPConstants.financialRequest)
          val expectedResult = Right(FinancialDataHIPConstants.wrappedFinancialDataResponse())

          await(result) shouldBe expectedResult
        }
      }

      "a invalid json response is returned" must {

        "return Future(Left(PenaltiesConstants.errorWrapper(InvalidJson))" in {

          mockRetrieveFinancialDataHIP(
            FinancialDataHIPConstants.financialRequest
          )(
            Left(FinancialDataConstants.errorWrapper(InvalidJson))
          )

          val result = TestService.retrieveFinancialDataHIP(FinancialDataHIPConstants.financialRequest)
          val expectedResult = Left(FinancialDataConstants.errorWrapper(InvalidJson))

          await(result) shouldBe expectedResult
        }
      }

      "a UnexpectedFailure response is returned" must {

        "return Future(Left(UnexpectedFailure)" in {

          mockRetrieveFinancialDataHIP(
            FinancialDataHIPConstants.financialRequest
          )(
            Left(FinancialDataHIPConstants.errorWrapper(UnexpectedFailure.mtdError(Status.INTERNAL_SERVER_ERROR, "error")))
          )

          val result = TestService.retrieveFinancialDataHIP(FinancialDataHIPConstants.financialRequest)
          val expectedResult = Left(FinancialDataConstants.errorWrapper(UnexpectedFailure.mtdError(Status.INTERNAL_SERVER_ERROR, "error")))

          await(result) shouldBe expectedResult
        }
      }
    }
}
