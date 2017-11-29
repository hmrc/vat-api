package uk.gov.hmrc.vatapi.resources

import uk.gov.hmrc.support.BaseFunctionalSpec
import play.api.libs.json.Json

class ValueAddedTaxReturnsSpec extends BaseFunctionalSpec {

  "VAT returns" should {

    "should allow users to submit VAT returns" in {
      given()
        .des().vatReturns.expectVatReturnFor(vrn)
        .when()
        .post(s"/$vrn/return", Some(Json.parse("""{
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

    "should not allow users to submit undeclared VAT returns" in {
      given()
        .des().vatReturns.expectVatReturnFor(vrn)
        .when()
        .post(s"/$vrn/return", Some(Json.parse("""{
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

  }

}
