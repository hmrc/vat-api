package uk.gov.hmrc.vat.definition

import uk.gov.hmrc.support.BaseFunctionalSpec

class DocumentationSpec extends BaseFunctionalSpec {

  "Request to /api/definition" should {
    "return 200 with json response" in {
      given()
        .when()
        .get("/api/definition")
        .thenAssertThat()
        .statusIs(200)
    }
  }

  "Request to /api/conf/0.1/application.raml" should {
    "return 200 with raml response" in {
      given()
        .when()
        .get("/api/conf/0.1/application.raml")
        .thenAssertThat()
        .statusIs(200)
    }
  }


}
