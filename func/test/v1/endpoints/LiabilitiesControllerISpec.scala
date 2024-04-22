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
import play.api.libs.json.Json
import play.api.libs.ws.WSRequest
import play.api.test.Helpers.AUTHORIZATION
import support.IntegrationBaseSpec
import v1.fixtures.RetrieveLiabilitiesFixture
import v1.models.errors._
import v1.stubs.{AuditStub, AuthStub, DesStub}

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class LiabilitiesControllerISpec extends IntegrationBaseSpec with RetrieveLiabilitiesFixture {

  private trait Test {

    val vrn: String = "123456789"
    val fromDate: String = "2017-01-01"
    val toDate: String = "2017-12-01"
    val correlationId: String = "X-ID"

    def uri: String = s"/$vrn/liabilities?from=$fromDate&to=$toDate"
    def desUrl: String = s"/enterprise/financial-data/VRN/$vrn/VATC"

    def mtdQueryParams: Seq[(String, String)] =
      Seq(
        ("from", fromDate),
        ("to" , toDate)
      )

    val desQueryParams: Map[String, String] =
      Map(
        "dateFrom" -> fromDate,
        "dateTo" -> toDate,
        "onlyOpenItems" -> "false",
        "includeLocks" -> "false",
        "calculateAccruedInterest" -> "true",
        "customerPaymentInformation" -> "true"
      )

    def setupStubs(): StubMapping

    def request: WSRequest = {
      setupStubs()
      buildRequest(uri)
        .addQueryStringParameters(mtdQueryParams: _*)
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

  "Making a request to the Retrieve Liabilities endpoint" should {
    "return a 200 status code with expected body" when {
      "a valid request is made" in new Test{

        override def setupStubs(): StubMapping = {
          AuditStub.audit()
          AuthStub.authorised()
          DesStub.onSuccess(DesStub.GET, desUrl, desQueryParams, OK, desJson)
        }

        private val response = await(request.get)
        response.status shouldBe OK
        response.json shouldBe mtdJson
        response.header("Content-Type") shouldBe Some("application/json")
      }
    }

    "return a 500 status code" when {
      "downstream is not accessible" in new Test{

        override def setupStubs(): StubMapping = {
          AuditStub.audit()
          AuthStub.authorised()
          DesStub.onError(DesStub.GET, desUrl, desQueryParams, BAD_REQUEST, "An internal server error occurred")
        }

        private val response = await(request.get)
        response.status shouldBe INTERNAL_SERVER_ERROR
        response.json shouldBe downStreamJson
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
            |            "reason": "The provided VRN is invalid"
            |        },
            |        {
            |            "code": "INVALID_DATEFROM",
            |            "reason": "The provided from date is invalid"
            |        }
            |    ]
            |}
          """.stripMargin

        override def setupStubs(): StubMapping = {
          AuditStub.audit()
          AuthStub.authorised()
          DesStub.onError(DesStub.GET, desUrl, desQueryParams, BAD_REQUEST, multipleErrors)
        }

        private val response = await(request.get)
        response.status shouldBe INTERNAL_SERVER_ERROR
        response.json shouldBe Json.toJson(DownstreamError)
        response.header("Content-Type") shouldBe Some("application/json")
      }
    }

    "return error according to spec" when {

      def validationErrorTest(requestVrn: String, requestFromDate: Option[String], requestToDate: Option[String],
                              expectedStatus: Int, expectedBody: MtdError, scenario: String): Unit = {
        s"validation fails with ${expectedBody.code} error in scenario: $scenario" in new Test {

          override val vrn: String = requestVrn
          override val fromDate: String = requestFromDate.getOrElse("")
          override val toDate: String = requestToDate.getOrElse("")

          override val mtdQueryParams: Seq[(String, String)] =
            Map(
              "from" -> requestFromDate,
              "to" -> requestToDate
            ).collect {
              case (k: String, Some(v: String)) => (k, v)
            }.toSeq

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

      def futureFrom(plusDays: Int = 1): String = {
        val fromDate = LocalDateTime.now().plusDays(plusDays)
        DateTimeFormatter.ofPattern("yyyy-MM-dd").format(fromDate)
      }

      def futureTo(plusDays: Int = 2): String = {
        val toDate = LocalDateTime.now().plusDays(plusDays)
        DateTimeFormatter.ofPattern("yyyy-MM-dd").format(toDate)
      }

      val input = Seq(
        ("badVrn", Some("2017-01-02"), Some("2018-01-01"), BAD_REQUEST, VrnFormatError, "invalid VRN"),
        ("badVrn", None, Some(futureTo()), BAD_REQUEST, VrnFormatError, "multiple errors (VRN)"),

        ("123456789", Some("notADate"), Some("2018-01-01"), BAD_REQUEST, FinancialDataInvalidDateFromError, "invalid 'from' date"),
        ("123456789", Some("2017-13-01"), Some("2018-01-01"), BAD_REQUEST, FinancialDataInvalidDateFromError, "not a real 'from' date"),
        ("123456789", Some("notADate"), Some("notADate"), BAD_REQUEST, FinancialDataInvalidDateFromError, "both dates invalid"),
        ("123456789", None, Some("2018-01-01"), BAD_REQUEST, FinancialDataInvalidDateFromError, "missing 'from' date"),
        ("123456789", None, None, BAD_REQUEST, FinancialDataInvalidDateFromError, "missing both dates"),
        ("123456789", Some("2016-04-05"), Some("2017-01-01"), BAD_REQUEST, FinancialDataInvalidDateFromError, "'from' date unsupported'"),

        ("123456789", Some("2017-01-02"), Some("notADate"), BAD_REQUEST, FinancialDataInvalidDateToError, "invalid 'to' date"),
        ("123456789", Some("2017-01-02"), Some("2017-01-32"), BAD_REQUEST, FinancialDataInvalidDateToError, "not a real 'to' date"),
        ("123456789", Some("2017-01-02"), Some(futureTo(plusDays = 1)), BAD_REQUEST, FinancialDataInvalidDateToError, "future 'to' date"),
        ("123456789", Some(futureFrom()), Some(futureTo()), BAD_REQUEST, FinancialDataInvalidDateToError, "future both dates"),
        ("123456789", Some("2017-01-02"), None, BAD_REQUEST, FinancialDataInvalidDateToError, "missing 'to' date"),

        ("123456789", Some("2017-01-01"), Some("2018-01-01"), BAD_REQUEST, FinancialDataInvalidDateRangeError, "date range too long"),
        ("123456789", Some("2017-01-01"), Some("2017-01-01"), BAD_REQUEST, FinancialDataInvalidDateRangeError, "dates are the same"),
        ("123456789", Some("2018-01-01"), Some("2017-01-01"), BAD_REQUEST, FinancialDataInvalidDateRangeError, "'from' date after 'to' date")
      )

      input.foreach(args => (validationErrorTest _).tupled(args))
    }

    "des service error" when {
      def serviceErrorTest(desStatus: Int, desCode: String, expectedStatus: Int, expectedBody: MtdError): Unit = {
        s"des returns an $desCode error and status $desStatus" in new Test {

          override def setupStubs(): StubMapping = {
            AuditStub.audit()
            AuthStub.authorised()
            DesStub.onError(DesStub.GET, desUrl, desQueryParams, desStatus, errorBody(desCode))
          }

          private val response = await(request.get)
          response.status shouldBe expectedStatus
          response.json shouldBe Json.toJson(expectedBody)
          response.header("Content-Type") shouldBe Some("application/json")
        }
      }

      val input = Seq(
        (BAD_REQUEST, "INVALID_IDTYPE", INTERNAL_SERVER_ERROR, DownstreamError),
        (BAD_REQUEST, "TEST_ONLY_UNMATCHED_STUB_ERROR", BAD_REQUEST, RuleIncorrectGovTestScenarioError),
        (BAD_REQUEST, "INVALID_IDNUMBER", BAD_REQUEST, VrnFormatErrorDes),
        (BAD_REQUEST, "INVALID_REGIMETYPE", INTERNAL_SERVER_ERROR, DownstreamError),
        (BAD_REQUEST, "INVALID_ONLYOPENITEMS", INTERNAL_SERVER_ERROR, DownstreamError),
        (BAD_REQUEST, "INVALID_INCLUDELOCKS", INTERNAL_SERVER_ERROR, DownstreamError),
        (BAD_REQUEST, "INVALID_CALCULATEACCRUEDINTEREST", INTERNAL_SERVER_ERROR, DownstreamError),
        (BAD_REQUEST, "INVALID_CUSTOMERPAYMENTINFORMATION", INTERNAL_SERVER_ERROR, DownstreamError),
        (BAD_REQUEST, "INVALID_DATEFROM", BAD_REQUEST, InvalidDateFromErrorDes),
        (BAD_REQUEST, "INVALID_DATETO", BAD_REQUEST, InvalidDateToErrorDes),
        (FORBIDDEN, "INSOLVENT_TRADER", FORBIDDEN, RuleInsolventTraderError),
        (NOT_FOUND, "NOT_FOUND", NOT_FOUND, LegacyNotFoundError),
        (UNPROCESSABLE_ENTITY, "INVALID_DATA", BAD_REQUEST, InvalidDataError),
        (INTERNAL_SERVER_ERROR, "SERVER_ERROR", INTERNAL_SERVER_ERROR, DownstreamError),
        (SERVICE_UNAVAILABLE, "SERVICE_UNAVAILABLE", INTERNAL_SERVER_ERROR, DownstreamError)
      )

      input.foreach(args => (serviceErrorTest _).tupled(args))
    }
  }
}
