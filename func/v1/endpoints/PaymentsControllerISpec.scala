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

package v1.endpoints

import com.github.tomakehurst.wiremock.stubbing.StubMapping
import play.api.http.HeaderNames.ACCEPT
import play.api.http.Status._
import play.api.libs.json.{JsValue, Json}
import play.api.libs.ws.WSRequest
import support.IntegrationBaseSpec
import v1.fixtures.PaymentsFixture
import v1.models.errors._
import v1.stubs.{AuditStub, AuthStub, DesStub}

class PaymentsControllerISpec extends IntegrationBaseSpec with PaymentsFixture {

  private trait Test {

    val vrn: String = "123456789"
    val fromDate: String = "2017-01-02"
    val toDate: String = "2018-01-01"
    val correlationId: String = "X-ID"

    val desJson: JsValue = paymentsDesJson
    val mtdJson: JsValue = paymentsMtdJson

    def uri: String = s"/$vrn/payments"
    def mtdQueryParams: Seq[(String, String)] =
      Seq(
        ("from", fromDate),
        ("to" , toDate)
      )

    def desUrl: String = s"/enterprise/financial-data/VRN/$vrn/VATC"
    def desQueryParams: Map[String, String] =
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
        .withHttpHeaders((ACCEPT, "application/vnd.hmrc.1.0+json"))

    }

    def errorBody(code: String): String =
      s"""
         |      {
         |        "code": "$code",
         |        "reason": "des message"
         |      }
    """.stripMargin
  }

  "Making a request to the 'retrieve payments' endpoint" should {
    "return a 200 status code with expected body" when {
      "a valid request is made" in new Test{

        override def setupStubs(): StubMapping = {
          AuditStub.audit()
          AuthStub.authorised()
          DesStub.onSuccess(DesStub.GET, desUrl, desQueryParams,  OK, desJson)
        }

        private val response = await(request.get())
        response.status shouldBe OK
        response.json shouldBe mtdJson
        response.header("Content-Type")  shouldBe Some("application/json")
      }
    }

    "return a 500 status code with expected body" when {
      "des returns multiple errors" in new Test{

        val multipleErrors: String =
          """
            |{
            |   "failures": [
            |        {
            |            "code": "INVALID_IDTYPE",
            |            "reason": "Submission has not passed validation. Invalid parameter idType."
            |        },
            |        {
            |            "code": "INVALID_IDNUMBER",
            |            "reason": "Submission has not passed validation. Invalid parameter idNumber."
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

      def validationErrorTest(requestVrn: String, requestFromDate: String, requestToDate: String,
                              expectedStatus: Int, expectedBody: MtdError): Unit = {
        s"validation fails with ${expectedBody.code} error" in new Test {

          override val vrn: String = requestVrn
          override val fromDate: String = requestFromDate
          override val toDate: String = requestToDate

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
        ("badVrn", "2017-01-02", "2018-01-01", BAD_REQUEST, VrnFormatError),
        ("123456789", "notADate", "2018-01-01", BAD_REQUEST, FinancialDataInvalidDateFromError),
        ("123456789", "2017-01-02", "notADate", BAD_REQUEST, FinancialDataInvalidDateToError),
        ("123456789", "2017-01-01", "2018-01-01", BAD_REQUEST, FinancialDataInvalidDateRangeError)
      )

      input.foreach(args => (validationErrorTest _).tupled(args))
    }

    "map errors correctly" when {
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
        (BAD_REQUEST, "INVALID_IDNUMBER", BAD_REQUEST, VrnFormatErrorDes),
        (BAD_REQUEST, "INVALID_REGIMETYPE", INTERNAL_SERVER_ERROR, DownstreamError),
        (BAD_REQUEST, "INVALID_ONLYOPENITEMS", INTERNAL_SERVER_ERROR, DownstreamError),
        (BAD_REQUEST, "INVALID_INCLUDELOCKS", INTERNAL_SERVER_ERROR, DownstreamError),
        (BAD_REQUEST, "INVALID_CALCULATEACCRUEDINTEREST", INTERNAL_SERVER_ERROR, DownstreamError),
        (BAD_REQUEST, "INVALID_CUSTOMERPAYMENTINFORMATION", INTERNAL_SERVER_ERROR, DownstreamError),
        (BAD_REQUEST, "INVALID_DATEFROM", BAD_REQUEST, InvalidDateFromErrorDes),
        (BAD_REQUEST, "INVALID_DATETO", BAD_REQUEST, InvalidDateToErrorDes),
        (NOT_FOUND, "NOT_FOUND", NOT_FOUND, LegacyNotFoundError),
        (UNPROCESSABLE_ENTITY, "INVALID_DATA", BAD_REQUEST, InvalidDataError),
        (INTERNAL_SERVER_ERROR, "SERVER_ERROR", INTERNAL_SERVER_ERROR, DownstreamError),
        (SERVICE_UNAVAILABLE, "SERVICE_UNAVAILABLE", INTERNAL_SERVER_ERROR, DownstreamError)
      )

      input.foreach(args => (serviceErrorTest _).tupled(args))
    }

    "date parameters are not supplied" must {
      def validationErrorTest(requestFromDate: Option[String], requestToDate: Option[String],
                              expectedStatus: Int, expectedBody: MtdError, scenario: String): Unit = {
        s"validation fails with ${expectedBody.code} error in scenario: $scenario" in new Test {

          override val mtdQueryParams: Seq[(String, String)] =
            Map(
              "from" -> requestFromDate,
              "to" -> requestFromDate
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

      val input = Seq(
        (None, Some("2018-01-01"), BAD_REQUEST, FinancialDataInvalidDateFromError, "missing from date"),
        (Some("2017-01-02"), None, BAD_REQUEST, FinancialDataInvalidDateToError, "missing to date"),
        (None, None, BAD_REQUEST, FinancialDataInvalidDateFromError, "both dates missing")
      )

      input.foreach(args => (validationErrorTest _).tupled(args))
    }
  }

}
