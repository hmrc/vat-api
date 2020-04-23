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

import org.joda.time.DateTime
import support.UnitSpec
import uk.gov.hmrc.domain.Vrn
import uk.gov.hmrc.http.HeaderCarrier
import utils.EndpointLogContext
import v1.mocks.connectors.MockSubmitReturnConnector
import v1.models.errors._
import v1.models.outcomes.ResponseWrapper
import v1.models.request.submit.{SubmitRequest, SubmitRequestBody}
import v1.models.response.submit.SubmitResponse

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class SubmitReturnServiceSpec extends UnitSpec {

  private val vrn: String = "123456789"
  private val correlationId = "X-123"

  private val submitRequestBody: SubmitRequestBody =
    SubmitRequestBody(
      periodKey = "F034",
      vatDueSales = 4567.23,
      vatDueAcquisitions = -456675.5,
      totalVatDue = 7756.65,
      vatReclaimedCurrPeriod = -756822354.64,
      netVatDue = 8956743245.12,
      totalValueSalesExVAT = 43556767890.00,
      totalValuePurchasesExVAT = 34556790.00,
      totalValueGoodsSuppliedExVAT = 34556.00,
      totalAcquisitionsExVAT = -68978.00,
      finalised = true
    )

  private val submitReturnRequest: SubmitRequest =
    SubmitRequest(
      vrn = Vrn(vrn),
      body = submitRequestBody
    )

  val submitReturnResponse: SubmitResponse = SubmitResponse(processingDate = new DateTime("2018-01-16T08:20:27.895+0000"),
    paymentIndicator = Some("BANK"),
    formBundleNumber = "256660290587",
    chargeRefNumber = Some("aCxFaNx0FZsCvyWF"))

  trait Test extends MockSubmitReturnConnector {

    implicit val hc: HeaderCarrier = HeaderCarrier()
    implicit val ec: EndpointLogContext = EndpointLogContext("SubmitReturnService", "submitReturn")

    val service = new SubmitReturnService(
      connector = mockSubmitReturnConnector
    )
  }

  "service" when {
    "service call successful" must {
      "return the mapped result" in new Test {

        MockSubmitReturnConnector.submitReturn(submitReturnRequest)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, submitReturnResponse))))

        await(service.viewReturn(submitReturnRequest)) shouldBe Right(ResponseWrapper(correlationId, submitReturnResponse))
      }
    }

    "service call unsuccessful" must {
      "map errors according to spec" when {

        def serviceError(desErrorCode: String, error: MtdError): Unit =
          s"a $desErrorCode error is returned from the service" in new Test {

            MockSubmitReturnConnector.submitReturn(submitReturnRequest)
              .returns(Future.successful(Left(ResponseWrapper(correlationId, DesErrors.single(DesErrorCode(desErrorCode))))))

            await(service.viewReturn(submitReturnRequest)) shouldBe Left(ErrorWrapper(Some(correlationId), error))
          }

        val input: Seq[(String, MtdError)] = Seq(
          ("INVALID_VRN", VrnFormatErrorDes),
          ("INVALID_PERIODKEY", PeriodKeyFormatErrorDes),
          ("INVALID_PAYLOAD", BadRequestError),
          ("TAX_PERIOD_NOT_ENDED", TaxPeriodNotEnded),
          ("DUPLICATE_SUBMISSION", DuplicateVatSubmission),
          ("NOT_FOUND_VRN", DownstreamError),
          ("INVALID_SUBMISSION", DownstreamError),
          ("INVALID_ORIGINATOR_ID", DownstreamError),
          ("SERVER_ERROR", DownstreamError),
          ("SERVICE_UNAVAILABLE", DownstreamError)
        )

        input.foreach(args => (serviceError _).tupled(args))
      }
    }
  }
}
