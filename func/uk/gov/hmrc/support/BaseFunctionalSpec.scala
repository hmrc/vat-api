package uk.gov.hmrc.support

import com.github.tomakehurst.wiremock.client.WireMock._
import com.github.tomakehurst.wiremock.matching.RequestPatternBuilder
import org.json.{JSONArray, JSONObject}
import org.skyscreamer.jsonassert.JSONAssert.assertEquals
import org.skyscreamer.jsonassert.JSONCompareMode.LENIENT
import play.api.libs.json._
import uk.gov.hmrc.api.controllers.ErrorNotFound
import uk.gov.hmrc.domain.{Nino, Vrn}
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}
import uk.gov.hmrc.vatapi.{TestApplication, VrnGenerator}
import uk.gov.hmrc.vatapi.models.ErrorNotImplemented
import uk.gov.hmrc.vatapi.resources.DesJsons

import scala.collection.mutable
import scala.util.matching.Regex
import org.joda.time.LocalDate

trait BaseFunctionalSpec extends TestApplication {
  protected val vrn = VrnGenerator().nextVrn()

  class Assertions(request: String, response: HttpResponse)(
      implicit urlPathVariables: mutable.Map[String, String])
      extends UrlInterpolation {
    def jsonBodyIsEmptyObject() = response.json shouldBe Json.obj()

    def jsonBodyIsEmptyArray() = response.json shouldBe JsArray()

    def responseContainsHeader(name: String, pattern: Regex): Assertions = {
      response.header(name) match {
        case Some(h) => h should fullyMatch regex pattern
        case _       => fail(s"Header [$name] not found in the response headers")
      }
      this
    }

    def when() = new HttpVerbs()

    def butResponseHasNo(sourceName: String, summaryName: String = "") = {
      val jsvOpt =
        // FIXME: use \\
        if (summaryName.isEmpty)
          (response.json \ "_embedded" \ sourceName).toOption
        else (response.json \ "_embedded" \ sourceName \ summaryName).toOption

      jsvOpt match {
        case Some(v) =>
          v.asOpt[List[String]] match {
            case Some(list) => list.isEmpty shouldBe true
            case _          =>
          }
        case None => ()
      }
      this
    }

    def bodyIsError(code: String) = body(_ \ "code").is(code)

    def isValidationError(error: (String, String)): Assertions =
      isValidationError(error._1, error._2)

    def isValidationError(path: String, code: String) = {
      statusIs(400).contentTypeIsJson().body(_ \ "code").is("INVALID_REQUEST")

      val errors = (response.json \ "errors").toOption
      errors match {
        case None => fail("didn't find 'errors' element in the json response")
        case Some(e) =>
          (e(0) \ "path").toOption shouldBe Some(JsString(path))
          (e(0) \ "code").toOption shouldBe Some(JsString(code))
      }
      this
    }

    def isBadRequest(path: String, code: String): Assertions = {
      statusIs(400)
        .contentTypeIsJson()
        .body(_ \ "path")
        .is(path)
        .body(_ \ "code")
        .is(code)
      this
    }

    def isBadRequest(code: String): Assertions = {
      statusIs(400).contentTypeIsJson().body(_ \ "code").is(code)
      this
    }

    def isBadRequest: Assertions = {
      isBadRequest("INVALID_REQUEST")
    }

    def isNotFound = {
      statusIs(404).contentTypeIsJson().bodyIsError(ErrorNotFound.errorCode)
      this
    }

    def isNotImplemented = {
      statusIs(501)
        .contentTypeIsJson()
        .bodyIsError(ErrorNotImplemented.errorCode)
      this
    }

    def contentTypeIsXml() = contentTypeIs("application/xml")

    def contentTypeIsJson() = contentTypeIs("application/json")

    def contentTypeIsHalJson() = contentTypeIs("application/hal+json")

    def noInteractionsWithExternalSystems() = {
      verify(0, RequestPatternBuilder.allRequests())
    }

    def bodyIs(expectedBody: String) = {
      response.body shouldBe expectedBody
      this
    }

    def bodyIs(expectedBody: JsValue) = {
      (response.json match {
        case JsObject(fields) => response.json.as[JsObject] - "_links" - "id"
        case json             => json
      }) shouldEqual expectedBody
      this
    }

    def bodyIsLike(expectedBody: String) = {
      response.json match {
        case JsArray(_) =>
          assertEquals(expectedBody, new JSONArray(response.body), LENIENT)
        case _ =>
          assertEquals(expectedBody, new JSONObject(response.body), LENIENT)
      }
      this
    }

    def bodyHasLink(rel: String, href: String) = {
      getLinkFromBody(rel) shouldEqual Some(interpolated(href))
      this
    }

    def bodyHasPath[T](path: String, value: T)(
        implicit reads: Reads[T]): Assertions = {
      extractPathElement(path) shouldEqual Some(value)
      this
    }

    def bodyHasPath(path: String, valuePattern: Regex) = {
      extractPathElement[String](path) match {
        case Some(x) =>
          valuePattern findFirstIn x match {
            case Some(v) =>
            case None    => fail(s"$x did not match pattern")
          }
        case None => fail(s"No value found for $path")
      }
      this
    }

    def bodyDoesNotHavePath[T](path: String)(implicit reads: Reads[T]) = {
      extractPathElement[T](path) match {
        case Some(x) => fail(s"$x match found")
        case None    =>
      }
      this
    }

    private def extractPathElement[T](path: String)(
        implicit reads: Reads[T]): Option[T] = {
      val pathSeq =
        path.filter(!_.isWhitespace).split('\\').toSeq.filter(!_.isEmpty)

      def op(js: Option[JsValue], pathElement: String): Option[JsValue] = {
        val pattern = """(.*)\((\d+)\)""".r
        js match {
          case Some(v) =>
            pathElement match {
              case pattern(arrayName, index) =>
                js match {
                  case Some(v) =>
                    if (arrayName.isEmpty) Some(v(index.toInt))
                    else Some((v \ arrayName)(index.toInt))
                  case None => None
                }
              case _ => (v \ pathElement).toOption
            }
          case None => None
        }
      }

      pathSeq
        .foldLeft(Some(response.json): Option[JsValue])(op)
        .map(jsValue => jsValue.asOpt[T])
        .getOrElse(None)
    }

    private def getLinkFromBody(rel: String): Option[String] =
      if (response.body.isEmpty) None
      else
        (for {
          links <- (response.json \ "_links").toOption
          link <- (links \ rel).toOption
          href <- (link \ "href").toOption

        } yield href.asOpt[String]).getOrElse(None)

    def bodyHasLink(rel: String, hrefPattern: Regex) = {
      getLinkFromBody(rel) match {
        case Some(href) =>
          interpolated(hrefPattern).r findFirstIn href match {
            case Some(v) =>
            case None    => fail(s"$href did not match pattern")
          }
        case None => fail(s"No href found for $rel")
      }
      this
    }

    def bodyHasString(content: String) = {
      response.body.contains(content) shouldBe true
      this
    }

    def bodyDoesNotHaveString(content: String) = {
      response.body.contains(content) shouldBe false
      this
    }

    def statusIs(statusCode: Regex) = {
      withClue(
        s"expected $request to return $statusCode; but got ${response.body}\n") {
        response.status.toString should fullyMatch regex statusCode
      }
      this
    }

    def statusIs(statusCode: Int) = {
      withClue(
        s"expected $request to return $statusCode; but got ${response.body}\n") {
        response.status shouldBe statusCode
      }
      this
    }

    private def contentTypeIs(contentType: String) = {
      response.header("Content-Type") shouldEqual Some(contentType)
      this
    }

    def body(myQuery: JsValue => JsLookupResult) = {
      new BodyAssertions(myQuery(response.json).toOption, this)
    }

    def selectFields(myQuery: JsValue => Seq[JsValue]) = {
      new BodyListAssertions(myQuery(response.json), this)
    }

    class BodyAssertions(content: Option[JsValue], assertions: Assertions) {
      def is(value: String) = {
        content match {
          case Some(v) =>
            v.asOpt[String] match {
              case Some(actualValue) => actualValue shouldBe value
              case _                 => "" shouldBe value
            }
          case None => ()
        }
        assertions
      }

      def isAbsent() = {
        content shouldBe None
        assertions
      }

      def is(value: BigDecimal) = {
        content match {
          case Some(v) => v.as[BigDecimal] shouldBe value
          case None    => fail()
        }
        assertions
      }
    }

    class BodyListAssertions(content: Seq[JsValue], assertions: Assertions) {
      def isLength(n: Int) = {
        content.size shouldBe n
        this
      }

      def matches(matcher: Regex) = {
        content.map(_.as[String]).forall {
          case matcher(_*) => true
          case _           => false
        } shouldBe true

        assertions
      }

      def is(value: String*) = {
        content.map(con => con.as[String]) should contain theSameElementsAs value
        assertions
      }
    }

  }

  class HttpRequest(method: String,
                    path: String,
                    body: Option[JsValue],
                    hc: HeaderCarrier = HeaderCarrier())(
      implicit urlPathVariables: mutable.Map[String, String])
      extends UrlInterpolation {

    private val interpolatedPath: String = interpolated(path)
    assert(interpolatedPath.startsWith("/"),
           "please provide only a path starting with '/'")

    val url = s"http://localhost:$port$interpolatedPath"
    var addAcceptHeader = true

    def thenAssertThat(): Assertions = {
      implicit val carrier =
        if (addAcceptHeader)
          hc.withExtraHeaders("Accept" -> "application/vnd.hmrc.1.0+json")
        else hc

      withClue(s"Request $method $url") {
        method match {
          case "GET"    => new Assertions(s"GET@$url", Http.get(url))
          case "DELETE" => new Assertions(s"DELETE@$url", Http.delete(url))
          case "POST" =>
            body match {
              case Some(jsonBody) =>
                new Assertions(s"POST@$url", Http.postJson(url, jsonBody))
              case None => new Assertions(s"POST@$url", Http.postEmpty(url))
            }
          case "PUT" =>
            val jsonBody = body.getOrElse(
              throw new RuntimeException("Body for PUT must be provided"))
            new Assertions(s"PUT@$url", Http.putJson(url, jsonBody))
        }
      }
    }

    def withAcceptHeader(): HttpRequest = {
      addAcceptHeader = true
      this
    }

    def withoutAcceptHeader(): HttpRequest = {
      addAcceptHeader = false
      this
    }

    def withHeaders(header: String, value: String): HttpRequest = {
      new HttpRequest(method, path, body, hc.withExtraHeaders(header -> value))
    }
  }

  class HttpPostBodyWrapper(method: String, body: Option[JsValue])(
      implicit urlPathVariables: mutable.Map[String, String]) {
    def to(url: String) = new HttpRequest(method, url, body)
  }

  class HttpPutBodyWrapper(method: String, body: Option[JsValue])(
      implicit urlPathVariables: mutable.Map[String, String]) {
    def at(url: String) = new HttpRequest(method, url, body)
  }

  class HttpVerbs()(
      implicit urlPathVariables: mutable.Map[String, String] = mutable.Map()) {

    def post(body: JsValue) = {
      new HttpPostBodyWrapper("POST", Some(body))
    }

    def put(body: JsValue) = {
      new HttpPutBodyWrapper("PUT", Some(body))
    }

    def get(path: String) = {
      new HttpRequest("GET", path, None)
    }

    def delete(path: String) = {
      new HttpRequest("DELETE", path, None)
    }

    def post(path: String, body: Option[JsValue] = None) = {
      new HttpRequest("POST", path, body)
    }

    def put(path: String, body: Option[JsValue]) = {
      new HttpRequest("PUT", path, body)
    }

  }

  class Givens {

    implicit val urlPathVariables: mutable.Map[String, String] = mutable.Map()

    def when() = new HttpVerbs()

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

    def userIsPartiallyAuthorisedForTheResource: Givens = {

      // First call to auth to check if fully authorised should fail.
      stubFor(
        post(urlPathEqualTo(s"/auth/authorise"))
          .withRequestBody(containing("HMRC-MTD-IT"))
          .willReturn(
            aResponse()
              .withStatus(401)
              .withHeader("Content-Length", "0")
              .withHeader("WWW-Authenticate",
                          "MDTP detail=\"InsufficientEnrolments\"")))

      // Second call to auth to check affinity is equal to 'Agent' should succeed.
      stubFor(
        post(urlPathEqualTo(s"/auth/authorise"))
          .withRequestBody(containing("Agent"))
          .willReturn(aResponse()
            .withStatus(200)
            .withBody("""
            |{
            |  "internalId": "some-id",
            |  "affinityGroup": "Agent",
            |  "agentCode": "some-agent-code",
            |  "loginTimes": {
            |     "currentLogin": "2016-11-27T09:00:00.000Z",
            |     "previousLogin": "2016-11-01T12:00:00.000Z"
            |  },
            |  "authorisedEnrolments": [
            |   {
            |         "key":"HMRC-AS-AGENT",
            |         "identifiers":[
            |            {
            |               "key":"AgentReferenceNumber",
            |               "value":"1000051409"
            |            }
            |         ],
            |         "state":"Activated"
            |      }
            |  ]
            |}
          """.stripMargin)))

      // Third call to auth to check FOA subscription status should succeed.
      stubFor(
        post(urlPathEqualTo(s"/auth/authorise"))
          .withRequestBody(containing("HMRC-AS-AGENT"))
          .willReturn(aResponse()
            .withStatus(200)
            .withBody("""
            |{
            |  "internalId": "some-id",
            |  "affinityGroup": "Agent",
            |  "agentCode": "some-agent-code",
            |  "loginTimes": {
            |     "currentLogin": "2016-11-27T09:00:00.000Z",
            |     "previousLogin": "2016-11-01T12:00:00.000Z"
            |  },
            |  "authorisedEnrolments": [
            |   {
            |         "key":"HMRC-AS-AGENT",
            |         "identifiers":[
            |            {
            |               "key":"AgentReferenceNumber",
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

    def userIsNotPartiallyAuthorisedForTheResource: Givens = {
      // First call to auth to check if fully authorised should fail.
      stubFor(
        post(urlPathEqualTo(s"/auth/authorise"))
          .withRequestBody(containing("HMRC-MTD-IT"))
          .willReturn(
            aResponse()
              .withStatus(401)
              .withHeader("Content-Length", "0")
              .withHeader("WWW-Authenticate",
                          "MDTP detail=\"InsufficientEnrolments\"")))

      // Second call to auth to check affinity is equal to 'Agent' should succeed.
      stubFor(
        post(urlPathEqualTo(s"/auth/authorise"))
          .withRequestBody(containing("Agent"))
          .willReturn(aResponse()
            .withStatus(200)
            .withBody("""
            |{
            |  "internalId": "some-id",
            |  "affinityGroup": "Agent",
            |  "loginTimes": {
            |     "currentLogin": "2016-11-27T09:00:00.000Z",
            |     "previousLogin": "2016-11-01T12:00:00.000Z"
            |  }
            |}
          """.stripMargin)))

      // Third call to auth to check FOA subscription status should fail.
      stubFor(
        post(urlPathEqualTo(s"/auth/authorise"))
          .withRequestBody(containing("HMRC-AS-AGENT"))
          .willReturn(
            aResponse()
              .withStatus(401)
              .withHeader("Content-Length", "0")
              .withHeader("WWW-Authenticate",
                          "MDTP detail=\"InsufficientEnrolments\"")))

      this
    }

    def userIsPartiallyAuthorisedForTheResourceNoAgentCode: Givens = {

      // First call to auth to check if fully authorised should fail.
      stubFor(
        post(urlPathEqualTo(s"/auth/authorise"))
          .withRequestBody(containing("HMRC-MTD-IT"))
          .willReturn(
            aResponse()
              .withStatus(401)
              .withHeader("Content-Length", "0")
              .withHeader("WWW-Authenticate",
                          "MDTP detail=\"InsufficientEnrolments\"")))

      // Second call to auth to check affinity is equal to 'Agent' should succeed.
      stubFor(
        post(urlPathEqualTo(s"/auth/authorise"))
          .withRequestBody(containing("Agent"))
          .willReturn(aResponse()
            .withStatus(200)
            .withBody("""
            |{
            |  "internalId": "some-id",
            |  "affinityGroup": "Agent",
            |  "loginTimes": {
            |     "currentLogin": "2016-11-27T09:00:00.000Z",
            |     "previousLogin": "2016-11-01T12:00:00.000Z"
            |  },
            |  "authorisedEnrolments": [
            |   {
            |         "key":"HMRC-AS-AGENT",
            |         "identifiers":[
            |            {
            |               "key":"AgentReferenceNumber",
            |               "value":"1000051409"
            |            }
            |         ],
            |         "state":"Activated"
            |      }
            |  ]
            |}
          """.stripMargin)))

      // Third call to auth to check FOA subscription status should succeed.
      stubFor(
        post(urlPathEqualTo(s"/auth/authorise"))
          .withRequestBody(containing("HMRC-AS-AGENT"))
          .willReturn(aResponse()
            .withStatus(200)
            .withBody("""
            |{
            |  "internalId": "some-id",
            |  "affinityGroup": "Agent",
            |  "loginTimes": {
            |     "currentLogin": "2016-11-27T09:00:00.000Z",
            |     "previousLogin": "2016-11-01T12:00:00.000Z"
            |  },
            |  "authorisedEnrolments": [
            |   {
            |         "key":"HMRC-AS-AGENT",
            |         "identifiers":[
            |            {
            |               "key":"AgentReferenceNumber",
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

    def clientIsFullyAuthorisedForTheResource: Givens = {
      stubFor(
        post(urlPathEqualTo(s"/auth/authorise"))
          .willReturn(
            aResponse()
              .withStatus(200)
              .withBody("""
            |{
            |  "internalId": "some-id",
            |  "affinityGroup": "Individual",
            |  "loginTimes": {
            |     "currentLogin": "2016-11-27T09:00:00.000Z",
            |     "previousLogin": "2016-11-01T12:00:00.000Z"
            |  },
            |  "authorisedEnrolments": [
            |   {
            |         "key":"HMRC-AS-AGENT",
            |         "identifiers":[
            |            {
            |               "key":"AgentReferenceNumber",
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

    def agentIsFullyAuthorisedForTheResource: Givens = {
      stubFor(
        post(urlPathEqualTo(s"/auth/authorise"))
          .willReturn(
            aResponse()
              .withStatus(200)
              .withBody("""
            |{
            |  "internalId": "some-id",
            |  "affinityGroup": "Agent",
            |  "agentCode": "some-agent-code",
            |  "loginTimes": {
            |     "currentLogin": "2016-11-27T09:00:00.000Z",
            |     "previousLogin": "2016-11-01T12:00:00.000Z"
            |  },
            |  "authorisedEnrolments": [
            |   {
            |         "key":"HMRC-AS-AGENT",
            |         "identifiers":[
            |            {
            |               "key":"AgentReferenceNumber",
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

    def agentIsFullyAuthorisedForTheResourceNoAgentCode: Givens = {
      stubFor(
        post(urlPathEqualTo(s"/auth/authorise"))
          .willReturn(
            aResponse()
              .withStatus(200)
              .withBody("""
            |{
            |  "internalId": "some-id",
            |  "affinityGroup": "Agent",
            |  "loginTimes": {
            |     "currentLogin": "2016-11-27T09:00:00.000Z",
            |     "previousLogin": "2016-11-01T12:00:00.000Z"
            |  },
            |  "authorisedEnrolments": [
            |   {
            |         "key":"HMRC-AS-AGENT",
            |         "identifiers":[
            |            {
            |               "key":"AgentReferenceNumber",
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

    def userIsFullyAuthorisedForTheResource: Givens = {
      stubFor(
        post(urlPathEqualTo(s"/auth/authorise"))
          .willReturn(
            aResponse()
              .withStatus(200)
              .withBody("""
            |{
            |  "internalId": "some-id",
            |  "loginTimes": {
            |     "currentLogin": "2016-11-27T09:00:00.000Z",
            |     "previousLogin": "2016-11-01T12:00:00.000Z"
            |  }
            |}
          """.stripMargin)))

      this
    }

    class Des(givens: Givens) {
      def isATeapotFor(nino: Nino): Givens = {
        stubFor(
          any(urlMatching(s".*/(calculation-data|nino)/$nino.*"))
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
              .withBody(DesJsons.Errors.invalidVrn)))

        givens
      }

      object obligations {
        def obligationNotFoundFor(vrn: Vrn): Givens = {
          stubFor(get(urlMatching(s".*/vrn/$vrn.*"))
            .willReturn(
              aResponse()
                .withStatus(404)
                .withHeader("Content-Type", "application/json")
                .withBody(DesJsons.Errors.notFound)))

          givens
        }

        def returnObligationsFor(vrn: Vrn): Givens = {
          stubFor(any(urlMatching(s".*/vrn/$vrn.*"))
            .willReturn(
              aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withHeader("CorrelationId", "abc")
                .withBody(DesJsons.Obligations(vrn.toString()))))

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
                  .withBody("[]")
              )
          )

          givens
        }

        def expectVatReturnSearchFor(vrn: Vrn, periodKey: String): Givens = {
          stubFor(
            get(urlEqualTo(s"/enterprise/return/vat/$vrn?periodKey=$periodKey"))
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
                      "totalAcquisitionsExVAT": 100,
                      "receivedAt": "2017-12-18T16:49:20.678Z"
                    }""")
              )
          )

          givens
        }

        def expectInvalidVatReturnSearchFor(vrn: Vrn, periodKey: String): Givens = {
          stubFor(
            get(urlEqualTo(s"/enterprise/return/vat/$vrn?periodKey=$periodKey"))
              .willReturn(
                aResponse()
                  .withStatus(200)
                  .withBody("not-json")
              )
          )

          givens
        }
      }

    }

    def des() = new Des(this)

  }

  def given() = new Givens()

  def when() = new HttpVerbs()

}
