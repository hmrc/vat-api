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

import javax.inject.{Inject, Singleton}
import play.api.http.Status
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient}
import utils.Logging
import utils.pagerDutyLogging.{Endpoint, PagerDutyLogging}
import v1.controllers.UserRequest

import v1.connectors.httpparsers.CustomerInfoHttpParser._
import v1.models.errors.{ConnectorError, ErrorWrapper, MtdError}
import v1.models.request.information.CustomerInfoRequest
import v1.models.response.information.CustomerInfoResponse

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class CustomerInfoConnector @Inject()(val http: HttpClient,
                                  val appConfig: AppConfig) extends BaseDownstreamConnector with Logging{

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

  def retrieveCustomerInfo(request: CustomerInfoRequest)(implicit hc: HeaderCarrier,
                                                       ec: ExecutionContext,
                                                       userRequest: UserRequest[_],
                                                       correlationId: String): Future[Outcome[CustomerInfoResponse]] = {
    val vrn = request.vrn.vrn
    val url = appConfig.vatSubscriptionUrl + s"/vat-subscription/$vrn/full-information"
    logger.debug(s"[CustomerInfoConnector][retrieveCustomerInfo] url: $url")

    def doGet(implicit hc: HeaderCarrier): Future[Outcome[CustomerInfoResponse]] = {
      http.GET[Outcome[CustomerInfoResponse]](url)
    }

    doGet(headerCarrier()).recover {
      case e =>
        val logDetails = s"request failed. ${e.getMessage}"

        errorLog(ConnectorError.log(
          logContext = "[CustomerInfoConnector][retrieveCustomerInfo]",
          vrn = vrn,
          details = logDetails,
        ))

        PagerDutyLogging.log(
          pagerDutyLoggingEndpointName = Endpoint.RetrieveCustomerInfo.requestFailedMessage,
          status = Status.INTERNAL_SERVER_ERROR,
          body = logDetails,
          f = errorLog(_),
          affinityGroup = userRequest.userDetails.userType
        )

        Left(ErrorWrapper(correlationId, MtdError("DOWNSTREAM_ERROR", e.getMessage)))
    }
  }

}
