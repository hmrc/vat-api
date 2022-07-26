package v1.endpoints

import com.github.tomakehurst.wiremock.stubbing.StubMapping
import config.AppConfig
import play.api.http.HeaderNames.ACCEPT
import play.api.http.Status._
import play.api.libs.json.{JsValue, Json}
import play.api.libs.ws.{WSRequest, WSResponse}
import play.api.test.Helpers.AUTHORIZATION
import support.IntegrationBaseSpec
import v1.constants.FinancialDataConstants
import v1.models.errors.{FinancialInvalidCorrelationId, FinancialNotDataFound, MtdError, VrnFormatError}
import v1.stubs.{AuditStub, AuthStub, PenaltiesStub}

class FinancialDataControllerISpec extends IntegrationBaseSpec {

  implicit val appConfig: AppConfig = app.injector.instanceOf[AppConfig]

  private trait Test {

    def uri: String = s"/${FinancialDataConstants.vrn}/financial-details"

    def setupStubs(): StubMapping

    def request: WSRequest = {
      setupStubs()
      buildRequest(uri)
        .withHttpHeaders(
          (ACCEPT, "application/vnd.hmrc.1.0+json"),
          (AUTHORIZATION, "Bearer 123")
        )
    }
  }

  "FinancialDataController" when {

    "GET /[VRN]/financial-details" when {

      "raw vrn cannot be parsed" must {

        "return BadRequest" in new Test {

          override def uri: String = s"/${FinancialDataConstants.invalidVrn}/penalties"

          override def setupStubs(): StubMapping = {
            AuditStub.audit()
            AuthStub.authorised()
          }

          val response: WSResponse = await(request.get())
          response.status shouldBe BAD_REQUEST
          response.json shouldBe Json.toJson(VrnFormatError)
          response.header("Content-Type") shouldBe Some("application/json")
        }
      }

      "vrn can be parsed" when {

        "a valid request is made" must {

          "return 200 and penalties data" in new Test {

            override def setupStubs(): StubMapping = {
              AuditStub.audit()
              AuthStub.authorised()
              PenaltiesStub.onSuccess(PenaltiesStub.GET, FinancialDataConstants.financialDataUrl(), OK, FinancialDataConstants.testFinancialResponseJsonMax)
            }

            val response: WSResponse = await(request.get())
            response.status shouldBe OK
            response.json shouldBe FinancialDataConstants.testFinancialResponseJsonMax
            response.header("Content-Type") shouldBe Some("application/json")
          }
        }

        "VRN is invalid" must {

          "return 400" in new Test {
            val errorBody: JsValue = Json.parse(
              """
                |{
                |"failures": [{
                | "code":"INVALID_CORRELATIONID",
                | "reason":"Some Reason"
                |}]
                |}
                |""".stripMargin)
            override def setupStubs(): StubMapping = {
              AuditStub.audit()
              AuthStub.authorised()
              PenaltiesStub.onError(PenaltiesStub.GET, FinancialDataConstants.financialDataUrl(), BAD_REQUEST, errorBody)
            }

            val response: WSResponse = await(request.get())
            response.status shouldBe BAD_REQUEST
            response.json shouldBe Json.toJson(FinancialInvalidCorrelationId)
            response.header("Content-Type") shouldBe Some("application/json")
          }

          "return multi 400 errors" in new Test {
            val errorBody: JsValue = Json.parse(
              """
                |{
                |"failures": [{
                | "code":"INVALID_DATE_FROM",
                | "reason":"Some Reason"
                |},
                |{
                | "code":"INVALID_DATE_TO",
                | "reason":"Some Reason"
                |}]
                |}
                |""".stripMargin)

            val expectedJson: JsValue = Json.parse(
              """
                |{
                | "code":"INVALID_DATE_FROM",
                | "message":"Invalid Date From",
                | "errors": [{
                | "code":"INVALID_DATE_TO",
                | "message": "Invalid Date to"
                | }]
                |
                |}
                |""".stripMargin
            )
            override def setupStubs(): StubMapping = {
              AuditStub.audit()
              AuthStub.authorised()
              PenaltiesStub.onError(PenaltiesStub.GET, FinancialDataConstants.financialDataUrl(), BAD_REQUEST, errorBody)
            }

            val response: WSResponse = await(request.get())
            response.status shouldBe BAD_REQUEST
            response.json shouldBe expectedJson
            response.header("Content-Type") shouldBe Some("application/json")
          }
        }

        "VRN is not found" must {

          "return 404" in new Test {

            val errorBody: JsValue = Json.parse(
              """
                |{
                |"failures": [{
                | "code":"NO_DATA_FOUND",
                | "reason":"Some Reason"
                |}]
                |}
                |""".stripMargin)

            override def setupStubs(): StubMapping = {
              AuditStub.audit()
              AuthStub.authorised()
              PenaltiesStub.onError(PenaltiesStub.GET, FinancialDataConstants.financialDataUrl(), NOT_FOUND, errorBody)
            }

            val response: WSResponse = await(request.get())
            response.status shouldBe NOT_FOUND
            response.json shouldBe Json.toJson(FinancialNotDataFound)
            response.header("Content-Type") shouldBe Some("application/json")
          }
        }

        "unexpected error" must {

          "return 500" in new Test {

            val errorBody: JsValue = Json.parse(
              """
                |{
                |"failures": [{
                | "code":"REASON",
                | "reason":"error"
                |}]
                |}
                |""".stripMargin)

            override def setupStubs(): StubMapping = {
              AuditStub.audit()
              AuthStub.authorised()
              PenaltiesStub.onError(PenaltiesStub.GET, FinancialDataConstants.financialDataUrl(), INTERNAL_SERVER_ERROR, errorBody)
            }

            val response: WSResponse = await(request.get())
            response.status shouldBe INTERNAL_SERVER_ERROR
            response.json shouldBe Json.toJson(MtdError("REASON", "error"))
            response.header("Content-Type") shouldBe Some("application/json")
          }
        }
      }
    }
  }
}
