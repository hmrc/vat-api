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
import play.api.libs.json.Json
import uk.gov.hmrc.http.HeaderCarrier
import utils.{EndpointLogContext, Logging}
import v1.connectors.SubmitReturnConnector
import v1.models.errors._
import v1.models.request.submit.SubmitRequest
import v1.models.response.submit.SubmitResponse
import v1.support.DesResponseMappingSupport

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class SubmitReturnService @Inject()(connector: SubmitReturnConnector) extends DesResponseMappingSupport with Logging {

  def submitReturn(request: SubmitRequest)(
    implicit hc: HeaderCarrier,
    ec: ExecutionContext,
    logContext: EndpointLogContext): Future[ServiceOutcome[SubmitResponse]] = {

    logger.warn(s"NEW VAT: \n${Json.prettyPrint(Json.toJson(request.body))}")

    val result = for {
      desResponseWrapper <- EitherT(connector.submitReturn(request)).leftMap(mapDesErrors(desErrorMap))
    } yield desResponseWrapper

    result.value
  }

  private def desErrorMap: Map[String, MtdError] =
    Map(
      "INVALID_VRN" -> VrnFormatErrorDes,
      "INVALID_PERIODKEY" -> PeriodKeyFormatErrorDes,
      "INVALID_PAYLOAD" -> BadRequestError,
      "TAX_PERIOD_NOT_ENDED" -> TaxPeriodNotEnded,
      "DUPLICATE_SUBMISSION" -> DuplicateVatSubmission,
      "NOT_FOUND_VRN" -> DownstreamError,
      "INVALID_SUBMISSION" -> DownstreamError,
      "INVALID_ORIGINATOR_ID" -> DownstreamError,
      "SERVICE_ERROR" -> DownstreamError,
      "SERVICE_UNAVAILABLE" -> DownstreamError
    )
}
