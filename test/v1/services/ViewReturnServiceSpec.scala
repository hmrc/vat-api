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
import v1.mocks.connectors.MockViewReturnConnector
import v1.models.errors._
import v1.models.outcomes.ResponseWrapper
import v1.models.request.viewReturn.ViewRequest
import v1.models.response.viewReturn.ViewReturnResponse

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class ViewReturnServiceSpec extends UnitSpec {

  private val vrn: String = "123456789"
  private val periodKey: String = "F034"
  private val correlationId = "X-123"

  private val viewReturnRequest: ViewRequest =
    ViewRequest(
      vrn = Vrn(vrn),
      periodKey = periodKey
    )

  private val viewReturnResponse: ViewReturnResponse =
    ViewReturnResponse(
      periodKey = "F034",
      vatDueSales = 4567.23,
      vatDueAcquisitions = -456675.5,
      totalVatDue = 7756.65,
      vatReclaimedCurrPeriod = -756822354.64,
      netVatDue = 8956743245.12,
      totalValueSalesExVAT = 43556767890.00,
      totalValuePurchasesExVAT = 34556790.00,
      totalValueGoodsSuppliedExVAT = 34556.00,
      totalAcquisitionsExVAT = -68978.00
    )

  trait Test extends MockViewReturnConnector {

    implicit val hc: HeaderCarrier = HeaderCarrier()
    implicit val ec: EndpointLogContext = EndpointLogContext("c", "ep")

    val service = new ViewReturnService(
      connector = mockViewReturnConnector
    )
  }

  "service" when {
    "service call successful" must {
      "return the mapped result" in new Test {

        MockViewReturnConnector.viewReturn(viewReturnRequest)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, viewReturnResponse))))

        await(service.viewReturn(viewReturnRequest)) shouldBe Right(ResponseWrapper(correlationId, viewReturnResponse))
      }
    }

    "service call unsuccessful" must {
      "map errors according to spec" when {

        def serviceError(desErrorCode: String, error: MtdError): Unit =
          s"a $desErrorCode error is returned from the service" in new Test {

            MockViewReturnConnector.viewReturn(viewReturnRequest)
              .returns(Future.successful(Left(ResponseWrapper(correlationId, DesErrors.single(DesErrorCode(desErrorCode))))))

            await(service.viewReturn(viewReturnRequest)) shouldBe Left(ErrorWrapper(Some(correlationId), error))
          }

        val input: Seq[(String, MtdError)] = Seq(
          ("INVALID_VRN", VrnFormatErrorDes),
          ("INVALID_PERIODKEY", PeriodKeyFormatErrorDes),
          ("INVALID_IDENTIFIER", PeriodKeyFormatErrorDesNotFound),
          ("NOT_FOUND_VRN", DownstreamError),
          ("INVALID_INPUTDATA", InvalidInputDataError),
          ("DATE_RANGE_TOO_LARGE", RuleDateRangeTooLargeError),
          ("NOT_FOUND", EmptyNotFoundError),
          ("SERVER_ERROR", DownstreamError),
          ("SERVICE_UNAVAILABLE", DownstreamError)
        )

        input.foreach(args => (serviceError _).tupled(args))
      }
    }
  }
}
