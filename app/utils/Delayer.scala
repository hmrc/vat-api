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

package utils


import akka.actor.Scheduler

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future, Promise}

trait Delayer {

  implicit val scheduler: Scheduler
  implicit val ec: ExecutionContext

  def delay(delay: FiniteDuration): Future[Unit] = {
    val promise = Promise[Unit]

    scheduler.scheduleOnce(delay)(promise.success(()))

    promise.future
  }

}
