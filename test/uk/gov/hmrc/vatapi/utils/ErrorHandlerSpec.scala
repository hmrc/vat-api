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

package uk.gov.hmrc.vatapi.utils

import org.joda.time.DateTime
import org.scalamock.scalatest.MockFactory
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Configuration
import play.api.libs.json.Json
import play.api.mvc.{AnyContentAsEmpty, RequestHeader}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.http.{HeaderCarrier, NotImplementedException}
import uk.gov.hmrc.play.audit.http.connector.AuditConnector
import uk.gov.hmrc.play.audit.http.connector.AuditResult.Success
import uk.gov.hmrc.play.audit.model.DataEvent
import uk.gov.hmrc.play.bootstrap.config.HttpAuditEvent
import uk.gov.hmrc.vatapi.UnitSpec
import uk.gov.hmrc.vatapi.models.{ErrorBadRequest, ErrorCode, ErrorNotImplemented}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionContext, Future}

class ErrorHandlerSpec extends UnitSpec with GuiceOneAppPerSuite  with MockFactory{

  def versionHeader: (String, String) = ACCEPT -> s"application/vnd.hmrc.1.0+json"

  class Test() {
    val method = "some-method"

    val requestHeader: FakeRequest[AnyContentAsEmpty.type] = FakeRequest().withHeaders(versionHeader)

    val auditConnector: AuditConnector = mock[AuditConnector]
    val httpAuditEvent: HttpAuditEvent = mock[HttpAuditEvent]

    val eventTags: Map[String, String] = Map("transactionName" -> "event.transactionName")

    val dataEvent = DataEvent(
      auditSource = "auditSource",
      auditType = "event.auditType",
      eventId = "",
      tags = eventTags,
      detail = Map("test" -> "test"),
      generatedAt = DateTime.now()
    )

    val configuration = Configuration("appName" -> "myApp")
    val handler = new ErrorHandler(configuration, auditConnector, httpAuditEvent)
  }

  "onClientError" should {

    "return 400 with ERROR_VRN_INVALID error message" when {
      "invalid vrn error occurred" in new Test() {
        private val result = handler.onClientError(requestHeader, BAD_REQUEST, "ERROR_VRN_INVALID")
        status(result) shouldBe BAD_REQUEST

        contentAsJson(result) shouldBe Json.toJson(ErrorBadRequest(ErrorCode.VRN_INVALID, "The provided Vrn is invalid"))
      }
    }

    "return 400 with ERROR_INVALID_DATE error message" when {
      "invalid vrn error occurred" in new Test() {
        private val result = handler.onClientError(requestHeader, BAD_REQUEST, "ERROR_INVALID_DATE")
        status(result) shouldBe BAD_REQUEST

        contentAsJson(result) shouldBe Json.toJson(ErrorBadRequest(ErrorCode.INVALID_DATE, "The provided date is invalid"))
      }
    }

    "return 400 with ERROR_INVALID_FROM_DATE error message" when {
      "invalid vrn error occurred" in new Test() {
        private val result = handler.onClientError(requestHeader, BAD_REQUEST, "ERROR_INVALID_FROM_DATE")
        status(result) shouldBe BAD_REQUEST

        contentAsJson(result) shouldBe Json.toJson(ErrorBadRequest(ErrorCode.INVALID_FROM_DATE, "The provided from date is invalid"))
      }
    }

    "return 400 with ERROR_INVALID_TO_DATE error message" when {
      "invalid vrn error occurred" in new Test() {
        private val result = handler.onClientError(requestHeader, BAD_REQUEST, "ERROR_INVALID_TO_DATE")
        status(result) shouldBe BAD_REQUEST

        contentAsJson(result) shouldBe Json.toJson(ErrorBadRequest(ErrorCode.INVALID_TO_DATE, "The provided to date is invalid"))
      }
    }

    "return 400 with INVALID_STATUS error message" when {
      "invalid vrn error occurred" in new Test() {
        private val result = handler.onClientError(requestHeader, BAD_REQUEST, "INVALID_STATUS")
        status(result) shouldBe BAD_REQUEST

        contentAsJson(result) shouldBe Json.toJson(Json.obj("statusCode" -> 400, "message" -> "INVALID_STATUS"))
      }
    }

    "return 400 with INVALID_DATE_RANGE error message" when {
      "invalid vrn error occurred" in new Test() {
        private val result = handler.onClientError(requestHeader, BAD_REQUEST, "INVALID_DATE_RANGE")
        status(result) shouldBe BAD_REQUEST

        contentAsJson(result) shouldBe Json.toJson(Json.obj("statusCode" -> 400, "message" -> "INVALID_DATE_RANGE"))
      }
    }

    "return 400 with unmatchedError error message" when {
      "invalid vrn error occurred" in new Test() {
        private val result = handler.onClientError(requestHeader, BAD_REQUEST, "INVALID_PAYLOAD")
        status(result) shouldBe BAD_REQUEST

        contentAsJson(result) shouldBe Json.toJson(Json.obj("statusCode" -> 400,
          "message" -> JsonErrorSanitiser.sanitise("INVALID_PAYLOAD")))
      }
    }
  }

  "onServerError" should {

    "return NotImplemented with error body and NOT_IMPLEMENTED status code" when {
      "NotImplementedException is thrown" in new Test() {

        (httpAuditEvent.dataEvent(_: String, _: String, _: RequestHeader, _: Map[String, String])(_: HeaderCarrier)).expects(*, *, *, *, *)
          .returns(dataEvent)

        (auditConnector.sendEvent(_ : DataEvent)(_: HeaderCarrier, _: ExecutionContext)).expects(*, *, *)
          .returns(Future.successful(Success))
        private val result = handler.onServerError(requestHeader, new Throwable("test", new NotImplementedException("test")))
        status(result) shouldBe NOT_IMPLEMENTED

        contentAsJson(result) shouldBe Json.toJson(ErrorNotImplemented)
      }
    }

    "return NotImplemented with error body" when {
      "NotImplementedException is thrown" in new Test() {

        (httpAuditEvent.dataEvent(_: String, _: String, _: RequestHeader, _: Map[String, String])(_: HeaderCarrier)).expects(*, *, *, *, *)
          .returns(dataEvent)

        (auditConnector.sendEvent(_ : DataEvent)(_: HeaderCarrier, _: ExecutionContext)).expects(*, *, *)
          .returns(Future.successful(Success))
        private val result = handler.onServerError(requestHeader, new NotImplementedException("test"))
        status(result) shouldBe NOT_IMPLEMENTED

        contentAsJson(result) shouldBe Json.parse("""{"statusCode":501,"message":"test"}""".stripMargin)
      }
    }
  }
}

