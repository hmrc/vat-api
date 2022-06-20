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
import v1.constants.PenaltiesConstants
import v1.mocks.MockHttpClient
import v1.models.errors.{InvalidJson, VrnNotFound, UnexpectedFailure, VrnFormatError}

import scala.concurrent.Future

class PenaltiesConnectorSpec extends GuiceBox with ConnectorSpec with MockHttpClient {

  object TestConnector extends PenaltiesConnector(mockHttpClient, appConfig)

  "PenaltiesConnector" when {

    "retrievePenalties" when {

      "a valid response is returned" must {

        "return Future(Right(PenaltiesResponse)" in {

          MockedHttpClient.get(
            url = appConfig.penaltiesBaseUrl + s"/penalties/vat/penalties/full-data/${PenaltiesConstants.vrn}",
            config = dummyHeaderCarrierConfig,
            queryParams = Seq()
          ).returns(
            Future.successful(Right(PenaltiesConstants.testPenaltiesResponse))
          )

          val result = TestConnector.retrievePenalties(PenaltiesConstants.penaltiesRequest)
          val expectedResult = Right(PenaltiesConstants.testPenaltiesResponse)

          await(result) shouldBe expectedResult
        }
      }

      "a invalid json response is returned" must {

        "return Future(Left(InvalidJson)" in {

          MockedHttpClient.get(
            url = appConfig.penaltiesBaseUrl + s"/penalties/vat/penalties/full-data/${PenaltiesConstants.vrn}",
            config = dummyHeaderCarrierConfig,
            queryParams = Seq()
          ).returns(
            Future.successful(Left(InvalidJson))
          )

          val result = TestConnector.retrievePenalties(PenaltiesConstants.penaltiesRequest)
          val expectedResult = Left(InvalidJson)

          await(result) shouldBe expectedResult
        }
      }

      "a InvalidVrn response is returned" must {

        "return Future(Left(InvalidVrn)" in {

          MockedHttpClient.get(
            url = appConfig.penaltiesBaseUrl + s"/penalties/vat/penalties/full-data/${PenaltiesConstants.vrn}",
            config = dummyHeaderCarrierConfig,
            queryParams = Seq()
          ).returns(
            Future.successful(Left(VrnFormatError))
          )

          val result = TestConnector.retrievePenalties(PenaltiesConstants.penaltiesRequest)
          val expectedResult = Left(VrnFormatError)

          await(result) shouldBe expectedResult
        }
      }

      "a VrnNotFound response is returned" must {

        "return Future(Left(VrnNotFound)" in {

          MockedHttpClient.get(
            url = appConfig.penaltiesBaseUrl + s"/penalties/vat/penalties/full-data/${PenaltiesConstants.vrn}",
            config = dummyHeaderCarrierConfig,
            queryParams = Seq()
          ).returns(
            Future.successful(Left(VrnNotFound))
          )

          val result = TestConnector.retrievePenalties(PenaltiesConstants.penaltiesRequest)
          val expectedResult = Left(VrnNotFound)

          await(result) shouldBe expectedResult
        }
      }

      "a UnexpectedFailure response is returned" must {

        "return Future(Left(UnexpectedFailure)" in {

          MockedHttpClient.get(
            url = appConfig.penaltiesBaseUrl + s"/penalties/vat/penalties/full-data/${PenaltiesConstants.vrn}",
            config = dummyHeaderCarrierConfig,
            queryParams = Seq()
          ).returns(
            Future.successful(Left(UnexpectedFailure.mtdError(Status.INTERNAL_SERVER_ERROR, "error")))
          )

          val result = TestConnector.retrievePenalties(PenaltiesConstants.penaltiesRequest)
          val expectedResult = Left(UnexpectedFailure.mtdError(Status.INTERNAL_SERVER_ERROR, "error"))

          await(result) shouldBe expectedResult
        }
      }
    }
  }
}
