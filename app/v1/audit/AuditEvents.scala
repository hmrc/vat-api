/*
 * Copyright 2022 HM Revenue & Customs
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

package v1.audit

import v1.models.audit.{AuditDetail, AuditEvent, AuditResponse, NrsAuditDetail, SubmitAuditDetail}
import v1.models.auth.UserDetails

object AuditEvents {

  def auditReturns(correlationId: String, userDetails: UserDetails, auditResponse: AuditResponse): AuditEvent[AuditDetail] =
    AuditEvent("retrieveVatReturns", "retrieve-vat-returns",
      AuditDetail(userDetails, correlationId, auditResponse))

  def auditLiabilities(correlationId: String, userDetails: UserDetails, auditResponse: AuditResponse): AuditEvent[AuditDetail] =
    AuditEvent("retrieveVatLiabilities", "retrieve-vat-liabilities",
      AuditDetail(userDetails, correlationId, auditResponse))

  def auditPayments(correlationId: String, userDetails: UserDetails, auditResponse: AuditResponse): AuditEvent[AuditDetail] =
    AuditEvent("retrieveVatPayments", "retrieve-vat-payments",
      AuditDetail(userDetails, correlationId, auditResponse))

  def auditObligations(correlationId: String, userDetails: UserDetails, auditResponse: AuditResponse): AuditEvent[AuditDetail] =
    AuditEvent("retrieveVatObligations", "retrieve-vat-obligations",
      AuditDetail(userDetails, correlationId, auditResponse))

  def auditSubmit(correlationId: String, userDetails: UserDetails, auditResponse: AuditResponse, rawData: String): AuditEvent[SubmitAuditDetail] =
    AuditEvent("submitVatReturn", "submit-vat-return",
      SubmitAuditDetail(userDetails, correlationId, auditResponse, rawData))

  def auditNrsSubmit(auditType: String, nrsAuditDetail: NrsAuditDetail): AuditEvent[NrsAuditDetail] =
    AuditEvent(auditType, "submit-vat-return", nrsAuditDetail)

  //TODO sign off with Alison after we know what the two endpoints will look like
  def auditPenalties(correlationId: String, userDetails: UserDetails, auditResponse: AuditResponse): AuditEvent[AuditDetail] =
    AuditEvent(
      auditType = "retrieveVatPenalties",
      transactionName = "retrieve-vat-penalties",
      detail = AuditDetail(userDetails, correlationId, auditResponse)
    )
}
