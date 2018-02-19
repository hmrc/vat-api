package uk.gov.hmrc.vatapi.resources

import play.api.libs.json.Json
import uk.gov.hmrc.support.BaseFunctionalSpec
import uk.gov.hmrc.vatapi.models.{Liabilities, Payments}

class FinancialDataResourceSpec extends BaseFunctionalSpec {

  "FinancialDataResource.getLiabilities" when {
    "a valid request is made" should {
      "retrieve a single liability where they exist" in {
        given().
          des().FinancialData.singleLiabilityFor(vrn)
          .when()
          .get(s"/$vrn/liabilities?from=2017-01-01&to=2017-01-02")
          .thenAssertThat()
          .statusIs(200)
          .bodyIsLike(Jsons.FinancialData.oneLiability.toString)
      }

      "retrieve a single liability where the minimum data exists" in {
        given().
          des().FinancialData.minLiabilityFor(vrn)
          .when()
          .get(s"/$vrn/liabilities?from=2017-01-01&to=2017-01-02")
          .thenAssertThat()
          .statusIs(200)
          .bodyIsLike(Jsons.FinancialData.minLiability.toString)
      }

      "retrieve multiple liabilities where they exist" in {
        given().
          des().FinancialData.multipleLiabilitiesFor(vrn)
          .when()
          .get(s"/$vrn/liabilities?from=2017-01-01&to=2017-12-31")
          .thenAssertThat()
          .statusIs(200)
          .bodyIsLike(Jsons.FinancialData.multipleLiabilities.toString)
      }

      "return an empty list if no liabilities exist" in {
        given()
          .des().FinancialData.emptyLiabilitiesFor(vrn)
          .when()
          .get(s"/$vrn/liabilities?from=2017-01-01&to=2017-12-31")
          .thenAssertThat()
          .statusIs(200)
          .bodyIsLike(Json.toJson(Liabilities(Seq())).toString)
      }
    }

    "a date range of greater than 1 year is supplied" should {
      "return an INVALID_DATE_RANGE error" in {
        when()
          .get(s"/$vrn/liabilities?from=2015-01-01&to=2016-01-01")
          .thenAssertThat()
          .statusIs(400)
      }
    }

    "an invalid 'from' date is supplied" should {
      "return an INVALID_DATE_TO error" in {
        when()
          .get(s"/$vrn/liabilities?from=2017-01-01&to=3017-12-31")
          .thenAssertThat()
          .statusIs(400)
      }
    }

    "an invalid 'to' date is supplied" should {
      "return and INVALID_DATE_FROM error" in {
        when()
          .get(s"/$vrn/liabilities?from=2001-01-01&to=2017-12-31")
          .thenAssertThat()
          .statusIs(400)
      }
    }

    "an invalid VRN is supplied" should {
      "return an INVALID_VRN error" in {
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
        given().
          des().FinancialData.singlePaymentFor(vrn)
          .when()
          .get(s"/$vrn/payments?from=2017-01-01&to=2017-01-02")
          .thenAssertThat()
          .statusIs(200)
          .bodyIsLike(Jsons.FinancialData.onePayment.toString)
      }
      "retrieve a single payment where the minimum data exists" in {
        given().
          des().FinancialData.minPaymentFor(vrn)
          .when()
          .get(s"/$vrn/payments?from=2017-01-01&to=2017-01-02")
          .thenAssertThat()
          .statusIs(200)
          .bodyIsLike(Jsons.FinancialData.minPayment.toString)
      }

      "retrieve multiple payments where they exist" in {
        given().
          des().FinancialData.multiplePaymentsFor(vrn)
          .when()
          .get(s"/$vrn/payments?from=2017-01-01&to=2017-12-31")
          .thenAssertThat()
          .statusIs(200)
          .bodyIsLike(Jsons.FinancialData.multiplePayments.toString)
      }

      "return an empty list if no payments exist" in {
        given()
          .des().FinancialData.emptyPaymentsFor(vrn)
          .when()
          .get(s"/$vrn/payments?from=2017-01-01&to=2017-12-31")
          .thenAssertThat()
          .statusIs(200)
          .bodyIsLike(Json.toJson(Payments(Seq())).toString)
      }
    }

    "a date range of greater than 1 year is supplied" should {
      "return an INVALID_DATE_RANGE error" in {
        when()
          .get(s"/$vrn/payments?from=2015-01-01&to=2016-01-01")
          .thenAssertThat()
          .statusIs(400)
      }
    }

    "an invalid 'from' date is supplied" should {
      "return an INVALID_DATE_TO error" in {
        when()
          .get(s"/$vrn/payments?from=2017-01-01&to=3017-12-31")
          .thenAssertThat()
          .statusIs(400)
      }
    }

    "an invalid 'to' date is supplied" should {
      "return and INVALID_DATE_FROM error" in {
        when()
          .get(s"/$vrn/payments?from=2001-01-01&to=2017-12-31")
          .thenAssertThat()
          .statusIs(400)
      }
    }

    "an invalid VRN is supplied" should {
      "return an INVALID_VRN error" in {
        when()
          .get(s"/invalidvrn/payments?from=2015-01-01&to=2017-12-31")
          .thenAssertThat()
          .statusIs(400)
      }
    }
  }

}
