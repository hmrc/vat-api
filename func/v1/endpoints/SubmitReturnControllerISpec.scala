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
import play.api.{Application, Environment, Mode}
import play.api.http.HeaderNames.ACCEPT
import play.api.http.Status._
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.{JsValue, Json}
import play.api.libs.ws.WSRequest
import support.IntegrationBaseSpec
import v1.models.errors._
import v1.stubs.{AuditStub, AuthStub, DesStub, NrsStub}

class SubmitReturnControllerISpec extends IntegrationBaseSpec {

  override lazy val app: Application = new GuiceApplicationBuilder()
    .in(Environment.simple(mode = Mode.Dev))
    .configure(    "microservice.services.des.host" -> mockHost,
      "microservice.services.des.port" -> mockPort,
      "microservice.services.auth.host" -> mockHost,
      "microservice.services.auth.port" -> mockPort,
      "auditing.consumer.baseUri.port" -> mockPort,
      "microservice.services.non-repudiation.host" -> mockHost,
      "microservice.services.non-repudiation.port" -> mockPort,
      "microservice.services.non-repudiation.numberOfRetries" -> 10,
      "microservice.services.non-repudiation.initialDelays" -> "5 milliseconds",
      "metrics.enabled" -> "false")
    .build()

  private trait Test {

    val vrn: String = "123456789"
    val periodKey: String = "#001"

    val desResponseJson: JsValue = Json.parse(
      """
        |{
        |    "processingDate": "2018-03-01T11:43:43.195Z",
        |    "paymentIndicator": "BANK",
        |    "formBundleNumber": "891713832155"
        |}
    """.stripMargin
    )

    val nrsSuccess: JsValue = Json.parse(
      s"""
         |{
         |  "nrSubmissionId":"2dd537bc-4244-4ebf-bac9-96321be13cdc",
         |  "cadesTSignature":"30820b4f06092a864886f70111111111c0445c464",
         |  "timestamp":""
         |}
         """.stripMargin)

    val mtdResponseJson: JsValue = Json.parse(
      """
        |{
        |	"processingDate": "2018-03-01T11:43:43.195Z",
        |	"paymentIndicator": "BANK",
        |	"formBundleNumber": "891713832155"
        |}
    """.stripMargin
    )

    val requestJson: JsValue = Json.parse(
      s"""
         |{
         |        "periodKey" : "$periodKey",
         |        "vatDueSales" : 1000,
         |        "vatDueAcquisitions" : -1000,
         |        "totalVatDue" : 0,
         |        "vatReclaimedCurrPeriod" : 100,
         |        "netVatDue" : 100,
         |        "totalValueSalesExVAT" : 5000,
         |        "totalValuePurchasesExVAT" : 1000,
         |        "totalValueGoodsSuppliedExVAT" : 9999999999999,
         |        "totalAcquisitionsExVAT" : 9999999999999,
         |        "finalised" : true
         |}
    """.stripMargin)

    val requestJsonNotFinalised: JsValue = Json.parse(
      s"""
         |{
         |  "periodKey": "$periodKey",
         |  "vatDueSales": 100.00,
         |  "vatDueAcquisitions": 100.00,
         |  "totalVatDue": 200.00,
         |  "vatReclaimedCurrPeriod": 100.00,
         |  "netVatDue": 100.00,
         |  "totalValueSalesExVAT": 500,
         |  "totalValuePurchasesExVAT": 500,
         |  "totalValueGoodsSuppliedExVAT": 500,
         |  "totalAcquisitionsExVAT": 500,
         |  "finalised": false
         |}
    """.stripMargin)

    def uri: String = s"/$vrn/returns"
    def desUrl: String = s"/enterprise/return/vat/$vrn"
    val nrsUrl: String = s".*/submission.*"

    def setupStubs(): StubMapping

    def request: WSRequest = {
        setupStubs()
      buildRequest(uri)
        .withHttpHeaders((ACCEPT, "application/vnd.hmrc.1.0+json"), ("Authorization", "Bearer testtoken"))
    }

    def errorBody(code: String): String =
      s"""
         |      {
         |        "code": "$code",
         |        "reason": "des message"
         |      }
    """.stripMargin
  }

  "Submit VAT Return endpoint" should {
//    "return a 201 status code with expected body" when {
//      "a valid request is made" in new Test {
//
//        override def setupStubs(): StubMapping = {
//          AuditStub.audit()
//          AuthStub.authorisedWithNrs()
//          NrsStub.onSuccess(NrsStub.POST, nrsUrl, ACCEPTED, nrsSuccess)
//          DesStub.onSuccess(DesStub.POST, desUrl, OK, desResponseJson)
//        }
//
//        private val response = await(request.post(requestJson))
//        response.status shouldBe CREATED
//        response.json shouldBe mtdResponseJson
//        response.header("Content-Type") shouldBe Some("application/json")
//      }
//
//      "NRS returns non bad_request response" in new Test {
//
//        override def uri: String = s"/$vrn/returns"
//
//        override def setupStubs(): StubMapping = {
//          AuditStub.audit()
//          AuthStub.authorisedWithNrs()
//          NrsStub.onSuccess(NrsStub.POST, nrsUrl, FORBIDDEN, nrsSuccess)
//          DesStub.onSuccess(DesStub.POST, desUrl, OK, desResponseJson)
//        }
//
//        private val response = await(request.post(requestJson))
//        response.status shouldBe CREATED
//        response.json shouldBe mtdResponseJson
//        response.header("Content-Type") shouldBe Some("application/json")
//      }
//    }

//    "return a 500 status code" when {
//     "NRS returns bad_request response" in new Test {
//
//        override def uri: String = s"/$vrn/returns"
//
//        override def setupStubs(): StubMapping = {
//          AuditStub.audit()
//          AuthStub.authorisedWithNrs()
//          NrsStub.onError(NrsStub.POST, nrsUrl, BAD_REQUEST, "{}")
//        }
//
//        private val response = await(request.post(requestJson))
//        response.status shouldBe INTERNAL_SERVER_ERROR
//       response.header("Content-Type") shouldBe Some("application/json")
//     }
//    }

    "return a FORBIDDEN status code" when {
      "a request is made without finalising the submission" in new Test {
        override def setupStubs(): StubMapping = {
          AuditStub.audit()
          AuthStub.authorisedWithNrs()
          NrsStub.onSuccess(NrsStub.POST, nrsUrl, ACCEPTED, nrsSuccess)
          DesStub.onSuccess(DesStub.POST, desUrl, OK, desResponseJson)
        }

        private val response = await(request.post(requestJsonNotFinalised))
        response.status shouldBe FORBIDDEN
        response.json shouldBe Json.toJson(FinalisedValueRuleError)
      }
    }

    "return a 400 status code" when {
      "a request is made with invalid monetary value" in new Test {

        val submitRequestBodyJsonWithInvalidFinalisedFormat: JsValue = Json.parse(
          """
            |{
            |   "periodKey": "#001",
            |   "vatDueSales": 	7000.00,
            |   "vatDueAcquisitions": 	3000.00,
            |   "totalVatDue": 	10000,
            |   "vatReclaimedCurrPeriod": 	1000,
            |   "netVatDue": 	9000,
            |   "totalValueSalesExVAT": 	1000,
            |   "totalValuePurchasesExVAT": 	200,
            |   "totalValueGoodsSuppliedExVAT": 	1000000000000000000000,
            |   "totalAcquisitionsExVAT": 	540,
            |   "finalised": true
            |}
            |""".stripMargin
        )

        val expectedError: JsValue = Json.parse(
          s"""
             |{
             |	"code": "INVALID_REQUEST",
             |	"message": "Invalid request",
             |	"errors": [{
             |		"code": "INVALID_MONETARY_AMOUNT",
             |		"message": "The value must be between -9999999999999 and 9999999999999",
             |		"path": "/totalValueGoodsSuppliedExVAT"
             |	}]
             |}
      """.stripMargin)

        override def setupStubs(): StubMapping = {
          AuditStub.audit()
          AuthStub.authorisedWithNrs()
        }

        private val response = await(request.post(submitRequestBodyJsonWithInvalidFinalisedFormat))
        response.status shouldBe BAD_REQUEST
        response.json shouldBe expectedError
      }
    }

    "return a 400 status code with VRN_INVALID" when {
      "a request is made with invalid vrn and body" in new Test {

        override val vrn = "123456789a"
        val submitRequestBodyJsonWithInvalidFinalisedFormat: String =
          """
            |{
            |   "periodKey": 1,
            |   "vatDueSales": 	Invalid Json,
            |   "vatDueAcquisitions": 	3000.00,
            |   "totalVatDue": 	10000,
            |   "vatReclaimedCurrPeriod": 	1000,
            |   "netVatDue": 	9000,
            |   "totalValueSalesExVAT": 	1000,
            |   "totalValuePurchasesExVAT": 	200,
            |   "totalValueGoodsSuppliedExVAT": 	100000,
            |   "totalAcquisitionsExVAT": 	540,
            |   "finalised": true
            |}
            |""".stripMargin

        override def setupStubs(): StubMapping = {
          AuditStub.audit()
          AuthStub.authorisedWithNrs()
        }

        private val response = await(request.post(submitRequestBodyJsonWithInvalidFinalisedFormat))
        response.status shouldBe BAD_REQUEST
        response.json shouldBe Json.toJson(VrnFormatError)
      }
    }

    "return a 500 status code with expected body" when {
      "des returns multiple errors" in new Test {

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
          AuthStub.authorisedWithNrs()
          NrsStub.onSuccess(NrsStub.POST, nrsUrl, ACCEPTED, nrsSuccess)
          DesStub.onError(DesStub.POST, desUrl, BAD_REQUEST, multipleErrors)
        }

        private val response = await(request.post(requestJson))
        response.status shouldBe INTERNAL_SERVER_ERROR
        response.json shouldBe Json.toJson(DownstreamError)
        response.header("Content-Type") shouldBe Some("application/json")
      }
    }

    "return 400 status code with multiple errors" when {
      "multiple fields validation fails" in new Test {

        val invalidRequestJson: JsValue = Json.parse(
          s"""
             |{
             |  "periodKey": "$periodKey",
             |  "vatDueSales": 100.00,
             |  "vatDueAcquisitions": 100.00,
             |  "totalVatDue": 200.00,
             |  "vatReclaimedCurrPeriod": 10000000000000000000000.00,
             |  "netVatDue": 100.00,
             |  "totalValueSalesExVAT": 500,
             |  "totalValuePurchasesExVAT": 500,
             |  "totalValueGoodsSuppliedExVAT": 500,
             |  "totalAcquisitionsExVAT": 50000000000000000000000,
             |  "finalised": true
             |}
    """.stripMargin)

        val multipleErrors: String =
          """
            |{
            |    "code": "INVALID_REQUEST",
            |    "message": "Invalid request",
            |    "errors": [
            |        {
            |            "code": "INVALID_MONETARY_AMOUNT",
            |            "message": "amount should be a monetary value (to 2 decimal places), between -9,999,999,999,999.99 and 9,999,999,999,999.99",
            |            "path": "/vatReclaimedCurrPeriod"
            |        },
            |        {
            |            "code": "INVALID_MONETARY_AMOUNT",
            |            "message": "The value must be between -9999999999999 and 9999999999999",
            |            "path": "/totalAcquisitionsExVAT"
            |        }
            |    ]
            |}
          """.stripMargin

        override def setupStubs(): StubMapping = {
          AuditStub.audit()
          AuthStub.authorisedWithNrs()
        }

        private val response = await(request.post(invalidRequestJson))
        response.status shouldBe BAD_REQUEST
        response.json shouldBe Json.parse(multipleErrors)
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
            AuthStub.authorisedWithNrs()
          }

          private val response = await(request.post(requestJson))
          response.status shouldBe expectedStatus
          response.json shouldBe Json.toJson(expectedBody)
          response.header("Content-Type") shouldBe Some("application/json")
        }
      }

      val input = Seq(
        ("badVrn", "AA11", BAD_REQUEST, VrnFormatError),
        ("123456789", "badPeriodKey", BAD_REQUEST, BodyPeriodKeyFormatError)
      )

      input.foreach(args => (validationErrorTest _).tupled(args))
    }

    "des service error" when {
      def serviceErrorTest(desStatus: Int, desCode: String, expectedStatus: Int, expectedBody: MtdError): Unit = {
        s"des returns an $desCode error and status $desStatus" in new Test {

          override def setupStubs(): StubMapping = {
            AuditStub.audit()
            AuthStub.authorisedWithNrs()
            NrsStub.onSuccess(NrsStub.POST, nrsUrl, ACCEPTED, nrsSuccess)
            DesStub.onError(DesStub.POST, desUrl, desStatus, errorBody(desCode))
          }

          private val response = await(request.post(requestJson))
          response.status shouldBe expectedStatus
          response.json shouldBe Json.toJson(expectedBody)
          response.header("Content-Type") shouldBe Some("application/json")
        }
      }

      val input = Seq(
        (BAD_REQUEST, "INVALID_VRN", BAD_REQUEST, VrnFormatErrorDes),
        (BAD_REQUEST, "INVALID_PERIODKEY", BAD_REQUEST, PeriodKeyFormatErrorDes),
        (BAD_REQUEST, "INVALID_PAYLOAD", BAD_REQUEST, BadRequestError),
        (FORBIDDEN, "TAX_PERIOD_NOT_ENDED", FORBIDDEN, TaxPeriodNotEnded),
        (CONFLICT, "DUPLICATE_SUBMISSION", FORBIDDEN, DuplicateVatSubmission),
        (FORBIDDEN, "NOT_FOUND_VRN", INTERNAL_SERVER_ERROR, DownstreamError),
        (INTERNAL_SERVER_ERROR, "SERVER_ERROR", INTERNAL_SERVER_ERROR, DownstreamError),
        (BAD_REQUEST, "INVALID_ORIGINATOR_ID", INTERNAL_SERVER_ERROR, DownstreamError),
        (SERVICE_UNAVAILABLE, "SERVICE_UNAVAILABLE", INTERNAL_SERVER_ERROR, DownstreamError),
        (BAD_REQUEST, "INVALID_SUBMISSION", INTERNAL_SERVER_ERROR, DownstreamError)
      )

      input.foreach(args => (serviceErrorTest _).tupled(args))
    }
  }
}
