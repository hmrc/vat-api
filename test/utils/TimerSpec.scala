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

import com.kenshoo.play.metrics.Metrics
import support.UnitSpec

import scala.concurrent.ExecutionContext.Implicits.global

class TimerSpec extends UnitSpec with Timer with Logging {
  val metrics: Metrics = new MockMetrics

  var timeMs: Long = _

  override def stopAndLog[A](name: String, timer: com.codahale.metrics.Timer.Context): Unit =
    timeMs = timer.stop() / 1000000

  "Timer" should {
    val sleepMs = 300

    "Time a future correctly" in {
        await(timeFuture("test timer", "test.sleep") {
        Thread.sleep(sleepMs)
      })
      val beWithinTolerance = be >= sleepMs.toLong and be <= (sleepMs + 100).toLong
      timeMs should beWithinTolerance
    }

    "Time a block correctly" in {
      await(time("test timer", "test.sleep") {
        Thread.sleep(sleepMs)
      })
      val beWithinTolerance = be >= sleepMs.toLong and be <= (sleepMs + 100).toLong
      timeMs should beWithinTolerance
    }
  }
}
