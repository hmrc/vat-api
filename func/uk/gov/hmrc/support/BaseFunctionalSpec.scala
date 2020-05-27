package uk.gov.hmrc.support

import org.scalatestplus.play.guice.GuiceOneServerPerSuite
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.ws.{WSClient, WSRequest}
import uk.gov.hmrc.domain.Vrn
import uk.gov.hmrc.vatapi.{TestApplication, VrnGenerator}

import scala.collection.mutable

trait BaseFunctionalSpec extends TestApplication with WireMockHelper with GuiceOneServerPerSuite{
  protected val vrn: Vrn = VrnGenerator().nextVrn()

  implicit val urlPathVariables: mutable.Map[String, String] = mutable.Map()


  lazy val client: WSClient = app.injector.instanceOf[WSClient]

  override lazy val app: Application = new GuiceApplicationBuilder().configure(Map(
    "auditing.consumer.baseUri.host" -> "localhost",
    "auditing.consumer.baseUri.port" -> 22222,
    "microservice.services.des.host" -> mockHost,
    "microservice.services.des.port" -> mockPort,
    "microservice.services.auth.host" -> mockHost,
    "microservice.services.auth.port" -> mockPort,
    "auditing.consumer.baseUri.port" -> mockPort,
    "access-keys.xApiKey" -> "dummy-api-key",
    "microservice.services.non-repudiation.host" -> mockHost,
    "microservice.services.non-repudiation.port" -> mockPort,
    "feature-switch.refactor.enabled" -> false,
    "feature-switch.refactor.prod.enabled" -> false
  )).build()

  def when() = new HttpVerbs()(urlPathVariables, timeout)
  def given() = new Givens(when())

  def buildRequest(path: String): WSRequest = client.url(s"http://localhost:$port$path").withFollowRedirects(false)
}

