/*
 * Copyright 2024 HM Revenue & Customs
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
import play.api.http.Status
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient}
import utils.Logging
import utils.pagerDutyLogging.{Endpoint, PagerDutyLogging}
import v1.connectors.httpparsers.FinancialDataHttpParser._
import v1.connectors.httpparsers.PenaltiesHttpParser._
import v1.controllers.UserRequest
import v1.models.errors.{ConnectorError, ErrorWrapper, MtdError}
import v1.models.request.penalties.{FinancialRequest, PenaltiesRequest}
import v1.models.response.financialData.FinancialDataResponse
import v1.models.response.penalties.PenaltiesResponse

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class PenaltiesConnector @Inject() (val http: HttpClient, val appConfig: AppConfig) extends BaseDownstreamConnector with Logging {

  private def buildHeaderCarrier(hc: HeaderCarrier, userRequest: UserRequest[_], correlationId: String): HeaderCarrier = {

    val maybeAuthHeader: String = userRequest.request.headers
      .get("Authorization")
      .getOrElse(s"Bearer ${appConfig.desToken}") // fallback if not present

    val contractHeaders: Seq[(String, String)] = Seq(
      "Authorization" -> maybeAuthHeader,
      "Environment"   -> appConfig.desEnv,
      "CorrelationId" -> correlationId
    )

    val otherHeaders: Seq[(String, String)] = hc.headers(appConfig.desEnvironmentHeaders.getOrElse(Seq.empty))

    HeaderCarrier(extraHeaders = hc.extraHeaders ++ contractHeaders ++ otherHeaders)
  }

  def retrievePenalties(request: PenaltiesRequest)(implicit
      hc: HeaderCarrier,
      ec: ExecutionContext,
      userRequest: UserRequest[_],
      correlationId: String): Future[Outcome[PenaltiesResponse]] = {
    val vrn = request.vrn.vrn
    val url = appConfig.penaltiesBaseUrl + s"/penalties/VATC/penalty-details/VRN/$vrn"
    logger.debug(s"[PenaltiesConnector][retrievePenalties] url: $url")

    def doGet(implicit hc: HeaderCarrier): Future[Outcome[PenaltiesResponse]] =
      http.GET[Outcome[PenaltiesResponse]](url)

    doGet(buildHeaderCarrier(hc, userRequest, correlationId)).recover { case e =>
      val logDetails = s"request failed. ${e.getMessage}"

      errorLog(
        ConnectorError.log(
          logContext = "[PenaltiesConnector][retrievePenaltiesData]",
          vrn = vrn,
          details = logDetails
        ))

      PagerDutyLogging.log(
        pagerDutyLoggingEndpointName = Endpoint.RetrievePenalties.requestFailedMessage,
        status = Status.INTERNAL_SERVER_ERROR,
        body = logDetails,
        f = errorLog(_),
        affinityGroup = userRequest.userDetails.userType
      )
      Left(ErrorWrapper(correlationId, MtdError("DOWNSTREAM_ERROR", e.getMessage)))
    }
  }

  def retrieveFinancialData(request: FinancialRequest)(implicit
      hc: HeaderCarrier,
      ec: ExecutionContext,
      userRequest: UserRequest[_],
      correlationId: String): Future[Outcome[FinancialDataResponse]] = {

    val vrn                                = request.vrn.vrn
    val url                                = appConfig.penaltiesBaseUrl + s"/penalties/VATC/penalty/financial-data/VRN/$vrn"
    val queryParams: Seq[(String, String)] = Seq("searchType" -> "CHGREF", "searchItem" -> request.searchItem)
    val newHC: HeaderCarrier               = buildHeaderCarrier(hc, userRequest, correlationId)

    http.GET[Outcome[FinancialDataResponse]](url, queryParams)(implicitly, newHC, implicitly).recover { case e =>
      val logDetails = s"request failed: ${e.getMessage}"

      errorLog(
        ConnectorError.log(
          logContext = "[PenaltiesConnector][retrieveFinancialData]",
          vrn = vrn,
          details = logDetails
        ))

      PagerDutyLogging.log(
        pagerDutyLoggingEndpointName = Endpoint.RetrieveFinancialData.requestFailedMessage,
        status = Status.INTERNAL_SERVER_ERROR,
        body = logDetails,
        f = errorLog(_),
        affinityGroup = userRequest.userDetails.userType
      )

      Left(ErrorWrapper(correlationId, MtdError("DOWNSTREAM_ERROR", e.getMessage)))
    }
  }

}
