package uk.gov.hmrc.vatapi.resources

import com.github.tomakehurst.wiremock.stubbing.StubMapping
import play.api.http.Status._
import play.api.libs.json.Json
import play.api.libs.ws.{WSRequest, WSResponse}
import uk.gov.hmrc.assets.des.Obligations.{Obligations, ObligationsWithNoIncomeSourceType, ObligationsWithoutIdentification}
import uk.gov.hmrc.assets.des.{Errors => DesError}
import uk.gov.hmrc.domain.Vrn
import uk.gov.hmrc.support.BaseFunctionalSpec
import uk.gov.hmrc.vatapi.models.Errors
import uk.gov.hmrc.vatapi.models.Errors.{ClientOrAgentNotAuthorized, Error}
import uk.gov.hmrc.vatapi.stubs.{AuditStub, AuthStub, DesStub}

class ObligationsResourceISpec extends BaseFunctionalSpec {

  private trait Test {

    def setupStubs(): StubMapping

    def uri: String

    def desUrl(vrn: Vrn) = s"/enterprise/obligation-data/vrn/$vrn/VATC"

    def request(): WSRequest = {
      setupStubs()
      buildRequest(uri)
    }
  }

  def errorBody(code: String): String =
    s"""
       |      {
       |        "code": "$code",
       |        "reason": "des message"
       |      }
      """.stripMargin

  "retrieveObligations" should {

    "reject client with no authorization" in new Test {
      override def setupStubs(): StubMapping = {
        AuditStub.audit()
        AuthStub.unauthorisedNotLoggedIn()
      }

      override def uri: String = s"/$vrn/obligations?from=2017-01-01&to=2017-03-31"
      val response: WSResponse = await(request().withHttpHeaders("Accept" -> "application/vnd.hmrc.1.0+json").get())
      response.status shouldBe FORBIDDEN
      response.json shouldBe Json.toJson(ClientOrAgentNotAuthorized)
    }

    "return code 400 when vrn is invalid" in new Test {
      override def setupStubs(): StubMapping = {
                AuditStub.audit()
              }

      override def uri: String = s"/abc/obligations"
      val response: WSResponse = await(request().withHttpHeaders("Accept" -> "application/vnd.hmrc.1.0+json").get())
      response.status shouldBe BAD_REQUEST
      response.json shouldBe  Json.toJson(Error("VRN_INVALID", "The provided Vrn is invalid", None))
    }

    "return code 400 when status is A" in new Test {
      override def setupStubs(): StubMapping = {
        AuthStub.authorised()
        AuditStub.audit()
      }

      override def uri: String = s"/$vrn/obligations?from=2017-01-01&to=2017-03-31&status=A"
      val response: WSResponse = await(request().withHttpHeaders("Accept" -> "application/vnd.hmrc.1.0+json").get())
      response.status shouldBe BAD_REQUEST
      response.json shouldBe Json.parse("""{"statusCode":400,"message":"INVALID_STATUS"}""")
    }

    "return code 400 when from is missing" in new Test {
      override def setupStubs(): StubMapping = {
        AuthStub.authorised()
        AuditStub.audit()
      }

      override def uri: String = s"/$vrn/obligations?to=2017-03-31&status=A"
      val response: WSResponse = await(request().withHttpHeaders("Accept" -> "application/vnd.hmrc.1.0+json").get())
      response.status shouldBe BAD_REQUEST
      response.json shouldBe Json.parse("""{"statusCode":400,"message":"INVALID_DATE_FROM"}""")
    }

    "return code 400 when from is invalid" in new Test {
      override def setupStubs(): StubMapping = {
        AuthStub.authorised()
        AuditStub.audit()
      }

      override def uri: String = s"/$vrn/obligations?from=abc&to=2017-03-31&status=A"
      val response: WSResponse = await(request().withHttpHeaders("Accept" -> "application/vnd.hmrc.1.0+json").get())
      response.status shouldBe BAD_REQUEST
      response.json shouldBe Json.parse("""{"statusCode":400,"message":"INVALID_DATE_FROM"}""")
    }

    "return code 400 when to is missing" in new Test {
      override def setupStubs(): StubMapping = {
        AuthStub.authorised()
        AuditStub.audit()
      }

      override def uri: String = s"/$vrn/obligations?from=2017-01-01&status=A"
      val response: WSResponse = await(request().withHttpHeaders("Accept" -> "application/vnd.hmrc.1.0+json").get())
      response.status shouldBe BAD_REQUEST
      response.json shouldBe Json.parse("""{"statusCode":400,"message":"INVALID_DATE_TO"}""")
    }

    "return code 400 when to is invalid" in new Test {
      override def setupStubs(): StubMapping = {
        AuthStub.authorised()
        AuditStub.audit()
      }

      override def uri: String = s"/$vrn/obligations?from=2017-01-01&to=abc&status=A"
      val response: WSResponse = await(request().withHttpHeaders("Accept" -> "application/vnd.hmrc.1.0+json").get())
      response.status shouldBe BAD_REQUEST
      response.json shouldBe Json.parse("""{"statusCode":400,"message":"INVALID_DATE_TO"}""")
    }

    "return code 400 when status is invalid" in new Test {
      override def setupStubs(): StubMapping = {
        AuthStub.authorised()
        AuditStub.audit()
      }

      override def uri: String = s"/$vrn/obligations?from=2017-01-01&to=2017-03-31&status=X"
      val response: WSResponse = await(request().withHttpHeaders("Accept" -> "application/vnd.hmrc.1.0+json").get())
      response.status shouldBe BAD_REQUEST
      response.json shouldBe Json.parse("""{"statusCode":400,"message":"INVALID_STATUS"}""")
    }

    "return code 400 when from is after to" in new Test {
      override def setupStubs(): StubMapping = {
        AuthStub.authorised()
        AuditStub.audit()
      }

      override def uri: String = s"/$vrn/obligations?from=2017-04-01&to=2017-03-31&status=F"
      val response: WSResponse = await(request().withHttpHeaders("Accept" -> "application/vnd.hmrc.1.0+json").get())
      response.status shouldBe BAD_REQUEST
      response.json shouldBe Json.parse("""{"statusCode":400,"message":"INVALID_DATE_RANGE"}""")
    }

    "return code 400 when date range between from and to is more than 366 days" in new Test {
      override def setupStubs(): StubMapping = {
        AuthStub.authorised()
        AuditStub.audit()
      }

      override def uri: String = s"/$vrn/obligations?from=2017-01-01&to=2018-01-02&status=O"
      val response: WSResponse = await(request().withHttpHeaders("Accept" -> "application/vnd.hmrc.1.0+json").get())
      response.status shouldBe BAD_REQUEST
      response.json shouldBe Json.parse("""{"statusCode":400,"message":"INVALID_DATE_RANGE"}""")
    }

    "return code 400 when idNumber parameter is invalid" in new Test {
      override def setupStubs(): StubMapping = {
        AuditStub.audit()
        AuthStub.authorised()
        DesStub.onError(DesStub.GET, desUrl(vrn), Map("from" -> "2017-01-01", "to" -> "2017-08-31"), BAD_REQUEST, DesError.invalidIdNumber)
      }

      override def uri: String = s"/$vrn/obligations?from=2017-01-01&to=2017-08-31&status=O"
      val response: WSResponse = await(request().withHttpHeaders("Accept" -> "application/vnd.hmrc.1.0+json").get())
      response.status shouldBe BAD_REQUEST
      response.json shouldBe Json.toJson(Errors.VrnInvalid)
    }


    "return code 404 when obligations does not exist" in new Test {
      override def setupStubs(): StubMapping = {
        AuditStub.audit()
        AuthStub.authorised()
        DesStub.onError(DesStub.GET, desUrl(vrn), Map("from" -> "2017-01-01", "to" -> "2017-08-31"), NOT_FOUND, DesError.notFound)
      }

      override def uri: String = s"/$vrn/obligations?from=2017-01-01&to=2017-08-31"
      val response: WSResponse = await(request().withHttpHeaders("Accept" -> "application/vnd.hmrc.1.0+json").get())
      response.status shouldBe NOT_FOUND
      response.json shouldBe Json.toJson(Errors.NotFound)
    }

    "return code 500 when regime type parameter is invalid" in new Test {
      override def setupStubs(): StubMapping = {
        AuditStub.audit()
        AuthStub.authorised()
        DesStub.onError(DesStub.GET, desUrl(vrn), Map("from" -> "2017-01-01", "to" -> "2017-08-31"), BAD_REQUEST, DesError.invalidRegime)
      }

      override def uri: String = s"/$vrn/obligations?from=2017-01-01&to=2017-08-31"
      val response: WSResponse = await(request().withHttpHeaders("Accept" -> "application/vnd.hmrc.1.0+json").get())
      response.status shouldBe INTERNAL_SERVER_ERROR
    }

    "return code 400 when status parameter is invalid" in new Test {
      override def setupStubs(): StubMapping = {
        AuditStub.audit()
        AuthStub.authorised()
        DesStub.onError(DesStub.GET, desUrl(vrn), Map("from" -> "2017-01-01", "to" -> "2017-08-31", "status" -> "A"), BAD_REQUEST, DesError.invalidStatus)
      }

      override def uri: String = s"/$vrn/obligations?from=2017-01-01&to=2017-08-31&status=A"
      val response: WSResponse = await(request().withHttpHeaders("Accept" -> "application/vnd.hmrc.1.0+json").get())
      response.status shouldBe BAD_REQUEST
    }

    "return code 500 when idType parameter is invalid" in new Test {
      override def setupStubs(): StubMapping = {
        AuditStub.audit()
        AuthStub.authorised()
        DesStub.onError(DesStub.GET, desUrl(vrn), Map("from" -> "2017-01-01", "to" -> "2017-08-31"), BAD_REQUEST, DesError.invalidIdType)
      }

      override def uri: String = s"/$vrn/obligations?from=2017-01-01&to=2017-08-31"
      val response: WSResponse = await(request().withHttpHeaders("Accept" -> "application/vnd.hmrc.1.0+json").get())
      response.status shouldBe INTERNAL_SERVER_ERROR
    }

    "return code 200 with a set of obligations" in new Test {

      override def uri: String = s"/$vrn/obligations?from=2017-01-01&to=2017-08-31"
      override def setupStubs(): StubMapping = {
        AuditStub.audit()
        AuthStub.authorised()
        DesStub.onSuccess(DesStub.GET, desUrl(vrn), Map("from" -> "2017-01-01", "to" -> "2017-08-31"), OK, Json.parse(Obligations(vrn.toString())))
      }
      val response: WSResponse = await(request().withHttpHeaders("Accept" -> "application/vnd.hmrc.1.0+json").get())
      response.status shouldBe OK
      response.json shouldBe Jsons.Obligations()
    }

    "return code 200 with a set of obligations with out identifications" in new Test {

      override def uri: String = s"/$vrn/obligations?from=2017-01-01&to=2017-08-31"
      override def setupStubs(): StubMapping = {
        AuditStub.audit()
        AuthStub.authorised()
        DesStub.onSuccess(DesStub.GET, desUrl(vrn), Map("from" -> "2017-01-01", "to" -> "2017-08-31"), OK, Json.parse(ObligationsWithoutIdentification(vrn.toString())))
      }
      val response: WSResponse = await(request().withHttpHeaders("Accept" -> "application/vnd.hmrc.1.0+json").get())
      response.status shouldBe OK
      response.json shouldBe Jsons.Obligations()
    }

    "return code 200 with a set of obligations with identifications but no incomeSourceType" in  new Test {

      override def uri: String = s"/$vrn/obligations?from=2017-01-01&to=2017-08-31"
      override def setupStubs(): StubMapping = {
        AuditStub.audit()
        AuthStub.authorised()
        DesStub.onSuccess(DesStub.GET, desUrl(vrn), Map("from" -> "2017-01-01", "to" -> "2017-08-31"), OK, Json.parse(ObligationsWithNoIncomeSourceType(vrn.toString())))
      }
      val response: WSResponse = await(request().withHttpHeaders("Accept" -> "application/vnd.hmrc.1.0+json").get())
      response.status shouldBe OK
      response.json shouldBe Jsons.Obligations()
    }

    "return code 200 with two sets of obligations" in new Test {

      override def uri: String = s"/$vrn/obligations?from=2017-01-01&to=2017-08-31"
      override def setupStubs(): StubMapping = {
        AuditStub.audit()
        AuthStub.authorised()
        DesStub.onSuccess(DesStub.GET, desUrl(vrn), Map("from" -> "2017-01-01", "to" -> "2017-08-31"), OK, Jsons.Obligations.desResponseTwo(vrn))
      }
      val response: WSResponse = await(request().withHttpHeaders("Accept" -> "application/vnd.hmrc.1.0+json").get())
      response.status shouldBe OK
      response.json shouldBe Jsons.Obligations.responseTwo()
    }

    "return code 200 with two sets of obligations but no identification" in new Test {

      override def uri: String = s"/$vrn/obligations?from=2017-01-01&to=2017-08-31"
      override def setupStubs(): StubMapping = {
        AuditStub.audit()
        AuthStub.authorised()
        DesStub.onSuccess(DesStub.GET, desUrl(vrn), Map("from" -> "2017-01-01", "to" -> "2017-08-31"), OK, Jsons.Obligations.desResponseTwoWithNoidentification(vrn))
      }
      val response: WSResponse = await(request().withHttpHeaders("Accept" -> "application/vnd.hmrc.1.0+json").get())
      response.status shouldBe OK
      response.json shouldBe Jsons.Obligations.responseTwo()
    }
  }

}
