package uk.gov.hmrc.support

import org.scalatest.Assertions._
import play.api.libs.json.JsValue
import play.api.test.Helpers
import uk.gov.hmrc.http.HeaderCarrier

import scala.collection.mutable
import scala.concurrent.duration.FiniteDuration

class HttpVerbs() (
    implicit urlPathVariables: mutable.Map[String, String] = mutable.Map(), timeout: FiniteDuration) {

    def post(body: JsValue): HttpPostBodyWrapper = {
      new HttpPostBodyWrapper("POST", Some(body))
    }

    def put(body: JsValue): HttpPutBodyWrapper = {
      new HttpPutBodyWrapper("PUT", Some(body))
    }

    def get(path: String): HttpRequest = {
      new HttpRequest("GET", path, None)
    }

    def delete(path: String): HttpRequest = {
      new HttpRequest("DELETE", path, None)
    }

    def post(path: String, body: Option[JsValue] = None): HttpRequest = {
      new HttpRequest("POST", path, body)
    }

    def put(path: String, body: Option[JsValue]): HttpRequest = {
      new HttpRequest("PUT", path, body)
    }

    class HttpRequest(method: String,
                      path: String,
                      body: Option[JsValue],
                      hc: HeaderCarrier = HeaderCarrier())(
                       implicit urlPathVariables: mutable.Map[String, String], timeout: FiniteDuration)
      extends UrlInterpolation {

      private val interpolatedPath: String = interpolated(path)
      assert(interpolatedPath.startsWith("/"),
        "please provide only a path starting with '/'")


      private val port: Int = Helpers.testServerPort
      private val url = s"http://localhost:$port$interpolatedPath"

      var addAcceptHeader: Boolean = true

      def thenAssertThat(): Assertions = {
        implicit val carrier: HeaderCarrier =
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
  }