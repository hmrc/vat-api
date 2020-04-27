package v1.endpoints

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

import com.github.tomakehurst.wiremock.stubbing.StubMapping
import play.api.http.HeaderNames.ACCEPT
import play.api.http.Status._
import play.api.libs.json.{JsValue, Json}
import play.api.libs.ws.WSRequest
import support.IntegrationBaseSpec
import v1.fixtures.ObligationsFixture
import v1.models.errors.{DownstreamError, InvalidDateFromErrorDes, InvalidDateToErrorDes, InvalidFromError, InvalidStatusError, InvalidStatusErrorDes, InvalidToError, LegacyNotFoundError, MtdError, RuleDateRangeInvalidError, RuleDateRangeTooLargeError, RuleDateRangeTooLargeErrorDes, VrnFormatError, VrnFormatErrorDes}
import v1.stubs.{AuditStub, AuthStub, DesStub}


class ObligationsControllerISpec extends IntegrationBaseSpec with ObligationsFixture {
  private trait Test {

    val vrn: String = "123456789"
    val fromDate: String = "2017-01-01"
    val toDate: String = "2017-12-01"
    val oblStatus: String = "F"
    val correlationId: String = "X-ID"

    val desJson: JsValue = obligationsDesJson
    val mtdJson: JsValue = obligationsMtdJson

    def uri: String = s"/$vrn/obligations?from=$fromDate&to=$toDate&status=$oblStatus"
    def mtdQueryParams: Seq[(String, String)] =
      Seq(
        ("from", fromDate),
        ("to", toDate),
        ("status", oblStatus)
      )

    def desUrl: String = s"/enterprise/obligation-data/vrn/$vrn/VATC"
    val desQueryParams: Map[String, String] =
      Map(
        "from" -> fromDate,
        "to" -> toDate,
        "status" -> oblStatus,
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

  "Making a request to the 'Retrieve Obligations' endpoint" should {
    "return a 200 status code with expected body" when {
      "a valid request is made" in new Test {

        override def setupStubs(): StubMapping = {
          AuditStub.audit()
          AuthStub.authorised()
          DesStub.onSuccess(DesStub.GET, desUrl, desQueryParams, OK, desJson)
        }

        private val response = await(request.get())
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
            |    "failures": [
            |        {
            |            "code": "INVALID_IDNUMBER",
            |            "reason": "Submission has not passed validation. Invalid parameter idNumber."
            |        },
            |        {
            |            "code": "INVALID_REGIME",
            |            "reason": "Submission has not passed validation.  Invalid parameter regimeType."
            |        },
            |        {
            |            "code": "INVALID_DATE_FROM",
            |            "reason": "Submission has not passed validation. Invalid parameter from."
            |        },
            |        {
            |            "code": "INVALID_DATE_TO",
            |            "reason": "Submission has not passed validation. Invalid parameter to."
            |        },
            |        {
            |            "code": "INVALID_STATUS",
            |            "reason": "Submission has not passed validation. Invalid parameter status."
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

      def validationErrorTest(requestVrn: String, requestFromDate: Option[String], requestToDate: Option[String], requestStatus: Option[String],
                              expectedStatus: Int, expectedBody: MtdError, scenario: String): Unit = {
        s"validation fails with ${expectedBody.code} error in scenario: $scenario" in new Test {

          override val vrn: String = requestVrn
          override val fromDate: String = requestFromDate.getOrElse("")
          override val toDate: String = requestToDate.getOrElse("")

          override val mtdQueryParams: Seq[(String, String)] =
            Map(
              "from" -> requestFromDate,
              "to" -> requestToDate,
              "status" -> requestStatus
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

        ("NotAVrn", Some("2017-01-02"), Some("2018-01-01"), Some("F") ,BAD_REQUEST, VrnFormatError, "invalid VRN"),
        ("NotAVrn", None, Some(futureTo()), Some("O"), BAD_REQUEST, VrnFormatError, "multiple errors (VRN)"),

        ("123456789", Some("notADate"), Some("2018-01-01"), Some("F"), BAD_REQUEST, InvalidFromError, "invalid 'from' date"),
        ("123456789", Some("2017-13-01"), Some("2018-01-01"), Some("F"),BAD_REQUEST, InvalidFromError, "not a real 'from' date"),
        ("123456789", Some("notADate"), Some("notADate"), Some("F"),BAD_REQUEST, InvalidFromError, "both dates invalid"),
        ("123456789", None, Some("2018-01-01"), Some("F"), BAD_REQUEST, InvalidFromError, "missing 'from' date"),
        ("123456789", None, None, Some("F"), BAD_REQUEST, InvalidFromError, "missing both dates"),

        ("123456789", Some("2017-01-02"), Some("notADate"), Some("F"), BAD_REQUEST, InvalidToError, "invalid 'to' date"),
        ("123456789", Some("2017-01-02"), Some("2017-01-32"), Some("F"), BAD_REQUEST, InvalidToError, "not a real 'to' date"),

        ("123456789", Some("2017-01-01"), Some("2017-05-01"), Some("NotAStatus"), BAD_REQUEST, InvalidStatusError, "invalid status"),
        ("123456789", Some("2017-01-01"), Some("2017-05-01"), None, BAD_REQUEST, InvalidStatusError, "missing status"),

        ("123456789", Some("2017-01-01"), Some("2018-01-02"), Some("F"), BAD_REQUEST, RuleDateRangeInvalidError, "date range too long"),
        ("123456789", Some("2017-01-01"), Some("2017-01-01"), Some("F"), BAD_REQUEST, RuleDateRangeInvalidError, "dates are the same"),
        ("123456789", Some("2018-01-01"), Some("2017-01-01"), Some("F"), BAD_REQUEST, RuleDateRangeInvalidError, "'from' date after 'to' date")
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
        (BAD_REQUEST, "INVALID_IDNUMBER", BAD_REQUEST, VrnFormatErrorDes),
        (BAD_REQUEST, "INVALID_STATUS", BAD_REQUEST, InvalidStatusErrorDes),
        (BAD_REQUEST, "INVALID_REGIME", INTERNAL_SERVER_ERROR, DownstreamError),
        (BAD_REQUEST, "INVALID_DATE_FROM", BAD_REQUEST, InvalidDateFromErrorDes),
        (BAD_REQUEST, "INVALID_DATE_TO", BAD_REQUEST, InvalidDateToErrorDes),
        (BAD_REQUEST, "INVALID_DATE_RANGE", BAD_REQUEST, RuleDateRangeTooLargeErrorDes),
        (FORBIDDEN, "NOT_FOUND_BKEY", INTERNAL_SERVER_ERROR, DownstreamError),
        (NOT_FOUND, "NOT_FOUND", NOT_FOUND, LegacyNotFoundError),
        (INTERNAL_SERVER_ERROR, "SERVER_ERROR", INTERNAL_SERVER_ERROR, DownstreamError),
        (SERVICE_UNAVAILABLE, "SERVICE_UNAVAILABLE", INTERNAL_SERVER_ERROR, DownstreamError)
      )

      input.foreach(args => (serviceErrorTest _).tupled(args))
    }
  }
}
