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

package uk.gov.hmrc.vatapi

import play.api.Logger
import play.api.libs.json.Writes
import uk.gov.hmrc.http.logging.Authorization
import uk.gov.hmrc.http.{HeaderCarrier, HttpReads, HttpResponse}
import uk.gov.hmrc.play.bootstrap.http.DefaultHttpClient
import uk.gov.hmrc.vatapi.config.AppContext
import uk.gov.hmrc.vatapi.resources.GovTestScenarioHeader
import uk.gov.hmrc.vatapi.resources.wrappers.Response

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionContext, Future}

trait BaseConnector {

  val http: DefaultHttpClient
  val appContext: AppContext

  private val logger = Logger("connectors")

  def httpGet[R <: Response](url: String, toResponse: HttpResponse => R)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[R] =
    withAdditionalHeaders[R](url, withDesHeaders()) {
      http.GET(url)(NoExceptionsReads, _, ec) map toResponse
    }

  private def withDesHeaders(orgFlag: Boolean = false): HeaderCarrier => HeaderCarrier = { hc =>

    val newHc: HeaderCarrier = if(orgFlag) {
      hc.copy(authorization = Some(Authorization(s"Bearer ${appContext.desToken}")))
        .withExtraHeaders("Environment" -> appContext.desEnv,
          "Accept" -> "application/json",
          "OriginatorID" -> "MDTP")
    }
    else{
      hc.copy(authorization = Some(Authorization(s"Bearer ${appContext.desToken}")))
        .withExtraHeaders("Environment" -> appContext.desEnv,
      "Accept" -> "application/json")
    }

    // HACK: http-verbs removes all "otherHeaders" from HeaderCarrier on outgoing requests.
    //       We want to preserve the Gov-Test-Scenario header, so we copy it into "extraHeaders".
    //       and remove it from "otherHeaders" to prevent it from being removed again.
    val lastHc = hc.otherHeaders
      .find { case (name, _) => name == GovTestScenarioHeader }
      .map(newHc.withExtraHeaders(_))
      .map(headers => headers.copy(otherHeaders = headers.otherHeaders.filterNot(_._1 == GovTestScenarioHeader)))
      .getOrElse(newHc)
    lastHc
  }

  private def desHeadersWithOriginatorId: HeaderCarrier => HeaderCarrier = withDesHeaders(true)

  private def withAdditionalHeaders[R <: Response](url: String, header: HeaderCarrier => HeaderCarrier)(f: HeaderCarrier => Future[R])(
    implicit hc: HeaderCarrier): Future[R] = {
    val newHc = header(hc)
    f(newHc)
  }

  def httpDesPostString[R <: Response](url: String, elem: String, toResponse: HttpResponse => R)(
    implicit hc: HeaderCarrier, ec: ExecutionContext): Future[R] =
    withAdditionalHeaders[R](url, withContentTypeJsonHeader andThen desHeadersWithOriginatorId) {
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
