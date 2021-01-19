/*
 * Copyright 2021 HM Revenue & Customs
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

import cats.data.EitherT
import cats.implicits._
import javax.inject.{Inject, Singleton}
import play.api.Logger
import uk.gov.hmrc.http.HeaderCarrier
import utils.{EndpointLogContext, Logging}
import v1.connectors.ObligationsConnector
import v1.controllers.UserRequest
import v1.models.errors._
import v1.models.request.obligations.ObligationsRequest
import v1.models.response.obligations.ObligationsResponse
import v1.support.DesResponseMappingSupport

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ObligationsService @Inject()(connector: ObligationsConnector) extends DesResponseMappingSupport with Logging {

  def retrieveObligations(request: ObligationsRequest)(
    implicit hc: HeaderCarrier,
    ec: ExecutionContext,
    logContext: EndpointLogContext,
    userRequest: UserRequest[_],
    correlationId: String): Future[ServiceOutcome[ObligationsResponse]] = {

    val result = for {
      desResponseWrapper <- EitherT(connector.retrieveObligations(request)).leftMap(mapDesErrors(desErrorMap))
    } yield desResponseWrapper

    result.value
  }

  private def desErrorMap: Map[String, MtdError] =
    Map(
      "INVALID_IDTYPE" -> DownstreamError,
      "INVALID_IDNUMBER" -> VrnFormatErrorDes,
      "INVALID_STATUS" -> InvalidStatusErrorDes,
      "INVALID_REGIME" -> DownstreamError,
      "INVALID_DATE_FROM" -> InvalidDateFromErrorDes,
      "INVALID_DATE_TO" -> InvalidDateToErrorDes,
      "INVALID_DATE_RANGE" -> RuleOBLDateRangeTooLargeError,
      "INSOLVENT_TRADER" -> RuleInsolventTraderError,
      "NOT_FOUND_BP_KEY" -> {
        Logger.warn("[ObligationsService] [desErrorMap] - Backend returned NOT_FOUND_BP_KEY error")
        DownstreamError
      },
      "NOT_FOUND" -> LegacyNotFoundError,
      "SERVICE_ERROR" -> DownstreamError,
      "SERVICE_UNAVAILABLE" -> DownstreamError
    )
}
