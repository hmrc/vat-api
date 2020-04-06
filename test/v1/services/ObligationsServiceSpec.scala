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
import v1.mocks.connectors.MockObligationsConnector
import v1.models.errors._
import v1.models.outcomes.ResponseWrapper
import v1.models.request.obligations.ObligationsRequest
import v1.models.response.obligations.{Obligation, ObligationsResponse}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class ObligationsServiceSpec extends UnitSpec {

  private val vrn: String = "123456789"
  private val correlationId = "X-123"

  private val obligationsRequest: ObligationsRequest =
    ObligationsRequest(
      vrn = Vrn(vrn),
      from = Some("2018-06-04"),
      to = Some("2018-06-04"),
      status = Some("O")
    )

  private val obligationsResponse: ObligationsResponse =
    ObligationsResponse(Seq(
      Obligation(
        periodKey = "18A2",
        start = "2018-04-06",
        end = "2018-04-05",
        due = "2017-05-05",
        status = "O",
        received  = None
      )
    ))

  trait Test extends MockObligationsConnector {

    implicit val hc: HeaderCarrier = HeaderCarrier()
    implicit val ec: EndpointLogContext = EndpointLogContext("c", "ep")

    val service = new ObligationsService(
      connector = mockObligationsConnector
    )
  }

  "obligationsService" when {
    "service call successful" must {
      "return the mapped result" in new Test {

        MockObligationsConnector.retrieveObligations(obligationsRequest)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, obligationsResponse))))

        await(service.retrieveObligations(obligationsRequest)) shouldBe Right(ResponseWrapper(correlationId, obligationsResponse))
      }
    }

    "service call unsuccessful" must {
      "map errors according to spec" when {

        def serviceError(desErrorCode: String, error: MtdError): Unit =
          s"a $desErrorCode error is returned from the service" in new Test {

            MockObligationsConnector.retrieveObligations(obligationsRequest)
              .returns(Future.successful(Left(ResponseWrapper(correlationId, DesErrors.single(DesErrorCode(desErrorCode))))))

            await(service.retrieveObligations(obligationsRequest)) shouldBe Left(ErrorWrapper(Some(correlationId), error))
          }

        val input: Seq[(String, MtdError)] = Seq(
          ("INVALID_IDTYPE", DownstreamError),
          ("INVALID_IDNUMBER", VrnFormatError),
          ("INVALID_REGIME", DownstreamError),
          ("NOT_FOUND_BPKEY", DownstreamError),
          ("NOT_FOUND", LegacyNotFoundError),
          ("INVALID_STATUS", LegacyInvalidStatusError),
          ("INVALID_DATE_FROM", LegacyInvalidDateFromError),
          ("INVALID_DATE_TO", LegacyInvalidDateToError),
          ("INVALID_DATE_RANGE", LegacyInvalidDateRangeError),
          ("SERVER_ERROR", DownstreamError),
          ("SERVICE_UNAVAILABLE", DownstreamError)
        )

        input.foreach(args => (serviceError _).tupled(args))
      }
    }
  }
}
