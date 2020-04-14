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
import v1.mocks.requestParsers.MockViewReturnRequestParser
import v1.mocks.services.{MockEnrolmentsAuthService, MockViewReturnService}
import v1.models.errors._
import v1.models.outcomes.ResponseWrapper
import v1.models.request.viewReturn.{ViewRawData, ViewRequest}
import v1.models.response.viewReturn.ViewReturnResponse

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class ViewReturnControllerSpec
  extends ControllerBaseSpec
    with MockEnrolmentsAuthService
    with MockViewReturnService
    with MockViewReturnRequestParser {


  trait Test {
    val hc: HeaderCarrier = HeaderCarrier()

    val controller: ViewReturnController = new ViewReturnController(
      mockEnrolmentsAuthService,
      mockViewReturnRequestParser,
      mockViewReturnService,
      cc
    )

    MockEnrolmentsAuthService.authoriseUser()
  }

  val vrn: String = "123456789"
  val periodKey: String = "18A1"
  val correlationId: String = "X-ID"

  val viewReturnRawData: ViewRawData =
    ViewRawData(
      vrn = vrn,
      periodKey = periodKey
    )

  val viewReturnRequest: ViewRequest =
    ViewRequest(
      vrn = Vrn(vrn),
      periodKey = periodKey
    )

  val desJson: JsValue = Json.parse(
    """
       |{
       |    "periodKey": "A001",
       |    "vatDueSales": 1234567890123.23,
       |    "vatDueAcquisitions": -9876543210912.87,
       |    "vatDueTotal": 1234567890112.23,
       |    "vatReclaimedCurrPeriod": -1234567890122.23,
       |    "vatDueNet": 2345678901.12,
       |    "totalValueSalesExVAT": 1234567890123.00,
       |    "totalValuePurchasesExVAT": 1234567890123.00,
       |    "totalValueGoodsSuppliedExVAT": 1234567890123.00,
       |    "totalAllAcquisitionsExVAT": -1234567890123.00
       |}
    """.stripMargin
  )

  val mtdJson: JsValue = Json.parse(
    """
      |{
      |    "periodKey": "A001",
      |    "vatDueSales": 1234567890123.23,
      |    "vatDueAcquisitions": -9876543210912.87,
      |    "totalVatDue": 1234567890112.23,
      |    "vatReclaimedCurrPeriod": -1234567890122.23,
      |    "netVatDue": 2345678901.12,
      |    "totalValueSalesExVAT": 1234567890123.00,
      |    "totalValuePurchasesExVAT": 1234567890123.00,
      |    "totalValueGoodsSuppliedExVAT": 1234567890123.00,
      |    "totalAcquisitionsExVAT": -1234567890123.00
      |}
    """.stripMargin
  )

  val viewReturnResponse: ViewReturnResponse =
    ViewReturnResponse(
      periodKey = "A001",
      vatDueSales = 1234567890123.23,
      vatDueAcquisitions = -9876543210912.87,
      totalVatDue = 1234567890112.23,
      vatReclaimedCurrPeriod = -1234567890122.23,
      netVatDue = 2345678901.12,
      totalValueSalesExVAT = 1234567890123.00,
      totalValuePurchasesExVAT = 1234567890123.00,
      totalValueGoodsSuppliedExVAT = 1234567890123.00,
      totalAcquisitionsExVAT = -1234567890123.00
    )

  "viewReturn" when {
    "a valid request is supplied" should {
      "return the expected data on a successful service call" in new Test {

        MockViewReturnRequestParser
          .parse(viewReturnRawData)
          .returns(Right(viewReturnRequest))

        MockViewReturnService
          .viewReturn(viewReturnRequest)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, viewReturnResponse))))

        private val result = controller.viewReturn(vrn, periodKey)(fakeGetRequest)

        status(result) shouldBe OK
        contentAsJson(result) shouldBe mtdJson
        header("X-CorrelationId", result) shouldBe Some(correlationId)
      }
    }

    "return the error as per spec" when {
      "parser errors occur" must {
        def errorsFromParserTester(error: MtdError, expectedStatus: Int): Unit = {
          s"a ${error.code} error is returned from the parser" in new Test {

            MockViewReturnRequestParser
              .parse(viewReturnRawData)
              .returns(Left(ErrorWrapper(Some(correlationId), error, None)))

            val result: Future[Result] = controller.viewReturn(vrn, periodKey)(fakeGetRequest)

            status(result) shouldBe expectedStatus
            contentAsJson(result) shouldBe Json.toJson(error)
            header("X-CorrelationId", result) shouldBe Some(correlationId)
          }
        }

        val input = Seq(
          (VrnFormatError, BAD_REQUEST),
          (PeriodKeyFormatError, BAD_REQUEST),
          (RuleDateRangeTooLargeError, FORBIDDEN)
        )

        input.foreach(args => (errorsFromParserTester _).tupled(args))
      }

      "service errors occur" must {
        def serviceErrors(mtdError: MtdError, expectedStatus: Int): Unit = {
          s"a $mtdError error is returned from the service" in new Test {

            MockViewReturnRequestParser
              .parse(viewReturnRawData)
              .returns(Right(viewReturnRequest))

            MockViewReturnService
              .viewReturn(viewReturnRequest)
              .returns(Future.successful(Left(ErrorWrapper(Some(correlationId), mtdError))))

            val result: Future[Result] = controller.viewReturn(vrn, periodKey)(fakeGetRequest)

            status(result) shouldBe expectedStatus
            contentAsJson(result) shouldBe Json.toJson(mtdError)
            header("X-CorrelationId", result) shouldBe Some(correlationId)
          }
        }

        val input = Seq(
          (VrnFormatErrorDes, BAD_REQUEST),
          (PeriodKeyFormatErrorDes, BAD_REQUEST),
          (PeriodKeyFormatErrorDesNotFound, NOT_FOUND),
          (RuleDateRangeTooLargeError, FORBIDDEN),
          (InvalidInputDataError, FORBIDDEN),
          (DownstreamError, INTERNAL_SERVER_ERROR)
        )

        input.foreach(args => (serviceErrors _).tupled(args))
      }

      "a NOT_FOUND error is returned from the service" must {
          s"return a 404 status with an empty body" in new Test {

            MockViewReturnRequestParser
              .parse(viewReturnRawData)
              .returns(Right(viewReturnRequest))

            MockViewReturnService
              .viewReturn(viewReturnRequest)
              .returns(Future.successful(Left(ErrorWrapper(Some(correlationId), EmptyNotFoundError))))

            val result: Future[Result] = controller.viewReturn(vrn, periodKey)(fakeGetRequest)

            status(result) shouldBe NOT_FOUND
            contentAsString(result) shouldBe ""
            header("X-CorrelationId", result) shouldBe Some(correlationId)
          }
        }
    }
  }
}
