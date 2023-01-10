/*
 * Copyright 2023 HM Revenue & Customs
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
import javax.inject.{ Inject, Singleton }
import uk.gov.hmrc.http.HeaderCarrier
import utils.{ EndpointLogContext, Logging }
import v1.connectors.LiabilitiesConnector
import v1.controllers.UserRequest
import v1.models.errors._
import v1.models.request.liabilities.LiabilitiesRequest
import v1.models.response.liabilities.LiabilitiesResponse
import v1.support.DesResponseMappingSupport

import scala.concurrent.{ ExecutionContext, Future }

@Singleton
class LiabilitiesService @Inject()(connector: LiabilitiesConnector) extends DesResponseMappingSupport with Logging {

  def retrieveLiabilities(request: LiabilitiesRequest)(implicit hc: HeaderCarrier,
                                                       ec: ExecutionContext,
                                                       logContext: EndpointLogContext,
                                                       userRequest: UserRequest[_],
                                                       correlationId: String): Future[ServiceOutcome[LiabilitiesResponse]] = {

    val result = for {
      desResponseWrapper <- EitherT(connector.retrieveLiabilities(request)).leftMap(mapDesErrors(desErrorMap))
      mtdResponseWrapper <- EitherT.fromEither[Future](validateLiabilitiesSuccessResponse(desResponseWrapper))
    } yield mtdResponseWrapper

    result.value
  }

  private val desErrorMap: Map[String, MtdError] =
    Map(
      "INVALID_IDTYPE"                     -> DownstreamError,
      "INVALID_IDNUMBER"                   -> VrnFormatErrorDes,
      "INVALID_REGIMETYPE"                 -> DownstreamError,
      "INVALID_ONLYOPENITEMS"              -> DownstreamError,
      "INVALID_INCLUDELOCKS"               -> DownstreamError,
      "INVALID_CALCULATEACCRUEDINTEREST"   -> DownstreamError,
      "INVALID_CUSTOMERPAYMENTINFORMATION" -> DownstreamError,
      "INVALID_DATEFROM"                   -> InvalidDateFromErrorDes,
      "INVALID_DATETO"                     -> InvalidDateToErrorDes,
      "NOT_FOUND"                          -> LegacyNotFoundError,
      "INVALID_DATA"                       -> InvalidDataError,
      "INSOLVENT_TRADER"                   -> RuleInsolventTraderError,
      "SERVER_ERROR"                       -> DownstreamError,
      "SERVICE_UNAVAILABLE"                -> DownstreamError,
      "DOWNSTREAM_ERROR"                   -> DownstreamError
    )
}
