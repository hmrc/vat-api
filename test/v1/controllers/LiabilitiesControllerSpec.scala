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

package v1.controllers

import play.api.libs.json.{JsValue, Json}
import play.api.mvc.Result
import uk.gov.hmrc.domain.Vrn
import uk.gov.hmrc.http.HeaderCarrier
import v1.mocks.requestParsers.MockLiabilitiesRequestParser
import v1.mocks.services.{MockEnrolmentsAuthService, MockLiabilitiesService}
import v1.models.errors._
import v1.models.outcomes.ResponseWrapper
import v1.models.request.liabilities.{LiabilitiesRawData, LiabilitiesRequest}
import v1.models.response.common.TaxPeriod
import v1.models.response.liabilities.{LiabilitiesResponse, Liability}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class LiabilitiesControllerSpec
  extends ControllerBaseSpec
    with MockEnrolmentsAuthService
    with MockLiabilitiesService
    with MockLiabilitiesRequestParser{

  trait Test {
    val hc: HeaderCarrier = HeaderCarrier()

    val controller: LiabilitiesController = new LiabilitiesController(
      mockEnrolmentsAuthService,
      mockLiabilitiesRequestParser,
      mockRetrieveLiabilitiesService,
      cc
    )

    MockEnrolmentsAuthService.authoriseUser()
  }

  val vrn: String = "123456789"
  val from: String = "01-01-2017"
  val to: String = "01-12-2017"
  val correlationId: String = "X-ID"

  val retrieveLiabilitiesRawData: LiabilitiesRawData =
    LiabilitiesRawData(
      vrn, Some(from), Some(to)
    )

  val retrieveLiabilitiesRequest: LiabilitiesRequest =
    LiabilitiesRequest(
      vrn = Vrn(vrn),  from, to
    )

  val mtdJson: JsValue = Json.parse(
    s"""
      |{
      |	"liabilities": [{
      |		"taxPeriod": {
      |			"from": "$from",
      |			"to": "$to"
      |		},
      |		"type": "VAT",
      |		"originalAmount": 1,
      |		"outstandingAmount": 1,
      |		"due": "2017-11-11"
      |	}]
      |}
    """.stripMargin
  )

  val liabilityResponse: LiabilitiesResponse =
    LiabilitiesResponse(
      Seq(
        Liability(
          Some(TaxPeriod(from, to)),
          "VAT",
          1.0,
          Some(1.0),
          Some("2017-11-11")
        )
      )
    )

  "retrieveLiabilities" when {
    "a valid request is supplied" should {
      "return the expected data on a successful service call" in new Test {

        MockLiabilitiesRequestParser
          .parse(retrieveLiabilitiesRawData)
          .returns(Right(retrieveLiabilitiesRequest))

        MockRetrieveLiabilitiesService
          .retrieveLiabilities(retrieveLiabilitiesRequest)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, liabilityResponse))))

        private val result = controller.retrieveLiabilities(vrn, Some(from), Some(to))(fakeGetRequest)

        status(result) shouldBe OK
        contentAsJson(result) shouldBe mtdJson
        header("X-CorrelationId", result) shouldBe Some(correlationId)
      }
    }

    "return the error as per spec" when {
      "parser errors occur" must {
        def errorsFromParserTester(error: MtdError, expectedStatus: Int): Unit = {
          s"a ${error.code} error is returned from the parser" in new Test {

            MockLiabilitiesRequestParser
              .parse(retrieveLiabilitiesRawData)
              .returns(Left(ErrorWrapper(Some(correlationId), error, None)))

            val result: Future[Result] = controller.retrieveLiabilities(vrn, Some(from), Some(to))(fakeGetRequest)

            status(result) shouldBe expectedStatus
            contentAsJson(result) shouldBe Json.toJson(error)
            header("X-CorrelationId", result) shouldBe Some(correlationId)
          }
        }

        val input = Seq(
          (VrnFormatError, BAD_REQUEST),
          (FinancialDataInvalidDateToError, BAD_REQUEST),
          (FinancialDataInvalidDateFromError, BAD_REQUEST),
          (FinancialDataInvalidDateRangeError, BAD_REQUEST)
        )

        input.foreach(args => (errorsFromParserTester _).tupled(args))
      }

      "service errors occur" must {
        def serviceErrors(mtdError: MtdError, expectedStatus: Int): Unit = {
          s"a $mtdError error is returned from the service" in new Test {

            MockLiabilitiesRequestParser
              .parse(retrieveLiabilitiesRawData)
              .returns(Right(retrieveLiabilitiesRequest))

            MockRetrieveLiabilitiesService
              .retrieveLiabilities(retrieveLiabilitiesRequest)
              .returns(Future.successful(Left(ErrorWrapper(Some(correlationId), mtdError))))

            val result: Future[Result] = controller.retrieveLiabilities(vrn, Some(from), Some(to))(fakeGetRequest)

            status(result) shouldBe expectedStatus
            contentAsJson(result) shouldBe Json.toJson(mtdError)
            header("X-CorrelationId", result) shouldBe Some(correlationId)
          }
        }

        val input = Seq(
          (InvalidDateFromErrorDes, BAD_REQUEST),
          (InvalidDateToErrorDes, BAD_REQUEST),
          (LegacyNotFoundError, NOT_FOUND),
          (InvalidDataError, BAD_REQUEST),
          (DownstreamError, INTERNAL_SERVER_ERROR)
        )

        input.foreach(args => (serviceErrors _).tupled(args))
      }
    }
  }
}
