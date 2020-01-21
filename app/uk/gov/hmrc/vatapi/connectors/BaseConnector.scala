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

package uk.gov.hmrc.vatapi.connectors

import uk.gov.hmrc.http.logging.Authorization
import uk.gov.hmrc.http.{HeaderCarrier, HttpReads, HttpResponse}
import uk.gov.hmrc.play.bootstrap.http.DefaultHttpClient
import uk.gov.hmrc.vatapi.config.AppContext
import uk.gov.hmrc.vatapi.resources.wrappers.Response

import scala.concurrent.{ExecutionContext, Future}

trait BaseConnector {

  val http: DefaultHttpClient
  val appContext: AppContext

  def httpGet[R <: Response](url: String, toResponse: HttpResponse => R)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[R] =
    withAdditionalHeaders[R](url, withDesHeaders) {
      http.GET(url)(NoExceptionsReads, _, ec) map toResponse
    }

  private def withDesHeaders: HeaderCarrier => HeaderCarrier = { hc =>
    hc.copy(authorization = Some(Authorization(s"Bearer ${appContext.desToken}")))
      .withExtraHeaders("Environment" -> appContext.desEnv,
        "Accept" -> "application/json",
        "OriginatorID" -> "MDTP")
  }

  private def withAdditionalHeaders[R <: Response](url: String, header: HeaderCarrier => HeaderCarrier)(f: HeaderCarrier => Future[R])(
    implicit hc: HeaderCarrier): Future[R] = {
    val newHc = header(hc)
    f(newHc)
  }

  def httpDesPostString[R <: Response](url: String, elem: String, toResponse: HttpResponse => R)(
    implicit hc: HeaderCarrier, ec: ExecutionContext): Future[R] =
    withAdditionalHeaders[R](url, withContentTypeJsonHeader andThen withDesHeaders) {
      http.POSTString(url, elem)(NoExceptionsReads, _, ec) map toResponse
    }

  def withContentTypeJsonHeader: HeaderCarrier => HeaderCarrier = { hc =>
    hc.copy().withExtraHeaders(("Content-Type" -> "application/json"))
  }

  // http-verbs converts non-2xx statuses into exceptions. We don't want this, so here we define
  // our own reader that returns the raw response.
  private object NoExceptionsReads extends HttpReads[HttpResponse] {
    override def read(method: String, url: String, response: HttpResponse): HttpResponse = response
  }
}
