/*
 * Copyright 2019 HM Revenue & Customs
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

package uk.gov.hmrc.vatapi.filters

import akka.stream.Materializer
import javax.inject.Inject
import play.api.http.HttpEntity
import play.api.mvc._

import scala.concurrent.{ExecutionContext, Future}

class EmptyResponseFilter @Inject()(implicit val mat: Materializer, ec: ExecutionContext) extends Filter {

  val emptyHeader = "Gov-Empty-Response"

  def apply(f: (RequestHeader) => Future[Result])(rh: RequestHeader): Future[Result] = {
    f(rh) map { res =>
      if ((res.header.status == 201 || res.header.status == 409) && res.body.isKnownEmpty) {
        val headers = res.header.headers
          .updated("Content-Type", "application/json")
          .updated(emptyHeader, "true")
        res.copy(res.header.copy(headers = headers), HttpEntity.NoEntity)
      } else res
    }
  }

}
