/*
 * Copyright 2022 HM Revenue & Customs
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

package v1.services

import uk.gov.hmrc.http.HeaderCarrier
import utils.Logging
import v1.connectors.PenaltiesConnector
import v1.connectors.httpparsers.PenaltiesHttpParser.PenaltiesHttpResponse
import v1.models.request.penalties.PenaltiesRequest

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class PenaltiesService @Inject()(connector: PenaltiesConnector) extends Logging {

  def retrievePenalties(request: PenaltiesRequest)
                       (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[PenaltiesHttpResponse] = {
    connector.retrievePenalties(request)
  }
}
