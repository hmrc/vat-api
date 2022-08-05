package v1.endpoints

import com.github.tomakehurst.wiremock.stubbing.StubMapping
import config.AppConfig
import play.api.http.HeaderNames.ACCEPT
import play.api.http.Status._
import play.api.libs.json.{JsValue, Json}
import play.api.libs.ws.{WSRequest, WSResponse}
import play.api.test.Helpers.AUTHORIZATION
import support.IntegrationBaseSpec
import v1.constants.PenaltiesConstants
import v1.models.errors.{MtdError, PenaltiesInvalidCorrelationId, PenaltiesNotDataFound, VrnFormatError}
import v1.stubs.{AuditStub, AuthStub, PenaltiesStub}

class PenaltiesControllerISpec extends IntegrationBaseSpec {

  implicit val appConfig = app.injector.instanceOf[AppConfig]

  private trait Test {

    def uri: String = s"/${PenaltiesConstants.vrn}/penalties"

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

  "PenaltiesController" when {

    "GET /[VRN]/penalties" when {

      "raw vrn cannot be parsed" must {

        "return BadRequest" in new Test {

          override def uri: String = s"/${PenaltiesConstants.invalidVrn}/penalties"

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
              PenaltiesStub.onSuccess(PenaltiesStub.GET, PenaltiesConstants.penaltiesURl(), OK, PenaltiesConstants.testPenaltiesResponseJsonMax)
            }

            val response: WSResponse = await(request.get())
            response.status shouldBe OK
            response.json shouldBe PenaltiesConstants.testPenaltiesResponseJsonMax
            response.header("Content-Type") shouldBe Some("application/json")
          }
        }

        "an invalid request is made" must {

          "return 500" in new Test {
            val errorBody: JsValue = Json.parse(
              """
                |{
                |"failures": [{
                | "code":"DOWNSTREAM_ERROR",
                | "reason":"test exception"
                |}]
                |}
                |""".stripMargin)
            override def setupStubs(): StubMapping = {
              AuditStub.audit()
              AuthStub.authorised()
              PenaltiesStub.onError(PenaltiesStub.GET, PenaltiesConstants.penaltiesURl(), INTERNAL_SERVER_ERROR, errorBody)
            }

            val response: WSResponse = await(request.get())
            response.status shouldBe INTERNAL_SERVER_ERROR
            response.json shouldBe Json.toJson(PenaltiesConstants.errorWrapper(MtdError("DOWNSTREAM_ERROR", "test exception")))
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
              PenaltiesStub.onError(PenaltiesStub.GET, PenaltiesConstants.penaltiesURl(), BAD_REQUEST, errorBody)
            }

            val response: WSResponse = await(request.get())
            response.status shouldBe BAD_REQUEST
            response.json shouldBe Json.toJson(PenaltiesInvalidCorrelationId)
            response.header("Content-Type") shouldBe Some("application/json")
          }

          "return multiple 400 errors" in new Test {

            val errorBody: JsValue = Json.parse(
              """
                |{
                |"failures": [{
                | "code":"INVALID_CORRELATIONID",
                | "reason":"Some Reason"
                |},
                |{
                | "code":"INVALID_DATELIMIT",
                | "reason":"Some Reason"
                |}
                |]
                |}
                |""".stripMargin)

            val expectedJson: JsValue = Json.parse(
              """
                |{
                |"code":"INVALID_CORRELATIONID",
                |"message":"Invalid correlation ID",
                |"errors": [{
                | "code":"INVALID_DATELIMIT",
                | "message":"Invalid Date Limit"
                |}]
                |}
                |""".stripMargin
            )

            override def setupStubs(): StubMapping = {
              AuditStub.audit()
              AuthStub.authorised()
              PenaltiesStub.onError(PenaltiesStub.GET, PenaltiesConstants.penaltiesURl(), BAD_REQUEST, errorBody)
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
              PenaltiesStub.onError(PenaltiesStub.GET, PenaltiesConstants.penaltiesURl(), NOT_FOUND, errorBody)
            }

            val response: WSResponse = await(request.get())
            response.status shouldBe NOT_FOUND
            response.json shouldBe Json.toJson(PenaltiesNotDataFound)
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
                | "reason":"Some Reason"
                |}]
                |}
                |""".stripMargin)

            override def setupStubs(): StubMapping = {
              AuditStub.audit()
              AuthStub.authorised()
              PenaltiesStub.onError(PenaltiesStub.GET, PenaltiesConstants.penaltiesURl(), INTERNAL_SERVER_ERROR, errorBody)
            }

            val response: WSResponse = await(request.get())
            response.status shouldBe INTERNAL_SERVER_ERROR
            response.json shouldBe Json.toJson(MtdError("REASON", "Some Reason"))
            response.header("Content-Type") shouldBe Some("application/json")
          }
        }
      }
    }
  }
}
