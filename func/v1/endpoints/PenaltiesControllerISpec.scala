package v1.endpoints

import com.github.tomakehurst.wiremock.stubbing.StubMapping
import config.AppConfig
import play.api.http.HeaderNames.ACCEPT
import play.api.http.Status.OK
import play.api.libs.ws.{WSRequest, WSResponse}
import play.api.test.Helpers.AUTHORIZATION
import support.IntegrationBaseSpec
import v1.fixtures.PenaltiesFixture
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
          response.header("Content-Type")  shouldBe Some("application/json")
        }
      }
    }
  }

}
