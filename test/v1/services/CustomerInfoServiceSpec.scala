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
import v1.constants.CustomerInfoConstants.{correlationId, userRequest}
import v1.constants.CustomerInfoConstants
import v1.mocks.connectors.MockCustomerInfoConnector
import v1.models.errors.{InvalidJson, UnexpectedFailure, VrnFormatError, VrnNotFound}

import scala.concurrent.ExecutionContext


class CustomerInfoServiceSpec extends GuiceBox with MockCustomerInfoConnector {

  object TestService extends CustomerInfoService(mockcustomerInfoConnector)

  implicit val hc: HeaderCarrier = HeaderCarrier()
  implicit val ec: ExecutionContext = app.injector.instanceOf[ExecutionContext]

  "CustomerInfoService" when {

    "retrieveCustomerInfo" when {

      "a valid response is returned" must {

        "return Future(Right(CustomerInfoResponse)" in {

          mockRetrieveCustomerInfo(
            CustomerInfoConstants.customerInfoRequest,
            Right(CustomerInfoConstants.wrappedCustomerInfoResponse())
          )

          val result = TestService.retrieveCustomerInfo(CustomerInfoConstants.customerInfoRequest)
          val expectedResult = Right(CustomerInfoConstants.wrappedCustomerInfoResponse())

          await(result) shouldBe expectedResult
        }
      }

      "a invalid json response is returned" must {

        "return Future(Left(CustomerInfoConstants.errorWrapper(InvalidJson))" in {

          mockRetrieveCustomerInfo(
            CustomerInfoConstants.customerInfoRequest,
            Left(CustomerInfoConstants.errorWrapper(InvalidJson))
          )
          val result = TestService.retrieveCustomerInfo(CustomerInfoConstants.customerInfoRequest)
          val expectedResult = Left(CustomerInfoConstants.errorWrapper(InvalidJson))

          await(result) shouldBe expectedResult
        }
      }

      "a VrnFormatError response is returned" must {

        "return Future(Left(VrnFormatError)" in {

          mockRetrieveCustomerInfo(
            CustomerInfoConstants.customerInfoRequest,
            Left(CustomerInfoConstants.errorWrapper(VrnFormatError))
          )

          val result = TestService.retrieveCustomerInfo(CustomerInfoConstants.customerInfoRequest)
          val expectedResult = Left(CustomerInfoConstants.errorWrapper(VrnFormatError))

          await(result) shouldBe expectedResult
        }
      }

      "a VrnNotFound response is returned" must {

        "return Future(Left(VrnNotFound)" in {

          mockRetrieveCustomerInfo(
            CustomerInfoConstants.customerInfoRequest,
            Left(CustomerInfoConstants.errorWrapper(VrnNotFound))
          )

          val result = TestService.retrieveCustomerInfo(CustomerInfoConstants.customerInfoRequest)
          val expectedResult = Left(CustomerInfoConstants.errorWrapper(VrnNotFound))

          await(result) shouldBe expectedResult
        }
      }

      "a UnexpectedFailure response is returned" must {

        "return Future(Left(UnexpectedFailure)" in {

          mockRetrieveCustomerInfo(
            CustomerInfoConstants.customerInfoRequest,
            Left(CustomerInfoConstants.errorWrapper(UnexpectedFailure.mtdError(Status.INTERNAL_SERVER_ERROR, "error")))
          )

          val result = TestService.retrieveCustomerInfo(CustomerInfoConstants.customerInfoRequest)
          val expectedResult = Left(CustomerInfoConstants.errorWrapper(UnexpectedFailure.mtdError(Status.INTERNAL_SERVER_ERROR, "error")))

          await(result) shouldBe expectedResult
        }
      }
    }
  }
}
