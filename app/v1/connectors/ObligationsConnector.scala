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
import play.api.http.Status
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient}
import utils.pagerDutyLogging.{Endpoint, PagerDutyLogging, PagerDutyLoggingEndpointName}
import v1.controllers.UserRequest
import v1.models.errors.DesErrorCode.NOT_FOUND_BPKEY
import v1.models.errors.{ConnectorError, DesErrorCode, DesErrors}
import v1.models.outcomes.ResponseWrapper
import v1.models.request.obligations.ObligationsRequest
import v1.models.response.obligations.ObligationsResponse

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Success

@Singleton
class ObligationsConnector @Inject()(val http: HttpClient, val appConfig: AppConfig) extends BaseDownstreamConnector { self =>

  def retrieveObligations(request: ObligationsRequest)(implicit hc: HeaderCarrier,
                                                       ec: ExecutionContext,
                                                       userRequest: UserRequest[_],
                                                       correlationId: String): Future[DesOutcome[ObligationsResponse]] = {

    import v1.connectors.httpparsers.StandardDesHttpParser._

    val vrn = request.vrn.vrn

    implicit val connectorError: ConnectorError =
      ConnectorError(vrn, hc.requestId.fold("") { requestId =>
        requestId.value
      })
    implicit val pagerDutyLoggingEndpointName: PagerDutyLoggingEndpointName.Value = Endpoint.RetrieveObligations.toLoggerMessage

    val queryParams: Seq[(String, String)] =
      Seq(
        "from"   -> request.from,
        "to"     -> request.to,
        "status" -> request.status
      ) collect {
        case (key, Some(value)) => key -> value
      }

    get(
      uri = DesUri[ObligationsResponse](s"enterprise/obligation-data/vrn/$vrn/VATC"),
      queryParams = queryParams
    ).andThen {
      case Success(Left(ResponseWrapper(_, DesErrors(errorCodes)))) if errorCodes.exists(_.code == NOT_FOUND_BPKEY) =>
        logger.warn(s"[ObligationsConnector] [retrieveObligations] - Backend returned $NOT_FOUND_BPKEY error")
    }.recover {
      case e =>

        val logDetails = s"request failed. ${e.getMessage}"

        logger.error(ConnectorError.log(
          logContext = "[ObligationsConnector][retrieveObligations]",
          vrn = vrn,
          details = logDetails,
        ))

        PagerDutyLogging.log(
          pagerDutyLoggingEndpointName = Endpoint.RetrieveObligations.requestFailedMessage,
          status = Status.INTERNAL_SERVER_ERROR,
          body = logDetails,
          f = logger.error(_),
          affinityGroup = userRequest.userDetails.userType
        )

        Left(ResponseWrapper(correlationId, DesErrors(List(DesErrorCode("DOWNSTREAM_ERROR"))))) }
  }
}
