/*
 * Copyright 2026 HM Revenue & Customs
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

package v1.controllers.internal

import play.api.libs.json.{JsValue, Json}
import play.api.mvc.{AnyContent, Result}
import uk.gov.hmrc.http.HeaderCarrier
import v1.mocks.MockIdGenerator
import v1.controllers.ControllerBaseSpec
import v1.mocks.requestParsers.MockAssistReturnRequestParser
import v1.mocks.services.MockEnrolmentsAuthService
import v1.models.domain.Vrn
import v1.models.errors._
import v1.models.request.submit.{SubmitRawData, SubmitRequest, SubmitRequestBody}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class AssistReturnControllerSpec
  extends ControllerBaseSpec
    with MockEnrolmentsAuthService
    with MockAssistReturnRequestParser
    with MockIdGenerator {

  val vrn: String           = "123456789"
  val correlationId: String = "X-ID"

  trait Test {
    val hc: HeaderCarrier = HeaderCarrier()

    val controller: AssistReturnController = new AssistReturnController(
      mockEnrolmentsAuthService,
      mockAssistReturnRequestParser,
      cc,
      mockIdGenerator
    )

    MockEnrolmentsAuthService.authoriseUser()
    MockIdGenerator.getUid.returns(correlationId).anyNumberOfTimes()
  }

  val requestBodyJson: JsValue = Json.parse(
    """
      |{
      |   "periodKey": "#001",
      |   "vatDueSales": 7000.00,
      |   "vatDueAcquisitions": 3000.00,
      |   "totalVatDue": 10000,
      |   "vatReclaimedCurrPeriod": 1000,
      |   "netVatDue": 9000,
      |   "totalValueSalesExVAT": 1000,
      |   "totalValuePurchasesExVAT": 200,
      |   "totalValueGoodsSuppliedExVAT": 100,
      |   "totalAcquisitionsExVAT": 540
      |}
      |""".stripMargin
  )

  val requestBody: SubmitRequestBody = SubmitRequestBody(
    periodKey                    = Some("#001"),
    vatDueSales                  = Some(7000.00),
    vatDueAcquisitions           = Some(3000),
    totalVatDue                  = Some(10000),
    vatReclaimedCurrPeriod       = Some(1000),
    netVatDue                    = Some(9000),
    totalValueSalesExVAT         = Some(1000),
    totalValuePurchasesExVAT     = Some(200),
    totalValueGoodsSuppliedExVAT = Some(100),
    totalAcquisitionsExVAT       = Some(540),
    finalised                    = None
  )

  val rawData: SubmitRawData =
    SubmitRawData(vrn, AnyContent(requestBodyJson))

  val parsedRequest: SubmitRequest =
    SubmitRequest(Vrn(vrn), requestBody)

  "validateReturn" when {

    "a valid request is supplied" should {
      "return 204 No Content with a correlation ID header" in new Test {

        MockAssistReturnRequestParser
          .parse(rawData)
          .returns(Right(parsedRequest))

        private val result: Future[Result] =
          controller.validateReturn(vrn)(fakePostRequest(requestBodyJson))

        status(result) shouldBe NO_CONTENT
        header("X-CorrelationId", result) shouldBe Some(correlationId)
      }
    }

    "the request body fails a single validation rule" should {
      "return 400 with the error" in new Test {

        MockAssistReturnRequestParser
          .parse(rawData)
          .returns(Left(ErrorWrapper(correlationId, VrnFormatError, None)))

        private val result: Future[Result] =
          controller.validateReturn(vrn)(fakePostRequest(requestBodyJson))

        status(result) shouldBe BAD_REQUEST
        contentAsJson(result) shouldBe Json.toJson(VrnFormatError)
        header("X-CorrelationId", result) shouldBe Some(correlationId)
      }
    }

    "the request body fails multiple validation rules" should {
      "return 400 with all errors nested under INVALID_REQUEST" in new Test {

        val expectedError: JsValue = Json.parse(
          s"""
             |{
             |  "code": "INVALID_REQUEST",
             |  "message": "Invalid request",
             |  "errors": [
             |    {
             |      "code": "VAT_TOTAL_VALUE",
             |      "message": "totalVatDue should be equal to vatDueSales + vatDueAcquisitions",
             |      "path": "/totalVatDue"
             |    },
             |    {
             |      "code": "VAT_NET_VALUE",
             |      "message": "netVatDue should be the difference between the largest and the smallest values among totalVatDue and vatReclaimedCurrPeriod",
             |      "path": "/netVatDue"
             |    }
             |  ]
             |}
             |""".stripMargin
        )

        MockAssistReturnRequestParser
          .parse(rawData)
          .returns(Left(ErrorWrapper(correlationId, BadRequestError, Some(List(VATTotalValueRuleError, VATNetValueRuleError)))))

        private val result: Future[Result] =
          controller.validateReturn(vrn)(fakePostRequest(requestBodyJson))

        status(result) shouldBe BAD_REQUEST
        contentAsJson(result) shouldBe expectedError
        header("X-CorrelationId", result) shouldBe Some(correlationId)
      }
    }

    "the parser returns a specific error" must {
      def errorsFromParserTester(error: MtdError, expectedStatus: Int): Unit =
        s"return $expectedStatus when a ${error.code} error is returned from the parser" in new Test {

          MockAssistReturnRequestParser
            .parse(rawData)
            .returns(Left(ErrorWrapper(correlationId, error, None)))

          val result: Future[Result] =
            controller.validateReturn(vrn)(fakePostRequest(requestBodyJson))

          status(result) shouldBe expectedStatus
          contentAsJson(result) shouldBe Json.toJson(error)
          header("X-CorrelationId", result) shouldBe Some(correlationId)
        }

      val input = Seq(
        (VrnFormatError, BAD_REQUEST),
        (BodyPeriodKeyFormatError, BAD_REQUEST),
        (MandatoryFieldRuleError, BAD_REQUEST),
        (StringFormatRuleError, BAD_REQUEST),
        (UnMappedPlayRuleError, BAD_REQUEST)
      )

      input.foreach(args => (errorsFromParserTester _).tupled(args))
    }
  }
}