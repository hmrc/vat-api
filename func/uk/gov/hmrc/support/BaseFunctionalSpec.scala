package uk.gov.hmrc.support

import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import uk.gov.hmrc.domain.Vrn
import uk.gov.hmrc.vatapi.{TestApplication, VrnGenerator}

import scala.collection.mutable

trait BaseFunctionalSpec extends TestApplication {
  protected val vrn: Vrn = VrnGenerator().nextVrn()

  implicit val urlPathVariables: mutable.Map[String, String] = mutable.Map()

  override lazy val app: Application = new GuiceApplicationBuilder().configure(Map(
    "auditing.consumer.baseUri.host" -> "localhost",
    "auditing.consumer.baseUri.port" -> 22222
  )).build()

  def when() = new HttpVerbs()(urlPathVariables, timeout)
  def given() = new Givens(when())

}
