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

package v1.filters

import akka.stream.Materializer
import javax.inject.Inject
import play.api.mvc._

import scala.concurrent.{ExecutionContext, Future}

class SetXContentTypeOptionsFilter @Inject()(implicit val mat: Materializer, ec: ExecutionContext) extends Filter {

  import SetXContentTypeOptionsFilter._

  def apply(f: (RequestHeader) => Future[Result])(rh: RequestHeader): Future[Result] = {
    f(rh).map(_.withHeaders((xContentTypeOptionsHeader, "nosniff")))
  }

}

object SetXContentTypeOptionsFilter {
  val xContentTypeOptionsHeader = "X-Content-Type-Options"
}
