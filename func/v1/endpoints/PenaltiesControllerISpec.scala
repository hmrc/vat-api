package v1.endpoints

import com.github.tomakehurst.wiremock.stubbing.StubMapping
import config.AppConfig
import play.api.http.HeaderNames.ACCEPT
import play.api.http.Status._
import play.api.libs.json.Json
import play.api.libs.ws.{WSRequest, WSResponse}
import play.api.test.Helpers.AUTHORIZATION
import support.IntegrationBaseSpec
import v1.fixtures.PenaltiesFixture
import v1.models.errors.{UnexpectedFailure, VrnFormatError, VrnNotFound}
import v1.stubs.{AuditStub, AuthStub, PenaltiesStub}

class PenaltiesControllerISpec extends IntegrationBaseSpec with PenaltiesFixture {

  implicit val appConfig = app.injector.instanceOf[AppConfig]

  private trait Test {

    def uri: String = s"/$vrn/penalties"

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

          override def uri: String = s"/$invalidVrn/penalties"

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
              PenaltiesStub.onSuccess(PenaltiesStub.GET, penaltiesURl(), OK, testPenaltiesResponseJson)
            }

            val response: WSResponse = await(request.get())
            response.status shouldBe OK
            response.json shouldBe testPenaltiesResponseJson
            response.header("Content-Type") shouldBe Some("application/json")
          }
        }

        "VRN is invalid" must {

          "return 400" in new Test {

            override def setupStubs(): StubMapping = {
              AuditStub.audit()
              AuthStub.authorised()
              PenaltiesStub.onError(PenaltiesStub.GET, penaltiesURl(), BAD_REQUEST, "error")
            }

            val response: WSResponse = await(request.get())
            response.status shouldBe BAD_REQUEST
            response.json shouldBe Json.toJson(VrnFormatError)
            response.header("Content-Type") shouldBe Some("application/json")
          }
        }

        "VRN is not found" must {

          "return 404" in new Test {

            override def setupStubs(): StubMapping = {
              AuditStub.audit()
              AuthStub.authorised()
              PenaltiesStub.onError(PenaltiesStub.GET, penaltiesURl(), NOT_FOUND, "error")
            }

            val response: WSResponse = await(request.get())
            response.status shouldBe NOT_FOUND
            response.json shouldBe Json.toJson(VrnNotFound)
            response.header("Content-Type") shouldBe Some("application/json")
          }
        }

        "unexpected error" must {

          "return 500" in new Test {

            override def setupStubs(): StubMapping = {
              AuditStub.audit()
              AuthStub.authorised()
              PenaltiesStub.onError(PenaltiesStub.GET, penaltiesURl(), INTERNAL_SERVER_ERROR, "error")
            }

            val response: WSResponse = await(request.get())
            response.status shouldBe INTERNAL_SERVER_ERROR
            response.json shouldBe Json.toJson(UnexpectedFailure.mtdError(INTERNAL_SERVER_ERROR, "error"))
            response.header("Content-Type") shouldBe Some("application/json")
          }
        }
      }
    }
  }
}
