/*
 * Copyright 2018 HM Revenue & Customs
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

package uk.gov.hmrc.vatapi.orchestrators

import org.joda.time.DateTime
import play.api.Logger
import uk.gov.hmrc.domain.Vrn
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.vatapi.audit.{AuditEvents, AuditService}
import uk.gov.hmrc.vatapi.auth.{Agent, AuthContext}
import uk.gov.hmrc.vatapi.httpparsers.{EmptyNrsData, NRSData}
import uk.gov.hmrc.vatapi.models.{ErrorResult, Errors, InternalServerErrorResult, VatReturnDeclaration}
import uk.gov.hmrc.vatapi.resources.AuthRequest
import uk.gov.hmrc.vatapi.resources.wrappers.VatReturnResponse
import uk.gov.hmrc.vatapi.services.{NRSService, VatReturnsService}
import uk.gov.hmrc.vatapi.utils.ImplicitDateTimeFormatter

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object VatReturnsOrchestrator extends VatReturnsOrchestrator {
  override val nrsService: NRSService = NRSService
  override val vatReturnsService: VatReturnsService = VatReturnsService
  override val auditService: AuditService = AuditService

  override def submissionTimestamp: DateTime = DateTime.now()

}

trait VatReturnsOrchestrator extends ImplicitDateTimeFormatter {

  val logger: Logger = Logger(this.getClass)

  val nrsService: NRSService
  val vatReturnsService: VatReturnsService
  val auditService: AuditService

  def submissionTimestamp: DateTime

  def submitVatReturn(vrn: Vrn,
                      vatReturn: VatReturnDeclaration
                     )(implicit hc: HeaderCarrier, request: AuthRequest[_]): Future[Either[ErrorResult, VatReturnResponse]] = {

    logger.debug(s"[VatReturnsOrchestrator][submitVatReturn] - Orchestrating calls to NRS and Vat Returns")
    nrsService.submit(vrn, vatReturn) flatMap {
      case Right(nrsData) =>
        logger.debug(s"[VatReturnsOrchestrator][submitVatReturn] - Succesfully retrieved data from NRS: $nrsData")
        val arn: Option[String] = request.authContext match {
          case Agent(_,_,_,enrolments) => enrolments.getEnrolment("HMRC-AS-AGENT").flatMap(_.getIdentifier("AgentReferenceNumber")).map(_.value)
          case c: AuthContext => c.agentReference
        }

        nrsData match {
          case EmptyNrsData =>
            vatReturnsService.submit(vrn,
              vatReturn.toDes(submissionTimestamp, arn)) map { response =>
                Right(response withNrsData nrsData.copy(timestamp = submissionTimestamp.toIsoInstant))
            }
          case _ =>
            auditService.audit(AuditEvents.nrsAudit(vrn, nrsData,
              request.headers.get("Authorization").getOrElse(""), request.headers.get("x-correlationid").getOrElse("")))
            vatReturnsService.submit(vrn,
              vatReturn.toDes(submissionTimestamp, arn)) map { response =>
                Right(response withNrsData nrsData.copy(timestamp = submissionTimestamp.toIsoInstant))
            }
        }
      case Left(e) =>
        logger.error(s"[VatReturnsOrchestrator][submitVatReturn] - Error retrieving data from NRS: $e")
        Future.successful(Left(InternalServerErrorResult(Errors.InternalServerError.message)))
    }
  }

  case class VatReturnOrchestratorResponse(nrs: NRSData, vatReturnResponse: VatReturnResponse)

}




