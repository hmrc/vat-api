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

package v1.connectors

import config.AppConfig
import play.api.Logger
import play.api.libs.json.{Json, Writes}
import play.api.libs.ws.WSClient
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.{ExecutionContext, Future}

trait BaseNrsConnector {
  val ws: WSClient
  val appConfig: AppConfig

  val logger = Logger(this.getClass)

  private[connectors] def nrsHeaderCarrier(implicit hc: HeaderCarrier): HeaderCarrier =
    hc.withExtraHeaders(
      "X-API-Key" -> appConfig.nrsApiKey,
      "User-Agent" -> appConfig.appName
    )

  def nrsPost[Body: Writes, Resp](body: Body, uri: String)(implicit ec: ExecutionContext,
                                                                                      hc: HeaderCarrier): Future[Unit] = {

    def doPost(implicit hc: HeaderCarrier): Future[Unit] = {

      ws.url(s"${appConfig.nrsBaseUrl}/${uri}")
        .withHttpHeaders(hc.headers: _*)
        .post(Json.toJson(body))
        .map(_ => ()).recover {
        case e: Exception =>
          logger.warn(s"[NrsConnector][nrsPost] - NRS Exception - $e")
          ()
      }
    }

    doPost(nrsHeaderCarrier(hc))
  }

}
