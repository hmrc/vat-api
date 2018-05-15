package uk.gov.hmrc.vatapi.resources

import play.api.http.Status._
import uk.gov.hmrc.assets.des.Errors
import uk.gov.hmrc.support.BaseFunctionalSpec

class ObligationsResourceSpec extends BaseFunctionalSpec {

  "retrieveObligations" should {

    "return code 400 when vrn is invalid" in {
      given()
        .stubAudit
      when()
        .get(s"/abc/obligations?from=2017-01-01&to=2017-03-31")
        .thenAssertThat()
        .statusIs(BAD_REQUEST)
        .isBadRequest("VRN_INVALID")
    }

    "return code 400 when status is A" in {
      given()
        .stubAudit
      when()
        .get(s"/$vrn/obligations?from=2017-01-01&to=2017-03-31&status=A")
        .thenAssertThat()
        .statusIs(BAD_REQUEST)
        .isBadRequest("VRN_INVALID")
    }

    "return code 400 when from is missing" in {
      given()
        .stubAudit
      when()
        .get(s"/$vrn/obligations?to=2017-03-31&status=A")
        .thenAssertThat()
        .statusIs(BAD_REQUEST)
    }

    "return code 400 when from is invalid" in {
      given()
        .stubAudit
      when()
        .get(s"/$vrn/obligations?from=abc&to=2017-03-31&status=A")
        .thenAssertThat()
        .statusIs(BAD_REQUEST)
    }

    "return code 400 when to is missing" in {
      given()
        .stubAudit
      when()
        .get(s"/$vrn/obligations?from=2017-01-01&status=A")
        .thenAssertThat()
        .statusIs(BAD_REQUEST)
    }

    "return code 400 when to is invalid" in {
      given()
        .stubAudit
      when()
        .get(s"/$vrn/obligations?from=2017-01-01&to=abc&status=A")
        .thenAssertThat()
        .statusIs(BAD_REQUEST)
    }

    "return code 400 when status is invalid" in {
      given()
        .stubAudit
      when()
        .get(s"/$vrn/obligations?from=2017-01-01&to=2017-03-31&status=X")
        .thenAssertThat()
        .statusIs(BAD_REQUEST)
    }

    "return code 400 when from is after to" in {
      given()
        .stubAudit
      when()
        .get(s"/$vrn/obligations?from=2017-04-01&to=2017-03-31&status=A")
        .thenAssertThat()
        .statusIs(BAD_REQUEST)
    }

    "return code 400 when date range between from and to is more than 366 days" in {
      given()
        .stubAudit
      when()
        .get(s"/$vrn/obligations?from=2017-01-01&to=2018-01-02&status=A")
        .thenAssertThat()
        .statusIs(BAD_REQUEST)
    }

    "return code 400 when idNumber parameter is invalid" in {
      given()
        .stubAudit
        .userIsFullyAuthorisedForTheResource
        .des().obligations.obligationParamsFor(vrn, Errors.invalidIdNumber)
        .when()
        .get(s"/$vrn/obligations?from=2017-01-01&to=2017-08-31&status=A")
        .thenAssertThat()
        .statusIs(BAD_REQUEST)
    }

    "return code 404 when obligations does not exist" in {
      given()
        .stubAudit
        .userIsFullyAuthorisedForTheResource
        .des().obligations.obligationNotFoundFor(vrn)
        .when()
        .get(s"/$vrn/obligations?from=2017-01-01&to=2017-08-31")
        .thenAssertThat()
        .statusIs(NOT_FOUND)
    }

    "return code 500 when regime type parameter is invalid" in {
      given()
        .stubAudit
        .userIsFullyAuthorisedForTheResource
        .des().obligations.obligationParamsFor(vrn, Errors.invalidRegime)
        .when()
        .get(s"/$vrn/obligations?from=2017-01-01&to=2017-08-31")
        .thenAssertThat()
        .statusIs(INTERNAL_SERVER_ERROR)
    }

    "return code 400 when status parameter is invalid" in {
      given()
        .stubAudit
        .userIsFullyAuthorisedForTheResource
        .des().obligations.obligationParamsFor(vrn, Errors.invalidStatus)
        .when()
        .get(s"/$vrn/obligations?from=2017-01-01&to=2017-08-31&status=A")
        .thenAssertThat()
        .statusIs(BAD_REQUEST)
    }

    "return code 500 when idType parameter is invalid" in {
      given()
        .stubAudit
        .userIsFullyAuthorisedForTheResource
        .des().obligations.obligationParamsFor(vrn, Errors.invalidIdType)
        .when()
        .get(s"/$vrn/obligations?from=2017-01-01&to=2017-08-31")
        .thenAssertThat()
        .statusIs(INTERNAL_SERVER_ERROR)
    }

    "return code 200 with a set of obligations" in {
      given()
        .stubAudit
        .userIsFullyAuthorisedForTheResource
        .des().obligations.returnObligationsFor(vrn)
        .when()
        .get(s"/$vrn/obligations?from=2017-01-01&to=2017-08-31")
        .thenAssertThat()
        .statusIs(OK)
        .bodyIsLike(Jsons.Obligations(firstMet = "F").toString)
    }

    "return code 200 with a set of obligations with out identifications" in {
      given()
        .stubAudit
        .userIsFullyAuthorisedForTheResource
        .des().obligations.returnObligationsWithoutIdentificationFor(vrn)
        .when()
        .get(s"/$vrn/obligations?from=2017-01-01&to=2017-08-31")
        .thenAssertThat()
        .statusIs(OK)
        .bodyIsLike(Jsons.Obligations(firstMet = "F").toString)
    }

    "return code 200 with a set of obligations with identifications but no incomeSourceType" in {
      given()
        .stubAudit
        .userIsFullyAuthorisedForTheResource
        .des().obligations.returnObligationsWithIdentificationButNoIncomeSourceTypeFor(vrn)
        .when()
        .get(s"/$vrn/obligations?from=2017-01-01&to=2017-08-31")
        .thenAssertThat()
        .statusIs(OK)
        .bodyIsLike(Jsons.Obligations(firstMet = "F").toString)
    }

    "reject client with no authorization" in {
      given()
        .stubAudit
        .userIsNotAuthorisedForTheResource
        .when()
        .get(s"/$vrn/obligations?from=2017-01-01&to=2017-08-31")
        .thenAssertThat()
        .statusIs(FORBIDDEN)
        .bodyHasPath("\\code", "CLIENT_OR_AGENT_NOT_AUTHORISED")
    }
  }

}
