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

  def nrsAudit(vrn: Vrn, nrsData: NRSData, authorization: String, correlationId: String): AuditEvent[Map[String, String]] =
    AuditEvent(
      auditType = "submitToNonRepudiationStore",
      transactionName = "submit-vat-return",
      detail = nrsAuditDetals(vrn, authorization, nrsData.nrSubmissionId, correlationId)
    )

  private def nrsAuditDetals(vrn: Vrn, authorization: String, nrSubmissionID: String, correlationId: String): Map[String, String] = Map(
    "vrn" -> vrn.vrn,
    "authorization" -> authorization,
    "nrSubmissionID" -> nrSubmissionID,
    "correlationId" -> correlationId
  )

  def submitVatReturn(xCorrelationId: String, userType: String, nrSubmissionId: Option[String]): AuditEvent[Map[String, String]] = {

    val nrSubmissionIdMap: Map[String, String] = nrSubmissionId.fold(Map.empty[String, String])(id => Map("nrSubmissionId" -> id))

    AuditEvent(
      auditType = "submitVatReturn",
      transactionName = "submit-vat-return",
      detail = Map(
        "X-CorrelationId" -> xCorrelationId,
        "userType" -> userType
      ) ++ nrSubmissionIdMap
    )
  }
}
