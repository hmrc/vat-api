package uk.gov.hmrc.vatapi.resources

import org.joda.time.DateTime
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

    val isoInstantRegex = "^\\d\\d\\d\\d-(0?[1-9]|1[0-2])-(0[1-9]|[12][0-9]|3[01])T(\\d\\d):(\\d\\d):(\\d\\d)Z"

    "allow users to submit VAT returns" in {
      given()
        .stubAudit
        .userIsFullyAuthorisedForTheNrsDependantResource
        .nrs().nrsVatReturnSuccessFor(vrn)
        .des().vatReturns.expectVatReturnSubmissionFor(vrn)
        .when()
        .post(s"/$vrn/returns", Some(Json.parse(body())))
        .withHeaders("Authorization", "Bearer testtoken")
        .thenAssertThat()
        .statusIs(201)
        .bodyHasPath("\\paymentIndicator", "BANK")
        .bodyHasPath("\\processingDate", "2018-03-01T11:43:43.195Z")
        .bodyHasPath("\\formBundleNumber", "891713832155")
        .responseContainsHeader("Receipt-Id", "2dd537bc-4244-4ebf-bac9-96321be13cdc")
        .responseContainsHeader("Receipt-Signature", "This has been deprecated - DO NOT USE")
        .responseContainsHeader("Receipt-TimeStamp", isoInstantRegex)
    }

    "return processing date with milliseconds and no paymentIndicator if DES returns them without" in {
      given()
        .stubAudit
        .userIsFullyAuthorisedForTheNrsDependantResource
        .nrs().nrsVatReturnSuccessFor(vrn)
        .des().vatReturns.expectVatReturnSubmissionWithIncorrectProcessingDateFor(vrn)
        .when()
        .post(s"/$vrn/returns", Some(Json.parse(body())))
        .withHeaders("Authorization", "Bearer testtoken")
        .thenAssertThat()
        .statusIs(201)
        .bodyHasPath("\\processingDate", "2018-03-01T11:43:43.000Z")
        .bodyHasPath("\\formBundleNumber", "891713832155")
        .responseContainsHeader("Receipt-Id", "2dd537bc-4244-4ebf-bac9-96321be13cdc")
        .responseContainsHeader("Receipt-Signature", "This has been deprecated - DO NOT USE")
        .responseContainsHeader("Receipt-TimeStamp", isoInstantRegex)
    }

    "allow users to submit VAT returns for non bad_request NRS response" in {
      given()
        .stubAudit
        .userIsFullyAuthorisedForTheNrsDependantResource
        .nrs().nrsFailureforNonBadRequest(vrn)
        .des().vatReturns.expectVatReturnSubmissionFor(vrn)
        .when()
        .post(s"/$vrn/returns", Some(Json.parse(body())))
        .withHeaders("Authorization", "Bearer testtoken")
        .thenAssertThat()
        .statusIs(201)
        .bodyHasPath("\\paymentIndicator", "BANK")
        .bodyHasPath("\\processingDate", "2018-03-01T11:43:43.195Z")
        .bodyHasPath("\\formBundleNumber", "891713832155")
        .responseContainsHeader("Receipt-Id", "")
        .responseContainsHeader("Receipt-Signature", "This has been deprecated - DO NOT USE")
        .responseContainsHeader("Receipt-TimeStamp", isoInstantRegex)
    }

    "allow users to submit VAT returns even with negative amounts" in {
      given()
        .stubAudit
        .userIsFullyAuthorisedForTheNrsDependantResource
        .nrs().nrsVatReturnSuccessFor(vrn)
        .des().vatReturns.expectVatReturnSubmissionFor(vrn)
        .when()
        .post(s"/$vrn/returns", Some(Json.parse(requestWithNegativeAmounts())))
        .withHeaders("Authorization", "Bearer testtoken")
        .thenAssertThat()
        .statusIs(201)
        .bodyHasPath("\\paymentIndicator", "BANK")
        .bodyHasPath("\\processingDate", "2018-03-01T11:43:43.195Z")
        .bodyHasPath("\\formBundleNumber", "891713832155")
        .responseContainsHeader("Receipt-Id", "2dd537bc-4244-4ebf-bac9-96321be13cdc")
        .responseContainsHeader("Receipt-Signature", "This has been deprecated - DO NOT USE")
        .responseContainsHeader("Receipt-TimeStamp", isoInstantRegex)
    }

    "reject client with no authorization" in {
      given()
        .stubAudit
        .userIsNotAuthorisedForTheResource
        .when()
        .post(s"/$vrn/returns", Some(Json.parse(body())))
        .thenAssertThat()
        .statusIs(403)
        .bodyHasPath("\\code", "CLIENT_OR_AGENT_NOT_AUTHORISED")
    }

    "not allow users to submit undeclared VAT returns" in {
      given()
        .stubAudit
        .userIsFullyAuthorisedForTheNrsDependantResource
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
        .stubAudit
        .userIsFullyAuthorisedForTheNrsDependantResource
        .nrs().nrsVatReturnSuccessFor(vrn)
        .des().vatReturns.expectVatReturnToFail(vrn, "INVALID_PERIODKEY", 400)
        .when()
        .post(s"/$vrn/returns", Some(Json.parse(body())))
        .withHeaders("Authorization", "Bearer testtoken")
        .thenAssertThat()
        .statusIs(400)
        .bodyHasPath("\\code", "PERIOD_KEY_INVALID")
    }

    "reject submission with invalid ARN" in {
      given()
        .stubAudit
        .userIsFullyAuthorisedForTheNrsDependantResource
        .nrs().nrsVatReturnSuccessFor(vrn)
        .des().vatReturns.expectVatReturnToFail(vrn, "INVALID_ARN", 400)
        .when()
        .post(s"/$vrn/returns", Some(Json.parse(body())))
        .withHeaders("Authorization", "Bearer testtoken")
        .thenAssertThat()
        .statusIs(500)
        .bodyHasPath("\\code", "INTERNAL_SERVER_ERROR")
    }

    "reject submission with invalid VRN" in {
      given()
        .stubAudit
        .userIsFullyAuthorisedForTheNrsDependantResource
        .nrs().nrsVatReturnSuccessFor(vrn)
        .des().vatReturns.expectVatReturnToFail(vrn, "INVALID_VRN", 400)
        .when()
        .post(s"/$vrn/returns", Some(Json.parse(body())))
        .withHeaders("Authorization", "Bearer testtoken")
        .thenAssertThat()
        .statusIs(400)
        .bodyHasPath("\\code", "VRN_INVALID")
    }

    "reject submission with invalid payload" in {
      given()
        .stubAudit
        .userIsFullyAuthorisedForTheNrsDependantResource
        .nrs().nrsVatReturnSuccessFor(vrn)
        .des().vatReturns.expectVatReturnToFail(vrn, "INVALID_PAYLOAD", 400)
        .when()
        .post(s"/$vrn/returns", Some(Json.parse(body())))
        .withHeaders("Authorization", "Bearer testtoken")
        .thenAssertThat()
        .statusIs(400)
        .bodyHasPath("\\code", "INVALID_REQUEST")
    }

    "reject duplicate submission" in {
      given()
        .stubAudit
        .userIsFullyAuthorisedForTheNrsDependantResource
        .nrs().nrsVatReturnSuccessFor(vrn)
        .des().vatReturns.expectVatReturnToFail(vrn, "DUPLICATE_SUBMISSION", 409)
        .when()
        .post(s"/$vrn/returns", Some(Json.parse(body())))
        .withHeaders("Authorization", "Bearer testtoken")
        .thenAssertThat()
        .statusIs(403)
        .bodyHasPath("\\errors(0)\\code", "DUPLICATE_SUBMISSION")
    }

    "reject submissions that are made too early" in {
      given()
        .stubAudit
        .userIsFullyAuthorisedForTheNrsDependantResource
        .nrs().nrsVatReturnSuccessFor(vrn)
        .des().vatReturns.expectVatReturnToFail(vrn, "TAX_PERIOD_NOT_ENDED", 403)
        .when()
        .post(s"/$vrn/returns", Some(Json.parse(body())))
        .withHeaders("Authorization", "Bearer testtoken")
        .thenAssertThat()
        .statusIs(403)
        .bodyHasPath("\\code", "TAX_PERIOD_NOT_ENDED")
    }

    "fail if submission to  Non-Repudiation service failed" in {
      given()
        .stubAudit
        .userIsFullyAuthorisedForTheNrsDependantResource
        .nrs().nrsFailurefor(vrn)
        .when()
        .post(s"/$vrn/returns", Some(Json.parse(body())))
        .withHeaders("Authorization", "Bearer testtoken")
        .thenAssertThat()
        .statusIs(500)
        .bodyHasPath("\\code", "INTERNAL_SERVER_ERROR")
    }
  }

  "VAT returns retrieval" should {

    "allow users to retrieve VAT returns for last four years" in {
      given()
        .stubAudit
        .userIsFullyAuthorisedForTheResource
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

    "allow users to retrieve VAT returns without receivedAt field for last four years" in {
      given()
        .stubAudit
        .userIsFullyAuthorisedForTheResource
        .des().vatReturns.expectVatReturnSearchForWithoutReceivedAt(vrn, "0001")
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
        .stubAudit
        .userIsFullyAuthorisedForTheResource
        .des().vatReturns.expectInvalidVatReturnSearchFor(vrn, "0001")
        .when()
        .get(s"/$vrn/returns/0001")
        .thenAssertThat()
        .statusIs(500)
    }

    "return internal server error on empty body response" in {
      given()
        .stubAudit
        .userIsFullyAuthorisedForTheResource
        .des().vatReturns.expectEmptyVatReturnSearchFor(vrn, "0001")
        .when()
        .get(s"/$vrn/returns/0001")
        .thenAssertThat()
        .statusIs(500)
    }

    "reject client with no authorization" in {
      given()
        .stubAudit
        .userIsNotAuthorisedForTheResource
        .when()
        .get(s"/$vrn/returns/0001")
        .thenAssertThat()
        .statusIs(403)
        .bodyHasPath("\\code", "CLIENT_OR_AGENT_NOT_AUTHORISED")
    }

    "return bad request (400) if the vrn is invalid" in {
      given()
        .stubAudit
        .userIsFullyAuthorisedForTheResource
        .des().vatReturns.expectVatReturnSearchFor(vrn, "0001")
        .when()
        .get(s"/invalid_vrn/returns/0001")
        .thenAssertThat()
        .statusIs(400)
        .bodyHasPath("\\code", "VRN_INVALID")
    }

    "return forbidden (403) if the vat return was submitted longer than 4 years ago" in {
      given()
        .stubAudit
        .userIsFullyAuthorisedForTheResource
        .des().vatReturns.expectVatReturnRetrieveToFail(vrn, "DATE_RANGE_TOO_LARGE")
        .when()
        .get(s"/$vrn/returns/0001")
        .thenAssertThat()
        .statusIs(403)
        .bodyHasPath("\\code", "BUSINESS_ERROR")
        .bodyHasPath("\\errors(0)\\code", "DATE_RANGE_TOO_LARGE")
    }

    "return internal server error (500) if the vat returns with DES vrn not found error" in {
      given()
        .stubAudit
        .userIsFullyAuthorisedForTheResource
        .des().vatReturns.expectVatReturnRetrieveToFail(vrn, "VRN_NOT_FOUND")
        .when()
        .get(s"/$vrn/returns/0001")
        .thenAssertThat()
        .statusIs(500)
        .bodyHasPath("\\code", "INTERNAL_SERVER_ERROR")
    }

    "return internal server error (500) if the vat returns from DES got NOT_FOUND_VRN error" in {
      given()
        .stubAudit
        .userIsFullyAuthorisedForTheResource
        .des().vatReturns.expectVatReturnRetrieveToFail(vrn, "NOT_FOUND_VRN")
        .when()
        .get(s"/$vrn/returns/0001")
        .thenAssertThat()
        .statusIs(500)
        .bodyHasPath("\\code", "INTERNAL_SERVER_ERROR")
    }

    "return bad request (400) if the periodKey is invalid" in {
      given()
        .stubAudit
        .userIsFullyAuthorisedForTheResource
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
        .stubAudit
        .userIsFullyAuthorisedForTheResource
        .des().vatReturns.expectNonExistentVrnFor(vrn, "0001")
        .when()
        .get(s"/$vrn/returns/0001")
        .thenAssertThat()
        .statusIs(404)
    }

    "return X-Content-Type-Options header with non-existent VRN" in {
      given()
        .stubAudit
        .userIsFullyAuthorisedForTheResource
        .des().vatReturns.expectNonExistentVrnFor(vrn, "0001")
        .when()
        .get(s"/$vrn/returns/0001")
        .thenAssertThat()
        .hasHeader("X-Content-Type-Options")
    }
  }
}
