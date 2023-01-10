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

package v1.controllers

import play.api.libs.json.{JsValue, Json}
import play.api.mvc.Result
import v1.models.domain.Vrn
import uk.gov.hmrc.http.HeaderCarrier
import v1.audit.AuditEvents
import v1.mocks.MockIdGenerator
import v1.mocks.requestParsers.MockPaymentsRequestParser
import v1.mocks.services.{MockAuditService, MockEnrolmentsAuthService, MockPaymentsService}
import v1.models.audit.{AuditError, AuditResponse}
import v1.models.auth.UserDetails
import v1.models.errors.{ErrorWrapper, MtdError, _}
import v1.models.outcomes.ResponseWrapper
import v1.models.request.payments.{PaymentsRawData, PaymentsRequest}
import v1.models.response.common.TaxPeriod
import v1.models.response.payments.PaymentsResponse.Payment
import v1.models.response.payments.{PaymentItem, PaymentsResponse}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class PaymentsControllerSpec
 extends ControllerBaseSpec
   with MockEnrolmentsAuthService
   with MockPaymentsService
   with MockPaymentsRequestParser
   with MockAuditService
   with MockIdGenerator {

  val vrn: String = "123456789"
  val toDate: String = "2017-01-01"
  val fromDate: String = "2018-01-01"
  val correlationId: String = "X-ID"

  trait Test{
    val hc: HeaderCarrier = HeaderCarrier()

    val controller: PaymentsController = new PaymentsController(
      authService = mockEnrolmentsAuthService,
      requestParser = mockPaymentsRequestParser,
      service = mockPaymentsService,
      auditService = stubAuditService,
      cc = cc,
      mockIdGenerator
    )

    MockIdGenerator.getUid.returns(correlationId)
    MockEnrolmentsAuthService.authoriseUser()
  }


  val rawData: PaymentsRawData =
    PaymentsRawData(vrn = vrn, from = Some(fromDate), to = Some(toDate))

  val request: PaymentsRequest =
    PaymentsRequest(vrn = Vrn(vrn), from = fromDate, to = toDate)

  val paymentsResponse: PaymentsResponse =
    PaymentsResponse(
      payments = Seq(
        Payment(
          taxPeriod = Some(TaxPeriod(from = "2017-02-01", to = "2017-02-28")),
          `type` = "VAT Return Debit Charge",
          paymentItems = Some(Seq(
            PaymentItem(amount = Some(15.0), received = Some("2017-02-11"))
          ))
        ),
        Payment(
          taxPeriod = Some(TaxPeriod(from = "2017-03-01", to = "2017-03-25")),
          `type` = "VAT Return Debit Charge",
          paymentItems = Some(Seq(
            PaymentItem(amount = Some(40.00), received = Some("2017-03-11")),
            PaymentItem(amount = Some(1001.00), received = Some("2017-03-12"))
          ))
        ),
        Payment(
          taxPeriod = Some(TaxPeriod(from = "2017-08-01", to = "2017-12-20")),
          `type` = "VAT Return Debit Charge",
          paymentItems = Some(Seq(
            PaymentItem(Some(322.00), Some("2017-08-05")),
            PaymentItem(Some(90.00), None),
            PaymentItem(Some(6.00), Some("2017-09-12"))
          ))
        )
      )
    )

  val mtdJson: JsValue = Json.parse(
    """
      |{
      |   "payments":[
      |      {
      |         "amount":15,
      |         "received":"2017-02-11"
      |      },
      |      {
      |         "amount":40,
      |         "received":"2017-03-11"
      |      },
      |      {
      |         "amount":1001,
      |         "received":"2017-03-12"
      |      },
      |      {
      |         "amount":322,
      |         "received":"2017-08-05"
      |      },
      |      {
      |         "amount":90
      |      },
      |      {
      |         "amount":6,
      |         "received":"2017-09-12"
      |      }
      |   ]
      |}
    """.stripMargin
  )

  "retrievePayments" when {
    "a valid request is supplied" should {
      "return the expected data on a successful service call" in new Test{

        MockPaymentsRequestParser
          .parse(rawData)
          .returns(Right(request))

        MockPaymentsService
          .retrievePayments(request)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, paymentsResponse))))

        private val result = controller.retrievePayments(vrn, Some(fromDate), Some(toDate))(fakeGetRequest)

        status(result) shouldBe OK
        contentAsJson(result) shouldBe mtdJson
        contentType(result) shouldBe Some("application/json")
        header("X-CorrelationId", result) shouldBe Some(correlationId)

        val auditResponse: AuditResponse = AuditResponse(OK, None, Some(mtdJson))
        MockedAuditService.verifyAuditEvent(AuditEvents.auditPayments(correlationId,
          UserDetails("Individual", None, "client-Id"), auditResponse)).once
      }
    }

    "return the error as per spec" when {
      "parser errors occur" must {
        def errorsFromParserTester(error: MtdError, expectedStatus: Int): Unit = {
          s"a ${error.code} error is returned from the parser" in new Test {

            MockPaymentsRequestParser
              .parse(rawData)
              .returns(Left(ErrorWrapper(correlationId, error, None)))

            val result: Future[Result] = controller.retrievePayments(vrn, Some(fromDate), Some(toDate))(fakeGetRequest)

            status(result) shouldBe expectedStatus
            contentAsJson(result) shouldBe Json.toJson(error)
            header("X-CorrelationId", result) shouldBe Some(correlationId)

            val auditResponse: AuditResponse = AuditResponse(expectedStatus, Some(Seq(AuditError(error.code))), None)
            MockedAuditService.verifyAuditEvent(AuditEvents.auditPayments(correlationId,
              UserDetails("Individual", None, "client-Id"), auditResponse)).once
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

            MockPaymentsRequestParser
              .parse(rawData)
              .returns(Right(request))

            MockPaymentsService
              .retrievePayments(request)
              .returns(Future.successful(Left(ErrorWrapper(correlationId, mtdError))))

            val result: Future[Result] = controller.retrievePayments(vrn, Some(fromDate), Some(toDate))(fakeGetRequest)

            status(result) shouldBe expectedStatus
            contentAsJson(result) shouldBe Json.toJson(mtdError)
            header("X-CorrelationId", result) shouldBe Some(correlationId)

            val auditResponse: AuditResponse = AuditResponse(expectedStatus, Some(Seq(AuditError(mtdError.code))), None)
            MockedAuditService.verifyAuditEvent(AuditEvents.auditPayments(correlationId,
              UserDetails("Individual", None, "client-Id"), auditResponse)).once
          }
        }

        val input = Seq(
          (VrnFormatErrorDes, BAD_REQUEST),
          (InvalidDateFromErrorDes, BAD_REQUEST),
          (InvalidDateToErrorDes, BAD_REQUEST),
          (InvalidDataError, BAD_REQUEST),
          (LegacyNotFoundError, NOT_FOUND),
          (RuleInsolventTraderError, FORBIDDEN),
          (DownstreamError, INTERNAL_SERVER_ERROR)
        )

        input.foreach(args => (serviceErrors _).tupled(args))
      }
    }
  }
}
