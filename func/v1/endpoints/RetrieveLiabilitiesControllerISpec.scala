///*
// * Copyright 2020 HM Revenue & Customs
// *
// * Licensed under the Apache License, Version 2.0 (the "License");
// * you may not use this file except in compliance with the License.
// * You may obtain a copy of the License at
// *
// *     http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// */
//
//package v1.endpoints
//
//import com.github.tomakehurst.wiremock.stubbing.StubMapping
//import play.api.http.HeaderNames.ACCEPT
//import play.api.http.Status._
//import play.api.libs.json.Json
//import play.api.libs.ws.WSRequest
//import support.IntegrationBaseSpec
//import v1.fixtures.RetrieveLiabilitiesFixture
//import v1.models.errors._
//import v1.stubs.{AuditStub, AuthStub, DesStub}
//
//class RetrieveLiabilitiesControllerISpec extends IntegrationBaseSpec with RetrieveLiabilitiesFixture {
//
//  private trait Test {
//
//    val vrn: String = "123456789"
//    val from: String = "2017-01-01"
//    val to: String = "2017-12-01"
//    val correlationId: String = "X-ID"
//
//    def uri: String = s"/$vrn/liabilities?from=$from&to=$to"
//    def desUrl: String = s"/VRN/$vrn/VATC"
//
//    def setupStubs(): StubMapping
//
//    def request: WSRequest = {
//      setupStubs()
//      buildRequest(uri)
//        .withHttpHeaders((ACCEPT, "application/vnd.hmrc.1.0+json"))
//        .withQueryStringParameters(("dateFrom",from),("dateTo",to),("onlyOpenItems","false"),("includeLocks","false"),("calculateAccruedInterest","true"),("customerPaymentInformation","true"))
//    }
//
//    def errorBody(code: String): String =
//      s"""
//         |      {
//         |        "code": "$code",
//         |        "reason": "des message"
//         |      }
//    """.stripMargin
//  }
//
//  "Making a request to the Retrieve Liabilities endpoint" should {
//    "return a 200 status code with expected body" when {
//      "a valid request is made" in new Test{
//
//        override def setupStubs(): StubMapping = {
//          AuditStub.audit()
//          AuthStub.authorised()
//          DesStub.onSuccess(DesStub.GET, desUrl,  OK, desJson)
//        }
//
//        private val response = await(request.get)
//        response.status shouldBe OK
//        response.json shouldBe mtdJson
//        response.header("Content-Type") shouldBe Some("application/json")
//      }
//    }
//
//    "return a 500 status code with expected body" when {
//      "des returns multiple errors" in new Test{
//
//        val multipleErrors: String =
//          """
//            |{
//            |   "failures": [
//            |        {
//            |            "code": "INVALID_VRN",
//            |            "reason": "The provided VRN is invalid"
//            |        },
//            |        {
//            |            "code": "INVALID_DATEFROM",
//            |            "reason": "The provided from date is invalid"
//            |        }
//            |    ]
//            |}
//          """.stripMargin
//
//        override def setupStubs(): StubMapping = {
//          AuditStub.audit()
//          AuthStub.authorised()
//          DesStub.onError(DesStub.GET, desUrl, BAD_REQUEST, multipleErrors)
//        }
//
//        private val response = await(request.get)
//        response.status shouldBe INTERNAL_SERVER_ERROR
//        response.json shouldBe Json.toJson(DownstreamError)
//        response.header("Content-Type") shouldBe Some("application/json")
//      }
//    }
//
//    "return error according to spec" when {
//
//      def validationErrorTest(requestVrn: String, fromDate: String, toDate: String,
//                              expectedStatus: Int, expectedBody: MtdError): Unit = {
//        s"validation fails with ${expectedBody.code} error" in new Test {
//
//          override val vrn: String = requestVrn
//          override val from: String = fromDate
//          override val to: String = toDate
//
//          override def setupStubs(): StubMapping = {
//            AuditStub.audit()
//            AuthStub.authorised()
//          }
//
//          private val response = await(request.get)
//          response.status shouldBe expectedStatus
//          response.json shouldBe Json.toJson(expectedBody)
//          response.header("Content-Type") shouldBe Some("application/json")
//        }
//      }
//
//      val input = Seq(
//        ("badVrn", "2017-01-01", "2017-12-01", BAD_REQUEST, VrnFormatError),
//        ("123456789", "111111", "2017-12-01", BAD_REQUEST, InvalidDateFromError),
//        ("123456789", "2017-01-01", "11111", BAD_REQUEST, InvalidDateToError)
//      )
//
//      input.foreach(args => (validationErrorTest _).tupled(args))
//    }
//
//    "des service error" when {
//      def serviceErrorTest(desStatus: Int, desCode: String, expectedStatus: Int, expectedBody: MtdError): Unit = {
//        s"des returns an $desCode error and status $desStatus" in new Test {
//
//          override def setupStubs(): StubMapping = {
//            AuditStub.audit()
//            AuthStub.authorised()
//            DesStub.onError(DesStub.GET, desUrl, desStatus, errorBody(desCode))
//          }
//
//          private val response = await(request.get)
//          response.status shouldBe expectedStatus
//          response.json shouldBe Json.toJson(expectedBody)
//          response.header("Content-Type") shouldBe Some("application/json")
//        }
//      }
//
//      val input = Seq(
//        (BAD_REQUEST, "INVALID_IDTYPE", INTERNAL_SERVER_ERROR, DownstreamError),
//        (BAD_REQUEST, "INVALID_IDNUMBER", BAD_REQUEST, VrnFormatErrorDes),
//        (BAD_REQUEST, "INVALID_REGIMETYPE", INTERNAL_SERVER_ERROR, DownstreamError),
//        (BAD_REQUEST, "INVALID_ONLYOPENITEMS", INTERNAL_SERVER_ERROR, DownstreamError),
//        (BAD_REQUEST, "INVALID_INCLUDELOCKS", INTERNAL_SERVER_ERROR, DownstreamError),
//        (BAD_REQUEST, "INVALID_CALCULATEACCRUEDINTEREST", INTERNAL_SERVER_ERROR, DownstreamError),
//        (BAD_REQUEST, "INVALID_CUSTOMERPAYMENTINFORMATION", INTERNAL_SERVER_ERROR, DownstreamError),
//        (BAD_REQUEST, "INVALID_DATEFROM", BAD_REQUEST, InvalidDateFromError),
//        (BAD_REQUEST, "INVALID_DATETO", BAD_REQUEST, InvalidDateToError),
//        (NOT_FOUND, "NOT_FOUND", NOT_FOUND, LegacyNotFoundError),
//        (UNPROCESSABLE_ENTITY, "INVALID_DATA", BAD_REQUEST, InvalidDataError),
//        (INTERNAL_SERVER_ERROR, "SERVER_ERROR", INTERNAL_SERVER_ERROR, DownstreamError),
//        (SERVICE_UNAVAILABLE, "SERVICE_UNAVAILABLE", INTERNAL_SERVER_ERROR, DownstreamError)
//      )
//
//      input.foreach(args => (serviceErrorTest _).tupled(args))
//    }
//  }
//}
