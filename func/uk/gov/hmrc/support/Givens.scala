package uk.gov.hmrc.support

import com.github.tomakehurst.wiremock.client.WireMock._
import play.api.http.Status._
import uk.gov.hmrc.assets.des.Errors
import uk.gov.hmrc.assets.des.Obligations.Obligations
import uk.gov.hmrc.domain.Vrn


class Givens(httpVerbs: HttpVerbs) {

  def when(): HttpVerbs = httpVerbs

  def stubAudit: Givens = {
    stubFor(post(urlPathMatching(s"/write/audit.*"))
      .willReturn(
        aResponse()
          .withStatus(NO_CONTENT)))
    this
  }

  def userIsFullyAuthorisedForTheResource: Givens = {
    stubFor(
      post(urlPathEqualTo(s"/auth/authorise"))
        .willReturn(
          aResponse()
            .withStatus(200)
            .withBody("""
                        |{
                        |  "agentInformation": {
                        |        "agentCode" : "TZRXXV",
                        |        "agentFriendlyName" : "Bodgitt & Legget LLP",
                        |        "agentId": "BDGL"
                        |    },
                        |  "affinityGroup": "Organisation",
                        |  "allEnrolments": [
                        |   {
                        |         "key":"HMRC-MTD-VAT",
                        |         "identifiers":[
                        |            {
                        |               "key":"VRN",
                        |               "value":"1000051409"
                        |            }
                        |         ],
                        |         "state":"Activated"
                        |      }
                        |  ]
                        |}
                      """.stripMargin)))
    this
  }

  class Des(givens: Givens) {
    def isATeapotFor(vrn: Vrn): Givens = {
      stubFor(
        any(urlMatching(s".*/(calculation-data|vrn)/$vrn.*"))
          .willReturn(aResponse()
            .withStatus(418)))

      givens
    }

    def invalidVrnFor(vrn: Vrn): Givens = {
      stubFor(any(urlMatching(s".*/vrn/$vrn.*"))
        .willReturn(
          aResponse()
            .withStatus(400)
            .withHeader("Content-Type", "application/json")
            .withBody(Errors.invalidVrn)))

      givens
    }

    object obligations {

      def obligationNotFoundFor(vrn: Vrn): Givens = {
        stubFor(get(urlMatching(s".*/vrn/$vrn.*"))
          .willReturn(
            aResponse()
              .withStatus(404)
              .withHeader("Content-Type", "application/json")
              .withBody(Errors.notFound)))

        givens
      }

      def returnObligationsFor(vrn: Vrn): Givens = {
        stubFor(any(urlMatching(s".*/vrn/$vrn.*"))
          .willReturn(
            aResponse()
              .withStatus(200)
              .withHeader("Content-Type", "application/json")
              .withHeader("CorrelationId", "abc")
              .withBody(Obligations(vrn.toString()))))

        givens
      }
    }

  }

  def des() = new Des(this)
}
