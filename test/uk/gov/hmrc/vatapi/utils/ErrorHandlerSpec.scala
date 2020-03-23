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

import javax.inject.Provider
import org.joda.time.DateTime
import org.scalamock.scalatest.MockFactory
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.libs.json.Json
import play.api.mvc.AnyContentAsEmpty
import play.api.routing.Router
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.api.{Configuration, Environment, OptionalSourceMapper}
import uk.gov.hmrc.http.NotImplementedException
import uk.gov.hmrc.play.audit.http.connector.AuditConnector
import uk.gov.hmrc.play.audit.model.DataEvent
import uk.gov.hmrc.vatapi.UnitSpec
import uk.gov.hmrc.vatapi.models.{ErrorBadRequest, ErrorCode, ErrorNotImplemented}

import scala.concurrent.ExecutionContext.Implicits.global

class ErrorHandlerSpec extends UnitSpec with GuiceOneAppPerSuite  with MockFactory{

  def versionHeader: (String, String) = ACCEPT -> s"application/vnd.hmrc.1.0+json"

  class Test() {
    val method = "some-method"

    val requestHeader: FakeRequest[AnyContentAsEmpty.type] = FakeRequest().withHeaders(versionHeader)

    val env: Environment = mock[Environment]
    val sourceMapper: OptionalSourceMapper = mock[OptionalSourceMapper]
    val provider: Provider[Router] = mock[Provider[Router]]


    val configuration = Configuration("appName" -> "myApp")
    val handler = new ErrorHandler(env, configuration, sourceMapper, provider)
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

        private val result = handler.onServerError(requestHeader, new Throwable("test", new NotImplementedException("test")))
        status(result) shouldBe NOT_IMPLEMENTED

        contentAsJson(result) shouldBe Json.toJson(ErrorNotImplemented)
      }
    }

    "return the result as standard for any other cases" when {
      "a any 5xx is thrown" in new Test() {

        private val result = handler.onServerError(requestHeader, new Throwable("any other exception", new NullPointerException("null pointer")))

        status(result) shouldBe 500
      }
    }
  }
}

