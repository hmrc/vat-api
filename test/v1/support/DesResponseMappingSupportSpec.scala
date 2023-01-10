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

package v1.support

import support.UnitSpec
import utils.{ EndpointLogContext, Logging }
import v1.models.errors._
import v1.models.outcomes.ResponseWrapper
import v1.models.response.liabilities.LiabilitiesResponse
import v1.models.response.payments.PaymentsResponse

class DesResponseMappingSupportSpec extends UnitSpec {
  class TestClass extends DesResponseMappingSupport with Logging

  implicit val endpointLogContext: EndpointLogContext = EndpointLogContext("", "")

  val testClass = new TestClass

  private val correlationId = "X-123"

  "validatePaymentsSuccessResponse" should {
    "return Left" when {
      "returned payments array is empty" in {
        testClass.validatePaymentsSuccessResponse(ResponseWrapper(correlationId, PaymentsResponse(payments = Seq.empty))) shouldBe
          Left(ErrorWrapper(correlationId, LegacyNotFoundError, None))
      }
    }

    "return Right" when {
      "anything else is returned" in {
        testClass.validatePaymentsSuccessResponse(ResponseWrapper(correlationId, "beans")) shouldBe
          Right(ResponseWrapper(correlationId, "beans"))
      }
    }
  }

  "validateLiabilitiesSuccessResponse" should {
    "return Left" when {
      "returned liabilities array is empty" in {
        testClass.validateLiabilitiesSuccessResponse(ResponseWrapper(correlationId, LiabilitiesResponse(liabilities = Seq.empty))) shouldBe
          Left(ErrorWrapper(correlationId, LegacyNotFoundError, None))
      }
    }

    "return Right" when {
      "anything else is returned" in {
        testClass.validatePaymentsSuccessResponse(ResponseWrapper(correlationId, "beans")) shouldBe
          Right(ResponseWrapper(correlationId, "beans"))
      }
    }
  }

  "mapDesErrors" when {
    "a single error is returned" should {
      val errorMap: Map[String, MtdError] = Map(
        "error" -> VrnFormatError
      )
      "return a mapped error if the error is in the provided Map" in {
        testClass.mapDesErrors(errorMap)(ResponseWrapper(correlationId, DesErrors(List(DesErrorCode("error"))))) shouldBe
          ErrorWrapper(correlationId, VrnFormatError)
      }
      "return a default error if the error is not in the provided Map" in {
        testClass.mapDesErrors(errorMap)(ResponseWrapper(correlationId, DesErrors(List(DesErrorCode("wrong error"))))) shouldBe
          ErrorWrapper(correlationId, DownstreamError)
      }
    }

    "multiple errors are returned" should {
      val errorMap: Map[String, MtdError] = Map(
        "error 1" -> VrnFormatError,
        "error 2" -> RuleInsolventTraderError,
        "error 3" -> DownstreamError,
      )
      "return mapped errors" when {
        "all errors are in the provided Map and none map to DownstreamError" in {
          testClass.mapDesErrors(errorMap)(ResponseWrapper(correlationId, DesErrors(List(DesErrorCode("error 1"), DesErrorCode("error 2"))))) shouldBe
            ErrorWrapper(correlationId, BadRequestError, Some(Seq(VrnFormatError, RuleInsolventTraderError)))
        }
      }
      "return a default error" when {
        "at least one error maps to DownstreamError" in {
          testClass.mapDesErrors(errorMap)(ResponseWrapper(correlationId, DesErrors(List(DesErrorCode("error 1"), DesErrorCode("error 3"))))) shouldBe
            ErrorWrapper(correlationId, DownstreamError)
        }
        "at least one error is not in the provided Map" in {
          testClass.mapDesErrors(errorMap)(ResponseWrapper(correlationId, DesErrors(List(DesErrorCode("wrong error"))))) shouldBe
            ErrorWrapper(correlationId, DownstreamError)
        }
      }
    }

    "OutboundError is returned" should {
      "return errors regardless of what's in them" in {
        testClass.mapDesErrors(Map[String, MtdError]())(ResponseWrapper(correlationId, OutboundError(DownstreamError, Some(Seq())))) shouldBe
          ErrorWrapper(correlationId, DownstreamError, Some(Seq()))
      }
    }
  }
}
