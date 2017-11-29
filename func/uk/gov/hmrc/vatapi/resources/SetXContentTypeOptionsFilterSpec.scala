package uk.gov.hmrc.vatapi.resources

import uk.gov.hmrc.support.BaseFunctionalSpec
import uk.gov.hmrc.vatapi.config.SetXContentTypeOptionsFilter

class SetXContentTypeOptionsFilterSpec extends BaseFunctionalSpec {

  "SetXContentTypeOptionsFilter filter should" should {
    "be applied for api definition" in {
      given()
        .when()
        .get("/api/definition")
        .thenAssertThat()
        .statusIs(200)
        .responseContainsHeader(SetXContentTypeOptionsFilter.xContentTypeOptionsHeader, "nosniff".r)
    }

    "be applied for application.raml" in {
      given()
        .when()
        .get("/api/conf/1.0/application.raml")
        .thenAssertThat()
        .statusIs(200)
        .responseContainsHeader(SetXContentTypeOptionsFilter.xContentTypeOptionsHeader, "nosniff".r)
    }

    "be applied for obligations " in {
      given()
        .des().obligations.returnObligationsFor(vrn)
        .when()
        .get(s"/$vrn/obligations?from=2017-01-01&to=2017-08-31&status=A")
        .thenAssertThat()
        .statusIs(200)
        .responseContainsHeader(SetXContentTypeOptionsFilter.xContentTypeOptionsHeader, "nosniff".r)
    }


    "be applied when vrn is invalid" in {
      when()
        .get(s"/abc/obligations?from=2017-01-01&to=2017-03-31&status=A")
        .thenAssertThat()
        .statusIs(400)
        .responseContainsHeader(SetXContentTypeOptionsFilter.xContentTypeOptionsHeader, "nosniff".r)
    }


    "be applied when obligations does not exist" in {
      given()
        .des().obligations.obligationNotFoundFor(vrn)
        .when()
        .get(s"/$vrn/obligations?from=2017-01-01&to=2017-08-31&status=A")
        .thenAssertThat()
        .statusIs(404)
        .responseContainsHeader(SetXContentTypeOptionsFilter.xContentTypeOptionsHeader, "nosniff".r)
    }

    "be applied for api definition without accept header" in {
      given()
        .when()
        .get("/api/definition")
        .withoutAcceptHeader()
        .thenAssertThat()
        .statusIs(200)
        .responseContainsHeader(SetXContentTypeOptionsFilter.xContentTypeOptionsHeader, "nosniff".r)
    }
  }

}
