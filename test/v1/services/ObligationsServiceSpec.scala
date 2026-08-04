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
import support.LogCapturing
import utils.pagerDutyLogging.Endpoint
import v1.controllers.UserRequest
import v1.mocks.connectors.MockObligationsConnector
import v1.models.auth.UserDetails
import v1.models.domain.Vrn
import v1.models.errors._
import v1.models.outcomes.ResponseWrapper
import v1.models.request.obligations.ObligationsRequest
import v1.models.response.obligations.{ Obligation, ObligationsResponse }

import scala.concurrent.Future

class ObligationsServiceSpec extends ServiceSpec with LogCapturing {

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

  val errorCodeCases: Seq[(String, MtdError)] = Seq(
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
    ("SERVICE_UNAVAILABLE", ServiceUnavailableError),
    ("TEST_ONLY_UNMATCHED_STUB_ERROR", RuleIncorrectGovTestScenarioError)
  )

  "obligationsService" when {
    "the connector returns a successful response" must {
      "return the response" in new Test {
        private val successResponse = Right(ResponseWrapper(correlationId, obligationsResponse))

        MockObligationsConnector.retrieveObligations(obligationsRequest).returns(Future.successful(successResponse))

        await(service.retrieveObligations(obligationsRequest)) shouldBe successResponse
      }
    }

    "the connector returns a failure response" must {
      "map each DES error correctly and log the error code" when {
        errorCodeCases.foreach {
          case (errorCode, mtdError) =>
            s"the error code is' $errorCode' and the returned MTD Error should be '$mtdError' in an ErrorWrapper" in new Test {
              private val failureResponse = Left(ResponseWrapper(correlationId, DesErrors.single(DesErrorCode(errorCode))))

              MockObligationsConnector.retrieveObligations(obligationsRequest).returns(Future.successful(failureResponse))

              withCaptureOfLoggingFrom(service.logger) { logs =>
                val result = await(service.retrieveObligations(obligationsRequest))

                logs.head.getMessage should include(s"Backend returned errors: $errorCode")
                result shouldBe Left(ErrorWrapper(correlationId, mtdError))
              }
            }
        }
      }

      "handle multiple error codes, logging all codes and returning a default 'DownstreamError'" in new Test {
        private val multipleFailureResponse = Left(
          ResponseWrapper(correlationId,
                          DesErrors(errors = List(DesErrorCode("INVALID_IDTYPE"), DesErrorCode("NOT_FOUND"), DesErrorCode("DOWNSTREAM_ERROR")))))

        MockObligationsConnector.retrieveObligations(obligationsRequest).returns(Future.successful(multipleFailureResponse))

        withCaptureOfLoggingFrom(service.logger) { logs =>
          val result = await(service.retrieveObligations(obligationsRequest))

          logs.head.getMessage should include("Backend returned errors: INVALID_IDTYPE, NOT_FOUND, DOWNSTREAM_ERROR")
          result shouldBe Left(ErrorWrapper(correlationId, DownstreamError))
        }
      }
    }

    "the connector call fails and throws an exception" must {
      "log the exception message, raise a PagerDuty RETRIEVE_OBLIGATIONS_REQUEST_FAILED log, and return a 'DownstreamError'" in new Test {
        private val exceptionMessage = "test exception"
        private val exception        = new Exception(exceptionMessage)

        MockObligationsConnector.retrieveObligations(obligationsRequest).returns(Future.failed(exception))

        withCaptureOfLoggingFrom(service.logger) { logs =>
          val result = await(service.retrieveObligations(obligationsRequest))

          logs.head.getMessage should include(s"errorStatus: 500, errorMessage: Request failed with error: $exceptionMessage")
          logs(1).getMessage should include(Endpoint.RetrieveObligations.requestFailedMessage.toString)

          result shouldBe Left(ErrorWrapper(correlationId, DownstreamError))
        }
      }
    }
  }

}
