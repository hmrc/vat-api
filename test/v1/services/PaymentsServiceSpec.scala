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

package v1.services

import support.UnitSpec
import uk.gov.hmrc.domain.Vrn
import uk.gov.hmrc.http.HeaderCarrier
import utils.EndpointLogContext
import v1.mocks.connectors.MockPaymentsConnector
import v1.models.errors._
import v1.models.outcomes.ResponseWrapper
import v1.models.request.payments.PaymentsRequest
import v1.models.response.common.TaxPeriod
import v1.models.response.payments.PaymentsResponse.Payment
import v1.models.response.payments.{PaymentItem, PaymentsResponse}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class PaymentsServiceSpec extends UnitSpec {

  private val vrn: String = "123456789"
  private val from: String = "2017-1-1"
  private val to: String = "2017-12-31"
  private val correlationId = "X-123"

  private val retrievePaymentsRequest: PaymentsRequest =
    PaymentsRequest(vrn = Vrn(vrn), from = from, to = to)

  private val retrievePaymentsResponse: PaymentsResponse =
    PaymentsResponse(
      payments = Seq(
        Payment(
          taxPeriod = Some(TaxPeriod(from = "2017-1-1", to = "2017-12-31")),
          `type` = "VAT Return Debit Charge",
          paymentItems = Some(Seq(
            PaymentItem(amount = Some(200.00), received = Some("2017-03-12"))
          ))
        )
      )
    )

  trait Test extends MockPaymentsConnector {

    implicit val hc: HeaderCarrier = HeaderCarrier()
    implicit val ec: EndpointLogContext = EndpointLogContext("c", "ep")

    val service = new PaymentsService(
      connector = mockRetrievePaymentsConnector
    )
  }

  "service" when {
    "service call successful" must {
      "return the mapped result" in new Test {

        MockRetrievePaymentsConnector.retrievePayments(retrievePaymentsRequest)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, retrievePaymentsResponse))))

        await(service.retrievePayments(retrievePaymentsRequest)) shouldBe Right(ResponseWrapper(correlationId, retrievePaymentsResponse))
      }

      "return a 404 Not Found for an empty payments response" in new Test {

        MockRetrievePaymentsConnector.retrievePayments(retrievePaymentsRequest)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, PaymentsResponse(Seq.empty[Payment])))))

        await(service.retrievePayments(retrievePaymentsRequest)) shouldBe Left(ErrorWrapper(Some(correlationId), NotFoundError))
      }
    }

    "service call unsuccessful" must {
      "map errors according to spec" when {

        def serviceError(desErrorCode: String, error: MtdError): Unit =
          s"a $desErrorCode error is returned from the service" in new Test {

            MockRetrievePaymentsConnector.retrievePayments(retrievePaymentsRequest)
              .returns(Future.successful(Left(ResponseWrapper(correlationId, DesErrors.single(DesErrorCode(desErrorCode))))))

            await(service.retrievePayments(retrievePaymentsRequest)) shouldBe Left(ErrorWrapper(Some(correlationId), error))
          }

        val input: Seq[(String, MtdError)] = Seq(
          "INVALID_IDTYPE" -> DownstreamError,
          "INVALID_IDNUMBER" -> VrnFormatErrorDes,
          "INVALID_REGIMETYPE" -> DownstreamError,
          "INVALID_ONLYOPENITEMS" -> DownstreamError,
          "INVALID_INCLUDELOCKS" -> DownstreamError,
          "INVALID_CALCULATEACCRUEDINTEREST" -> DownstreamError,
          "INVALID_CUSTOMERPAYMENTINFORMATION" -> DownstreamError,
          "INVALID_DATEFROM" -> InvalidDateFromErrorDes,
          "INVALID_DATETO" -> InvalidDateToErrorDes,
          "NOT_FOUND" -> LegacyNotFoundError,
          "INVALID_DATA" -> InvalidDataError,
          "SERVER_ERROR" -> DownstreamError,
          "SERVICE_UNAVAILABLE" -> DownstreamError
        )

        input.foreach(args => (serviceError _).tupled(args))
      }
    }
  }
}
