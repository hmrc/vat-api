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

package v1.endpoints

import com.github.tomakehurst.wiremock.stubbing.StubMapping
import play.api.http.HeaderNames.ACCEPT
import play.api.http.Status._
import play.api.libs.json.{JsValue, Json}
import play.api.libs.ws.WSRequest
import play.api.test.Helpers.AUTHORIZATION
import support.IntegrationBaseSpec
import v1.fixtures.ViewReturnFixture
import v1.models.errors._
import v1.stubs.{AuditStub, AuthStub, DesStub}


class ViewReturnControllerISpec extends IntegrationBaseSpec with ViewReturnFixture {

  private trait Test {

    val vrn: String = "123456789"
    val periodKey: String = "A1A2"
    val correlationId: String = "X-ID"

    val desJson: JsValue = viewReturnDesJson
    val mtdJson: JsValue = viewReturnMtdJson

    def uri: String = s"/$vrn/returns/$periodKey"
    def desUrl: String = s"/vat/returns/vrn/$vrn"

    val queryParams: Map[String, String] = Map("period-key" -> periodKey)

    def setupStubs(): StubMapping

    def request: WSRequest = {
      setupStubs()
      buildRequest(uri)
        .withHttpHeaders(
          (ACCEPT, "application/vnd.hmrc.1.0+json"),
          (AUTHORIZATION, "Bearer 123")
        )
    }

    def errorBody(code: String): String =
      s"""
         |      {
         |        "code": "$code",
         |        "reason": "des message"
         |      }
    """.stripMargin
  }

  "Making a request to the View VAT Return endpoint" should {
    "return a 200 status code with expected body" when {
      "a valid request is made" in new Test{

        override def setupStubs(): StubMapping = {
          AuditStub.audit()
          AuthStub.authorised()
          DesStub.onSuccess(GET, desUrl, queryParams,  OK, desJson)
        }

        private val response = await(request.get)
        response.status shouldBe OK
        response.json shouldBe mtdJson
        response.header("Content-Type") shouldBe Some("application/json")


      }
    }

    "return a 500 status code with expected body" when {
      "des returns multiple errors" in new Test{

        val multipleErrors: String =
          """
            |{
            |   "failures": [
            |        {
            |            "code": "INVALID_VRN",
            |            "reason": "Submission has not passed validation. Invalid parameter idType/idValue."
            |        },
            |        {
            |            "code": "INVALID_PERIODKEY",
            |            "reason": "Submission has not passed validation. Invalid parameter period-key."
            |        }
            |    ]
            |}
          """.stripMargin

        override def setupStubs(): StubMapping = {
          AuditStub.audit()
          AuthStub.authorised()
          DesStub.onError(GET, desUrl, queryParams, BAD_REQUEST, multipleErrors)
        }

        private val response = await(request.get)
        response.status shouldBe INTERNAL_SERVER_ERROR
        response.json shouldBe Json.toJson(DownstreamError)
        response.header("Content-Type") shouldBe Some("application/json")
      }
    }

    "return a 500 status code" when {
      "downstream not available" in new Test{

        val multipleErrors: String =
          """
            |{
            |   "failures": [
            |        {
            |            "code": "INTERNAL_SERVER_ERROR",
            |            "reason": "An internal server error occurred"
            |        }
            |    ]
            |}
          """.stripMargin

        override def setupStubs(): StubMapping = {
          AuditStub.audit()
          AuthStub.authorised()
          DesStub.onError(GET, desUrl, queryParams, BAD_REQUEST, multipleErrors)
        }

        private val response = await(request.get)
        response.status shouldBe INTERNAL_SERVER_ERROR
        response.json shouldBe Json.toJson(DownstreamError)
        response.header("Content-Type") shouldBe Some("application/json")
      }
    }

    "return error according to spec" when {

      def validationErrorTest(requestVrn: String, requestPeriodKey: String,
                              expectedStatus: Int, expectedBody: MtdError): Unit = {
        s"validation fails with ${expectedBody.code} error" in new Test {

          override val vrn: String = requestVrn
          override val periodKey: String = requestPeriodKey

          override def setupStubs(): StubMapping = {
            AuditStub.audit()
            AuthStub.authorised()
          }

          private val response = await(request.get)
          response.status shouldBe expectedStatus
          response.json shouldBe Json.toJson(expectedBody)
          response.header("Content-Type") shouldBe Some("application/json")
        }
      }

      val input = Seq(
        ("badVrn", "AA11", BAD_REQUEST, VrnFormatError),
        ("123456789", "badPeriodKey", BAD_REQUEST, PeriodKeyFormatError)
      )

      input.foreach(args => (validationErrorTest _).tupled(args))
    }

    "des service error" when {
      def serviceErrorTest(desStatus: Int, desCode: String, expectedStatus: Int, expectedBody: MtdError): Unit = {
        s"des returns an $desCode error and status $desStatus" in new Test {

          override def setupStubs(): StubMapping = {
            AuditStub.audit()
            AuthStub.authorised()
            DesStub.onError(GET, desUrl, queryParams, desStatus, errorBody(desCode))
          }

          private val response = await(request.get)
          response.status shouldBe expectedStatus
          response.json shouldBe Json.toJson(expectedBody)
          response.header("Content-Type") shouldBe Some("application/json")
        }
      }

      val input = Seq(
        (BAD_REQUEST, "INVALID_VRN", BAD_REQUEST, VrnFormatErrorDes),
        (BAD_REQUEST, "INVALID_PERIODKEY", BAD_REQUEST, PeriodKeyFormatErrorDes),
        (FORBIDDEN, "INVALID_IDENTIFIER", NOT_FOUND, PeriodKeyFormatErrorDesNotFound),
        (FORBIDDEN, "NOT_FOUND_VRN", INTERNAL_SERVER_ERROR, DownstreamError),
        (FORBIDDEN, "INVALID_INPUTDATA", FORBIDDEN, InvalidInputDataError),
        (FORBIDDEN, "DATE_RANGE_TOO_LARGE", FORBIDDEN, RuleDateRangeTooLargeError),
        (FORBIDDEN, "INSOLVENT_TRADER", FORBIDDEN, RuleInsolventTraderError),
        (BAD_REQUEST, "SERVER_ERROR", INTERNAL_SERVER_ERROR, DownstreamError),
        (SERVICE_UNAVAILABLE, "SERVICE_UNAVAILABLE", INTERNAL_SERVER_ERROR, DownstreamError),
        (BAD_REQUEST, "TEST_ONLY_UNMATCHED_STUB_ERROR", BAD_REQUEST, RuleIncorrectGovTestScenarioError)
      )

      input.foreach(args => (serviceErrorTest _).tupled(args))
    }

    "return a 404 status with empty body" when {
        s"des returns a 404 NOT_FOUND error" in new Test {

          override def setupStubs(): StubMapping = {
            AuditStub.audit()
            AuthStub.authorised()
            DesStub.onError(GET, desUrl, queryParams, NOT_FOUND, errorBody("NOT_FOUND"))
          }

          private val response = await(request.get)
          response.status shouldBe NOT_FOUND
          response.body shouldBe ""
          response.header("Content-Type") shouldBe None
        }
      }
  }
}
