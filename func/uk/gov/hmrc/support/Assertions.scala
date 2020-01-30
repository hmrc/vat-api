package uk.gov.hmrc.support

import org.json.{JSONArray, JSONObject}
import org.scalatest.matchers.should.Matchers
import org.skyscreamer.jsonassert.JSONAssert.assertEquals
import org.skyscreamer.jsonassert.JSONCompareMode.LENIENT
import play.api.libs.json._
import uk.gov.hmrc.http.HttpResponse

import scala.collection.mutable
import scala.util.matching.Regex

class Assertions(request: String, response: HttpResponse) (
  implicit urlPathVariables: mutable.Map[String, String])
  extends UrlInterpolation with Matchers {

  //Headers
  def hasHeader(headerName: String): Assertions = {
    response.allHeaders(headerName) should not be 'empty
    this
  }
  def responseContainsHeader(name: String, pattern: Regex): Assertions = {
    response.header(name) match {
      case Some(h) => h should fullyMatch regex pattern
      case _ => fail(s"Header [$name] not found in the response headers")
    }
    this
  }
  
  def responseContainsHeader(name: String, value: String): Assertions = responseContainsHeader(name, value.r)

  //Status
  def isBadRequest(code: String): Assertions = {
    statusIs(400).contentTypeIsJson().bodyIsError(code)
    this
  }

  def bodyIsError(code: String): Assertions = body(_ \ "code").is(code)

  def statusIs(statusCode: Int): Assertions = {
    withClue(
      s"expected $request to return $statusCode; but got ${response.body}\n") {
      response.status shouldBe statusCode
    }
    this
  }

  //Content Type
  def contentTypeIsJson(): Assertions = contentTypeIs("application/json")
  private def contentTypeIs(contentType: String) = {
    response.header("Content-Type") shouldEqual Some(contentType)
    this
  }

  //Body matchers
  def bodyIsLike(expectedBody: String): Assertions = {
    response.json match {
      case JsArray(_) =>
        assertEquals(expectedBody, new JSONArray(response.body), LENIENT)
      case _ =>
        assertEquals(expectedBody, new JSONObject(response.body), LENIENT)
    }
    this
  }

  def bodyHasPath[T](path: String, value: T)(
    implicit reads: Reads[T]): Assertions = {
    extractPathElement(path) shouldEqual Some(value)
    this
  }

  //Body extractors
  def bodyHasLink(rel: String, hrefPattern: Regex): Assertions = {
    getLinkFromBody(rel) match {
      case Some(href) =>
        interpolated(hrefPattern).r findFirstIn href match {
          case Some(v) =>
          case None => fail(s"$href did not match pattern")
        }
      case None => fail(s"No href found for $rel")
    }
    this
  }

  //Helpers
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
                  else Some((v \ arrayName) (index.toInt))
                case None => None
              }
            case _ => (v \ pathElement).toOption
          }
        case None => None
      }
    }

    pathSeq
      .foldLeft(Some(response.json): Option[JsValue])(op)
      .flatMap(jsValue => jsValue.asOpt[T])
  }

  private def getLinkFromBody(rel: String): Option[String] =
    if (response.body.isEmpty) None
    else
      (for {
        links <- (response.json \ "_links").toOption
        link <- (links \ rel).toOption
        href <- (link \ "href").toOption

      } yield href.asOpt[String]).flatten


  private def body(myQuery: JsValue => JsLookupResult): BodyAssertions = {
    new BodyAssertions(myQuery(response.json).toOption, this)
  }

  class BodyAssertions(content: Option[JsValue], assertions: Assertions) {
    def is(value: String): Assertions = {
      content match {
        case Some(v) =>
          v.asOpt[String] match {
            case Some(actualValue) => actualValue shouldBe value
            case _ => "" shouldBe value
          }
        case None => ()
      }
      assertions
    }
  }
}
