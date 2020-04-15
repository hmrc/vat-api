//package uk.gov.hmrc
//
//import uk.gov.hmrc.support.BaseFunctionalSpec
//import uk.gov.hmrc.vatapi.models.ErrorCode
//import uk.gov.hmrc.vatapi.resources._
//
//class AgentSimulationFilterSpec extends BaseFunctionalSpec {
//
//  "Request for vat returns with Gov-Test-Scenario = CLIENT_OR_AGENT_NOT_AUTHORISED" should {
//    "return HTTP 403 with error code informing client should be subscribed to MTD" in {
//      given()
//        .userIsFullyAuthorisedForTheResource
//        .when()
//        .get(s"/vat/returns/vrn/$vrn")
//        .withHeaders(GovTestScenarioHeader, "CLIENT_OR_AGENT_NOT_AUTHORISED")
//        .thenAssertThat()
//        .statusIs(403)
//        .bodyIsError(ErrorCode.CLIENT_OR_AGENT_NOT_AUTHORISED.toString)
//    }
//  }
//
//  "Submit for vat returns with Gov-Test-Scenario = CLIENT_OR_AGENT_NOT_AUTHORISED" should {
//    "return HTTP 403 with error code informing client should be subscribed to MTD" in {
//      given()
//        .userIsFullyAuthorisedForTheResource
//        .when()
//        .post(s"/$vrn/returns", None)
//        .withHeaders(GovTestScenarioHeader, "CLIENT_OR_AGENT_NOT_AUTHORISED")
//        .thenAssertThat()
//        .statusIs(403)
//        .bodyIsError(ErrorCode.CLIENT_OR_AGENT_NOT_AUTHORISED.toString)
//    }
//  }
//
//  "Request for obligations with Gov-Test-Scenario = CLIENT_OR_AGENT_NOT_AUTHORISED" should {
//    "return HTTP 403 with error code informing client should be subscribed to MTD" in {
//      given()
//        .userIsFullyAuthorisedForTheResource
//        .when()
//        .post(s"/$vrn/obligations?from=2017-01-01&to=2017-08-31&status=A", None)
//        .withHeaders(GovTestScenarioHeader, "CLIENT_OR_AGENT_NOT_AUTHORISED")
//        .thenAssertThat()
//        .statusIs(403)
//        .bodyIsError(ErrorCode.CLIENT_OR_AGENT_NOT_AUTHORISED.toString)
//    }
//  }
//
//  "Request for payments with Gov-Test-Scenario = CLIENT_OR_AGENT_NOT_AUTHORISED" should {
//    "return HTTP 403 with error code informing client should be subscribed to MTD" in {
//      given()
//        .userIsFullyAuthorisedForTheResource
//        .when()
//        .post(s"/$vrn/payments?from=2017-01-01&to=2017-06-02", None)
//        .withHeaders(GovTestScenarioHeader, "CLIENT_OR_AGENT_NOT_AUTHORISED")
//        .thenAssertThat()
//        .statusIs(403)
//        .bodyIsError(ErrorCode.CLIENT_OR_AGENT_NOT_AUTHORISED.toString)
//    }
//  }
//}
