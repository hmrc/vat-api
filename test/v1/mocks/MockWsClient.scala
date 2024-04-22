/*
 * Copyright 2023 HM Revenue & Customs
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

import play.api.libs.ws.{BodyWritable, WSClient, WSRequest, WSResponse}

import scala.concurrent.Future
import scala.concurrent.duration.Duration

trait MockWsClient extends MockFactory {

  val mockWsClient: WSClient = mock[WSClient]
  val mockWsRequest: WSRequest = mock[WSRequest]

  object MockWsClient {

    def url(url: String): CallHandler[WSRequest] = {
      (mockWsClient.url(_: String))
        .expects(url)
    }
  }

  object MockWsRequest {

    def withHttpHeaders(headers: Seq[(String, String)]): CallHandler[WSRequest] = {
      (mockWsRequest.withHttpHeaders _ ).expects(*)
    }

    def withRequestTimeout(timeout: Duration): CallHandler[WSRequest] = {
      (mockWsRequest.withRequestTimeout(_: Duration))
        .expects(timeout)
    }

    def post[I: BodyWritable](body: I): CallHandler[Future[WSResponse]] = {
      (mockWsRequest.post(_: I)(_: BodyWritable[I]))
        .expects(body, *)
    }
  }

}
