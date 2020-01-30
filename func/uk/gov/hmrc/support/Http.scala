package uk.gov.hmrc.support

import play.api.libs.json.{JsValue, Json, Writes}
import play.api.libs.ws.{EmptyBody, WSRequest, WSResponse}
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}
import uk.gov.hmrc.play.http.ws.WSHttpResponse

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.concurrent.{Await, Future}

object Http extends BaseFunctionalSpec {

  def get(url: String)(implicit hc: HeaderCarrier, timeout: FiniteDuration): HttpResponse = perform(url) { request =>
    request.get()
  }

  def post[A](url: String, body: A, headers: Seq[(String, String)] = Seq.empty)(
      implicit writes: Writes[A],
      hc: HeaderCarrier,
      timeout: FiniteDuration): HttpResponse = perform(url) { request =>
    request.post(Json.toJson(body))
  }

  def postString(url: String, body: String, headers: Seq[(String, String)] = Seq.empty)(
    implicit hc: HeaderCarrier,
    timeout: FiniteDuration): HttpResponse = perform(url) { request =>
    request.withHttpHeaders("Content-Type" -> "application/json").post[String](body)
  }

  def postJson(url: String, body: JsValue, headers: Seq[(String, String)] = Seq.empty)(
      implicit hc: HeaderCarrier,
      timeout: FiniteDuration): HttpResponse = perform(url) { request =>
    request.post(body)
  }

  def putJson(url: String, body: JsValue, headers: Seq[(String, String)] = Seq.empty)(
      implicit hc: HeaderCarrier,
      timeout: FiniteDuration): HttpResponse = perform(url) { request =>
    request.put(body)
  }

  def postEmpty(url: String)(implicit hc: HeaderCarrier, timeout: FiniteDuration): HttpResponse = perform(url) {
    request =>
      request.post(EmptyBody)
  }

  def delete(url: String)(implicit hc: HeaderCarrier, timeout: FiniteDuration): HttpResponse = perform(url) {
    request =>
      request.delete()
  }

  def perform(url: String)(fun: WSRequest => Future[WSResponse])(implicit hc: HeaderCarrier,
                                                                         timeout: FiniteDuration): WSHttpResponse =
    await(fun(client.url(url).addHttpHeaders(hc.headers: _*).withRequestTimeout(timeout)).map(new WSHttpResponse(_)))

  override def await[A](future: Future[A])(implicit timeout: FiniteDuration) = Await.result(future, timeout)

}
