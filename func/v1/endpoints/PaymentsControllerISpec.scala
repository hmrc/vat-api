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

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

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
    "return only payments with amounts" when {
      "payments are received with no amount but have a clearing date" in new Test{

        val paymentsDesJson: JsValue = Json.parse(
          """
            |{
            |   "idType":"VRN",
            |   "idNumber":"100062914",
            |   "regimeType":"VATC",
            |   "processingDate":"2017-05-13T09:30:00.000Z",
            |   "financialTransactions":[
            |      {
            |         "chargeType":"VAT Return Debit Charge",
            |         "mainType":"VAT Return Charge",
            |         "periodKey":"15AD",
            |         "periodKeyDescription":"February 2018",
            |         "taxPeriodFrom":"2017-02-01",
            |         "taxPeriodTo":"2017-02-28",
            |         "businessPartner":"0100062914",
            |         "contractAccountCategory":"33",
            |         "contractAccount":"000917000429",
            |         "contractObjectType":"ZVAT",
            |         "contractObject":"00000018000000000104",
            |         "sapDocumentNumber":"003390002284",
            |         "sapDocumentNumberItem":"0001",
            |         "chargeReference":"XQ002750002150",
            |         "mainTransaction":"4700",
            |         "subTransaction":"1174",
            |         "originalAmount":15.65,
            |         "outstandingAmount":10.65,
            |         "clearedAmount":5.0,
            |         "accruedInterest":0,
            |         "items":[
            |            {
            |               "subItem":"000",
            |               "dueDate":"2017-02-11",
            |               "amount":15.0,
            |               "clearingDate": "2017-02-11",
            |               "clearingReason":"01",
            |               "outgoingPaymentMethod":"A",
            |               "paymentLock":"a",
            |               "clearingLock":"A",
            |               "interestLock":"C",
            |               "dunningLock":"1",
            |               "returnFlag":true,
            |               "paymentReference":"a",
            |               "paymentMethod":"A",
            |               "paymentLot":"081203010024",
            |               "paymentLotItem":"000001",
            |               "clearingSAPDocument":"3350000212",
            |               "statisticalDocument":"A"
            |            },
            |            {
            |               "subItem":"000",
            |               "dueDate":"2017-02-11",
            |               "amount":15.0,
            |               "clearingDate": "2017-02-11",
            |               "clearingReason":"01",
            |               "outgoingPaymentMethod":"A",
            |               "paymentLock":"a",
            |               "clearingLock":"A",
            |               "interestLock":"C",
            |               "dunningLock":"1",
            |               "returnFlag":true,
            |               "paymentReference":"a",
            |               "paymentMethod":"A",
            |               "paymentAmount":15.0,
            |               "paymentLot":"081203010024",
            |               "paymentLotItem":"000001",
            |               "clearingSAPDocument":"3350000212",
            |               "statisticalDocument":"A"
            |            }
            |         ]
            |      },
            |      {
            |         "chargeType":"Payment on account",
            |         "items":[
            |            {
            |               "subItem":"000",
            |               "amount":-10
            |            },
            |            {
            |               "subItem":"001",
            |               "dueDate":"2017-04-01",
            |               "amount":-10,
            |               "clearingDate":"2017-11-27",
            |               "paymentAmount":999999999,
            |               "paymentMethod":"DIRECT DEBIT"
            |            }
            |         ],
            |         "periodKey":"0318",
            |         "taxPeriodFrom":"2017-01-01",
            |         "taxPeriodTo":"2017-03-31"
            |      }
            |   ]
            |}
    """.stripMargin
        )

        val paymentsMtdJson: JsValue = Json.parse(
          """
            |{
            |   "payments":[
            |      {
            |         "amount":15,
            |         "received":"2017-02-11"
            |      }
            |   ]
            |}
    """.stripMargin
        )

        override def setupStubs(): StubMapping = {
          AuditStub.audit()
          AuthStub.authorised()
          DesStub.onSuccess(DesStub.GET, desUrl, desQueryParams,  OK, paymentsDesJson)
        }

        private val response = await(request.get())
        response.status shouldBe OK
        response.json shouldBe paymentsMtdJson
        response.header("Content-Type")  shouldBe Some("application/json")
      }
    }

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

    "return a 404 NOT_FOUND" when {
      "all payment items are filtered away" in new Test{

        override val fromDate = "2019-02-02"
        override val toDate = "2019-02-28"

        override def setupStubs(): StubMapping = {
          AuditStub.audit()
          AuthStub.authorised()
          DesStub.onSuccess(DesStub.GET, desUrl, desQueryParams,  OK, unsupportedPaymentsDesJson)
        }

        private val response = await(request.get())
        response.status shouldBe NOT_FOUND
        response.json shouldBe Json.toJson(LegacyNotFoundError)
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
  }
}
