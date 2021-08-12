/*
 * Copyright 2021 HM Revenue & Customs
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
import v1.models.domain.Vrn
import v1.controllers.UserRequest
import v1.mocks.connectors.MockObligationsConnector
import v1.models.auth.UserDetails
import v1.models.errors._
import v1.models.outcomes.ResponseWrapper
import v1.models.request.obligations.ObligationsRequest
import v1.models.response.obligations.{ Obligation, ObligationsResponse }

import scala.concurrent.Future

class ObligationsServiceSpec extends ServiceSpec {

  implicit val userRequest: UserRequest[AnyContentAsEmpty.type] = UserRequest(UserDetails("Individual", None, "id"), FakeRequest())
  private val vrn: String                                       = "123456789"

  private val obligationsRequest: ObligationsRequest =
    ObligationsRequest(
      vrn = Vrn(vrn),
      from = Some("2018-06-04"),
      to = Some("2018-06-04"),
      status = Some("O")
    )

  private val obligationsResponse: ObligationsResponse =
    ObligationsResponse(
      Seq(
        Obligation(
          periodKey = "18A2",
          start = "2018-04-06",
          end = "2018-04-05",
          due = "2017-05-05",
          status = "O",
          received = None
        )
      ))

  trait Test extends MockObligationsConnector {

    val service = new ObligationsService(
      connector = mockObligationsConnector
    )
  }

  "obligationsService" when {
    "service call successful" must {
      "return the mapped result" in new Test {

        MockObligationsConnector
          .retrieveObligations(obligationsRequest)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, obligationsResponse))))

        await(service.retrieveObligations(obligationsRequest)) shouldBe Right(ResponseWrapper(correlationId, obligationsResponse))
      }
    }

    "service call unsuccessful" must {
      "map errors according to spec" when {

        def serviceError(desErrorCode: String, error: MtdError): Unit =
          s"a $desErrorCode error is returned from the service" in new Test {

            MockObligationsConnector
              .retrieveObligations(obligationsRequest)
              .returns(Future.successful(Left(ResponseWrapper(correlationId, DesErrors.single(DesErrorCode(desErrorCode))))))

            await(service.retrieveObligations(obligationsRequest)) shouldBe Left(ErrorWrapper(correlationId, error))
          }

        val input: Seq[(String, MtdError)] = Seq(
          ("INVALID_IDTYPE", DownstreamError),
          ("INVALID_IDNUMBER", VrnFormatErrorDes),
          ("INVALID_REGIME", DownstreamError),
          ("NOT_FOUND_BPKEY", DownstreamError),
          ("NOT_FOUND", LegacyNotFoundError),
          ("INVALID_STATUS", InvalidStatusErrorDes),
          ("INVALID_DATE_FROM", InvalidDateFromErrorDes),
          ("INVALID_DATE_TO", InvalidDateToErrorDes),
          ("INVALID_DATE_RANGE", RuleOBLDateRangeTooLargeError),
          ("INSOLVENT_TRADER", RuleInsolventTraderError),
          ("SERVER_ERROR", DownstreamError),
          ("SERVICE_UNAVAILABLE", DownstreamError)
        )

        input.foreach(args => (serviceError _).tupled(args))
      }
    }
  }
}
