package uk.gov.hmrc.vatapi.resources

import com.github.tomakehurst.wiremock.stubbing.StubMapping
import play.api.http.Status._
import play.api.libs.json.Json
import play.api.libs.ws.{WSRequest, WSResponse}
import uk.gov.hmrc.assets.des.{FinancialData, Errors => DesErrors}
import uk.gov.hmrc.domain.Vrn
import uk.gov.hmrc.support.BaseFunctionalSpec
import uk.gov.hmrc.vatapi.models.Errors
import uk.gov.hmrc.vatapi.models.Errors.{ClientOrAgentNotAuthorized, Error}
import uk.gov.hmrc.vatapi.stubs.{AuditStub, AuthStub, DesStub}

class FinancialDataResourceISpec extends BaseFunctionalSpec {

  def queryString(from: String, to: String) = Map("dateFrom" -> from, "dateTo" -> to, "onlyOpenItems" -> "false", "includeLocks" -> "false",
    "calculateAccruedInterest" -> "true", "customerPaymentInformation" -> "true")

  private trait Test {

    def setupStubs(): StubMapping

    def uri: String

    def desUrl(vrn: Vrn) = s"/enterprise/financial-data/VRN/$vrn/VATC"

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

  "FinancialDataResource.getLiabilities" when {
    "a valid request is made" should {

      "reject client with no authorization" in new Test {
        override def setupStubs(): StubMapping = {
          //AuditStub.audit()
          AuthStub.unauthorisedNotLoggedIn()
        }

        override def uri: String = s"/$vrn/liabilities?from=2017-01-01&to=2017-06-02"

        val response: WSResponse = await(request().withHttpHeaders("Accept" -> "application/vnd.hmrc.1.0+json").get())
        response.status shouldBe FORBIDDEN
        response.json shouldBe Json.toJson(ClientOrAgentNotAuthorized)
      }

      "retrieve a single liability where they exist" in new Test {

        override def uri: String = s"/$vrn/liabilities?from=2017-01-01&to=2017-06-02"

        override def setupStubs(): StubMapping = {
          //AuditStub.audit()
          AuthStub.authorised()
          DesStub.onSuccess(DesStub.GET, desUrl(vrn), queryString("2017-01-01", "2017-06-02"), OK, FinancialData.oneLiability)
        }

        val response: WSResponse = await(request().withHttpHeaders("Accept" -> "application/vnd.hmrc.1.0+json").get())
        response.status shouldBe OK
        response.json shouldBe Jsons.FinancialData.oneLiability
      }

      "retrieve a single liability where multiple liabilities exist with only one within the specific period to date - Param to date is after period to date" in new Test {

        override def uri: String = s"/$vrn/liabilities?from=2017-01-01&to=2017-06-02"

        override def setupStubs(): StubMapping = {
          //AuditStub.audit()
          AuthStub.authorised()
          DesStub.onSuccess(DesStub.GET, desUrl(vrn), queryString("2017-01-01", "2017-06-02"), OK, FinancialData.oneLiability)
        }

        val response: WSResponse = await(request().withHttpHeaders("Accept" -> "application/vnd.hmrc.1.0+json").get())
        response.status shouldBe OK
        response.json shouldBe Jsons.FinancialData.oneLiability
      }

      "retrieve a single liability where multiple liabilities exist with only one within the specific period to date - Param to date before period to date" in new Test {

        override def uri: String = s"/$vrn/liabilities?from=2017-01-01&to=2017-03-30"

        override def setupStubs(): StubMapping = {
          //AuditStub.audit()
          AuthStub.authorised()
          DesStub.onSuccess(DesStub.GET, desUrl(vrn), queryString("2017-01-01", "2017-03-30"), OK, FinancialData.oneLiability)
        }

        val response: WSResponse = await(request().withHttpHeaders("Accept" -> "application/vnd.hmrc.1.0+json").get())
        response.status shouldBe NOT_FOUND
        response.json shouldBe Json.toJson(uk.gov.hmrc.vatapi.models.Errors.NotFound)
      }

      "retrieve a single liability where multiple liabilities exist with only one within the specific period to date - Param to date is equal to period to date " in new Test {

        override def uri: String = s"/$vrn/liabilities?from=2017-01-01&to=2017-03-31"

        override def setupStubs(): StubMapping = {
          AuditStub.audit()
          AuthStub.authorised()
          DesStub.onSuccess(DesStub.GET, desUrl(vrn), queryString("2017-01-01", "2017-03-31"), OK, FinancialData.oneLiability)
        }

        val response: WSResponse = await(request().withHttpHeaders("Accept" -> "application/vnd.hmrc.1.0+json").get())
        response.status shouldBe OK
        response.json shouldBe Jsons.FinancialData.oneLiability
      }

      "retrieve a single liability where the minimum data exists" in new Test {

        override def uri: String = s"/$vrn/liabilities?from=2017-01-01&to=2017-06-02"

        override def setupStubs(): StubMapping = {
          //AuditStub.audit()
          AuthStub.authorised()
          DesStub.onSuccess(DesStub.GET, desUrl(vrn), queryString("2017-01-01", "2017-06-02"), OK, FinancialData.minLiability)
        }

        val response: WSResponse = await(request().withHttpHeaders("Accept" -> "application/vnd.hmrc.1.0+json").get())
        response.status shouldBe OK
        response.json shouldBe Jsons.FinancialData.minLiability
      }

      "retrieve a single liability if DES returns two liabilities and the second liability overlaps the supplied 'to' date" in new Test {

        override def uri: String = s"/$vrn/liabilities?from=2017-01-01&to=2017-06-02"

        override def setupStubs(): StubMapping = {
          //AuditStub.audit()
          AuthStub.authorised()
          DesStub.onSuccess(DesStub.GET, desUrl(vrn), queryString("2017-01-01", "2017-06-02"), OK, FinancialData.liabilitiesOverlapping)
        }

        val response: WSResponse = await(request().withHttpHeaders("Accept" -> "application/vnd.hmrc.1.0+json").get())
        response.status shouldBe OK
        response.json shouldBe Jsons.FinancialData.oneLiability
      }

      "retrieve multiple liabilities where they exist" in new Test {

        override def uri: String = s"/$vrn/liabilities?from=2017-01-01&to=2017-12-31"

        override def setupStubs(): StubMapping = {
          //AuditStub.audit()
          AuthStub.authorised()
          DesStub.onSuccess(DesStub.GET, desUrl(vrn), queryString("2017-01-01", "2017-12-31"), OK, FinancialData.multipleLiabilities)
        }

        val response: WSResponse = await(request().withHttpHeaders("Accept" -> "application/vnd.hmrc.1.0+json").get())
        response.status shouldBe OK
        response.json shouldBe Jsons.FinancialData.multipleLiabilities
      }

      "retrieve multiple liabilities where they exist excluding Payment on Account" in new Test {

        override def uri: String = s"/$vrn/liabilities?from=2017-01-01&to=2017-12-31"

        override def setupStubs(): StubMapping = {
          //AuditStub.audit()
          AuthStub.authorised()
          DesStub.onSuccess(DesStub.GET, desUrl(vrn), queryString("2017-01-01", "2017-12-31"), OK, FinancialData.multipleLiabilitiesWithPaymentOnAccount)
        }

        val response: WSResponse = await(request().withHttpHeaders("Accept" -> "application/vnd.hmrc.1.0+json").get())
        response.status shouldBe OK
        response.json shouldBe Jsons.FinancialData.multipleLiabilitiesWithoutNoHybrids
      }

      "return code 400 when idNumber parameter is invalid" in new Test {

        override def uri: String = s"/$vrn/liabilities?from=2017-01-01&to=2017-12-31"

        override def setupStubs(): StubMapping = {
          //AuditStub.audit()
          AuthStub.authorised()
          DesStub.onError(DesStub.GET, desUrl(vrn), queryString("2017-01-01", "2017-12-31"), BAD_REQUEST, DesErrors.invalidIdNumber)
        }

        val response: WSResponse = await(request().withHttpHeaders("Accept" -> "application/vnd.hmrc.1.0+json").get())
        response.status shouldBe BAD_REQUEST
        response.json shouldBe Json.toJson(Errors.VrnInvalid)
      }


      "return a 404 (Not Found) if no liabilities exist" in new Test {

        override def uri: String = s"/$vrn/liabilities?from=2017-01-01&to=2017-12-31"

        override def setupStubs(): StubMapping = {
          //AuditStub.audit()
          AuthStub.authorised()
          DesStub.onSuccess(DesStub.GET, desUrl(vrn), queryString("2017-01-01", "2017-12-31"), OK, FinancialData.emptyLiabilities)
        }

        val response: WSResponse = await(request().withHttpHeaders("Accept" -> "application/vnd.hmrc.1.0+json").get())
        response.status shouldBe NOT_FOUND
      }
    }

    "return code 500 when idType parameter is invalid" in new Test {

      override def uri: String = s"/$vrn/liabilities?from=2017-01-01&to=2017-12-31"

      override def setupStubs(): StubMapping = {
        //AuditStub.audit()
        AuthStub.authorised()
        DesStub.onError(DesStub.GET, desUrl(vrn), queryString("2017-01-01", "2017-12-31"), BAD_REQUEST, DesErrors.invalidIdType)
      }

      val response: WSResponse = await(request().withHttpHeaders("Accept" -> "application/vnd.hmrc.1.0+json").get())
      response.status shouldBe INTERNAL_SERVER_ERROR
    }

    "return code 500 when regime type parameter is invalid" in new Test {

      override def uri: String = s"/$vrn/liabilities?from=2017-01-01&to=2017-12-31"

      override def setupStubs(): StubMapping = {
        //AuditStub.audit()
        AuthStub.authorised()
        DesStub.onError(DesStub.GET, desUrl(vrn), queryString("2017-01-01", "2017-12-31"), BAD_REQUEST, DesErrors.invalidRegimeType)
      }

      val response: WSResponse = await(request().withHttpHeaders("Accept" -> "application/vnd.hmrc.1.0+json").get())
      response.status shouldBe INTERNAL_SERVER_ERROR
    }

    "return code 500 when openitems parameter is invalid" in new Test {

      override def uri: String = s"/$vrn/liabilities?from=2017-01-01&to=2017-12-31"

      override def setupStubs(): StubMapping = {
        AuthStub.authorised()
        AuthStub.authorised()
        DesStub.onError(DesStub.GET, desUrl(vrn), queryString("2017-01-01", "2017-12-31"), BAD_REQUEST, DesErrors.invalidOnlyOpenItems)
      }

      val response: WSResponse = await(request().withHttpHeaders("Accept" -> "application/vnd.hmrc.1.0+json").get())
      response.status shouldBe INTERNAL_SERVER_ERROR
    }

    "a date range of greater than 1 year is supplied" should {
      "return an INVALID_DATE_RANGE error" in new Test {
        override def setupStubs(): StubMapping = {
          AuthStub.authorised()
        }

        override def uri: String = s"/$vrn/liabilities?from=2017-01-01&to=2019-01-01"

        val response: WSResponse = await(request().withHttpHeaders("Accept" -> "application/vnd.hmrc.1.0+json").get())
        response.status shouldBe BAD_REQUEST
        response.json shouldBe Json.parse("""{"statusCode":400,"message":"DATE_RANGE_INVALID"}""")
      }
    }

    "an invalid 'from' date is supplied" should {
      "return an INVALID_DATE_TO error" in new Test {
        override def setupStubs(): StubMapping = {
          AuthStub.authorised()
        }

        override def uri: String = s"/$vrn/liabilities?from=2017-01-01&to=3017-12-31"

        val response: WSResponse = await(request().withHttpHeaders("Accept" -> "application/vnd.hmrc.1.0+json").get())
        response.status shouldBe BAD_REQUEST
        response.json shouldBe Json.parse("""{"statusCode":400,"message":"DATE_TO_INVALID"}""")
      }
    }

    "an invalid 'to' date is supplied" should {
      "return and INVALID_DATE_FROM error" in new Test {
        override def setupStubs(): StubMapping = {
          AuthStub.authorised()
        }

        override def uri: String = s"/$vrn/liabilities?from=2001-01-01&to=2017-12-31"

        val response: WSResponse = await(request().withHttpHeaders("Accept" -> "application/vnd.hmrc.1.0+json").get())
        response.status shouldBe BAD_REQUEST
        response.json shouldBe Json.parse("""{"statusCode":400,"message":"DATE_FROM_INVALID"}""")
      }
    }

    "an invalid VRN is supplied" should {
      "return an VRN_INVALID error" in new Test {
        override def setupStubs(): StubMapping = {
          AuthStub.authorised()
        }

        override def uri: String = s"/invalidvrn/liabilities?from=2015-01-01&to=2017-12-31"

        val response: WSResponse = await(request().withHttpHeaders("Accept" -> "application/vnd.hmrc.1.0+json").get())
        response.status shouldBe BAD_REQUEST
        response.json shouldBe Json.toJson(Error("VRN_INVALID", "The provided Vrn is invalid", None))
      }
    }
  }


 "FinancialDataResource.getPayments" when {
   "a valid request is made" should {
     "retrieve a single payment where they exist" in new Test {

       override def uri: String = s"/$vrn/payments?from=2017-01-01&to=2017-06-02"

       override def setupStubs(): StubMapping = {
         //AuditStub.audit()
         AuthStub.authorised()
         DesStub.onSuccess(DesStub.GET, desUrl(vrn), queryString("2017-01-01", "2017-06-02"), OK, FinancialData.onePayment)
       }

       val response: WSResponse = await(request().withHttpHeaders("Accept" -> "application/vnd.hmrc.1.0+json").get())
       response.status shouldBe OK
       response.json shouldBe Jsons.FinancialData.onePayment
     }

     "retrieve a single payment where the minimum data exists" in new Test {

       override def uri: String = s"/$vrn/payments?from=2017-01-01&to=2017-06-02"

       override def setupStubs(): StubMapping = {
         //AuditStub.audit()
         AuthStub.authorised()
         DesStub.onSuccess(DesStub.GET, desUrl(vrn), queryString("2017-01-01", "2017-06-02"), OK, FinancialData.minPayment)
       }

       val response: WSResponse = await(request().withHttpHeaders("Accept" -> "application/vnd.hmrc.1.0+json").get())
       response.status shouldBe OK
       response.json shouldBe Jsons.FinancialData.minPayment
     }

     "return only those payments belonging to a liability that falls before the 'to' date" in new Test {

       def queryString(from: String, to: String) = Map("dateFrom" -> from, "dateTo" -> to, "onlyOpenItems" -> "false", "includeLocks" -> "false",
         "calculateAccruedInterest" -> "true", "customerPaymentInformation" -> "true")

       override def uri: String = s"/$vrn/payments?from=2017-01-01&to=2017-06-02"

       override def setupStubs(): StubMapping = {
         //AuditStub.audit()
         AuthStub.authorised()
         DesStub.onSuccess(DesStub.GET, desUrl(vrn), queryString("2017-01-01", "2017-06-02"), OK, FinancialData.overlappingPayment)
       }

       val response: WSResponse = await(request().withHttpHeaders("Accept" -> "application/vnd.hmrc.1.0+json").get())
       response.status shouldBe OK
       response.json shouldBe Jsons.FinancialData.onePayment
     }

     "retrieve multiple payments where they exist" in new Test {

       def queryString(from: String, to: String) = Map("dateFrom" -> from, "dateTo" -> to, "onlyOpenItems" -> "false", "includeLocks" -> "false",
         "calculateAccruedInterest" -> "true", "customerPaymentInformation" -> "true")

       override def uri: String = s"/$vrn/payments?from=2017-01-01&to=2017-12-31"

       override def setupStubs(): StubMapping = {
         //AuditStub.audit()
         AuthStub.authorised()
         DesStub.onSuccess(DesStub.GET, desUrl(vrn), queryString("2017-01-01", "2017-12-31"), OK, FinancialData.multiplePayments)
       }

       val response: WSResponse = await(request().withHttpHeaders("Accept" -> "application/vnd.hmrc.1.0+json").get())
       response.status shouldBe OK
       response.json shouldBe Jsons.FinancialData.multiplePayments
     }

     "return a 404 (Not Found) if no payments exist" in new Test {

       override def uri: String = s"/$vrn/payments?from=2017-01-01&to=2017-12-31"

       override def setupStubs(): StubMapping = {
         //AuditStub.audit()
         AuthStub.authorised()
         DesStub.onSuccess(DesStub.GET, desUrl(vrn), queryString("2017-01-01", "2017-12-31"), OK, FinancialData.noPayment)
       }

       val response: WSResponse = await(request().withHttpHeaders("Accept" -> "application/vnd.hmrc.1.0+json").get())
       response.status shouldBe NOT_FOUND
     }
   }

   "a date range of greater than 1 year is supplied" should {
     "return an INVALID_DATE_RANGE error" in new Test {
       override def setupStubs(): StubMapping = {
         AuthStub.authorised()
       }

       override def uri: String = s"/$vrn/payments?from=2017-01-01&to=2019-01-01"

       val response: WSResponse = await(request().withHttpHeaders("Accept" -> "application/vnd.hmrc.1.0+json").get())
       response.status shouldBe BAD_REQUEST
       response.json shouldBe Json.parse("""{"statusCode":400,"message":"DATE_RANGE_INVALID"}""")
     }
   }

   "an invalid 'from' date is supplied" should {
     "return an INVALID_DATE_TO error" in new Test {
       override def setupStubs(): StubMapping = {
         AuthStub.authorised()
       }

       override def uri: String = s"/$vrn/payments?from=2017-01-01&to=3017-12-31"

       val response: WSResponse = await(request().withHttpHeaders("Accept" -> "application/vnd.hmrc.1.0+json").get())
       response.status shouldBe BAD_REQUEST
       response.json shouldBe Json.parse("""{"statusCode":400,"message":"DATE_TO_INVALID"}""")
     }
   }

   "an invalid 'to' date is supplied" should {
     "return and INVALID_DATE_FROM error" in new Test {
       override def setupStubs(): StubMapping = {
         AuthStub.authorised()
       }

       override def uri: String = s"/$vrn/payments?from=2001-01-01&to=2017-12-31"

       val response: WSResponse = await(request().withHttpHeaders("Accept" -> "application/vnd.hmrc.1.0+json").get())
       response.status shouldBe BAD_REQUEST
       response.json shouldBe Json.parse("""{"statusCode":400,"message":"DATE_FROM_INVALID"}""")
     }
   }

   "an invalid VRN is supplied" should {
     "return an VRN_INVALID error" in new Test {
       override def setupStubs(): StubMapping = {
         AuthStub.authorised()
       }

       override def uri: String = s"/invalidvrn/payments?from=2015-01-01&to=2017-12-31"

       val response: WSResponse = await(request().withHttpHeaders("Accept" -> "application/vnd.hmrc.1.0+json").get())
       response.status shouldBe BAD_REQUEST
       response.json shouldBe Json.toJson(Error("VRN_INVALID", "The provided Vrn is invalid", None))
     }
   }
 }
}
