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

package v1.services

import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import v1.controllers.UserRequest
import v1.mocks.connectors.MockSubmitReturnConnector
import v1.models.auth.UserDetails
import v1.models.domain.Vrn
import v1.models.errors._
import v1.models.outcomes.ResponseWrapper
import v1.models.request.submit.{SubmitRequest, SubmitRequestBody}
import v1.models.response.submit.SubmitResponse

import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import scala.concurrent.Future

class SubmitReturnServiceSpec extends ServiceSpec {

  implicit val userRequest: UserRequest[AnyContentAsEmpty.type] = UserRequest(UserDetails("Individual",None,"id"),FakeRequest())
  private val vrn: String = "123456789"

  private val submitRequestBody: SubmitRequestBody =
    SubmitRequestBody(
      periodKey = Some("F034"),
      vatDueSales = Some(4567.23),
      vatDueAcquisitions = Some(-456675.5),
      totalVatDue = Some(7756.65),
      vatReclaimedCurrPeriod = Some(-756822354.64),
      netVatDue = Some(8956743245.12),
      totalValueSalesExVAT = Some(43556767890.00),
      totalValuePurchasesExVAT = Some(34556790.00),
      totalValueGoodsSuppliedExVAT = Some(34556.00),
      totalAcquisitionsExVAT = Some(-68978.00),
      finalised = Some(true),
      receivedAt = None,
      agentReference = None
    )

  private val submitReturnRequest: SubmitRequest =
    SubmitRequest(
      vrn = Vrn(vrn),
      body = submitRequestBody
    )
  val dateTimePattern = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
  val submitReturnResponse: SubmitResponse = SubmitResponse(processingDate = OffsetDateTime.parse("2018-01-16T08:20:27.895Z"),
    paymentIndicator = Some("BANK"),
    formBundleNumber = "256660290587",
    chargeRefNumber = Some("aCxFaNx0FZsCvyWF"))

  trait Test extends MockSubmitReturnConnector {

    val service = new SubmitReturnService(
      connector = mockSubmitReturnConnector
    )
  }

  "service" when {
    "service call successful" must {
      "return the mapped result" in new Test {

        MockSubmitReturnConnector.submitReturn(submitReturnRequest)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, submitReturnResponse))))

        await(service.submitReturn(submitReturnRequest)) shouldBe Right(ResponseWrapper(correlationId, submitReturnResponse))
      }
    }

    "service call unsuccessful" must {
      "map errors according to spec" when {

        def serviceError(desErrorCode: String, error: MtdError): Unit =
          s"a $desErrorCode error is returned from the service" in new Test {

            MockSubmitReturnConnector.submitReturn(submitReturnRequest)
              .returns(Future.successful(Left(ResponseWrapper(correlationId, DesErrors.single(DesErrorCode(desErrorCode))))))

            await(service.submitReturn(submitReturnRequest)) shouldBe Left(ErrorWrapper(correlationId, error))
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
          ("INSOLVENT_TRADER", RuleInsolventTraderError),
          ("SERVER_ERROR", DownstreamError),
          ("SERVICE_UNAVAILABLE", DownstreamError)
        )

        input.foreach(args => (serviceError _).tupled(args))
      }
    }
  }
}
