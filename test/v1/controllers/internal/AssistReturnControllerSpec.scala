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

import play.api.libs.json.{ JsValue, Json }
import play.api.mvc.{ AnyContent, Result }
import uk.gov.hmrc.http.HeaderCarrier
import v1.controllers.ControllerBaseSpec
import v1.mocks.services.{ MockAssistReturnService, MockEnrolmentsAuthService }
import v1.models.errors._
import v1.models.outcomes.ResponseWrapper
import v1.models.request.submit.SubmitRawData
import v1.models.response.obligations.Obligation

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class AssistReturnControllerSpec extends ControllerBaseSpec with MockEnrolmentsAuthService with MockAssistReturnService {

  val vrn: String           = "123456789"
  val periodKey: String     = "18A2"
  val correlationId: String = "X-ID"

  trait Test {
    val hc: HeaderCarrier = HeaderCarrier()

    val controller: AssistReturnController = new AssistReturnController(
      authService = mockEnrolmentsAuthService,
      assistReturnService = mockAssistReturnService,
      cc = cc
    )

    MockEnrolmentsAuthService.authoriseUser()
  }

  val requestBodyJson: JsValue = Json.parse(
    s"""
       |{
       |   "periodKey": "$periodKey",
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

  val rawRequest: SubmitRawData = SubmitRawData(vrn, AnyContent(requestBodyJson))

  val obligation: Obligation = Obligation(
    periodKey = periodKey,
    start = "2026-01-01",
    end = "2026-03-31",
    due = "2026-05-07",
    status = "O",
    received = None
  )

  val obligationJson: JsValue = Json.parse(
    s"""
       |{
       |  "periodKey": "$periodKey",
       |  "start": "2026-01-01",
       |  "end": "2026-03-31",
       |  "due": "2026-05-07",
       |  "status": "O"
       |}
       |""".stripMargin
  )

  private def postRequest(body: JsValue = requestBodyJson) =
    fakePostRequest(body).withHeaders("X-CorrelationId" -> correlationId)

  "validateReturnAndRetrieveObligation" when {

    "a valid request is made" should {
      "return the matched obligation on a successful service call" in new Test {

        MockAssistReturnService
          .validateAndRetrieveOpenObligation(rawRequest)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, obligation))))

        private val result = controller.validateReturnAndRetrieveObligation(vrn)(postRequest())

        status(result) shouldBe OK
        contentAsJson(result) shouldBe obligationJson
        contentType(result) shouldBe Some("application/json")
        header("X-CorrelationId", result) shouldBe Some(correlationId)
      }
    }

    "return the error as per spec" when {

      "multiple validation errors occur" must {
        "return 400 with all errors nested under INVALID_REQUEST" in new Test {

          val expectedError: JsValue = Json.parse(
            """
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

          MockAssistReturnService
            .validateAndRetrieveOpenObligation(rawRequest)
            .returns(Future.successful(Left(ErrorWrapper(correlationId, BadRequestError, Some(List(VATTotalValueRuleError, VATNetValueRuleError))))))

          private val result: Future[Result] = controller.validateReturnAndRetrieveObligation(vrn)(postRequest())

          status(result) shouldBe BAD_REQUEST
          contentAsJson(result) shouldBe expectedError
          header("X-CorrelationId", result) shouldBe Some(correlationId)
        }
      }

      "service errors occur" must {
        def serviceErrors(mtdError: MtdError, expectedStatus: Int): Unit =
          s"return a $mtdError error from the service" in new Test {

            MockAssistReturnService
              .validateAndRetrieveOpenObligation(rawRequest)
              .returns(Future.successful(Left(ErrorWrapper(correlationId, mtdError))))

            val result: Future[Result] = controller.validateReturnAndRetrieveObligation(vrn)(postRequest())

            status(result) shouldBe expectedStatus
            contentAsJson(result) shouldBe Json.toJson(mtdError)
            header("X-CorrelationId", result) shouldBe Some(correlationId)
          }

        val input = Seq(
          // Validation failures — 400
          (VrnFormatError, BAD_REQUEST),
          (BodyPeriodKeyFormatError, BAD_REQUEST),
          (MandatoryFieldRuleError, BAD_REQUEST),
          (StringFormatRuleError, BAD_REQUEST),
          (UnMappedPlayRuleError, BAD_REQUEST),
          // Obligation outcomes
          (TaxPeriodNotEnded, BAD_REQUEST),
          (NoOpenObligation, BAD_REQUEST),
          // Obligation lookup failures
          (ServiceUnavailableError, SERVICE_UNAVAILABLE),
          (DownstreamError, SERVICE_UNAVAILABLE)
        )

        input.foreach(args => (serviceErrors _).tupled(args))
      }
    }
  }
}
