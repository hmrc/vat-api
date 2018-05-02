package uk.gov.hmrc.support

import com.github.tomakehurst.wiremock.client.WireMock._
import play.api.http.Status
import uk.gov.hmrc.assets.des.Errors
import uk.gov.hmrc.assets.des.FinancialData._
import uk.gov.hmrc.assets.des.Obligations.{Obligations, ObligationsWithNoIncomeSourceType, ObligationsWithoutIdentification}
import uk.gov.hmrc.assets.nrs.NRS
import uk.gov.hmrc.domain.Vrn


class Givens(httpVerbs: HttpVerbs) {

  def when(): HttpVerbs = httpVerbs

  def missingBearerToken: Givens = {
    stubFor(
      post(urlPathEqualTo(s"/auth/authorise"))
        .willReturn(
          aResponse()
            .withStatus(401)
            .withHeader("Content-Length", "0")
            .withHeader("WWW-Authenticate",
              "MDTP detail=\"MissingBearerToken\"")))

    this
  }

  def upstream502BearerTokenDecryptionError: Givens = {
    stubFor(
      post(urlPathEqualTo(s"/auth/authorise"))
        .willReturn(
          aResponse()
            .withStatus(502)
            .withHeader("Content-Type", "application/json")
            .withBody(
              """{"statusCode":500,"message":"Unable to decrypt value"}""")))

    this
  }

  def upstream5xxError: Givens = {
    stubFor(
      post(urlPathEqualTo(s"/auth/authorise"))
        .willReturn(aResponse()
          .withStatus(500)))

    this
  }

  def upstream4xxError: Givens = {
    stubFor(
      post(urlPathEqualTo(s"/auth/authorise"))
        .willReturn(aResponse()
          .withStatus(403)))

    this
  }

  def upstreamNonFatal: Givens = {
    stubFor(
      post(urlPathEqualTo(s"/auth/authorise"))
        .willReturn(aResponse()
          .withStatus(509))) // very brittle test that relies on how http-verbs.HttpErrorFunctions maps upstream status codes

    this
  }

  def userIsNotAuthorisedForTheResource: Givens = {
    stubFor(
      post(urlPathEqualTo(s"/auth/authorise"))
        .willReturn(
          aResponse()
            .withStatus(401)
            .withHeader("Content-Length", "0")
            .withHeader("WWW-Authenticate",
              "MDTP detail=\"InsufficientEnrolments\"")))

    // The user is an 'Individual/Group', so the affinity check for 'Agent' should fail.
    stubFor(
      post(urlPathEqualTo(s"/auth/authorise"))
        .withRequestBody(containing("Agent"))
        .willReturn(
          aResponse()
            .withStatus(401)
            .withHeader("Content-Length", "0")
            .withHeader("WWW-Authenticate",
              "MDTP detail=\"UnsupportedAffinityGroup\"")))

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
                        |  "authorisedEnrolments": [
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

  def userIsFullyAuthorisedForTheNrsDependantResource: Givens = {
    stubFor(
      post(urlPathEqualTo(s"/auth/authorise"))
        .willReturn(
          aResponse()
            .withStatus(200)
            .withBody("""
                        |{
                        |  "internalId": "some-id",
                        |  "externalId": "some-id",
                        |  "credentials" : {"providerId":"8124873381064832", "providerType":"GovernmentGateway"},
                        |  "confidenceLevel": 200,
                        |  "name": { "name": "test", "lastName": "test" },
                        |  "dateOfBirth": "1985-01-01",
                        |  "postCode":"NW94HD",
                        |  "description" : "description",
                        |  "agentInformation": {
                        |        "agentCode" : "TZRXXV",
                        |        "agentFriendlyName" : "Bodgitt & Legget LLP",
                        |        "agentId": "BDGL"
                        |    },
                        |  "groupIdentifier" : "GroupId",
                        |  "credentialRole": "admin",
                        |  "itmpName" : { "givenName": "test", "middleName": "test", "familyName": "test" },
                        |  "itmpDateOfBirth" : "1985-01-01",
                        |  "itmpAddress" : {
                        |    "line1" : "Line 1",
                        |    "line2" : "",
                        |    "line3" : "",
                        |    "line4" : "",
                        |    "line5" : "",
                        |    "postCode" :"NW94HD",
                        |    "countryName" : "United Kingdom",
                        |    "countryCode" : "UK"
                        |    },
                        |  "affinityGroup": "Organisation",
                        |  "loginTimes": {
                        |     "currentLogin": "2016-11-27T09:00:00.000Z",
                        |     "previousLogin": "2016-11-01T12:00:00.000Z"
                        |  },
                        |  "authorisedEnrolments": [
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

      def returnObligationsWithoutIdentificationFor(vrn: Vrn): Givens = {
        stubFor(any(urlMatching(s".*/vrn/$vrn.*"))
          .willReturn(
            aResponse()
              .withStatus(200)
              .withHeader("Content-Type", "application/json")
              .withHeader("CorrelationId", "abc")
              .withBody(ObligationsWithoutIdentification(vrn.toString()))))

        givens
      }

      def returnObligationsWithIdentificationButNoIncomeSourceTypeFor(vrn: Vrn): Givens = {
        stubFor(any(urlMatching(s".*/vrn/$vrn.*"))
          .willReturn(
            aResponse()
              .withStatus(200)
              .withHeader("Content-Type", "application/json")
              .withHeader("CorrelationId", "abc")
              .withBody(ObligationsWithNoIncomeSourceType(vrn.toString()))))

        givens
      }
    }

    object vatReturns {
      def expectVatReturnSubmissionFor(vrn: Vrn): Givens = {
        stubFor(
          any(urlMatching(s"/enterprise/return/vat/$vrn"))
            .willReturn(
              aResponse()
                .withStatus(200)
                .withBody(s"""
                             |{
                             |    "processingDate": "2018-03-01T11:43:43.195Z",
                             |    "paymentIndicator": "BANK",
                             |    "formBundleNumber": "891713832155"
                             |}
                            """.stripMargin)
            )
        )
        givens
      }

      def expectVatReturnToFail(vrn: Vrn, code: String, status: Int, reason: String = "Irrelevant"): Givens = {
        stubFor(
          any(urlMatching(s"/enterprise/return/vat/$vrn"))
            .willReturn(
              aResponse()
                .withStatus(status)
                .withBody(s"""
                             |{
                             |  "code": "$code",
                             |  "reason": "$reason"
                             |}
                            """.stripMargin)
            )
        )
        givens
      }

      def expectVatReturnRetrieveToFail(vrn: Vrn, code: String, reason: String = "Irrelevant"): Givens = {
        stubFor(
          get(urlEqualTo(s"/vat/returns/vrn/$vrn?period-key=0001"))
            .willReturn(
              aResponse()
                .withStatus(403)
                .withBody(s"""
                             |{
                             |  "code": "$code",
                             |  "reason": "$reason"
                             |}
                            """.stripMargin)
            )
        )
        givens
      }

      def expectVatReturnSearchFor(vrn: Vrn, periodKey: String): Givens = {
        stubFor(
          get(urlEqualTo(s"/vat/returns/vrn/$vrn?period-key=$periodKey"))
            .willReturn(
              aResponse()
                .withStatus(200)
                .withBody("""
                    {
                      "periodKey": "0001",
                      "inboundCorrespondenceFromDate": "2017-01-01",
                      "inboundCorrespondenceToDate": "2017-12-31",
                      "vatDueSales": 100.25,
                      "vatDueAcquisitions": 100.25,
                      "vatDueTotal": 200.50,
                      "vatReclaimedCurrPeriod": 100.25,
                      "vatDueNet": 100.25,
                      "totalValueSalesExVAT": 100,
                      "totalValuePurchasesExVAT": 100,
                      "totalValueGoodsSuppliedExVAT": 100,
                      "totalAllAcquisitionsExVAT": 100,
                      "receivedAt": "2017-12-18T16:49:20.678Z"
                    }""")
            )
        )
        givens
      }

      def expectVatReturnSearchForWithoutReceivedAt(vrn: Vrn, periodKey: String): Givens = {
        stubFor(
          get(urlEqualTo(s"/vat/returns/vrn/$vrn?period-key=$periodKey"))
            .willReturn(
              aResponse()
                .withStatus(200)
                .withBody("""
                    {
                      "periodKey": "0001",
                      "inboundCorrespondenceFromDate": "2017-01-01",
                      "inboundCorrespondenceToDate": "2017-12-31",
                      "vatDueSales": 100.25,
                      "vatDueAcquisitions": 100.25,
                      "vatDueTotal": 200.50,
                      "vatReclaimedCurrPeriod": 100.25,
                      "vatDueNet": 100.25,
                      "totalValueSalesExVAT": 100,
                      "totalValuePurchasesExVAT": 100,
                      "totalValueGoodsSuppliedExVAT": 100,
                      "totalAllAcquisitionsExVAT": 100
                    }""")
            )
        )
        givens
      }

      def expectInvalidVatReturnSearchFor(vrn: Vrn, periodKey: String): Givens = {
        stubFor(
          get(urlEqualTo(s"/vat/returns/vrn/$vrn?period-key=$periodKey"))
            .willReturn(
              aResponse()
                .withStatus(200)
                .withBody("not-json")
            )
        )
        givens
      }

      def expectEmptyVatReturnSearchFor(vrn: Vrn, periodKey: String): Givens = {
        stubFor(
          get(urlEqualTo(s"/vat/returns/vrn/$vrn?period-key=$periodKey"))
            .willReturn(
              aResponse()
                .withStatus(200)
                .withBody("")
            )
        )
        givens
      }

      def expectNonExistentVrnFor(vrn: Vrn, periodKey: String): Givens = {
        stubFor(
          get(urlEqualTo(s"/vat/returns/vrn/$vrn?period-key=$periodKey"))
            .willReturn(aResponse().withStatus(404))
        )
        givens
      }
    }

    object FinancialData {
      def singleLiabilityFor(vrn: Vrn): Givens = {
        stubFor(any(urlMatching(s".*/VRN/$vrn.*"))
          .willReturn(
            aResponse()
              .withStatus(200)
              .withHeader("Content-Type", "application/json")
              .withBody(oneLiability.toString)
          ))
        givens
      }
      def overlappingLiabilitiesFor(vrn: Vrn): Givens = {
        stubFor(any(urlMatching(s".*/VRN/$vrn.*"))
          .willReturn(
            aResponse()
              .withStatus(200)
              .withHeader("Content-Type", "application/json")
              .withBody(liabilitiesOverlapping.toString)
          ))
        givens
      }
      def minLiabilityFor(vrn: Vrn): Givens = {
        stubFor(any(urlMatching(s".*/VRN/$vrn.*"))
          .willReturn(
            aResponse()
              .withStatus(200)
              .withHeader("Content-Type", "application/json")
              .withBody(minLiability.toString)
          ))
        givens
      }

      def multipleLiabilitiesFor(vrn: Vrn): Givens = {
        stubFor(any(urlMatching(s".*/VRN/$vrn.*"))
          .willReturn(
            aResponse()
              .withStatus(200)
              .withHeader("Content-Type", "application/json")
              .withBody(multipleLiabilities.toString)
          )
        )
        givens
      }
      def emptyLiabilitiesFor(vrn: Vrn): Givens = {
        stubFor(any(urlMatching(s".*/VRN/$vrn.*"))
          .willReturn(
            aResponse()
              .withStatus(200)
              .withHeader("Content-Type", "application/json")
              .withBody(emptyLiabilities.toString)
          )
        )
        givens
      }

      def singlePaymentFor(vrn: Vrn): Givens = {
        stubFor(any(urlMatching(s".*/VRN/$vrn.*"))
          .willReturn(
            aResponse()
              .withStatus(200)
              .withHeader("Content-Type", "application/json")
              .withBody(onePayment.toString)
          ))
        givens
      }
      def overlappingPaymentsFor(vrn: Vrn): Givens = {
        stubFor(any(urlMatching(s".*/VRN/$vrn.*"))
          .willReturn(
            aResponse()
              .withStatus(200)
              .withHeader("Content-Type", "application/json")
              .withBody(overlappingPayment.toString)
          ))
        givens
      }
      def minPaymentFor(vrn: Vrn): Givens = {
        stubFor(any(urlMatching(s".*/VRN/$vrn.*"))
          .willReturn(
            aResponse()
              .withStatus(200)
              .withHeader("Content-Type", "application/json")
              .withBody(minPayment.toString)
          ))
        givens
      }

      def multiplePaymentsFor(vrn: Vrn): Givens = {
        stubFor(any(urlMatching(s".*/VRN/$vrn.*"))
          .willReturn(
            aResponse()
              .withStatus(200)
              .withHeader("Content-Type", "application/json")
              .withBody(multiplePayments.toString)
          )
        )
        givens
      }
      def emptyPaymentsFor(vrn: Vrn): Givens = {
        stubFor(any(urlMatching(s".*/VRN/$vrn.*"))
          .willReturn(
            aResponse()
              .withStatus(200)
              .withHeader("Content-Type", "application/json")
              .withBody(emptyLiabilities.toString)
          )
        )
        givens
      }
    }
  }

  class Nrs(givens: Givens) {
    def nrsVatReturnSuccessFor(vrn: Vrn): Givens = {
      stubFor(any(urlMatching(s".*/submission.*"))
        .willReturn(
          aResponse()
            .withStatus(Status.ACCEPTED)
            .withHeader("Content-Type", "application/json")
            .withHeader("Receipt-Id", "de1249ad-c242-4f22-9fe6-f357b1bfcccf")
            .withHeader("Receipt-Signature", "757b1365-d89e-4dac-8317-ba87efca6c21")
            .withHeader("Receipt-Timestamp", "2018-03-27T15:10:44.798Z")
            .withBody(NRS.success().toString()
            )
        )
      )
      givens
    }

    def nrsFailurefor(vrn: Vrn): Givens = {
      stubFor(any(urlMatching(s".*/submission/$vrn.*"))
        .willReturn(
          aResponse()
            .withStatus(Status.BAD_REQUEST)
            .withBody("{}")
        )
      )
      givens
    }
  }

  def des() = new Des(this)
  def nrs() = new Nrs(this)

}
