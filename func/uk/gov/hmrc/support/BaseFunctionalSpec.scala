package uk.gov.hmrc.support

import uk.gov.hmrc.domain.Vrn
import uk.gov.hmrc.vatapi.{TestApplication, VrnGenerator}

import scala.collection.mutable

trait BaseFunctionalSpec extends TestApplication {
  protected val vrn: Vrn = VrnGenerator().nextVrn()

  implicit val urlPathVariables: mutable.Map[String, String] = mutable.Map()

  def when() = new HttpVerbs()(urlPathVariables, timeout)
  def given() = new Givens(when())

}
