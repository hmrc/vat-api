/*
 * Copyright 2017 HM Revenue & Customs
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

import org.joda.time.{DateTime, DateTimeZone}
import org.scalatest.{AsyncWordSpec, Matchers, OptionValues, WordSpec}

import scala.concurrent.duration._
import scala.concurrent.{Await, Future}

trait BaseUnitSpec extends Matchers with OptionValues with TestUtils {
  implicit val timeout: FiniteDuration = 5 seconds

  def await[T](f: Future[T])(implicit duration: FiniteDuration = timeout): T =
    Await.result(f, duration)
}

trait UnitSpec extends WordSpec with BaseUnitSpec

trait AsyncUnitSpec extends AsyncWordSpec with BaseUnitSpec

trait TestUtils {
  private val vrnGenerator = VrnGenerator()

  def now: DateTime = DateTime.now(DateTimeZone.UTC)
  def generateVrn = vrnGenerator.nextVrn()
}

object TestUtils extends TestUtils
