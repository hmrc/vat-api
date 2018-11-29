package uk.gov.hmrc.vatapi.resources

import play.api.http.Status._
import play.api.libs.json.Json
import uk.gov.hmrc.assets.des.Errors
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
          .statusIs(FORBIDDEN)
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
          .statusIs(OK)
          .bodyIsLike(Jsons.FinancialData.oneLiability.toString)
      }

      "retrieve a single liability when VAT Hybrid data is returned" in {
        given()
          .stubAudit
          .userIsFullyAuthorisedForTheResource
          .des().FinancialData.vatHybridLiabilityFor(vrn)
          .when()
          .get(s"/$vrn/liabilities?from=2017-01-01&to=2017-06-02")
          .thenAssertThat()
          .statusIs(OK)
          .bodyIsLike(Jsons.FinancialData.vatHybrid.toString)
      }

      "retrieve a single liability where multiple liabilities exist with only one within the specific period to date - Param to date is after period to date" in {
        given()
          .stubAudit
          .userIsFullyAuthorisedForTheResource
          .des().FinancialData.singleLiabilityFor(vrn)
          .when()
          .get(s"/$vrn/liabilities?from=2017-01-01&to=2017-06-02")
          .thenAssertThat()
          .statusIs(OK)
          .bodyIsLike(Jsons.FinancialData.oneLiability.toString)
      }

      "retrieve a single liability where multiple liabilities exist with only one within the specific period to date - Param to date is equal to period to date " in {
        given()
          .stubAudit
          .userIsFullyAuthorisedForTheResource
          .des().FinancialData.singleLiabilityFor(vrn)
          .when()
          .get(s"/$vrn/liabilities?from=2017-01-01&to=2017-03-31")
          .thenAssertThat()
          .statusIs(OK)
          .bodyIsLike(Jsons.FinancialData.oneLiability.toString)
      }

      "retrieve a single liability where multiple liabilities exist with only one within the specific period to date - Param to date before period to date " in {
        given()
          .stubAudit
          .userIsFullyAuthorisedForTheResource
          .des().FinancialData.singleLiabilityFor(vrn)
          .when()
          .get(s"/$vrn/liabilities?from=2017-01-01&to=2017-03-30")
          .thenAssertThat()
          .statusIs(NOT_FOUND)
          .bodyIsLike(Json.toJson(uk.gov.hmrc.vatapi.models.Errors.NotFound).toString())
      }

      "retrieve a single liability where the minimum data exists" in {
        given()
          .stubAudit
          .userIsFullyAuthorisedForTheResource
          .des().FinancialData.minLiabilityFor(vrn)
          .when()
          .get(s"/$vrn/liabilities?from=2017-01-01&to=2017-06-02")
          .thenAssertThat()
          .statusIs(OK)
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
          .statusIs(OK)
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
          .statusIs(OK)
          .bodyIsLike(Jsons.FinancialData.multipleLiabilities.toString)
      }

      "retrieve multiple liabilities where they exist excluding Payment on Account" in {
        given()
          .stubAudit
          .userIsFullyAuthorisedForTheResource
          .des().FinancialData.multipleLiabilitiesWithPaymentOnAccountFor(vrn)
          .when()
          .get(s"/$vrn/liabilities?from=2017-01-01&to=2017-12-31")
          .thenAssertThat()
          .statusIs(OK)
          .bodyIsLike(Jsons.FinancialData.multipleLiabilitiesWithoutPaymentOnAccount.toString)
      }

      "return code 400 when idNumber parameter is invalid" in {
        given()
          .stubAudit
          .userIsFullyAuthorisedForTheResource
          .des().FinancialData.invalidPaymentsParamsFor(vrn, Errors.invalidIdNumber)
          .when()
          .get(s"/$vrn/liabilities?from=2017-01-01&to=2017-12-31")
          .thenAssertThat()
          .statusIs(BAD_REQUEST)
      }

      "a date range of greater than 1 year is supplied" should {
        "return an INVALID_DATE_RANGE error" in {
          given()
            .stubAudit
          when()
            .get(s"/$vrn/liabilities?from=2015-01-01&to=2016-01-01")
            .thenAssertThat()
            .statusIs(BAD_REQUEST)
        }
      }

      "an invalid 'from' date is supplied" should {
        "return an INVALID_DATE_TO error" in {
          given()
            .stubAudit
          when()
            .get(s"/$vrn/liabilities?from=2017-01-01&to=3017-12-31")
            .thenAssertThat()
            .statusIs(BAD_REQUEST)
        }
      }

      "an invalid 'to' date is supplied" should {
        "return and INVALID_DATE_FROM error" in {
          given()
            .stubAudit
          when()
            .get(s"/$vrn/liabilities?from=2001-01-01&to=2017-12-31")
            .thenAssertThat()
            .statusIs(BAD_REQUEST)
        }
      }

      "an invalid VRN is supplied" should {
        "return an INVALID_VRN error" in {
          given()
            .stubAudit
          when()
            .get(s"/invalidvrn/liabilities?from=2015-01-01&to=2017-12-31")
            .thenAssertThat()
            .statusIs(BAD_REQUEST)
        }
      }
      "return a 404 (Not Found) if no liabilities exist" in {
        given()
          .stubAudit
          .userIsFullyAuthorisedForTheResource
          .des().FinancialData.emptyLiabilitiesFor(vrn)
          .when()
          .get(s"/$vrn/liabilities?from=2017-01-01&to=2017-12-31")
          .thenAssertThat()
          .statusIs(NOT_FOUND)
      }
    }

    "return code 500 when idType parameter is invalid" in {
      given()
        .stubAudit
        .userIsFullyAuthorisedForTheResource
        .des().FinancialData.invalidPaymentsParamsFor(vrn, Errors.invalidIdType)
        .when()
        .get(s"/$vrn/liabilities?from=2017-01-01&to=2017-12-31")
        .thenAssertThat()
        .statusIs(INTERNAL_SERVER_ERROR)
    }

    "return code 500 when regime type parameter is invalid" in {
      given()
        .stubAudit
        .userIsFullyAuthorisedForTheResource
        .des().FinancialData.invalidPaymentsParamsFor(vrn, Errors.invalidRegimeType)
        .when()
        .get(s"/$vrn/liabilities?from=2017-01-01&to=2017-12-31")
        .thenAssertThat()
        .statusIs(INTERNAL_SERVER_ERROR)
    }

    "return code 500 when openitems parameter is invalid" in {
      given()
        .stubAudit
        .userIsFullyAuthorisedForTheResource
        .des().FinancialData.invalidPaymentsParamsFor(vrn, Errors.invalidOnlyOpenItems)
        .when()
        .get(s"/$vrn/liabilities?from=2017-01-01&to=2017-12-31")
        .thenAssertThat()
        .statusIs(INTERNAL_SERVER_ERROR)
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
          .statusIs(OK)
          .bodyIsLike(Jsons.FinancialData.onePayment.toString)
      }

      "retrieve a single payment when VAT Hybrid data is returned" in {
        given()
          .stubAudit
          .userIsFullyAuthorisedForTheResource
          .des().FinancialData.vatHybridLiabilityFor(vrn)
          .when()
          .get(s"/$vrn/payments?from=2017-01-01&to=2017-06-02")
          .thenAssertThat()
          .statusIs(OK)
          .bodyIsLike(Jsons.FinancialData.vatHybridPayment.toString)
      }


      "retrieve a single payment where the minimum data exists" in {
        given()
          .stubAudit
          .userIsFullyAuthorisedForTheResource
          .des().FinancialData.minPaymentFor(vrn)
          .when()
          .get(s"/$vrn/payments?from=2017-01-01&to=2017-06-02")
          .thenAssertThat()
          .statusIs(OK)
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
          .statusIs(OK)
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
          .statusIs(OK)
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
          .statusIs(NOT_FOUND)
      }
    }

    "a date range of greater than 1 year is supplied" should {
      "return an INVALID_DATE_RANGE error" in {
        given()
          .stubAudit
        when()
          .get(s"/$vrn/payments?from=2015-01-01&to=2016-01-01")
          .thenAssertThat()
          .statusIs(BAD_REQUEST)
      }
    }

    "an invalid 'from' date is supplied" should {
      "return an INVALID_DATE_TO error" in {
        given()
          .stubAudit
        when()
          .get(s"/$vrn/payments?from=2017-01-01&to=3017-12-31")
          .thenAssertThat()
          .statusIs(BAD_REQUEST)
      }
    }

    "an invalid 'to' date is supplied" should {
      "return and INVALID_DATE_FROM error" in {
        given()
          .stubAudit
        when()
          .get(s"/$vrn/payments?from=2001-01-01&to=2017-12-31")
          .thenAssertThat()
          .statusIs(BAD_REQUEST)
      }
    }

    "an invalid VRN is supplied" should {
      "return an INVALID_VRN error" in {
        given()
          .stubAudit
        when()
          .get(s"/invalidvrn/payments?from=2015-01-01&to=2017-12-31")
          .thenAssertThat()
          .statusIs(BAD_REQUEST)
      }
    }
  }

}
