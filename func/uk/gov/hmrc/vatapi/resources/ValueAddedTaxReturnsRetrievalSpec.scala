package uk.gov.hmrc.vatapi.resources

import play.api.http.Status._
import com.github.tomakehurst.wiremock.stubbing.StubMapping
import play.api.libs.json.{JsValue, Json}
import play.api.libs.ws.{WSRequest, WSResponse}
import uk.gov.hmrc.assets.des.VatReturns
import uk.gov.hmrc.domain.Vrn
import uk.gov.hmrc.support.BaseFunctionalSpec
import uk.gov.hmrc.vatapi.models.Errors
import uk.gov.hmrc.vatapi.models.Errors.{ClientOrAgentNotAuthorized, DateRangeTooLarge, DuplicateVatSubmission, InvalidPeriodKey, InvalidRequest, NotFinalisedDeclaration, TaxPeriodNotEnded, VrnInvalid}
import uk.gov.hmrc.vatapi.stubs.{AuditStub, AuthStub, DesStub, NrsStub}

class ValueAddedTaxReturnsRetrievalSpec extends BaseFunctionalSpec {

  private def body(finalised: Boolean = true) =
    s"""{
          "periodKey": "#001",
          "vatDueSales": 50.00,
          "vatDueAcquisitions": 100.30,
          "totalVatDue": 150.30,
          "vatReclaimedCurrPeriod": 40.00,
          "netVatDue": 110.30,
          "totalValueSalesExVAT": 1000,
          "totalValuePurchasesExVAT": 200.00,
          "totalValueGoodsSuppliedExVAT": 100.00,
          "totalAcquisitionsExVAT": 540.00,
          "finalised": $finalised
        }"""

  private def requestWithNegativeAmounts(finalised: Boolean = true) =
    s"""{
          "periodKey": "#001",
          "vatDueSales": 50.00,
          "vatDueAcquisitions": 100.30,
          "totalVatDue": 150.30,
          "vatReclaimedCurrPeriod": 40.00,
          "netVatDue": 110.30,
          "totalValueSalesExVAT": -1000,
          "totalValuePurchasesExVAT": 200.00,
          "totalValueGoodsSuppliedExVAT": 100.00,
          "totalAcquisitionsExVAT": 540.00,
          "finalised": $finalised
        }"""

  val invalidJson: JsValue = Json.parse(
    """
      |{
      |   "periodKey": "#001",
      |   "vatDueSales": 50.00,
      |   "vatDueAcquisitions": 100.30,
      |   "totalVatDue": 150.30,
      |   "vatReclaimedCurrPeriod": 40.00,
      |   "netVatDue": 110.30,
      |   "totalValueSalesExVAT": 1000,
      |   "totalValuePurchasesExVAT": 200.00,
      |   "totalValueGoodsSuppliedExVAT": 100.00,
      |   "totalAcquisitionsExVAT": 540.00,
      |   "finalised": "thisiswrongsosowrong"
      |}
        """.stripMargin)

  private trait Test {

    def setupStubs(): StubMapping

    def uri: String

    def desUrl(vrn: Vrn) = s"/enterprise/return/vat/$vrn"

    def retrieveDesUrl(vrn: Vrn) = s"/vat/returns/vrn/$vrn"

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

  "VAT returns submission" should {

    "allow users to submit VAT returns" in new Test {

      override def uri: String = s"/$vrn/returns"

      override def setupStubs(): StubMapping = {
        AuditStub.audit()
        AuthStub.authorisedWithNrs()
        NrsStub.success()
        DesStub.onSuccess(DesStub.POST, desUrl(vrn), OK, Json.parse(VatReturns.submissionSuccessBody))
      }

      val response: WSResponse = await(request().
        withHttpHeaders("Accept" -> "application/vnd.hmrc.1.0+json", "Authorization" -> "Bearer testtoken").
        post(Json.parse(body())))
      response.status shouldBe CREATED
      response.json shouldBe Json.parse("""{"processingDate":"2018-03-01T11:43:43.195Z","paymentIndicator":"BANK","formBundleNumber":"891713832155"}""")
    }

    "return processing date with milliseconds and no paymentIndicator if DES returns them without" in new Test {

      override def uri: String = s"/$vrn/returns"

      override def setupStubs(): StubMapping = {
        AuditStub.audit()
        AuthStub.authorisedWithNrs()
        NrsStub.success()
        DesStub.onSuccess(DesStub.POST, desUrl(vrn), OK, Json.parse(VatReturns.successBodyWithoutPaymentIndicator))
      }

      val response: WSResponse = await(request().
        withHttpHeaders("Accept" -> "application/vnd.hmrc.1.0+json", "Authorization" -> "Bearer testtoken").
        post(Json.parse(body())))
      response.status shouldBe CREATED
      response.json shouldBe Json.parse("""{"processingDate":"2018-03-01T11:43:43.000Z","formBundleNumber":"891713832155"}""")
    }

    "allow users to submit VAT returns even with negative amounts" in new Test {

      override def uri: String = s"/$vrn/returns"

      override def setupStubs(): StubMapping = {
        AuditStub.audit()
        AuthStub.authorisedWithNrs()
        NrsStub.success()
        DesStub.onSuccess(DesStub.POST, desUrl(vrn), OK, Json.parse(VatReturns.submissionSuccessBody))
      }

      val response: WSResponse = await(request().
        withHttpHeaders("Accept" -> "application/vnd.hmrc.1.0+json", "Authorization" -> "Bearer testtoken").
        post(Json.parse(requestWithNegativeAmounts())))
      response.status shouldBe CREATED
      response.json shouldBe Json.parse("""{"processingDate":"2018-03-01T11:43:43.195Z","paymentIndicator":"BANK","formBundleNumber":"891713832155"}""")
    }

    "allow users to submit VAT returns for non bad_request NRS response" in new Test {

      override def uri: String = s"/$vrn/returns"

      override def setupStubs(): StubMapping = {
        AuditStub.audit()
        AuthStub.authorisedWithNrs()
        NrsStub.onError(FORBIDDEN)
        DesStub.onSuccess(DesStub.POST, desUrl(vrn), OK, Json.parse(VatReturns.submissionSuccessBody))
      }

      val response: WSResponse = await(request().
        withHttpHeaders("Accept" -> "application/vnd.hmrc.1.0+json", "Authorization" -> "Bearer testtoken").
        post(Json.parse(body())))
      response.status shouldBe CREATED
      response.json shouldBe Json.parse("""{"processingDate":"2018-03-01T11:43:43.195Z","paymentIndicator":"BANK","formBundleNumber":"891713832155"}""")
    }

    "reject client with no authorization" in new Test {
      override def setupStubs(): StubMapping = {
        AuditStub.audit()
        AuthStub.unauthorisedNotLoggedIn()
      }

      override def uri: String = s"/$vrn/returns"
      val response: WSResponse = await(request().
        withHttpHeaders("Accept" -> "application/vnd.hmrc.1.0+json", "Authorization" -> "Bearer testtoken").
        post(Json.parse(body())))
      response.status shouldBe FORBIDDEN
      response.json shouldBe Json.toJson(ClientOrAgentNotAuthorized)
    }

    "not allow users to submit undeclared VAT returns" in new Test {
      override def setupStubs(): StubMapping = {
        AuditStub.audit()
        AuthStub.authorisedWithNrs()
        NrsStub.success()
        DesStub.onSuccess(DesStub.POST, desUrl(vrn), OK, Json.parse(VatReturns.submissionSuccessBody))
      }

      override def uri: String = s"/$vrn/returns"
      val response: WSResponse = await(request().
        withHttpHeaders("Accept" -> "application/vnd.hmrc.1.0+json", "Authorization" -> "Bearer testtoken").
        post(Json.parse(body(false))))
      response.status shouldBe FORBIDDEN
      response.json shouldBe Json.toJson(Errors.businessError(NotFinalisedDeclaration))
    }

    "reject submission with invalid period key" in new Test {

      override def uri: String = s"/$vrn/returns"

      override def setupStubs(): StubMapping = {
        AuditStub.audit()
        AuthStub.authorisedWithNrs()
        NrsStub.success()
        DesStub.onError(DesStub.POST, desUrl(vrn), BAD_REQUEST, errorBody("INVALID_PERIODKEY"))
      }

      val response: WSResponse = await(request().
        withHttpHeaders("Accept" -> "application/vnd.hmrc.1.0+json", "Authorization" -> "Bearer testtoken").
        post(Json.parse(body())))
      response.status shouldBe BAD_REQUEST
      response.json shouldBe Json.toJson(InvalidPeriodKey)
    }

    "reject submission with invalid ARN" in new Test {

      override def uri: String = s"/$vrn/returns"

      override def setupStubs(): StubMapping = {
        AuditStub.audit()
        AuthStub.authorisedWithNrs()
        NrsStub.success()
        DesStub.onError(DesStub.POST, desUrl(vrn), BAD_REQUEST, errorBody("INVALID_ARN"))
      }

      val response: WSResponse = await(request().
        withHttpHeaders("Accept" -> "application/vnd.hmrc.1.0+json", "Authorization" -> "Bearer testtoken").
        post(Json.parse(body())))
      response.status shouldBe INTERNAL_SERVER_ERROR
    }

    "reject submission with invalid VRN" in new Test {

      override def uri: String = s"/$vrn/returns"

      override def setupStubs(): StubMapping = {
        AuditStub.audit()
        AuthStub.authorisedWithNrs()
        NrsStub.success()
        DesStub.onError(DesStub.POST, desUrl(vrn), BAD_REQUEST, errorBody("INVALID_VRN"))
      }

      val response: WSResponse = await(request().
        withHttpHeaders("Accept" -> "application/vnd.hmrc.1.0+json", "Authorization" -> "Bearer testtoken").
        post(Json.parse(body())))
      response.status shouldBe BAD_REQUEST
      response.json shouldBe Json.toJson(VrnInvalid)
    }

    "reject submission with invalid INVALID_ORIGINATOR_ID" in new Test {

      override def uri: String = s"/$vrn/returns"

      override def setupStubs(): StubMapping = {
        AuditStub.audit()
        AuthStub.authorisedWithNrs()
        NrsStub.success()
        DesStub.onError(DesStub.POST, desUrl(vrn), BAD_REQUEST, errorBody("INVALID_ORIGINATOR_ID"))
      }

      val response: WSResponse = await(request().
        withHttpHeaders("Accept" -> "application/vnd.hmrc.1.0+json", "Authorization" -> "Bearer testtoken").
        post(Json.parse(body())))
      response.status shouldBe INTERNAL_SERVER_ERROR
    }

    "reject submission with invalid payload" in new Test {

      override def uri: String = s"/$vrn/returns"

      override def setupStubs(): StubMapping = {
        AuditStub.audit()
        AuthStub.authorisedWithNrs()
        NrsStub.success()
        DesStub.onError(DesStub.POST, desUrl(vrn), BAD_REQUEST, errorBody("INVALID_PAYLOAD"))
      }

      val response: WSResponse = await(request().
        withHttpHeaders("Accept" -> "application/vnd.hmrc.1.0+json", "Authorization" -> "Bearer testtoken").
        post(Json.parse(body())))
      response.status shouldBe BAD_REQUEST
      response.json shouldBe Json.toJson(InvalidRequest)
    }

    "reject duplicate submission" in new Test {

      override def uri: String = s"/$vrn/returns"

      override def setupStubs(): StubMapping = {
        AuditStub.audit()
        AuthStub.authorisedWithNrs()
        NrsStub.success()
        DesStub.onError(DesStub.POST, desUrl(vrn), CONFLICT, errorBody("DUPLICATE_SUBMISSION"))
      }

      val response: WSResponse = await(request().
        withHttpHeaders("Accept" -> "application/vnd.hmrc.1.0+json", "Authorization" -> "Bearer testtoken").
        post(Json.parse(body())))
      response.status shouldBe FORBIDDEN
      response.json shouldBe Json.toJson(Errors.businessError(DuplicateVatSubmission))
    }

    "reject submission with malformed JSON and not expose internal class details" in new Test {
      override def uri: String = s"/$vrn/returns"

      override def setupStubs(): StubMapping = {
        AuditStub.audit()
        AuthStub.authorisedWithNrs()
      }

      val response: WSResponse = await(request().
        withHttpHeaders("Accept" -> "application/vnd.hmrc.1.0+json", "Authorization" -> "Bearer testtoken").post(invalidJson))

      response.status shouldBe BAD_REQUEST
    }

    "reject submissions that are made too early" in new Test {

      override def uri: String = s"/$vrn/returns"

      override def setupStubs(): StubMapping = {
        AuditStub.audit()
        AuthStub.authorisedWithNrs()
        NrsStub.success()
        DesStub.onError(DesStub.POST, desUrl(vrn), FORBIDDEN, errorBody("TAX_PERIOD_NOT_ENDED"))
      }

      val response: WSResponse = await(request().
        withHttpHeaders("Accept" -> "application/vnd.hmrc.1.0+json", "Authorization" -> "Bearer testtoken").
        post(Json.parse(body())))
      response.status shouldBe FORBIDDEN
      response.json shouldBe Json.toJson(TaxPeriodNotEnded)
    }

    "fail if submission to Non-Repudiation service failed" in new Test {

      override def uri: String = s"/$vrn/returns"

      override def setupStubs(): StubMapping = {
        AuditStub.audit()
        AuthStub.authorisedWithNrs()
        NrsStub.onError(BAD_REQUEST)
      }

      val response: WSResponse = await(request().
        withHttpHeaders("Accept" -> "application/vnd.hmrc.1.0+json", "Authorization" -> "Bearer testtoken").
        post(Json.parse(body())))
      response.status shouldBe INTERNAL_SERVER_ERROR
    }

    "pass if submission to Non-Repudiation service call times out" in new Test {

      override def uri: String = s"/$vrn/returns"

      override def setupStubs(): StubMapping = {
        AuditStub.audit()
        AuthStub.authorisedWithNrs()
        NrsStub.onError(499)
        DesStub.onSuccess(DesStub.POST, desUrl(vrn), OK, Json.parse(VatReturns.submissionSuccessBody))
      }

      val response: WSResponse = await(request().
        withHttpHeaders("Accept" -> "application/vnd.hmrc.1.0+json", "Authorization" -> "Bearer testtoken").
        post(Json.parse(body())))
      response.status shouldBe CREATED
      response.json shouldBe Json.parse("""{"processingDate":"2018-03-01T11:43:43.195Z","paymentIndicator":"BANK","formBundleNumber":"891713832155"}""")
    }

    "pass if Non-Repudiation service returns a 500 Internal Server Error" in new Test {

      override def uri: String = s"/$vrn/returns"

      override def setupStubs(): StubMapping = {
        AuditStub.audit()
        AuthStub.authorisedWithNrs()
        NrsStub.onError(INTERNAL_SERVER_ERROR)
        DesStub.onSuccess(DesStub.POST, desUrl(vrn), OK, Json.parse(VatReturns.submissionSuccessBody))
      }

      val response: WSResponse = await(request().
        withHttpHeaders("Accept" -> "application/vnd.hmrc.1.0+json", "Authorization" -> "Bearer testtoken").
        post(Json.parse(body())))
      response.status shouldBe CREATED
      response.json shouldBe Json.parse("""{"processingDate":"2018-03-01T11:43:43.195Z","paymentIndicator":"BANK","formBundleNumber":"891713832155"}""")
    }

    "pass if Non-Repudiation service returns a 502 Bad Gateway" in new Test {

      override def uri: String = s"/$vrn/returns"

      override def setupStubs(): StubMapping = {
        AuditStub.audit()
        AuthStub.authorisedWithNrs()
        NrsStub.onError(BAD_GATEWAY)
        DesStub.onSuccess(DesStub.POST, desUrl(vrn), OK, Json.parse(VatReturns.submissionSuccessBody))
      }

      val response: WSResponse = await(request().
        withHttpHeaders("Accept" -> "application/vnd.hmrc.1.0+json", "Authorization" -> "Bearer testtoken").
        post(Json.parse(body())))
      response.status shouldBe CREATED
      response.json shouldBe Json.parse("""{"processingDate":"2018-03-01T11:43:43.195Z","paymentIndicator":"BANK","formBundleNumber":"891713832155"}""")
    }

    "pass if Non-Repudiation service returns a 503 Service Unavailable" in new Test {

      override def uri: String = s"/$vrn/returns"

      override def setupStubs(): StubMapping = {
        AuditStub.audit()
        AuthStub.authorisedWithNrs()
        NrsStub.onError(SERVICE_UNAVAILABLE)
        DesStub.onSuccess(DesStub.POST, desUrl(vrn), OK, Json.parse(VatReturns.submissionSuccessBody))
      }

      val response: WSResponse = await(request().
        withHttpHeaders("Accept" -> "application/vnd.hmrc.1.0+json", "Authorization" -> "Bearer testtoken").
        post(Json.parse(body())))
      response.status shouldBe CREATED
      response.json shouldBe Json.parse("""{"processingDate":"2018-03-01T11:43:43.195Z","paymentIndicator":"BANK","formBundleNumber":"891713832155"}""")
    }
  }

  "VAT returns retrieval" should {

    "allow users to retrieve VAT returns for last four years" in new Test {

      override def uri: String = s"/$vrn/returns/0001"

      override def setupStubs(): StubMapping = {
        AuditStub.audit()
        AuthStub.authorised()
        DesStub.onSuccess(DesStub.GET, retrieveDesUrl(vrn), Map("period-key" -> "0001"), OK, VatReturns.retrieveVatReturnsDesSuccessBody)
      }

      val response: WSResponse = await(request().
        withHttpHeaders("Accept" -> "application/vnd.hmrc.1.0+json").get())
      response.status shouldBe OK
      response.json shouldBe VatReturns.retrieveVatReturnsMtdSuccessBody
    }

    "allow users to retrieve VAT returns without receivedAt field for last four years" in new Test {

      override def uri: String = s"/$vrn/returns/0001"

      override def setupStubs(): StubMapping = {
        AuditStub.audit()
        AuthStub.authorised()
        DesStub.onSuccess(DesStub.GET, retrieveDesUrl(vrn), Map("period-key" -> "0001"), OK, VatReturns.retrieveVatReturnsDesResponseWithNoReceivedAt)
      }

      val response: WSResponse = await(request().
        withHttpHeaders("Accept" -> "application/vnd.hmrc.1.0+json").get())
      response.status shouldBe OK
      response.json shouldBe VatReturns.retrieveVatReturnsMtdSuccessBody
    }

    "return internal server error on malformed response" in new Test {

      override def uri: String = s"/$vrn/returns/0001"

      override def setupStubs(): StubMapping = {
        AuditStub.audit()
        AuthStub.authorised()
        DesStub.onSuccess(DesStub.GET, retrieveDesUrl(vrn), Map("period-key" -> "0001"), OK, "not-json")
      }

      val response: WSResponse = await(request().
        withHttpHeaders("Accept" -> "application/vnd.hmrc.1.0+json").get())
      response.status shouldBe INTERNAL_SERVER_ERROR
    }

    "return internal server error on empty body response" in new Test {

      override def uri: String = s"/$vrn/returns/0001"

      override def setupStubs(): StubMapping = {
        AuditStub.audit()
        AuthStub.authorised()
        DesStub.onSuccess(DesStub.GET, retrieveDesUrl(vrn), Map("period-key" -> "0001"), OK, "")
      }

      val response: WSResponse = await(request().
        withHttpHeaders("Accept" -> "application/vnd.hmrc.1.0+json").get())
      response.status shouldBe INTERNAL_SERVER_ERROR
    }

    "reject client with no authorization" in new Test {
      override def setupStubs(): StubMapping = {
        AuditStub.audit()
        AuthStub.unauthorisedNotLoggedIn()
      }

      override def uri: String = s"/$vrn/returns/0001"
      val response: WSResponse = await(request().
        withHttpHeaders("Accept" -> "application/vnd.hmrc.1.0+json").get())
      response.status shouldBe FORBIDDEN
      response.json shouldBe Json.toJson(ClientOrAgentNotAuthorized)
    }

    "return bad request (400) if the vrn is invalid" in new Test {

      override def uri: String = s"/invalid_vrn/returns/0001"

      override def setupStubs(): StubMapping = {
        AuditStub.audit()
        AuthStub.authorised()
      }

      val response: WSResponse = await(request().
        withHttpHeaders("Accept" -> "application/vnd.hmrc.1.0+json").get())
      response.status shouldBe BAD_REQUEST
      response.json shouldBe Json.parse(
        """
          |{"code":"VRN_INVALID","message":"The provided Vrn is invalid"}
          |""".stripMargin)
    }

    "return forbidden (403) if the vat return was submitted longer than 4 years ago" in new Test {

      override def uri: String = s"/$vrn/returns/0001"

      override def setupStubs(): StubMapping = {
        AuditStub.audit()
        AuthStub.authorised()
        DesStub.onError(DesStub.GET, retrieveDesUrl(vrn), Map("period-key" -> "0001"), FORBIDDEN, errorBody("DATE_RANGE_TOO_LARGE"))
      }

      val response: WSResponse = await(request().
        withHttpHeaders("Accept" -> "application/vnd.hmrc.1.0+json").get())
      response.status shouldBe FORBIDDEN
      response.json shouldBe Json.toJson(Errors.businessError(DateRangeTooLarge))
    }

    "return internal server error (500) if the vat returns with DES vrn not found error" in new Test {

      override def uri: String = s"/$vrn/returns/0001"

      override def setupStubs(): StubMapping = {
        AuditStub.audit()
        AuthStub.authorised()
        DesStub.onError(DesStub.GET, retrieveDesUrl(vrn), Map("period-key" -> "0001"), FORBIDDEN, errorBody("VRN_NOT_FOUND"))
      }

      val response: WSResponse = await(request().
        withHttpHeaders("Accept" -> "application/vnd.hmrc.1.0+json").get())
      response.status shouldBe INTERNAL_SERVER_ERROR
    }

    "return internal server error (500) if the vat returns from DES got NOT_FOUND_VRN error" in new Test {

      override def uri: String = s"/$vrn/returns/0001"

      override def setupStubs(): StubMapping = {
        AuditStub.audit()
        AuthStub.authorised()
        DesStub.onError(DesStub.GET, retrieveDesUrl(vrn), Map("period-key" -> "0001"), FORBIDDEN, errorBody("NOT_FOUND_VRN"))
      }

      val response: WSResponse = await(request().
        withHttpHeaders("Accept" -> "application/vnd.hmrc.1.0+json").get())
      response.status shouldBe INTERNAL_SERVER_ERROR
    }

    "return bad request (400) if the periodKey is invalid" in new Test {

      override def uri: String = s"/$vrn/returns/001"

      override def setupStubs(): StubMapping = {
        AuditStub.audit()
        AuthStub.authorised()
      }

      val response: WSResponse = await(request().
        withHttpHeaders("Accept" -> "application/vnd.hmrc.1.0+json").get())
      response.status shouldBe BAD_REQUEST
      response.json shouldBe Json.toJson(Errors.badRequest(InvalidPeriodKey))
    }

    "return not found (404) with non-existent VRN" in new Test {

      override def uri: String = s"/$vrn/returns/0001"

      override def setupStubs(): StubMapping = {
        AuditStub.audit()
        AuthStub.authorised()
        DesStub.onError(DesStub.GET, retrieveDesUrl(vrn), Map("period-key" -> "0001"), NOT_FOUND, "NOT_FOUND")
      }

      val response: WSResponse = await(request().
        withHttpHeaders("Accept" -> "application/vnd.hmrc.1.0+json").get())
      response.status shouldBe NOT_FOUND
    }

    "return X-Content-Type-Options header with non-existent VRN" in new Test {

      override def uri: String = s"/$vrn/returns/0001"

      override def setupStubs(): StubMapping = {
        AuditStub.audit()
        AuthStub.authorised()
        DesStub.onError(DesStub.GET, retrieveDesUrl(vrn), Map("period-key" -> "0001"), NOT_FOUND, "NOT_FOUND")
      }

      val response: WSResponse = await(request().
        withHttpHeaders("Accept" -> "application/vnd.hmrc.1.0+json").get())
      response.status shouldBe NOT_FOUND
      response.header("X-Content-Type-Options") nonEmpty
    }
  }
}
