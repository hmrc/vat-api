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

package uk.gov.hmrc.vatapi.audit

import play.api.libs.json.{JsObject, Json}
import uk.gov.hmrc.domain.Vrn
import uk.gov.hmrc.vatapi.httpparsers.NRSData
import uk.gov.hmrc.vatapi.models.NRSSubmission
import uk.gov.hmrc.vatapi.models.audit.{AuditDetail, AuditEvent, AuditResponse}

object AuditEvents {

  val agentRef: Option[String] => Map[String, String] = arn => arn.fold(Map.empty[String, String])(arn => Map("agentReferenceNumber" -> arn))

  def nrsAudit(vrn: Vrn, nrsData: NRSData, authorization: String): AuditEvent[Map[String, String]] =
    AuditEvent(
      auditType = "submitToNonRepudiationStore",
      transactionName = "submit-vat-return",
      detail = Map(
        "vrn" -> vrn.vrn,
        "authorization" -> authorization,
        "nrSubmissionID" -> nrsData.nrSubmissionId,
        "correlationId" -> "" //this is meant to be empty and with an incorrect name - see Vat Api TxM assessment confluence page
      )
    )

  def nrsEmptyAudit(vrn: Vrn, submission: NRSSubmission, authorization: String): AuditEvent[JsObject] =
    AuditEvent(
      auditType = "submitToNonRepudiationStoreFailure",
      transactionName = "submit-vat-return",
      detail = Json.obj(
        "vrn" -> vrn.vrn,
        "authorization" -> authorization,
        "request" -> NRSSubmission.format.writes(submission),
        "correlationId" -> "" //this is meant to be empty and with an incorrect name - see Vat Api TxM assessment confluence page
      )
    )

  def submitVatReturn(correlationId: String, userType: String, nrSubmissionId: Option[String], arn: Option[String],
                      response: AuditResponse): AuditEvent[AuditDetail] = {
    AuditEvent(
      auditType = "submitVatReturn",
      transactionName = "submit-vat-return",
      detail = AuditDetail(userType = userType, arn = arn, `X-CorrelationId` = correlationId, response, nrSubmissionId)
    )
  }

  def retrieveVatObligationsAudit(correlationId: String, userType: String, arn: Option[String], response: AuditResponse): AuditEvent[AuditDetail] = {
    AuditEvent(
      auditType = "retrieveVatObligations",
      transactionName = "retrieve-vat-obligations",
      detail = AuditDetail(userType = userType, arn = arn, `X-CorrelationId` = correlationId, response)
    )
  }

  def retrieveVatReturnsAudit(correlationId: String, userType: String, arn: Option[String], response: AuditResponse): AuditEvent[AuditDetail] = {

    AuditEvent(
      auditType = "retrieveVatReturns",
      transactionName = "retrieve-vat-returns",
      detail = AuditDetail(userType = userType, arn = arn, `X-CorrelationId` = correlationId, response)
    )
  }

  def retrieveVatLiabilitiesAudit(correlationId: String, userType: String, arn: Option[String], response: AuditResponse): AuditEvent[AuditDetail] = {

    AuditEvent(
      auditType = "retrieveVatLiabilities",
      transactionName = "retrieve-vat-liabilities",
      detail = AuditDetail(userType = userType, arn = arn, `X-CorrelationId` = correlationId, response)
    )
  }

  def retrieveVatPaymentsAudit(correlationId: String, userType: String, arn: Option[String], response: AuditResponse): AuditEvent[AuditDetail] = {

    AuditEvent(
      auditType = "retrieveVatPayments",
      transactionName = "retrieve-vat-payments",
      detail = AuditDetail(userType = userType, arn = arn, `X-CorrelationId` = correlationId, response)
    )

  }
}
