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

package v1.nrs

import com.kenshoo.play.metrics.Metrics
import org.joda.time.DateTime
import play.api.libs.json.Json
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.audit.http.connector.AuditResult
import utils.{HashUtil, Logging, Timer}
import v1.audit.AuditEvents
import v1.controllers.UserRequest
import v1.models.audit.NrsAuditDetail
import v1.models.request.submit.SubmitRequest
import v1.nrs.models.request.{Metadata, NrsSubmission, SearchKeys}
import v1.nrs.models.response.NrsResponse
import v1.services.AuditService

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class NrsService @Inject()(auditService: AuditService,
                           connector: NrsConnector,
                           hashUtil: HashUtil,
                           override val metrics: Metrics) extends Timer with Logging {

  def submit(vatSubmission: SubmitRequest, nrsId: String, submissionTimestamp: DateTime)(
    implicit request: UserRequest[_],
    hc: HeaderCarrier,
    ec: ExecutionContext,
    correlationId: String): Future[Option[NrsResponse]] = {

    val nrsSubmission = buildNrsSubmission(vatSubmission, nrsId, submissionTimestamp, request)

    def audit(resp: NrsOutcome): Future[AuditResult] = resp match {
      case Left(err) =>
        auditService.auditEvent(
          AuditEvents.auditNrsSubmit("submitToNonRepudiationStoreFailure",
            NrsAuditDetail(
              vatSubmission.vrn.toString,
              request.headers.get("Authorization").getOrElse(""),
              Some(nrsId),
              Some(Json.toJson(nrsSubmission)),
              correlationId))
        )
      case _ =>
        auditService.auditEvent(
          AuditEvents.auditNrsSubmit("submitToNonRepudiationStore",
            NrsAuditDetail(
              vatSubmission.vrn.toString,
              request.headers.get("Authorization").getOrElse(""),
              Some(nrsId),
              None,
              correlationId))
        )
    }

    timeFuture("NRS Submission", "nrs.submission") {
      connector.submit(nrsSubmission).map { response =>
        audit(response)
        response.toOption
      }
    }
  }

  def buildNrsSubmission(vatSubmission: SubmitRequest,
                         nrsId: String,
                         submissionTimestamp: DateTime,
                         request: UserRequest[_]): NrsSubmission = {

    import vatSubmission._

    val payloadString = Json.toJson(body).toString()
    val encodedPayload = hashUtil.encode(payloadString)
    val sha256Checksum = hashUtil.getHash(payloadString)

    NrsSubmission(
      payload = encodedPayload,
      Metadata(
        nrSubmissionId = Some(nrsId),
        businessId = "vat",
        notableEvent = "vat-return",
        payloadContentType = "application/json",
        payloadSha256Checksum = sha256Checksum,
        userSubmissionTimestamp = submissionTimestamp,
        identityData = request.userDetails.identityData,
        userAuthToken = request.headers.get("Authorization").get,
        headerData = Json.toJson(request.headers.toMap.map { h => h._1 -> h._2.head }),
        searchKeys =
          SearchKeys(
            vrn = Some(vrn.vrn),
            companyName = None,
            periodKey = body.periodKey,
            taxPeriodEndDate = None
          )
      )
    )
  }


}
