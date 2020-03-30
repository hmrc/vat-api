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

package v1.services

import cats.data.EitherT
import cats.implicits._
import javax.inject.{Inject, Singleton}
import uk.gov.hmrc.http.HeaderCarrier
import utils.{EndpointLogContext, Logging}
import v1.connectors.RetrieveLiabilitiesConnector
import v1.models.errors._
import v1.models.request.viewReturn.ViewRequest
import v1.models.response.liability.LiabilityResponse
import v1.support.DesResponseMappingSupport

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class RetrieveLiabilitiesService @Inject()(connector: RetrieveLiabilitiesConnector) extends DesResponseMappingSupport with Logging {

  def retrieveLiabilities(request: ViewRequest)(
    implicit hc: HeaderCarrier,
    ec: ExecutionContext,
    logContext: EndpointLogContext): Future[ServiceOutcome[LiabilityResponse]] = {

    val result = for {
      desResponseWrapper <- EitherT(connector.retrieveLiabilities(request)).leftMap(mapDesErrors(desErrorMap))
    } yield desResponseWrapper

    result.value
  }

  private def desErrorMap: Map[String, MtdError] =
    Map(
      "INVALID_VRN" -> FormatVrnError,
      "INVALID_PERIODKEY" -> FormatPeriodKeyError,
      "INVALID_IDENTIFIER" -> DownstreamError,
      "NOT_FOUND_VRN" -> DownstreamError,
      "INVALID_INPUTDATA" -> RuleDateRangeTooLargeError,
      "NOT_FOUND" -> EmptyNotFoundError,
      "SERVICE_ERROR" -> DownstreamError,
      "SERVICE_UNAVAILABLE" -> DownstreamError
    )
}
