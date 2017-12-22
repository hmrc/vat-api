package uk.gov.hmrc.vatapi.resources

import uk.gov.hmrc.support.BaseFunctionalSpec
import play.api.libs.json.Json
import org.joda.time.LocalDate

class ValueAddedTaxReturnsSpec extends BaseFunctionalSpec {

  "VAT returns" should {

    "allow users to submit VAT returns" in {
      given()
        .des().vatReturns.expectVatReturnSubmissionFor(vrn)
        .when()
        .post(s"/$vrn/returns", Some(Json.parse("""{
          "periodKey": "#001",
          "vatDueSales": 100.25,
          "vatDueAcquisitions": 100.25,
          "totalVatDue": 200.50,
          "vatReclaimedCurrPeriod": 100.25,
          "netVatDue": 100.25,
          "totalValueSalesExVAT": 100,
          "totalValuePurchasesExVAT": 100,
          "totalValueGoodsSuppliedExVAT": 100,
          "totalAcquisitionsExVAT": 100,
          "finalised": true
        }""")))
        .thenAssertThat()
        .statusIs(201)
    }

    "not allow users to submit undeclared VAT returns" in {
      given()
        .des().vatReturns.expectVatReturnSubmissionFor(vrn)
        .when()
        .post(s"/$vrn/returns", Some(Json.parse("""{
          "periodKey": "#001",
          "vatDueSales": 100.25,
          "vatDueAcquisitions": 100.25,
          "totalVatDue": 200.50,
          "vatReclaimedCurrPeriod": 100.25,
          "netVatDue": 100.25,
          "totalValueSalesExVAT": 100,
          "totalValuePurchasesExVAT": 100,
          "totalValueGoodsSuppliedExVAT": 100,
          "totalAcquisitionsExVAT": 100,
          "finalised": false
        }""")))
        .thenAssertThat()
        .statusIs(403)
        .bodyHasPath("\\errors(0)\\code", "NOT_FINALISED")
        .bodyHasPath("\\errors(0)\\path", "/finalised")
    }

    val today = new LocalDate

    "allow users to retrieve VAT returns for last four years" in {
      given()
        .des().vatReturns.expectVatReturnSearchFor(vrn, today.minusYears(4), today)
        .when()
        .get(s"/$vrn/returns")
        .thenAssertThat()
        .statusIs(200)
        .bodyHasPath("\\vatReturns(0)\\period\\key", "#001")
        .bodyHasPath("\\vatReturns(0)\\period\\start", "2017-01-01")
        .bodyHasPath("\\vatReturns(0)\\period\\end", "2017-12-31")
        .bodyHasPath("\\vatReturns(0)\\vatDueSales", 100.25)
        .bodyHasPath("\\vatReturns(0)\\vatDueAcquisitions", 100.25)
        .bodyHasPath("\\vatReturns(0)\\totalVatDue", 200.50)
        .bodyHasPath("\\vatReturns(0)\\vatReclaimedCurrPeriod", 100.25)
        .bodyHasPath("\\vatReturns(0)\\netVatDue", 100.25)
        .bodyHasPath("\\vatReturns(0)\\totalValueSalesExVAT", 100)
        .bodyHasPath("\\vatReturns(0)\\totalValuePurchasesExVAT", 100)
        .bodyHasPath("\\vatReturns(0)\\totalValueGoodsSuppliedExVAT", 100)
        .bodyHasPath("\\vatReturns(0)\\totalAcquisitionsExVAT", 100)
        .bodyHasPath("\\vatReturns(0)\\received", "2017-12-18")
    }

    "return internal server error on malformed response" in {
      given()
        .des().vatReturns.expectInvalidVatReturnSearchFor(vrn, today.minusYears(4), today)
        .when()
        .get(s"/$vrn/returns")
        .thenAssertThat()
        .statusIs(500)
    }

    "allow users to retrieve VAT returns for date range" in {
      val today = new LocalDate
      val from = today.minusDays(3)
      val to = today.minusDays(1)

      given()
        .des().vatReturns.expectVatReturnSearchFor(vrn, from, to)
        .when()
        .get(s"/$vrn/returns?from=$from&to=$to")
        .thenAssertThat()
        .statusIs(200)
    }

    "allow users to retrieve VAT returns with just from date" in {
      val today = new LocalDate
      val from = today.minusDays(3)

      given()
        .des().vatReturns.expectVatReturnSearchFor(vrn, from, today)
        .when()
        .get(s"/$vrn/returns?from=$from")
        .thenAssertThat()
        .statusIs(200)
    }

    "allow users to retrieve VAT returns with just to date" in {
      val today = new LocalDate
      val to = today.minusDays(1)

      given()
        .des().vatReturns.expectVatReturnSearchFor(vrn, today.minusYears(4), to)
        .when()
        .get(s"/$vrn/returns?to=$to")
        .thenAssertThat()
        .statusIs(200)
    }

    "allow users to retrieve VAT returns for the same day" in {
      val today = new LocalDate

      given()
        .des().vatReturns.expectVatReturnSearchFor(vrn, today, today)
        .when()
        .get(s"/$vrn/returns?from=$today&to=$today")
        .thenAssertThat()
        .statusIs(200)
    }

    "fail for invalid from date" in {
      val today = new LocalDate
      val from = today.minusDays(3)
      val to = today.minusDays(1)

      given()
        .des().vatReturns.expectVatReturnSearchFor(vrn, from, to)
        .when()
        .get(s"/$vrn/returns?from=not-a-date&to=$to")
        .thenAssertThat()
        .statusIs(400)
        .bodyHasPath("\\code", "INVALID_FROM_DATE")
    }

    "fail for invalid to date" in {
      val today = new LocalDate
      val from = today.minusDays(3)
      val to = today.minusDays(1)

      given()
        .des().vatReturns.expectVatReturnSearchFor(vrn, from, to)
        .when()
        .get(s"/$vrn/returns?from=$from&to=not-a-date")
        .thenAssertThat()
        .statusIs(400)
        .bodyHasPath("\\code", "INVALID_TO_DATE")
    }

    "fail for invalid date range" in {
      val today = new LocalDate
      val from = today.minusDays(1)
      val to = today.minusDays(3)

      given()
        .des().vatReturns.expectVatReturnSearchFor(vrn, from, to)
        .when()
        .get(s"/$vrn/returns?from=$from&to=$to")
        .thenAssertThat()
        .statusIs(400)
        .bodyHasPath("\\errors(0)\\code", "INVALID_DATE_RANGE")
    }

    "fail for date range greater than the last four years" in {
      val today = new LocalDate
      val fromToEarly = today.minusYears(5)

      given()
        .des().vatReturns.expectVatReturnSearchFor(vrn, fromToEarly, today)
        .when()
        .get(s"/$vrn/returns?from=$fromToEarly")
        .thenAssertThat()
        .statusIs(403)
        .bodyHasPath("\\errors(0)\\code", "DATE_RANGE_TOO_LARGE")
    }

  }

}
