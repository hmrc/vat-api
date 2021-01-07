/*
 * Copyright 2021 HM Revenue & Customs
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

package utils

import java.util.concurrent.TimeUnit

import akka.actor.{ActorSystem, Scheduler}
import com.google.common.base.Stopwatch
import org.scalatest.concurrent.ScalaFutures
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.Injecting
import play.api.{Application, Environment, Mode}
import support.UnitSpec

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future, TimeoutException}
import scala.util.control.NoStackTrace
import scala.util.{Failure, Success, Try}

class RetryingSpec extends UnitSpec with ScalaFutures with GuiceOneAppPerSuite with Injecting {

  override lazy val app: Application = new GuiceApplicationBuilder()
    .in(Environment.simple(mode = Mode.Dev))
    .configure("metrics.enabled" -> "false")
    .build()

  val actorSystem: ActorSystem = inject[ActorSystem]

  val retrying: Retrying = new Retrying with Delayer {
    override val ec: ExecutionContext = ExecutionContext.global
    override val scheduler: Scheduler = actorSystem.scheduler
  }

  def noDelayRetries(num: Int): List[FiniteDuration] = List.fill(num)(0.millis)

  val exception = new RuntimeException with NoStackTrace
  def succeedAfter(numFails: Int): Int => Future[String] = { i =>
    if (i < numFails) Future.failed(exception) else Future.successful(s"Attempt $i")
  }

  "Retrying" when {

    "initially successful" must {
      "return the result" in {
        retrying
          .retry[String](noDelayRetries(5), !_.isSuccess)(succeedAfter(0))
          .futureValue shouldBe "Attempt 0"
      }
    }

    "initially fails with failed future" must {
      val numRetries = 5
      "retry successfully" in {
        retrying
          .retry[String](noDelayRetries(numRetries), !_.isSuccess)(succeedAfter(numRetries))
          .futureValue shouldBe s"Attempt $numRetries"
      }

      "fail with last failed future when no more retries" in {
        retrying
          .retry[String](noDelayRetries(numRetries), !_.isSuccess)(succeedAfter(numRetries + 1))
          .failed
          .futureValue shouldBe exception
      }
    }

    "fails" must {
      "allow aborting without using remaining retries" in {

        val numRetries = 5

        val timeoutException = new TimeoutException with NoStackTrace

        val retryUnlessOk: Try[String] => Boolean = {
          case Success(_)                  => false
          case Failure(`timeoutException`) => false
          case Failure(_)                  => true
        }

        retrying
          .retry[String](noDelayRetries(numRetries), retryUnlessOk) { i =>
            if (i == 2) Future.failed(timeoutException) else Future.failed(exception)
          }
          .failed
          .futureValue shouldBe timeoutException
      }
    }

    "initially fails because bad value returned" must {
      val numRetries = 5

      val retryUnlessOk: Try[String] => Boolean = {
        case Success(s) => !(s startsWith "Ok")
        case Failure(e) => true
      }

      def okAfter(numFails: Int): Int => Future[String] = { i =>
        if (i < numFails) Future.successful(s"Attempt $i") else Future.successful(s"Ok $i")
      }

      "retry successfully" in {
        retrying
          .retry[String](noDelayRetries(numRetries), retryUnlessOk)(okAfter(numRetries))
          .futureValue shouldBe s"Ok $numRetries"
      }

      "fail with last future when no more retries" in {
        retrying
          .retry[String](noDelayRetries(numRetries), retryUnlessOk)(okAfter(numRetries + 1))
          .futureValue shouldBe s"Attempt $numRetries"
      }
    }

    "successive attempts are delayed" must {
      "use these delays between attempts" in {
        val delay     = 500.millis
        val stopwatch = Stopwatch.createStarted()

        await(
          retrying
            .retry[String](List(delay), !_.isSuccess)(succeedAfter(1)))(delay * 2) shouldBe s"Attempt 1"

        stopwatch.stop()
        stopwatch.elapsed(TimeUnit.MILLISECONDS) shouldBe >(delay.toMillis)
      }

      "not delay when no retry is necessary" in {
        val delay     = 10.seconds
        val stopwatch = Stopwatch.createStarted()

        await(
          retrying
            .retry[String](List(delay), !_.isSuccess)(succeedAfter(0)))(delay * 2) shouldBe s"Attempt 0"

        stopwatch.stop()
        stopwatch.elapsed(TimeUnit.MILLISECONDS) shouldBe <(delay.toMillis)
      }
    }

    "getting fibonacci delays" must {
      "return fibonacci values" in {
        Retrying
          .fibonacciDelays(1.second, 7) shouldBe Seq(
          1.second,
          1.second,
          2.seconds,
          3.seconds,
          5.seconds,
          8.seconds,
          13.seconds)
      }

      "must allow to be scaled by a factor" in {
        Retrying.fibonacciDelays(500.milliseconds, 5) shouldBe Seq(
          500.milliseconds,
          500.milliseconds,
          1000.milliseconds,
          1500.milliseconds,
          2500.milliseconds)
      }
    }
  }
}
