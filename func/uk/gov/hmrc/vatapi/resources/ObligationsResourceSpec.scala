package uk.gov.hmrc.vatapi.resources

import uk.gov.hmrc.support.BaseFunctionalSpec

class ObligationsResourceSpec extends BaseFunctionalSpec {

  "retrieveObligations" should {

    "return code 400 when vrn is invalid" in {
      when()
        .get(s"/vat/abc/obligations?fromDate=2017-01-01&toDate=2017-03-31&status=A")
        .thenAssertThat()
        .statusIs(400)
    }

    "return code 400 when fromDate is missing" in {
      when()
        .get(s"/vat/abc/obligations?toDate=2017-03-31&status=A")
        .thenAssertThat()
        .statusIs(400)
    }

    "return code 400 when fromDate is invalid" in {
      when()
        .get(s"/vat/abc/obligations?fromDate=abc&toDate=2017-03-31&status=A")
        .thenAssertThat()
        .statusIs(400)
    }

    "return code 400 when toDate is missing" in {
      when()
        .get(s"/vat/abc/obligations?fromDate=2017-01-01&status=A")
        .thenAssertThat()
        .statusIs(400)
    }

    "return code 400 when toDate is invalid" in {
      when()
        .get(s"/vat/abc/obligations?fromDate=2017-01-01&toDate=abc&status=A")
        .thenAssertThat()
        .statusIs(400)
    }

    "return code 400 when status is missing" in {
      when()
        .get(s"/vat/abc/obligations?fromDate=2017-01-01&toDate=2017-03-31")
        .thenAssertThat()
        .statusIs(400)
    }

    "return code 400 when status is invalid" in {
      when()
        .get(s"/vat/abc/obligations?fromDate=2017-01-01&toDate=2017-03-31&status=X")
        .thenAssertThat()
        .statusIs(400)
    }

    "return code 400 when fromDate is after toDate" in {
      when()
        .get(s"/vat/abc/obligations?fromDate=2017-04-01&toDate=2017-03-31&status=A")
        .thenAssertThat()
        .statusIs(400)
    }

    "return code 400 when date range between fromDate and toDate is more than 366 days" in {
      when()
        .get(s"/vat/abc/obligations?fromDate=2017-01-01&toDate=2018-01-02&status=A")
        .thenAssertThat()
        .statusIs(400)
    }

    "return code 404 when obligations does not exist" in {
      given()
        .des().obligations.obligationNotFoundFor(vrn)
        .when()
        .get(s"/vat/$vrn/obligations?fromDate=2017-01-01&toDate=2017-03-31&status=A")
        .thenAssertThat()
        .statusIs(404)
    }

    "return code 200 with a set of obligations" in {
      given()
        .des().obligations.returnObligationsFor(vrn)
        .when()
        .get(s"/vat/$vrn/obligations?fromDate=2017-01-01&toDate=2017-03-31&status=A")
        .thenAssertThat()
        .statusIs(200)
        .bodyIsLike(Jsons.Obligations().toString)
    }

  }

}
