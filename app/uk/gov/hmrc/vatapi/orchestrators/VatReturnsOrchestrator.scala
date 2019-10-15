/*
 * Copyright 2019 HM Revenue & Customs
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

import javax.inject.Inject

import org.joda.time.DateTime
import play.api.Logger
import play.api.libs.json.JsObject
import uk.gov.hmrc.domain.Vrn
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.vatapi.audit.AuditEvents
import uk.gov.hmrc.vatapi.httpparsers.{EmptyNrsData, NRSData}
import uk.gov.hmrc.vatapi.models.audit.AuditEvent
import uk.gov.hmrc.vatapi.models.{ErrorResult, Errors, InternalServerErrorResult, NRSSubmission, VatReturnDeclaration}
import uk.gov.hmrc.vatapi.resources.AuthRequest
import uk.gov.hmrc.vatapi.resources.wrappers.VatReturnResponse
import uk.gov.hmrc.vatapi.services.{AuditService, NRSService, VatReturnsService}
import uk.gov.hmrc.vatapi.utils.ImplicitDateTimeFormatter

import scala.concurrent.{ExecutionContext, Future}


class VatReturnsOrchestrator @Inject()(
                                        nrsService: NRSService,
                                        vatReturnsService: VatReturnsService,
                                        auditService: AuditService
                                      )(implicit ec: ExecutionContext) extends ImplicitDateTimeFormatter {

  val logger: Logger = Logger(this.getClass)

  def submissionTimestamp: DateTime = DateTime.now()

  def submitVatReturn(vrn: Vrn, vatReturn: VatReturnDeclaration, arn: Option[String])
                     (implicit hc: HeaderCarrier, request: AuthRequest[_]): Future[Either[ErrorResult, VatReturnResponse]] = {

    logger.debug(s"[VatReturnsOrchestrator][submitVatReturn] - Orchestrating calls to NRS and Vat Returns")

    val submission = nrsService.convertToNrsSubmission(vrn, vatReturn)

    nrsService.submit(vrn, submission) flatMap {
      case Left(e) =>
        logger.error(s"[VatReturnsOrchestrator][submitVatReturn] - Error retrieving data from NRS: $e")
        Future.successful(Left(InternalServerErrorResult(Errors.InternalServerError.message)))
      case Right(nrsData) =>
        logger.debug(s"[VatReturnsOrchestrator][submitVatReturn] - Successfully retrieved data from NRS: $nrsData")

        val thisSubmissionTimestamp = submissionTimestamp

        nrsData match {
          case EmptyNrsData =>
            auditService.audit(buildEmptyNrsAudit(vrn, submission, request))
            vatReturnsService.submit(vrn, vatReturn.toDes(thisSubmissionTimestamp, arn)) map {
              response => Right(response withNrsData nrsData.copy(timestamp = thisSubmissionTimestamp.toIsoInstant))
            }
          case _ =>
            auditService.audit(buildNrsAudit(vrn, nrsData, request))

            vatReturnsService.submit(vrn, vatReturn.toDes(thisSubmissionTimestamp, arn)) map {
              response => Right(response withNrsData nrsData.copy(timestamp = thisSubmissionTimestamp.toIsoInstant))
            }
        }
    }
  }

  case class VatReturnOrchestratorResponse(nrs: NRSData, vatReturnResponse: VatReturnResponse)

  private def buildNrsAudit(vrn: Vrn, nrsData: NRSData, request: AuthRequest[_]): AuditEvent[Map[String, String]] =
    AuditEvents.nrsAudit(vrn, nrsData, request.headers.get("Authorization").getOrElse(""))

  private def buildEmptyNrsAudit(vrn: Vrn, submission: NRSSubmission, request: AuthRequest[_]): AuditEvent[JsObject] =
    AuditEvents.nrsEmptyAudit(vrn, submission, request.headers.get("Authorization").getOrElse(""))

}




