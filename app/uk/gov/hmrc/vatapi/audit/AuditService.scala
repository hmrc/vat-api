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

import javax.inject.Inject
import org.joda.time.{DateTime, DateTimeZone}
import play.api.Logger
import play.api.libs.json.{Format, Json}
import play.api.mvc.Request
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.audit.AuditExtensions
import uk.gov.hmrc.play.audit.model.ExtendedDataEvent
import uk.gov.hmrc.vatapi.connectors.MicroserviceAuditConnector
import uk.gov.hmrc.vatapi.resources.BusinessResult

import scala.concurrent.ExecutionContext

class AuditService @Inject()(auditConnector: MicroserviceAuditConnector) {

  val logger: Logger = Logger(this.getClass)

  def audit[T](event: AuditEvent[T])(
    implicit hc: HeaderCarrier,
    fmt: Format[T],
    request: Request[_],
    ec: ExecutionContext
  ): BusinessResult[Unit] = {

    logger.debug(s"[AuditService][audit] Generating ${event.auditType} audit event for vat-api.")

    val auditEvent =
      ExtendedDataEvent(
        auditSource = "vat-api",
        auditType = event.auditType,
        tags = AuditExtensions.auditHeaderCarrier(hc).toAuditTags(event.transactionName, request.path),
        detail = Json.toJson(event.detail),
        generatedAt = DateTime.now(DateTimeZone.UTC)
      )

    BusinessResult.success(auditConnector.sendExtendedEvent(auditEvent))

  }

}
