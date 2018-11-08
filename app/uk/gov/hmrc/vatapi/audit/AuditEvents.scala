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

package uk.gov.hmrc.vatapi.audit

import uk.gov.hmrc.domain.Vrn
import uk.gov.hmrc.vatapi.httpparsers.NRSData

object AuditEvents {

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

  def submitVatReturn(correlationId: String, userType: String, nrSubmissionId: Option[String], arn: Option[String]): AuditEvent[Map[String, String]] = {

    val nrSubmissionIdMap: Map[String, String] = nrSubmissionId.fold(Map.empty[String, String])(id => Map("nrSubmissionId" -> id))
    val arnMap: Map[String, String] = arn.fold(Map.empty[String, String])(arn => Map("agentReferenceNumber" -> arn))

    AuditEvent(
      auditType = "submitVatReturn",
      transactionName = "submit-vat-return",
      detail = Map(
        "X-CorrelationId" -> correlationId,
        "userType" -> userType
      ) ++ nrSubmissionIdMap ++ arnMap
    )
  }
}
