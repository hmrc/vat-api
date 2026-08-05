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

package v1.services

import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import support.LogCapturing
import utils.pagerDutyLogging.Endpoint
import v1.controllers.UserRequest
import v1.mocks.connectors.MockObligationsConnector
import v1.models.auth.UserDetails
import v1.models.domain.Vrn
import v1.models.errors._
import v1.models.outcomes.ResponseWrapper
import v1.models.request.obligations.ObligationsRequest
import v1.models.request.submit.{ SubmitRequest, SubmitRequestBody }
import v1.models.response.obligations.{ Obligation, ObligationsResponse }

import java.time.LocalDate
import scala.concurrent.Future

class AssistObligationServiceSpec extends ServiceSpec with LogCapturing {

  implicit val userRequest: UserRequest[AnyContentAsEmpty.type] =
    UserRequest(UserDetails("Individual", None, "id"), FakeRequest())

  private val vrn: String       = "123456789"
  private val periodKey: String = "18A2"
  private val today: LocalDate  = LocalDate.parse("2026-05-12")

  private val submitRequestBody: SubmitRequestBody = SubmitRequestBody(
    periodKey = Some(periodKey),
    vatDueSales = Some(7000.00),
    vatDueAcquisitions = Some(3000.00),
    totalVatDue = Some(10000.00),
    vatReclaimedCurrPeriod = Some(1000.00),
    netVatDue = Some(9000.00),
    totalValueSalesExVAT = Some(1000),
    totalValuePurchasesExVAT = Some(200),
    totalValueGoodsSuppliedExVAT = Some(100),
    totalAcquisitionsExVAT = Some(540),
    finalised = None
  )

  private val submitRequest: SubmitRequest = SubmitRequest(Vrn(vrn), submitRequestBody)

  /** The service always requests open obligations only, with no date range. */
  private val obligationsRequest: ObligationsRequest =
    ObligationsRequest(vrn = Vrn(vrn), from = None, to = None, status = Some("O"))

  private def obligation(periodKey: String, end: String): Obligation =
    Obligation(
      periodKey = periodKey,
      start = "2026-01-01",
      end = end,
      due = "2026-05-07",
      status = "O",
      received = None
    )

  private def obligationsResponse(obligations: Obligation*): ObligationsResponse =
    ObligationsResponse(obligations)

  trait Test extends MockObligationsConnector {
    val service = new AssistObligationService(connector = mockObligationsConnector)

    def connectorReturns(response: ObligationsResponse): Unit =
      MockObligationsConnector
        .retrieveObligations(obligationsRequest)
        .returns(Future.successful(Right(ResponseWrapper(correlationId, response))))
  }

  "retrieveOpenObligation" when {

    "the period key matches an open obligation whose period has ended" must {

      "return the matched obligation" in new Test {
        private val matched = obligation(periodKey, end = "2026-03-31")
        connectorReturns(obligationsResponse(matched))

        await(service.retrieveOpenObligation(submitRequest, today)) shouldBe
          Right(ResponseWrapper(correlationId, matched))
      }

      "select the obligation matching the period key when several are returned" in new Test {
        private val other   = obligation("19B1", end = "2026-02-28")
        private val matched = obligation(periodKey, end = "2026-03-31")
        connectorReturns(obligationsResponse(other, matched))

        await(service.retrieveOpenObligation(submitRequest, today)) shouldBe
          Right(ResponseWrapper(correlationId, matched))
      }

      "return the inbound correlation ID rather than the one returned by DES" in new Test {
        private val matched = obligation(periodKey, end = "2026-03-31")

        MockObligationsConnector
          .retrieveObligations(obligationsRequest)
          .returns(Future.successful(Right(ResponseWrapper("des-correlation-id", obligationsResponse(matched)))))

        await(service.retrieveOpenObligation(submitRequest, today)) shouldBe
          Right(ResponseWrapper(correlationId, matched))
      }
    }

    "the period key matches but the period has not ended" must {

      "return TaxPeriodNotEnded when the end date is in the future" in new Test {
        connectorReturns(obligationsResponse(obligation(periodKey, end = "2026-06-30")))

        await(service.retrieveOpenObligation(submitRequest, today)) shouldBe
          Left(ErrorWrapper(correlationId, TaxPeriodNotEnded))
      }

      "return TaxPeriodNotEnded when the end date is today, as today must be strictly after it" in new Test {
        connectorReturns(obligationsResponse(obligation(periodKey, end = "2026-05-12")))

        await(service.retrieveOpenObligation(submitRequest, today)) shouldBe
          Left(ErrorWrapper(correlationId, TaxPeriodNotEnded))
      }

      "return the matched obligation when the end date is the day before today" in new Test {
        private val matched = obligation(periodKey, end = "2026-05-11")
        connectorReturns(obligationsResponse(matched))

        await(service.retrieveOpenObligation(submitRequest, today)) shouldBe
          Right(ResponseWrapper(correlationId, matched))
      }
    }

    "no open obligation matches the period key" must {

      "return the no-match error when other obligations are returned" in new Test {
        connectorReturns(obligationsResponse(obligation("19B1", end = "2026-02-28")))

        await(service.retrieveOpenObligation(submitRequest, today)) shouldBe
          Left(ErrorWrapper(correlationId, NoOpenObligation))
      }

      "return the no-match error when no obligations are returned at all" in new Test {
        connectorReturns(obligationsResponse())

        await(service.retrieveOpenObligation(submitRequest, today)) shouldBe
          Left(ErrorWrapper(correlationId, NoOpenObligation))
      }
    }

    "the matched obligation has an unparseable end date" must {
      "return DownstreamError" in new Test {
        connectorReturns(obligationsResponse(obligation(periodKey, end = "not-a-date")))

        await(service.retrieveOpenObligation(submitRequest, today)) shouldBe
          Left(ErrorWrapper(correlationId, DownstreamError))
      }
    }

    "the period key is absent from the parsed body" must {
      "fall back to the sentinel and return the no-match error" in new Test {
        private val requestWithoutPeriodKey = SubmitRequest(Vrn(vrn), submitRequestBody.copy(periodKey = None))
        connectorReturns(obligationsResponse(obligation(periodKey, end = "2026-03-31")))

        await(service.retrieveOpenObligation(requestWithoutPeriodKey, today)) shouldBe
          Left(ErrorWrapper(correlationId, NoOpenObligation))
      }
    }

    "the obligations call fails" must {

      "collapse every downstream error code to ServiceUnavailableError" when {

        def downstreamError(desErrorCode: String): Unit =
          s"a $desErrorCode error is returned from the connector" in new Test {

            MockObligationsConnector
              .retrieveObligations(obligationsRequest)
              .returns(Future.successful(Left(ResponseWrapper(correlationId, DesErrors.single(DesErrorCode(desErrorCode))))))

            await(service.retrieveOpenObligation(submitRequest, today)) shouldBe
              Left(ErrorWrapper(correlationId, ServiceUnavailableError))
          }

        val input = Seq(
          "INVALID_IDTYPE",
          "INVALID_IDNUMBER",
          "INVALID_REGIME",
          "INVALID_STATUS",
          "NOT_FOUND_BPKEY",
          "NOT_FOUND",
          "SERVER_ERROR",
          "SERVICE_UNAVAILABLE",
          "DOWNSTREAM_ERROR"
        )

        input.foreach(downstreamError)
      }

      "collapse multiple downstream error codes to ServiceUnavailableError" in new Test {

        MockObligationsConnector
          .retrieveObligations(obligationsRequest)
          .returns(Future.successful(
            Left(ResponseWrapper(correlationId, DesErrors(List(DesErrorCode("INVALID_IDTYPE"), DesErrorCode("INVALID_IDNUMBER")))))))

        await(service.retrieveOpenObligation(submitRequest, today)) shouldBe
          Left(ErrorWrapper(correlationId, ServiceUnavailableError))
      }

      "collapse an OutboundError to ServiceUnavailableError" in new Test {

        MockObligationsConnector
          .retrieveObligations(obligationsRequest)
          .returns(Future.successful(Left(ResponseWrapper(correlationId, OutboundError(DownstreamError)))))

        await(service.retrieveOpenObligation(submitRequest, today)) shouldBe
          Left(ErrorWrapper(correlationId, ServiceUnavailableError))
      }

      "return the txr correlation ID even when DES returns its own" in new Test {

        MockObligationsConnector
          .retrieveObligations(obligationsRequest)
          .returns(Future.successful(Left(ResponseWrapper("des-correlation-id", DesErrors.single(DesErrorCode("SERVER_ERROR"))))))

        await(service.retrieveOpenObligation(submitRequest, today)) shouldBe
          Left(ErrorWrapper(correlationId, ServiceUnavailableError))
      }

      "return ServiceUnavailableError when the connector call throws an exception" in new Test {

        MockObligationsConnector
          .retrieveObligations(obligationsRequest)
          .returns(Future.failed(new RuntimeException("connection reset")))

        await(service.retrieveOpenObligation(submitRequest, today)) shouldBe
          Left(ErrorWrapper(correlationId, ServiceUnavailableError))
      }

      "log the failure, raise a PagerDuty alert, and return ServiceUnavailableError when the connector throws" in new Test {

        private val exceptionMessage = "connection reset"

        MockObligationsConnector
          .retrieveObligations(obligationsRequest)
          .returns(Future.failed(new RuntimeException(exceptionMessage)))

        withCaptureOfLoggingFrom(service.logger) { logs =>
          val result = await(service.retrieveOpenObligation(submitRequest, today))

          val allLogs = logs.map(_.getMessage).mkString

          allLogs should include(s"Request failed with error: $exceptionMessage")
          allLogs should include(Endpoint.RetrieveObligations.requestFailedMessage.toString)

          result shouldBe Left(ErrorWrapper(correlationId, ServiceUnavailableError))
        }
      }
    }
  }
}
