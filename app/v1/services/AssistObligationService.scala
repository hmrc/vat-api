/*
 * Copyright 2026 HM Revenue & Customs
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
import utils.{ EndpointLogContext, Logging }
import v1.connectors.ObligationsConnector
import v1.controllers.UserRequest
import v1.models.errors.OpenObligationMatchError.{ NoMatchingObligation, PeriodNotEnded, UnreadableEndDate }
import v1.models.errors._
import v1.models.outcomes.ResponseWrapper
import v1.models.request.obligations.ObligationsRequest
import v1.models.request.submit.SubmitRequest
import v1.models.response.obligations.{ Obligation, ObligationsResponse }

import java.time.LocalDate
import javax.inject.{ Inject, Singleton }
import scala.concurrent.{ ExecutionContext, Future }

/** Retrieves the open VAT obligation matching a submitted return's period key, for the internal
  * TxR assist flow. Separate from [[ObligationsService]] (which serves the public obligations endpoint)
  */
@Singleton
class AssistObligationService @Inject()(connector: ObligationsConnector) extends Logging {

  def retrieveOpenObligation(request: SubmitRequest, today: LocalDate = LocalDate.now())(
      implicit hc: HeaderCarrier,
      ec: ExecutionContext,
      logContext: EndpointLogContext,
      userRequest: UserRequest[_],
      correlationId: String): Future[ServiceOutcome[Obligation]] = {

    val vrn                = request.vrn.vrn
    val periodKey          = request.body.periodKey.getOrElse("no-period-key-found")
    val obligationsRequest = ObligationsRequest(vrn = request.vrn, from = None, to = None, status = Some("O"))

    infoLog(s"$logContext retrieving open obligations for VRN : $vrn, periodKey : $periodKey, correlationId : $correlationId")

    connector.retrieveObligations(obligationsRequest).map {

      case Right(ResponseWrapper(desCorrelationId, obligationsResponse)) =>
        findOpenObligation(obligationsResponse, periodKey, today) match {

          case Right(obligation) =>
            infoLog(
              s"$logContext matched open obligation for periodKey : $periodKey, VRN : $vrn, " +
                s"correlationId : $correlationId, desCorrelationId : $desCorrelationId")
            Right(ResponseWrapper(correlationId, obligation))

          case Left(failure) =>
            infoLog(
              s"$logContext ${failure.detail} for VRN : $vrn, " +
                s"correlationId : $correlationId, desCorrelationId : $desCorrelationId")
            Left(ErrorWrapper(correlationId, failure.error))
        }

      case Left(ResponseWrapper(desCorrelationId, desError)) =>
        warnLog(
          s"$logContext obligations lookup failed for VRN : $vrn, correlationId : $correlationId, " +
            s"desCorrelationId : $desCorrelationId, downstreamError : ${describe(desError)}")
        Left(ErrorWrapper(correlationId, ServiceUnavailableError))
    }
  }

  private def describe(desError: DesError): String = desError match {
    case DesErrors(codes)        => codes.map(_.code).mkString(",")
    case OutboundError(error, _) => error.code
  }

  private def findOpenObligation(obligationsResponse: ObligationsResponse,
                                 periodKey: String,
                                 today: LocalDate): Either[OpenObligationMatchError, Obligation] =
    obligationsResponse.obligations.find(_.periodKey == periodKey) match {
      case None =>
        Left(NoMatchingObligation(periodKey, obligationsResponse.obligations.map(_.periodKey)))

      case Some(obligation) =>
        obligation.hasEnded(today) match {
          case None        => Left(UnreadableEndDate(periodKey, obligation.end))
          case Some(true)  => Right(obligation)
          case Some(false) => Left(PeriodNotEnded(periodKey, obligation.end, today))
        }
    }
}
