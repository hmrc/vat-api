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
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.http.HttpClient
import utils.pagerDutyLogging.{Endpoint, LoggerMessages}
import v1.controllers.UserRequest
import v1.models.errors.ConnectorError
import v1.models.request.liabilities.LiabilitiesRequest
import v1.models.response.liabilities.LiabilitiesResponse

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class LiabilitiesConnector @Inject()(val http: HttpClient,
                                     val appConfig: AppConfig) extends BaseDesConnector {

  def retrieveLiabilities(request: LiabilitiesRequest)(implicit hc: HeaderCarrier,
                                                       ec: ExecutionContext,
                                                       userRequest: UserRequest[_]): Future[DesOutcome[LiabilitiesResponse]] = {

    import v1.connectors.httpparsers.StandardDesHttpParser._
    implicit val requestToDate: String = request.to
    implicit val connectorError: ConnectorError =
      ConnectorError(request.vrn.vrn, hc.requestId.fold(""){ requestId => requestId.value})
    implicit val logMessage: LoggerMessages.Value = Endpoint.RetrieveLiabilities.toLoggerMessage
    val queryParams: Seq[(String, String)] = Seq(
      ("dateFrom", request.from),
      ("dateTo", request.to),
      ("onlyOpenItems", "false"),
      ("includeLocks", "false"),
      ("calculateAccruedInterest", "true"),
      ("customerPaymentInformation", "true")
    )

    get(
      uri = DesUri[LiabilitiesResponse](s"enterprise/financial-data/VRN/${request.vrn.vrn}/VATC"),
      queryParams = queryParams
    )
  }
}
