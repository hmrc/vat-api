package uk.gov.hmrc.vatapi.resources

import uk.gov.hmrc.support.BaseFunctionalSpec

class FinancialDataResourceSpec extends BaseFunctionalSpec {

  "FinancialDataResource.getLiabilities" when {
    "a valid request is made" should {
      "reject client with no authorization" in {
        given()
          .stubAudit
          .userIsNotAuthorisedForTheResource
          .when()
          .get(s"/$vrn/liabilities?from=2017-01-01&to=2017-06-02")
          .thenAssertThat()
          .statusIs(403)
          .bodyHasPath("\\code", "CLIENT_OR_AGENT_NOT_AUTHORISED")
      }

      "retrieve a single liability where they exist" in {
        given()
          .stubAudit
          .userIsFullyAuthorisedForTheResource
          .des().FinancialData.singleLiabilityFor(vrn)
          .when()
          .get(s"/$vrn/liabilities?from=2017-01-01&to=2017-06-02")
          .thenAssertThat()
          .statusIs(200)
          .bodyIsLike(Jsons.FinancialData.oneLiability.toString)
      }

      "retrieve a single liability where the minimum data exists" in {
        given()
          .stubAudit
          .userIsFullyAuthorisedForTheResource
          .des().FinancialData.minLiabilityFor(vrn)
          .when()
          .get(s"/$vrn/liabilities?from=2017-01-01&to=2017-06-02")
          .thenAssertThat()
          .statusIs(200)
          .bodyIsLike(Jsons.FinancialData.minLiability.toString)
      }
      "retrieve a single liability if DES returns two liabilities and the second liability overlaps the supplied 'to' date" in {
        given()
          .stubAudit
          .userIsFullyAuthorisedForTheResource
          .des().FinancialData.overlappingLiabilitiesFor(vrn)
          .when()
          .get(s"/$vrn/liabilities?from=2017-01-01&to=2017-06-02")
          .thenAssertThat()
          .statusIs(200)
          .bodyIsLike(Jsons.FinancialData.oneLiability.toString)
      }

      "retrieve multiple liabilities where they exist" in {
        given()
          .stubAudit
          .userIsFullyAuthorisedForTheResource
          .des().FinancialData.multipleLiabilitiesFor(vrn)
          .when()
          .get(s"/$vrn/liabilities?from=2017-01-01&to=2017-12-31")
          .thenAssertThat()
          .statusIs(200)
          .bodyIsLike(Jsons.FinancialData.multipleLiabilities.toString)
      }

      "return a 404 (Not Found) if no liabilities exist" in {
        given()
          .stubAudit
          .userIsFullyAuthorisedForTheResource
          .des().FinancialData.emptyLiabilitiesFor(vrn)
          .when()
          .get(s"/$vrn/liabilities?from=2017-01-01&to=2017-12-31")
          .thenAssertThat()
          .statusIs(404)
      }
    }

    "a date range of greater than 1 year is supplied" should {
      "return an INVALID_DATE_RANGE error" in {
        given()
          .stubAudit
        when()
          .get(s"/$vrn/liabilities?from=2015-01-01&to=2016-01-01")
          .thenAssertThat()
          .statusIs(400)
      }
    }

    "an invalid 'from' date is supplied" should {
      "return an INVALID_DATE_TO error" in {
        given()
          .stubAudit
        when()
          .get(s"/$vrn/liabilities?from=2017-01-01&to=3017-12-31")
          .thenAssertThat()
          .statusIs(400)
      }
    }

    "an invalid 'to' date is supplied" should {
      "return and INVALID_DATE_FROM error" in {
        given()
          .stubAudit
        when()
          .get(s"/$vrn/liabilities?from=2001-01-01&to=2017-12-31")
          .thenAssertThat()
          .statusIs(400)
      }
    }

    "an invalid VRN is supplied" should {
      "return an INVALID_VRN error" in {
        given()
          .stubAudit
        when()
          .get(s"/invalidvrn/liabilities?from=2015-01-01&to=2017-12-31")
          .thenAssertThat()
          .statusIs(400)
      }
    }
  }

  "FinancialDataResource.getPayments" when {
    "a valid request is made" should {
      "retrieve a single payment where they exist" in {
        given()
          .stubAudit
          .userIsFullyAuthorisedForTheResource
          .des().FinancialData.singlePaymentFor(vrn)
          .when()
          .get(s"/$vrn/payments?from=2017-01-01&to=2017-06-02")
          .thenAssertThat()
          .statusIs(200)
          .bodyIsLike(Jsons.FinancialData.onePayment.toString)
      }
      "retrieve a single payment where the minimum data exists" in {
        given()
          .stubAudit
          .userIsFullyAuthorisedForTheResource
          .des().FinancialData.minPaymentFor(vrn)
          .when()
          .get(s"/$vrn/payments?from=2017-01-01&to=2017-06-02")
          .thenAssertThat()
          .statusIs(200)
          .bodyIsLike(Jsons.FinancialData.minPayment.toString)
      }

      "return only those payments belonging to a liability that falls before the 'to' date" in {
        given()
          .stubAudit
          .userIsFullyAuthorisedForTheResource
          .des().FinancialData.overlappingPaymentsFor(vrn)
          .when()
          .get(s"/$vrn/payments?from=2017-01-01&to=2017-06-02")
          .thenAssertThat()
          .statusIs(200)
          .bodyIsLike(Jsons.FinancialData.onePayment.toString)
      }

      "retrieve multiple payments where they exist" in {
        given()
          .stubAudit
          .userIsFullyAuthorisedForTheResource
          .des().FinancialData.multiplePaymentsFor(vrn)
          .when()
          .get(s"/$vrn/payments?from=2017-01-01&to=2017-12-31")
          .thenAssertThat()
          .statusIs(200)
          .bodyIsLike(Jsons.FinancialData.multiplePayments.toString)
      }

      "return a 404 (Not Found) if no payments exist" in {
        given()
          .stubAudit
          .userIsFullyAuthorisedForTheResource
          .des().FinancialData.emptyPaymentsFor(vrn)
          .when()
          .get(s"/$vrn/payments?from=2017-01-01&to=2017-12-31")
          .thenAssertThat()
          .statusIs(404)
      }
    }

    "a date range of greater than 1 year is supplied" should {
      "return an INVALID_DATE_RANGE error" in {
        given()
          .stubAudit
        when()
          .get(s"/$vrn/payments?from=2015-01-01&to=2016-01-01")
          .thenAssertThat()
          .statusIs(400)
      }
    }

    "an invalid 'from' date is supplied" should {
      "return an INVALID_DATE_TO error" in {
        given()
          .stubAudit
        when()
          .get(s"/$vrn/payments?from=2017-01-01&to=3017-12-31")
          .thenAssertThat()
          .statusIs(400)
      }
    }

    "an invalid 'to' date is supplied" should {
      "return and INVALID_DATE_FROM error" in {
        given()
          .stubAudit
        when()
          .get(s"/$vrn/payments?from=2001-01-01&to=2017-12-31")
          .thenAssertThat()
          .statusIs(400)
      }
    }

    "an invalid VRN is supplied" should {
      "return an INVALID_VRN error" in {
        given()
          .stubAudit
        when()
          .get(s"/invalidvrn/payments?from=2015-01-01&to=2017-12-31")
          .thenAssertThat()
          .statusIs(400)
      }
    }
  }

}
