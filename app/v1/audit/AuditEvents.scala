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

package v1.audit

import v1.models.audit.{AuditDetail, AuditEvent, AuditResponse}
import v1.models.auth.UserDetails

object AuditEvents {

  def auditReturns(nino: String, correlationId: String, userDetails: UserDetails, auditResponse: AuditResponse): AuditEvent = {
    AuditEvent("retrieveVatReturns","retrieve-vat-returns",
      AuditDetail(userDetails, nino, correlationId, auditResponse))
  }

  def auditLiability(nino: String, correlationId: String, userDetails: UserDetails, auditResponse: AuditResponse): AuditEvent = {
    AuditEvent("retrieveVatLiabilities","retrieve-vat-liabilities",
      AuditDetail(userDetails, nino, correlationId, auditResponse))
  }

  def auditPayments(nino: String, correlationId: String, userDetails: UserDetails, auditResponse: AuditResponse): AuditEvent = {
    AuditEvent("retrieveVatPayments","retrieve-vat-payments",
      AuditDetail(userDetails, nino, correlationId, auditResponse))
  }

  def auditObligations(nino: String, correlationId: String, userDetails: UserDetails, auditResponse: AuditResponse): AuditEvent = {
    AuditEvent("retrieveVatObligations","retrieve-vat-obligations",
      AuditDetail(userDetails, nino, correlationId, auditResponse))
  }

  def auditSubmit(nino: String, correlationId: String, userDetails: UserDetails, auditResponse: AuditResponse): AuditEvent = {
    AuditEvent("submitVatReturn","submit-vat-return",
      AuditDetail(userDetails, nino, correlationId, auditResponse))
  }
}
