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

import org.joda.time.DateTime
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.{AnyContent, Result}
import uk.gov.hmrc.domain.Vrn
import uk.gov.hmrc.http.HeaderCarrier
import utils.DateUtils
import v1.audit.AuditEvents
import v1.mocks.{MockCurrentDateTime, MockIdGenerator}
import v1.mocks.requestParsers.MockSubmitReturnRequestParser
import v1.mocks.services.{MockAuditService, MockEnrolmentsAuthService, MockNrsService, MockSubmitReturnService}
import v1.models.audit.{AuditError, AuditResponse}
import v1.models.auth.UserDetails
import v1.models.errors._
import v1.models.nrs.response.NrsResponse
import v1.models.outcomes.ResponseWrapper
import v1.models.request.submit.{SubmitRawData, SubmitRequest, SubmitRequestBody}
import v1.models.response.submit.SubmitResponse

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class SubmitReturnControllerSpec
  extends ControllerBaseSpec
    with MockEnrolmentsAuthService
    with MockSubmitReturnService
    with MockSubmitReturnRequestParser
    with MockAuditService
    with MockCurrentDateTime
    with MockNrsService
    with MockIdGenerator {

  val date: DateTime = DateTime.parse("2017-01-01T00:00:00.000Z")
  val fmt: String = DateUtils.dateTimePattern
  val vrn: String = "123456789"
  val correlationId: String = "X-ID"
  val uid: String = "a5894863-9cd7-4d0d-9eee-301ae79cbae6"
  val periodKey: String = "A1A2"

  trait Test {
    val hc: HeaderCarrier = HeaderCarrier()
    
    val controller: SubmitReturnController = new SubmitReturnController(
      mockEnrolmentsAuthService,
      mockSubmitReturnRequestParser,
      mockSubmitReturnService,
      mockNrsService,
      auditService = mockAuditService,
      cc,
      dateTime = mockCurrentDateTime,
      mockIdGenerator
    )

    MockEnrolmentsAuthService.authoriseUser()
    MockCurrentDateTime.getCurrentDate.returns(date).anyNumberOfTimes()
    MockIdGenerator.getUid.returns(uid)
    MockIdGenerator.getCorrelationId.returns(correlationId)
  }


  val submitRequestBody: SubmitRequestBody = SubmitRequestBody(
    periodKey = Some("#001"),
    vatDueSales = Some(7000.00),
    vatDueAcquisitions = Some(3000),
    totalVatDue = Some(10000),
    vatReclaimedCurrPeriod = Some(1000),
    netVatDue = Some(9000),
    totalValueSalesExVAT = Some(1000),
    totalValuePurchasesExVAT = Some(200),
    totalValueGoodsSuppliedExVAT = Some(100),
    totalAcquisitionsExVAT = Some(540),
    finalised = Some(true)
  )

  val submitRequestBodyJson: JsValue = Json.parse(
    """
      |{
      |   "periodKey": "#001",
      |   "vatDueSales": 	7000.00,
      |   "vatDueAcquisitions": 	3000.00,
      |   "vatDueTotal": 	10000,
      |   "vatReclaimedCurrPeriod": 	1000,
      |   "vatDueNet": 	9000,
      |   "totalValueSalesExVAT": 	1000,
      |   "totalValuePurchasesExVAT": 	200,
      |   "totalValueGoodsSuppliedExVAT": 	100,
      |   "totalAllAcquisitionsExVAT": 	540,
      |   "finalised": true
      |}
      |""".stripMargin
  )

  val submitRequestRawData: SubmitRawData = SubmitRawData(
    vrn,
    AnyContent(submitRequestBodyJson)
  )

  val submitReturnRequest: SubmitRequest =
    SubmitRequest(
      vrn = Vrn(vrn),
      submitRequestBody.copy(receivedAt = Some(date.toString(fmt)))
    )

  val submitReturnResponse: SubmitResponse =
    SubmitResponse(
      processingDate = DateTime.parse("2017-01-01T00:00:00.000Z"),
      paymentIndicator = Some("DD"),
      formBundleNumber = "123456789012",
      chargeRefNumber = Some("SKDJGFH9URGT")
    )

  val submitReturnResponseJson: JsValue = Json.parse(
    """
      |{
      |   "processingDate": "2017-01-01T00:00:00.000Z",
      |   "formBundleNumber": "123456789012",
      |   "paymentIndicator": "DD",
      |   "chargeRefNumber": "SKDJGFH9URGT"
      |}
      |""".stripMargin
  )

  private val nrsResponse: NrsResponse =
    NrsResponse(
      "id",
      "This has been deprecated - DO NOT USE",
      ""
    )

  "submitReturn" when {
    "a valid request is supplied" should {
      "return the expected data on a successful service call" in new Test {

        MockSubmitReturnRequestParser
          .parse(submitRequestRawData)
          .returns(Right(submitReturnRequest))

        MockNrsService
          .submitNrs(submitReturnRequest, uid, date)
          .returns(Future.successful(Right(nrsResponse)))

        MockSubmitReturnService
          .submitReturn(submitReturnRequest)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, submitReturnResponse))))

        private val result: Future[Result] = controller.submitReturn(vrn)(fakePostRequest(submitRequestBodyJson))

        status(result) shouldBe CREATED
        contentAsJson(result) shouldBe submitReturnResponseJson
        header("X-CorrelationId", result) shouldBe Some(correlationId)
        header("Receipt-Timestamp", result).getOrElse("No Header") should fullyMatch.regex(DateUtils.isoInstantDateRegex)

        val auditResponse: AuditResponse = AuditResponse(CREATED, None, Some(submitReturnResponseJson))
        MockedAuditService.verifyAuditEvent(AuditEvents.auditSubmit(correlationId,
          UserDetails("Individual", None, "N/A"), auditResponse)).once
      }
    }

    "a valid request is supplied but NRS is failed" should {
      "return the INTERNAL_SERVER_ERROR" in new Test {

        MockSubmitReturnRequestParser
          .parse(submitRequestRawData)
          .returns(Right(submitReturnRequest))

        MockNrsService
          .submitNrs(submitReturnRequest, uid, date)
          .returns(Future.successful(Left(ErrorWrapper(correlationId, DownstreamError, None))))

        private val result: Future[Result] = controller.submitReturn(vrn)(fakePostRequest(submitRequestBodyJson))

        status(result) shouldBe INTERNAL_SERVER_ERROR
        contentAsJson(result) shouldBe Json.toJson(DownstreamError)

        val auditResponse: AuditResponse = AuditResponse(INTERNAL_SERVER_ERROR, Some(Seq(AuditError("INTERNAL_SERVER_ERROR"))), None)
        MockedAuditService.verifyAuditEvent(AuditEvents.auditSubmit(correlationId,
          UserDetails("Individual", None, "N/A"), auditResponse)).once
      }
    }

    "a invalid finalised format request is supplied" should {
      "return the UNMAPPED_PLAY_ERROR" in new Test {

        val submitRequestBodyJsonWithInvalidFinalisedFormat: JsValue = Json.parse(
          """
            |{
            |   "periodKey": "#001",
            |   "vatDueSales": 	7000.00,
            |   "vatDueAcquisitions": 	3000.00,
            |   "totalVatDue": 	10000,
            |   "vatReclaimedCurrPeriod": 	1000,
            |   "netVatDue": 	9000,
            |   "totalValueSalesExVAT": 	1000,
            |   "totalValuePurchasesExVAT": 	200,
            |   "totalValueGoodsSuppliedExVAT": 	100,
            |   "totalAllAcquisitionsExVAT": 	540,
            |   "finalised": "TEST"
            |}
            |""".stripMargin
        )

        val expectedError: JsValue = Json.parse(
          s"""
             |{
             |  "code": "INVALID_REQUEST",
             |  "message": "Invalid request",
             |  "errors": [
             |    {
             |      "code": "UNMAPPED_PLAY_ERROR",
             |      "message": "error.expected.jsboolean",
             |      "path": "/finalised"
             |    }
             |  ]
             |}
      """.stripMargin)

        MockSubmitReturnRequestParser
          .parse(submitRequestRawData.copy(body = AnyContent(submitRequestBodyJsonWithInvalidFinalisedFormat)))
          .returns(Left(ErrorWrapper(correlationId, UnMappedPlayRuleError, None)))

        private val result: Future[Result] = controller.submitReturn(vrn)(fakePostRequest(submitRequestBodyJsonWithInvalidFinalisedFormat))

        status(result) shouldBe BAD_REQUEST
        contentAsJson(result) shouldBe expectedError
        header("X-CorrelationId", result) shouldBe Some(correlationId)

        val auditResponse: AuditResponse = AuditResponse(BAD_REQUEST, Some(Seq(AuditError("UNMAPPED_PLAY_ERROR"))), None)
        MockedAuditService.verifyAuditEvent(AuditEvents.auditSubmit(correlationId,
          UserDetails("Individual", None, "N/A"), auditResponse)).once
      }
    }

    "submitted request is not obeying the RULE validations" should {
      "return the errors" in new Test {

        val submitRequestBodyJsonWithInvalidFinalisedFormat: JsValue = Json.parse(
          """
            |{
            |   "periodKey": "#001",
            |   "vatDueSales": 	7000.00,
            |   "vatDueAcquisitions": 	3000.00,
            |   "totalVatDue": 	4000,
            |   "vatReclaimedCurrPeriod": 	1000,
            |   "netVatDue": 	9000,
            |   "totalValueSalesExVAT": 	1000,
            |   "totalValuePurchasesExVAT": 	200,
            |   "totalValueGoodsSuppliedExVAT": 	100,
            |   "totalAllAcquisitionsExVAT": 	540,
            |   "finalised": "TEST"
            |}
            |""".stripMargin
        )

        val expectedError: JsValue = Json.parse(
          s"""
             |{
             |        "code" : "INVALID_REQUEST",
             |        "message" : "Invalid request",
             |        "errors" : [ {
             |          "code" : "VAT_TOTAL_VALUE",
             |          "message" : "totalVatDue should be equal to vatDueSales + vatDueAcquisitions",
             |          "path" : "/totalVatDue"
             |        }, {
             |          "code" : "VAT_NET_VALUE",
             |          "message" : "netVatDue should be the difference between the largest and the smallest values among totalVatDue and vatReclaimedCurrPeriod",
             |          "path" : "/netVatDue"
             |        } ]
             |      }
      """.stripMargin)

        MockSubmitReturnRequestParser
          .parse(submitRequestRawData.copy(body = AnyContent(submitRequestBodyJsonWithInvalidFinalisedFormat)))
          .returns(Left(ErrorWrapper(correlationId, BadRequestError, Some(List(VATTotalValueRuleError, VATNetValueRuleError)))))

        private val result: Future[Result] = controller.submitReturn(vrn)(fakePostRequest(submitRequestBodyJsonWithInvalidFinalisedFormat))

        status(result) shouldBe BAD_REQUEST
        contentAsJson(result) shouldBe expectedError
        header("X-CorrelationId", result) shouldBe Some(correlationId)

        val auditResponse: AuditResponse = AuditResponse(BAD_REQUEST, Some(Seq(AuditError("VAT_TOTAL_VALUE"), AuditError("VAT_NET_VALUE"))), None)
        MockedAuditService.verifyAuditEvent(AuditEvents.auditSubmit(correlationId,
          UserDetails("Individual", None, "N/A"), auditResponse)).once
      }
    }

    "return the error as per spec" when {
      "parser errors occur" must {
        def errorsFromParserTester(error: MtdError, expectedStatus: Int): Unit = {
          s"a ${error.code} error is returned from the parser" in new Test {

            MockSubmitReturnRequestParser
              .parse(submitRequestRawData)
              .returns(Left(ErrorWrapper(correlationId, error, None)))

            val result: Future[Result] = controller.submitReturn(vrn)(fakePostRequest(submitRequestBodyJson))

            status(result) shouldBe expectedStatus
            contentAsJson(result) shouldBe Json.toJson(error)
            header("X-CorrelationId", result) shouldBe Some(correlationId)

            val auditResponse: AuditResponse = AuditResponse(expectedStatus, Some(Seq(AuditError(error.code))), None)
            MockedAuditService.verifyAuditEvent(AuditEvents.auditSubmit(correlationId,
              UserDetails("Individual", None, "N/A"), auditResponse)).once
          }
        }

        val input = Seq(
          (VrnFormatError, BAD_REQUEST),
          (PeriodKeyFormatError, BAD_REQUEST),
        )

        input.foreach(args => (errorsFromParserTester _).tupled(args))
      }

      "service errors occur" must {
        def serviceErrors(mtdError: MtdError, expectedStatus: Int): Unit = {
          s"a $mtdError error is returned from the service" in new Test {

            MockSubmitReturnRequestParser
              .parse(submitRequestRawData)
              .returns(Right(submitReturnRequest))

            MockNrsService
              .submitNrs(submitReturnRequest, uid, date)
              .returns(Future.successful(Right(nrsResponse)))

            MockSubmitReturnService
              .submitReturn(submitReturnRequest)
              .returns(Future.successful(Left(ErrorWrapper(correlationId, mtdError))))

            val result: Future[Result] = controller.submitReturn(vrn)(fakePostRequest(submitRequestBodyJson))

            status(result) shouldBe expectedStatus
            contentAsJson(result) shouldBe Json.toJson(mtdError)
            header("X-CorrelationId", result) shouldBe Some(correlationId)

            val auditResponse: AuditResponse = AuditResponse(expectedStatus, Some(Seq(AuditError(mtdError.code))), None)
            MockedAuditService.verifyAuditEvent(AuditEvents.auditSubmit(correlationId,
              UserDetails("Individual", None, "N/A"), auditResponse)).once
          }
        }

        val input = Seq(
          (VrnFormatErrorDes, BAD_REQUEST),
          (BadRequestError, BAD_REQUEST),
          (PeriodKeyFormatErrorDes, BAD_REQUEST),
          (TaxPeriodNotEnded, FORBIDDEN),
          (DuplicateVatSubmission, FORBIDDEN),
          (DownstreamError, INTERNAL_SERVER_ERROR)
        )

        input.foreach(args => (serviceErrors _).tupled(args))
      }
    }
  }
}
