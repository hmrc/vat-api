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

package v1.connectors

import config.AppConfig
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient}
import utils.Logging
import v1.connectors.httpparsers.PenaltiesHttpParser._
import v1.connectors.httpparsers.FinancialDataHttpParser._
import v1.controllers.UserRequest
import v1.models.errors.{DesErrorCode, DesErrors, ErrorWrapper, MtdError}
import v1.models.outcomes.ResponseWrapper
import v1.models.request.penalties.{FinancialRequest, PenaltiesRequest}
import v1.models.response.penalties.PenaltiesResponse
import v1.models.response.financialData.FinancialDataResponse
import v1.models.response.penalties.PenaltiesResponse

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class PenaltiesConnector @Inject()(val http: HttpClient,
                                   val appConfig: AppConfig) extends BaseDownstreamConnector with Logging {

  private def headerCarrier(additionalHeaders: Seq[String] = Seq.empty)(implicit hc: HeaderCarrier,
                                                                           correlationId: String): HeaderCarrier =
    HeaderCarrier(
      extraHeaders = hc.extraHeaders ++
        // Contract headers
        Seq(
          "Authorization" -> s"Bearer ${appConfig.desToken}",
          "Environment" -> appConfig.desEnv,
          "CorrelationId" -> correlationId
        ) ++
        // Other headers (i.e Gov-Test-Scenario, Content-Type)
        hc.headers(additionalHeaders ++ appConfig.desEnvironmentHeaders.getOrElse(Seq.empty))
    )

  def retrievePenalties(request: PenaltiesRequest)(implicit hc: HeaderCarrier,
                                                   ec: ExecutionContext,
                                                   userRequest: UserRequest[_],
                                                   correlationId: String): Future[Outcome[PenaltiesResponse]] = {
    val vrn = request.vrn.vrn
    val url = appConfig.penaltiesBaseUrl + s"/penalties/penalty-details/VAT/VRN/$vrn"
    logger.debug(s"[PenaltiesConnector][retrievePenalties] url: $url")

    def doGet(implicit hc: HeaderCarrier): Future[Outcome[PenaltiesResponse]] = {
      http.GET[Outcome[PenaltiesResponse]](url)
    }
    doGet(headerCarrier()).recover {
      case e => Left(ErrorWrapper(correlationId, MtdError("DOWNSTREAM_ERROR", e.getMessage))) }
  }


  def retrieveFinancialData(request: FinancialRequest)(implicit hc: HeaderCarrier,
                                                   ec: ExecutionContext,
                                                   userRequest: UserRequest[_],
                                                   correlationId: String): Future[Outcome[FinancialDataResponse]] = {
    val vrn = request.vrn.vrn
    val url = appConfig.penaltiesBaseUrl + s"/penalties/penalty/financial-data/VRN/$vrn/VATC"
    logger.debug(s"[PenaltiesConnector][retrieveFinancialData] url: $url")

    def doGet(implicit hc: HeaderCarrier): Future[Outcome[FinancialDataResponse]] = {
      http.GET[Outcome[FinancialDataResponse]](url)
    }
    doGet(headerCarrier()).recover {
      case e => Left(ErrorWrapper(correlationId, MtdError("DOWNSTREAM_ERROR", e.getMessage))) }
  }
}
