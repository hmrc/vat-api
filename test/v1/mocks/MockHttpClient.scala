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

package v1.mocks

import org.scalamock.handlers.CallHandler
import org.scalamock.scalatest.MockFactory
import play.api.libs.json.Writes
import uk.gov.hmrc.http.{HeaderCarrier, HttpReads}
import uk.gov.hmrc.play.bootstrap.http.HttpClient

import scala.concurrent.{ExecutionContext, Future}

trait MockHttpClient extends MockFactory {

  val mockHttpClient: HttpClient = mock[HttpClient]

  object MockedHttpClient {

    def get[T](url: String, requiredHeaders: (String, String)*): CallHandler[Future[T]] = {
      (mockHttpClient
        .GET(_: String)(_: HttpReads[T], _: HeaderCarrier, _: ExecutionContext))
        .expects(where { (actualUrl, _, hc, _) =>
          url == actualUrl && requiredHeaders.forall(h => hc.headers.contains(h))
        })
    }

    def get[T](url: String, queryParams: Seq[(String, String)], requiredHeaders: (String, String)*): CallHandler[Future[T]] = {
      (mockHttpClient
        .GET(_: String, _: Seq[(String, String)])(_: HttpReads[T], _: HeaderCarrier, _: ExecutionContext))
        .expects(where { (actualUrl, params,  _, hc, _) =>
          url == actualUrl && requiredHeaders.forall(h => hc.headers.contains(h)) && params == queryParams
        })
    }

    def post[I, T](url: String, body: I, requiredHeaders: (String, String)*): CallHandler[Future[T]] = {
      (mockHttpClient
        .POST[I, T](_: String, _: I, _: Seq[(String, String)])(_: Writes[I], _: HttpReads[T], _: HeaderCarrier, _: ExecutionContext))
        .expects(where { (actualUrl, actualBody, _, _, _, hc, _) =>
          url == actualUrl && body == actualBody && requiredHeaders.forall(h => hc.headers.contains(h))
        })
    }
  }

}
