/*
 * Copyright 2026 HM Revenue & Customs
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
import play.api.libs.json.{ JsValue, Json }
import play.api.libs.ws.WSRequest
import play.api.test.Helpers.AUTHORIZATION
import support.IntegrationBaseSpec
import v1.fixtures.ObligationsFixture
import v1.models.errors._
import v1.stubs.{ AuditStub, AuthStub, DesStub }

class AssistReturnControllerISpec extends IntegrationBaseSpec with ObligationsFixture {

  private trait Test {

    val vrn: String           = "123456789"
    val periodKey: String     = "18A2"
    val correlationId: String = "X-ID"

    def desUrl: String = s"/enterprise/obligation-data/vrn/$vrn/VATC"

    val desQueryParams: Map[String, String] = Map("status" -> "O")

    lazy val validRequestJson: JsValue = Json.parse(s"""
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
         |  "totalAcquisitionsExVAT": 500
         |}
    """.stripMargin)

    def uri: String = s"/internal/validate/$vrn"

    def setupStubs(): StubMapping

    def request: WSRequest = {
      setupStubs()
      buildRequest(uri)
        .withHttpHeaders(
          (ACCEPT, "application/vnd.hmrc.1.0+json"),
          (AUTHORIZATION, "Bearer 123"),
          ("X-CorrelationId", correlationId)
        )
    }
  }

  "Validate VAT return and retrieve obligation endpoint" should {

    "return a 200 with the matched obligation" when {

      "the return is valid and an ended open obligation matches the period key" in new Test {

        override def setupStubs(): StubMapping = {
          AuditStub.audit()
          AuthStub.authorised()
          DesStub.onSuccess(DesStub.GET, desUrl, desQueryParams, OK, obligationsDesJson)
        }

        private val response = await(request.post(validRequestJson))

        response.status shouldBe OK
        response.json shouldBe assistObligationMtdJson("18A2", "2017-04-01", "2017-06-30", "2017-08-07")
        response.header("X-CorrelationId") shouldBe Some(correlationId)
        response.header("Content-Type") shouldBe Some("application/json")
      }
    }

    "return a 400 status code" when {

      "the period key does not match any open obligation" in new Test {

        override val periodKey: String = "ZZ99"

        override def setupStubs(): StubMapping = {
          AuditStub.audit()
          AuthStub.authorised()
          DesStub.onSuccess(DesStub.GET, desUrl, desQueryParams, OK, obligationsDesJson)
        }

        private val response = await(request.post(validRequestJson))

        response.status shouldBe BAD_REQUEST
        response.json shouldBe Json.toJson(NoOpenObligation)
        response.header("Content-Type") shouldBe Some("application/json")
      }

      "the matched obligation's period has not ended" in new Test {

        override def setupStubs(): StubMapping = {
          AuditStub.audit()
          AuthStub.authorised()
          DesStub.onSuccess(DesStub.GET,
                            desUrl,
                            desQueryParams,
                            OK,
                            openObligationDesJson(periodKey, from = "2999-01-01", to = "2999-03-31", due = "2999-05-07"))
        }

        private val response = await(request.post(validRequestJson))

        response.status shouldBe BAD_REQUEST
        response.json shouldBe Json.toJson(TaxPeriodNotEnded)
        response.header("Content-Type") shouldBe Some("application/json")
      }

      "a request is made with an invalid monetary value" in new Test {

        val invalidMonetaryJson: JsValue = Json.parse(s"""
             |{
             |  "periodKey": "$periodKey",
             |  "vatDueSales": 100.00,
             |  "vatDueAcquisitions": 100.00,
             |  "totalVatDue": 200.00,
             |  "vatReclaimedCurrPeriod": 100.00,
             |  "netVatDue": 100.00,
             |  "totalValueSalesExVAT": 500,
             |  "totalValuePurchasesExVAT": 500,
             |  "totalValueGoodsSuppliedExVAT": 1000000000000000000000,
             |  "totalAcquisitionsExVAT": 500
             |}
    """.stripMargin)

        val expectedError: JsValue = Json.parse("""
            |{
            |  "code": "INVALID_REQUEST",
            |  "message": "Invalid request",
            |  "errors": [
            |    {
            |      "code": "INVALID_MONETARY_AMOUNT",
            |      "message": "The value must be between -9999999999999 and 9999999999999",
            |      "path": "/totalValueGoodsSuppliedExVAT"
            |    }
            |  ]
            |}
    """.stripMargin)

        override def setupStubs(): StubMapping = {
          AuditStub.audit()
          AuthStub.authorised()
        }

        private val response = await(request.post(invalidMonetaryJson))

        response.status shouldBe BAD_REQUEST
        response.json shouldBe expectedError
        response.header("Content-Type") shouldBe Some("application/json")
      }

      "a request is made with multiple field validation failures" in new Test {

        val invalidRequestJson: JsValue = Json.parse(s"""
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
             |  "totalAcquisitionsExVAT": 50000000000000000000000
             |}
    """.stripMargin)

        val multipleErrors: JsValue = Json.parse(
          """
            |{
            |  "code": "INVALID_REQUEST",
            |  "message": "Invalid request",
            |  "errors": [
            |    {
            |      "code": "INVALID_MONETARY_AMOUNT",
            |      "message": "amount should be a monetary value (to 2 decimal places), between -9,999,999,999,999.99 and 9,999,999,999,999.99",
            |      "path": "/vatReclaimedCurrPeriod"
            |    },
            |    {
            |      "code": "INVALID_MONETARY_AMOUNT",
            |      "message": "The value must be between -9999999999999 and 9999999999999",
            |      "path": "/totalAcquisitionsExVAT"
            |    }
            |  ]
            |}
    """.stripMargin)

        override def setupStubs(): StubMapping = {
          AuditStub.audit()
          AuthStub.authorised()
        }

        private val response = await(request.post(invalidRequestJson))

        response.status shouldBe BAD_REQUEST
        response.json shouldBe multipleErrors
        response.header("Content-Type") shouldBe Some("application/json")
      }

      "a request is made with an invalid VRN" in new Test {

        override val vrn: String = "123456789a"

        override def setupStubs(): StubMapping = {
          AuditStub.audit()
          AuthStub.authorised()
        }

        private val response = await(request.post(validRequestJson))

        response.status shouldBe BAD_REQUEST
        response.json shouldBe Json.toJson(VrnFormatError)
        response.header("Content-Type") shouldBe Some("application/json")
      }
    }

    "return a 503 status code" when {

      def downstreamErrorTest(desStatus: Int, desCode: String): Unit =
        s"DES returns $desStatus $desCode" in new Test {

          override def setupStubs(): StubMapping = {
            AuditStub.audit()
            AuthStub.authorised()
            DesStub.onError(DesStub.GET, desUrl, desQueryParams, desStatus, Json.obj("code" -> desCode, "reason" -> "downstream failure").toString)
          }

          private val response = await(request.post(validRequestJson))

          response.status shouldBe SERVICE_UNAVAILABLE
          response.json shouldBe Json.toJson(ServiceUnavailableError)
          response.header("Content-Type") shouldBe Some("application/json")
        }

      val input = Seq(
        (BAD_REQUEST, "INVALID_IDNUMBER"),
        (BAD_REQUEST, "INVALID_IDTYPE"),
        (NOT_FOUND, "NOT_FOUND"),
        (FORBIDDEN, "NOT_FOUND_BPKEY"),
        (INTERNAL_SERVER_ERROR, "SERVER_ERROR"),
        (SERVICE_UNAVAILABLE, "SERVICE_UNAVAILABLE")
      )

      input.foreach(args => (downstreamErrorTest _).tupled(args))
    }

    "return the error according to spec" when {

      def validationErrorTest(requestVrn: String, requestPeriodKey: String, expectedStatus: Int, expectedBody: MtdError): Unit =
        s"validation fails with ${expectedBody.code} error" in new Test {

          override val vrn: String       = requestVrn
          override val periodKey: String = requestPeriodKey

          override def setupStubs(): StubMapping = {
            AuditStub.audit()
            AuthStub.authorised()
          }

          private val response = await(request.post(validRequestJson))

          response.status shouldBe expectedStatus
          response.json shouldBe Json.toJson(expectedBody)
          response.header("Content-Type") shouldBe Some("application/json")
        }

      val input = Seq(
        ("badVrn", "18A2", BAD_REQUEST, VrnFormatError),
        ("123456789", "badPeriodKey", BAD_REQUEST, BodyPeriodKeyFormatError)
      )

      input.foreach(args => (validationErrorTest _).tupled(args))
    }
  }
}
