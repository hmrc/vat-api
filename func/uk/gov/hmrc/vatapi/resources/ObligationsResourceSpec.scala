package uk.gov.hmrc.vatapi.resources

import uk.gov.hmrc.support.BaseFunctionalSpec

class ObligationsResourceSpec extends BaseFunctionalSpec {

  "retrieveObligations" should {

    "return code 400 when vrn is invalid" in {
      when()
        .get(s"/abc/obligations?from=2017-01-01&to=2017-03-31&status=A")
        .thenAssertThat()
        .statusIs(400)
        .isBadRequest("VRN_INVALID")
    }

    "return code 400 when from is missing" in {
      when()
        .get(s"/$vrn/obligations?to=2017-03-31&status=A")
        .thenAssertThat()
        .statusIs(400)
    }

    "return code 400 when from is invalid" in {
      when()
        .get(s"/$vrn/obligations?from=abc&to=2017-03-31&status=A")
        .thenAssertThat()
        .statusIs(400)
    }

    "return code 400 when to is missing" in {
      when()
        .get(s"/$vrn/obligations?from=2017-01-01&status=A")
        .thenAssertThat()
        .statusIs(400)
    }

    "return code 400 when to is invalid" in {
      when()
        .get(s"/$vrn/obligations?from=2017-01-01&to=abc&status=A")
        .thenAssertThat()
        .statusIs(400)
    }

    "return code 400 when status is missing" in {
      when()
        .get(s"/$vrn/obligations?from=2017-01-01&to=2017-03-31")
        .thenAssertThat()
        .statusIs(400)
    }

    "return code 400 when status is invalid" in {
      when()
        .get(s"/$vrn/obligations?from=2017-01-01&to=2017-03-31&status=X")
        .thenAssertThat()
        .statusIs(400)
    }

    "return code 400 when from is after to" in {
      when()
        .get(s"/$vrn/obligations?from=2017-04-01&to=2017-03-31&status=A")
        .thenAssertThat()
        .statusIs(400)
    }

    "return code 400 when date range between from and to is more than 366 days" in {
      when()
        .get(s"/$vrn/obligations?from=2017-01-01&to=2018-01-02&status=A")
        .thenAssertThat()
        .statusIs(400)
    }

    "return code 404 when obligations does not exist" in {
      given()
        .des().obligations.obligationNotFoundFor(vrn)
        .when()
        .get(s"/$vrn/obligations?from=2017-01-01&to=2017-08-31&status=A")
        .thenAssertThat()
        .statusIs(404)
    }

    "return code 200 with a set of obligations" in {
      given()
        .des().obligations.returnObligationsFor(vrn)
        .when()
        .get(s"/$vrn/obligations?from=2017-01-01&to=2017-08-31&status=A")
        .thenAssertThat()
        .statusIs(200)
        .bodyIsLike(Jsons.Obligations(firstMet = "F").toString)
    }

  }

}
