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

import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.duration.FiniteDuration
import scala.util.{Failure, Success, Try}
import scala.util.control.NonFatal

object Retrying {
  private val fibonacci: Stream[Int] = 1 #:: fibonacci.scanLeft(1)(_ + _)

  def fibonacciDelays(initialDelay: FiniteDuration, numRetries: Int): List[FiniteDuration] =
    fibonacci.take(numRetries).map(i => i * initialDelay).toList
}

trait Retrying {
  delayer: Delayer =>

  implicit val ec: ExecutionContext

  /**
    * Retries an operation returning a future
    * @param delays delays between retries
    * @param retryCondition whether to retry based on a result or otherwise return that result (which may be a failed future)
    * @param task the task returning a future (a function that accepts the attempt number)
    * @return the result of the last attempt
    */
  def retry[A](delays: List[FiniteDuration], retryCondition: Try[A] => Boolean)(task: Int => Future[A]): Future[A] = {

    def loop(attemptNumber: Int, delays: List[FiniteDuration]): Future[A] = {
      def retryIfPossible(result: Try[A]): Future[A] =
        delays match {
          case Nil => Future.fromTry(result)
          case delay :: tail =>
            if (retryCondition(result)) {
              for {
                _      <- delayer.delay(delay)
                result <- loop(attemptNumber + 1, tail)
              } yield result
            } else {
              Future.fromTry(result)
            }
        }

      task(attemptNumber)
        .transformWith {
          case e: Success[A]            => retryIfPossible(e)
          case e @ Failure(NonFatal(_)) => retryIfPossible(e)
          case Failure(e)               => Future.failed(e)
        }
    }

    loop(0, delays)
  }
}

