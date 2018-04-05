package uk.gov.hmrc.vatapi.resources

import play.api.libs.json.Json
import uk.gov.hmrc.support.BaseFunctionalSpec

class ValueAddedTaxReturnsSpec extends BaseFunctionalSpec {

  private def body(finalised: Boolean = true) =  s"""{
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

  private def requestWithNegativeAmounts(finalised: Boolean = true) =  s"""{
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

  "VAT returns submission" should {

    "allow users to submit VAT returns" in {
      given()
        .nrs().nrsVatReturnSuccessFor(vrn)
        .des().vatReturns.expectVatReturnSubmissionFor(vrn)
        .when()
        .post(s"/$vrn/returns", Some(Json.parse(body())))
        .thenAssertThat()
        .statusIs(201)
        .bodyHasPath("\\paymentIndicator", "BANK")
        .bodyHasPath("\\processingDate", "2018-03-01T11:43:43.195Z")
        .bodyHasPath("\\formBundleNumber", "891713832155")
        .hasHeader("Receipt-Id")
        .hasHeader("Receipt-Signature")
        .hasHeader("Receipt-TimeStamp")
    }

    "allow users to submit VAT returns even with negative amounts" in {
      given()
        .nrs().nrsVatReturnSuccessFor(vrn)
        .des().vatReturns.expectVatReturnSubmissionFor(vrn)
        .when()
        .post(s"/$vrn/returns", Some(Json.parse(requestWithNegativeAmounts())))
        .thenAssertThat()
        .statusIs(201)
        .bodyHasPath("\\paymentIndicator", "BANK")
        .bodyHasPath("\\processingDate", "2018-03-01T11:43:43.195Z")
        .bodyHasPath("\\formBundleNumber", "891713832155")
        .hasHeader("Receipt-Id")
        .hasHeader("Receipt-Signature")
        .hasHeader("Receipt-TimeStamp")
    }

    "not allow users to submit undeclared VAT returns" in {
      given()
        .nrs().nrsVatReturnSuccessFor(vrn)
        .des().vatReturns.expectVatReturnSubmissionFor(vrn)
        .when()
        .post(s"/$vrn/returns", Some(Json.parse(body(false))))
        .thenAssertThat()
        .statusIs(403)
        .bodyHasPath("\\errors(0)\\code", "NOT_FINALISED")
        .bodyHasPath("\\errors(0)\\path", "/finalised")
    }

    "reject submission with invalid period key" in {
      given()
        .nrs().nrsVatReturnSuccessFor(vrn)
        .des().vatReturns.expectVatReturnToFail(vrn, "INVALID_PERIODKEY")
        .when()
        .post(s"/$vrn/returns", Some(Json.parse(body())))
        .thenAssertThat()
        .statusIs(400)
        .bodyHasPath("\\code", "PERIOD_KEY_INVALID")
    }

    "reject submission with invalid ARN" in {
      given()
        .nrs().nrsVatReturnSuccessFor(vrn)
        .des().vatReturns.expectVatReturnToFail(vrn, "INVALID_ARN")
        .when()
        .post(s"/$vrn/returns", Some(Json.parse(body())))
        .thenAssertThat()
        .statusIs(500)
        .bodyHasPath("\\code", "INTERNAL_SERVER_ERROR")
    }

    "reject submission with invalid VRN" in {
      given()
        .nrs().nrsVatReturnSuccessFor(vrn)
        .des().vatReturns.expectVatReturnToFail(vrn, "INVALID_VRN")
        .when()
        .post(s"/$vrn/returns", Some(Json.parse(body())))
        .thenAssertThat()
        .statusIs(400)
        .bodyHasPath("\\code", "VRN_INVALID")
    }

    "reject submission with invalid payload" in {
      given()
        .nrs().nrsVatReturnSuccessFor(vrn)
        .des().vatReturns.expectVatReturnToFail(vrn, "INVALID_PAYLOAD")
        .when()
        .post(s"/$vrn/returns", Some(Json.parse(body())))
        .thenAssertThat()
        .statusIs(400)
        .bodyHasPath("\\code", "INVALID_REQUEST")
    }

    "reject duplicate submission" in {
      given()
        .nrs().nrsVatReturnSuccessFor(vrn)
        .des().vatReturns.expectVatReturnToFail(vrn, "DUPLICATE_SUBMISSION")
        .when()
        .post(s"/$vrn/returns", Some(Json.parse(body())))
        .thenAssertThat()
        .statusIs(403)
        .bodyHasPath("\\errors(0)\\code", "DUPLICATE_SUBMISSION")
    }

    "fail if submission to  Non-Repudiation service failed" in {
      given()
        .nrs().nrsFailurefor(vrn)
        .when()
        .post(s"/$vrn/returns", Some(Json.parse(body())))
        .thenAssertThat()
        .statusIs(500)
        .bodyHasPath("\\code", "INTERNAL_SERVER_ERROR")
    }
  }

  "VAT returns retrieval" should {

    "allow users to retrieve VAT returns for last four years" in {
      given()
        .des().vatReturns.expectVatReturnSearchFor(vrn, "0001")
        .when()
        .get(s"/$vrn/returns/0001")
        .thenAssertThat()
        .statusIs(200)
        .bodyHasPath("\\periodKey", "0001")
        .bodyHasPath("\\vatDueSales", 100.25)
        .bodyHasPath("\\vatDueAcquisitions", 100.25)
        .bodyHasPath("\\totalVatDue", 200.50)
        .bodyHasPath("\\vatReclaimedCurrPeriod", 100.25)
        .bodyHasPath("\\netVatDue", 100.25)
        .bodyHasPath("\\totalValueSalesExVAT", 100)
        .bodyHasPath("\\totalValuePurchasesExVAT", 100)
        .bodyHasPath("\\totalValueGoodsSuppliedExVAT", 100)
        .bodyHasPath("\\totalAcquisitionsExVAT", 100)
    }

    "return internal server error on malformed response" in {
      given()
        .des().vatReturns.expectInvalidVatReturnSearchFor(vrn, "0001")
        .when()
        .get(s"/$vrn/returns/0001")
        .thenAssertThat()
        .statusIs(500)
    }

    "return bad request (400) if the vrn is invalid" in {
      given()
        .des().vatReturns.expectVatReturnSearchFor(vrn, "0001")
        .when()
        .get(s"/invalid_vrn/returns/0001")
        .thenAssertThat()
        .statusIs(400)
        .bodyHasPath("\\code", "VRN_INVALID")
    }

    "return forbidden (403) if the vat return was submitted longer than 4 years ago" in {
      given()
        .des().vatReturns.expectVatReturnRetrieveToFail(vrn, "DATE_RANGE_TOO_LARGE")
        .when()
        .get(s"/$vrn/returns/0001")
        .thenAssertThat()
        .statusIs(403)
        .bodyHasPath("\\code", "BUSINESS_ERROR")
        .bodyHasPath("\\errors(0)\\code", "DATE_RANGE_TOO_LARGE")
    }

    "return bad request (400) if the periodKey is invalid" in {
      given()
        .des().vatReturns.expectVatReturnSearchFor(vrn, "001")
        .when()
        .get(s"/$vrn/returns/001")
        .thenAssertThat()
        .statusIs(400)
        .bodyHasPath("\\code", "INVALID_REQUEST")
        .bodyHasPath("\\errors(0)\\code", "PERIOD_KEY_INVALID")
    }

    "return not found (404) with non-existent VRN" in {
      given()
        .des().vatReturns.expectNonExistentVrnFor(vrn, "0001")
        .when()
        .get(s"/$vrn/returns/0001")
        .thenAssertThat()
        .statusIs(404)
    }

    "return X-Content-Type-Options header with non-existent VRN" in {
      given()
        .des().vatReturns.expectNonExistentVrnFor(vrn, "0001")
        .when()
        .get(s"/$vrn/returns/0001")
        .thenAssertThat()
        .hasHeader("X-Content-Type-Options")
    }
  }
}
