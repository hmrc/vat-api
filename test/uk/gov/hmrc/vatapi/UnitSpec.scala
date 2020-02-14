/*
 * Copyright 2020 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.gov.hmrc.vatapi

import org.joda.time.{DateTime, DateTimeZone, LocalDate}
import org.scalamock.scalatest.MockFactory
import org.scalatest.OptionValues
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.{AnyWordSpec, AsyncWordSpec}
import play.api.test.{DefaultAwaitTimeout, FutureAwaits}

import scala.concurrent.duration._
import scala.concurrent.{Await, Future}
import scala.language.postfixOps

trait BaseUnitSpec extends Matchers with OptionValues with TestUtils with FutureAwaits
  with DefaultAwaitTimeout {
  implicit val timeout: FiniteDuration = 5 seconds

  def await[T](f: Future[T])(implicit duration: FiniteDuration = timeout): T =
    Await.result(f, duration)
}

trait UnitSpec extends AnyWordSpec with BaseUnitSpec{
  implicit def extractAwait[A](future: Future[A]): A = await[A](future)

  def await[A](future: Future[A])(implicit timeout: Duration): A = Await.result(future, timeout)
}

trait AsyncUnitSpec extends AsyncWordSpec with BaseUnitSpec

trait TestUtils {
  private val vrnGenerator = VrnGenerator()

  def now: DateTime = DateTime.now(DateTimeZone.UTC)
  def generateVrn = vrnGenerator.nextVrn()

  implicit def toLocalDate(d: DateTime): LocalDate = d.toLocalDate
}

object TestUtils extends TestUtils
