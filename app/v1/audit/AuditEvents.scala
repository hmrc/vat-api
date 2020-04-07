package v1.audit

import v1.models.audit.{AuditDetail, AuditEvent, AuditResponse}
import v1.models.auth.UserDetails

object AuditEvents {

  def auditLiability(nino: String, correlationId: String, userDetails: UserDetails, auditResponse: AuditResponse): AuditEvent = {
    AuditEvent("retrieveVatLiabilities","retrieve-vat-liabilities", AuditDetail(userDetails, nino, correlationId, auditResponse))
  }


}
