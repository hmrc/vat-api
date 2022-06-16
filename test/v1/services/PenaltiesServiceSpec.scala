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
import v1.connectors.httpparsers.{InvalidJson, InvalidVrn, UnexpectedFailure, VrnNotFound}
import v1.constants.PenaltiesConstants
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
            Right(PenaltiesConstants.testPenaltiesResponse)
          )

          val result = TestService.retrievePenalties(PenaltiesConstants.penaltiesRequest)
          val expectedResult = Right(PenaltiesConstants.testPenaltiesResponse)

          await(result) shouldBe expectedResult
        }
      }

      "a invalid json response is returned" must {

        "return Future(Left(InvalidJson)" in {

          mockRetrievePenalties(
            PenaltiesConstants.penaltiesRequest,
            Left(InvalidJson)
          )

          val result = TestService.retrievePenalties(PenaltiesConstants.penaltiesRequest)
          val expectedResult = Left(InvalidJson)

          await(result) shouldBe expectedResult
        }
      }

      "a InvalidVrn response is returned" must {

        "return Future(Left(InvalidVrn)" in {

          mockRetrievePenalties(
            PenaltiesConstants.penaltiesRequest,
            Left(InvalidVrn)
          )

          val result = TestService.retrievePenalties(PenaltiesConstants.penaltiesRequest)
          val expectedResult = Left(InvalidVrn)

          await(result) shouldBe expectedResult
        }
      }

      "a VrnNotFound response is returned" must {

        "return Future(Left(VrnNotFound)" in {

          mockRetrievePenalties(
            PenaltiesConstants.penaltiesRequest,
            Left(VrnNotFound)
          )

          val result = TestService.retrievePenalties(PenaltiesConstants.penaltiesRequest)
          val expectedResult = Left(VrnNotFound)

          await(result) shouldBe expectedResult
        }
      }

      "a UnexpectedFailure response is returned" must {

        "return Future(Left(UnexpectedFailure)" in {

          mockRetrievePenalties(
            PenaltiesConstants.penaltiesRequest,
            Left(UnexpectedFailure(Status.INTERNAL_SERVER_ERROR, "error"))
          )

          val result = TestService.retrievePenalties(PenaltiesConstants.penaltiesRequest)
          val expectedResult = Left(UnexpectedFailure(Status.INTERNAL_SERVER_ERROR, "error"))

          await(result) shouldBe expectedResult
        }
      }
    }
  }

}
