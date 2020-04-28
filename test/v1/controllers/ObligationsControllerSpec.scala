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

import play.api.libs.json.{JsValue, Json}
import play.api.mvc.Result
import uk.gov.hmrc.domain.Vrn
import uk.gov.hmrc.http.HeaderCarrier

import v1.mocks.requestParsers.MockObligationRequestParser
import v1.mocks.services.{MockAuditService, MockEnrolmentsAuthService, MockObligationService}

import v1.models.errors.{DownstreamError, EmptyNotFoundError, ErrorWrapper, InvalidDateFromErrorDes, InvalidDateToErrorDes, InvalidStatusErrorDes, InvalidFromError, InvalidInputDataError, InvalidStatusError, InvalidToError, MtdError, RuleDateRangeTooLargeError, VrnFormatError, VrnFormatErrorDes}
import v1.models.outcomes.ResponseWrapper
import v1.models.request.obligations.{ObligationsRawData, ObligationsRequest}
import v1.models.response.obligations.{Obligation, ObligationsResponse}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class ObligationsControllerSpec extends ControllerBaseSpec
  with MockEnrolmentsAuthService
  with MockObligationService
  with MockObligationRequestParser
  with MockAuditService {

  trait Test {
    val hc: HeaderCarrier = HeaderCarrier()

    val controller: ObligationsController = new ObligationsController (
      mockEnrolmentsAuthService,
      mockObligationRequestParser,
      mockObligationsService,
      cc
    )

    MockEnrolmentsAuthService.authoriseUser()
  }

  val vrn: String = "123456789"
  val from: String = "2017-01-01"
  val to: String = "2017-03-31"
  val obligationStatus: String = "F"
  val correlationId: String = "X-ID"

  val retrieveObligationsRawData: ObligationsRawData =
    ObligationsRawData(
      vrn, Some(from), Some(to), Some(obligationStatus)
    )

  val retrieveObligationsRequest: ObligationsRequest =
    ObligationsRequest(
      vrn = Vrn(vrn),  Some(from), Some(to), Some(obligationStatus)
    )

  val desJson: JsValue = Json.parse(
    s"""
       |{
       |   "obligations":[
       |      {
       |         "identification":{
       |            "referenceNumber":"123456789",
       |            "referenceType":"VRN"
       |         },
       |         "obligationDetails":[
       |            {
       |               "status":"F",
       |               "inboundCorrespondenceFromDate":"2017-01-01",
       |               "inboundCorrespondenceToDate":"2017-03-31",
       |               "inboundCorrespondenceDateReceived":"2017-05-06",
       |               "inboundCorrespondenceDueDate":"2017-05-07",
       |               "periodKey":"18A1"
       |            },
       |            {
       |               "status":"O",
       |               "inboundCorrespondenceFromDate":"2017-04-01",
       |               "inboundCorrespondenceToDate":"2017-06-30",
       |               "inboundCorrespondenceDueDate":"2017-08-07",
       |               "periodKey":"18A2"
       |            }
       |         ]
       |      }
       |   ]
       |}
       |""".stripMargin
  )
  val mtdJson: JsValue = Json.parse(
    s"""
       |{
       |  "obligations": [
       |    {
       |      "start": "2017-01-01",
       |      "end": "2017-03-31",
       |      "due": "2017-05-07",
       |      "status": "F",
       |      "periodKey": "18A1",
       |      "received": "2017-05-06"
       |    },
       |    {
       |      "start": "2017-04-01",
       |      "end": "2017-06-30",
       |      "due": "2017-08-07",
       |      "status": "O",
       |      "periodKey": "18A2"
       |    }
       |  ]
       |}
       |""".stripMargin
  )

  val obligationsResponse: ObligationsResponse =
    ObligationsResponse(Seq(
      Obligation(
        start = "2017-01-01",
        end = "2017-03-31",
        due = "2017-05-07",
        status = "F",
        periodKey = "18A1",
        received = Some("2017-05-06")
      ),
      Obligation(
        start = "2017-04-01",
        end =  "2017-06-30",
        due = "2017-08-07",
        status = "O",
        periodKey = "18A2",
        received = None
      )
    )
    )

  "obligations" when {
    "a valid request is supplied" should {
      "return the expected data on a successful service call" in new Test {

        MockObligationRequestParser
          .parse(retrieveObligationsRawData)
          .returns(Right(retrieveObligationsRequest))

        MockObligationService
          .receiveObligations(retrieveObligationsRequest)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, obligationsResponse))))

        private val result = controller.retrieveObligations(vrn, Some(from), Some(to), Some(obligationStatus))(fakeGetRequest)

        status(result) shouldBe OK
        contentAsJson(result) shouldBe mtdJson
        header("X-CorrelationId", result) shouldBe Some(correlationId)

      }
    }

    "return the error as per spec" when {
      "parser errors occur" must {
        def errorsFromParserTester(error: MtdError, expectedStatus: Int): Unit = {
          s"a ${error.code} error is returned from the parser" in new Test {

            MockObligationRequestParser
              .parse(retrieveObligationsRawData)
              .returns(Left(ErrorWrapper(Some(correlationId), error, None)))

            val result: Future[Result] = controller.retrieveObligations(vrn, Some(from), Some(to), Some(obligationStatus))(fakeGetRequest)

            status(result) shouldBe expectedStatus
            contentAsJson(result) shouldBe Json.toJson(error)
            header("X-CorrelationId", result) shouldBe Some(correlationId)

          }
        }

        val input = Seq(
          (VrnFormatError, BAD_REQUEST),
          (InvalidFromError, BAD_REQUEST),
          (InvalidToError, BAD_REQUEST),
          (InvalidStatusError, BAD_REQUEST),
          (RuleDateRangeTooLargeError, BAD_REQUEST)
        )

        input.foreach(args => (errorsFromParserTester _).tupled(args))
      }

      "service errors occur" must {
        def serviceErrors(mtdError: MtdError, expectedStatus: Int): Unit = {
          s"a $mtdError error is returned from the service" in new Test {

            MockObligationRequestParser
              .parse(retrieveObligationsRawData)
              .returns(Right(retrieveObligationsRequest))

            MockObligationService
              .receiveObligations(retrieveObligationsRequest)
              .returns(Future.successful(Left(ErrorWrapper(Some(correlationId), mtdError))))

            val result: Future[Result] = controller.retrieveObligations(vrn, Some(from), Some(to), Some(obligationStatus))(fakeGetRequest)

            status(result) shouldBe expectedStatus
            contentAsJson(result) shouldBe Json.toJson(mtdError)
            header("X-CorrelationId", result) shouldBe Some(correlationId)

          }
        }

        val input = Seq(
          (VrnFormatErrorDes, BAD_REQUEST),
          (InvalidDateFromErrorDes, BAD_REQUEST),
          (InvalidDateToErrorDes, BAD_REQUEST),
          (InvalidStatusErrorDes, BAD_REQUEST),
          (RuleDateRangeTooLargeError, BAD_REQUEST),
          (InvalidInputDataError, FORBIDDEN),
          (DownstreamError, INTERNAL_SERVER_ERROR)
        )

        input.foreach(args => (serviceErrors _).tupled(args))
      }

      "a NOT_FOUND error is returned from the service" must {
        s"return a 404 status with an empty body" in new Test {

          MockObligationRequestParser
            .parse(retrieveObligationsRawData)
            .returns(Right(retrieveObligationsRequest))

          MockObligationService
            .receiveObligations(retrieveObligationsRequest)
            .returns(Future.successful(Left(ErrorWrapper(Some(correlationId), EmptyNotFoundError))))

          val result: Future[Result] = controller.retrieveObligations(vrn, Some(from), Some(to), Some(obligationStatus))(fakeGetRequest)

          status(result) shouldBe NOT_FOUND
          contentAsString(result) shouldBe ""
          header("X-CorrelationId", result) shouldBe Some(correlationId)

        }
      }
    }
  }
}
