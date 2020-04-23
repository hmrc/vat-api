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
import javax.inject.{Inject, Singleton}
import play.api.libs.ws.WSClient
import uk.gov.hmrc.http.HeaderCarrier
import v1.models.nrs.request.NrsSubmission
import v1.models.nrs.response.NrsResponse

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class NrsConnector @Inject()(val ws: WSClient,
                             val appConfig: AppConfig) extends BaseNrsConnector {

  def submitNrs(body: NrsSubmission)(
    implicit hc: HeaderCarrier,
    ec: ExecutionContext): Future[NrsOutcome[NrsResponse]] = {

    import v1.connectors.httpparsers.StandardNrsWsParser._

    nrsPost(
      uri = NrsUri[NrsResponse](s"submission"),
      body = body,
      defaultResult = Right(NrsResponse.empty)
    )
  }
}
