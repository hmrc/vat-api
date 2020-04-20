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

package v1.services

import javax.inject.{Inject, Singleton}
import play.api.libs.json.{Json, Writes}
import play.api.{Configuration, Logger}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.audit.AuditExtensions
import uk.gov.hmrc.play.audit.http.connector.{AuditConnector, AuditResult}
import uk.gov.hmrc.play.audit.model.ExtendedDataEvent
import uk.gov.hmrc.play.bootstrap.config.AppName
import v1.models.audit.AuditEvent

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class AuditService @Inject()(auditConnector: AuditConnector,
                             appNameConfiguration: Configuration) {

  val logger: Logger = Logger(this.getClass)

  def auditEvent[T](event: AuditEvent[T])(implicit hc: HeaderCarrier, ec: ExecutionContext, writer: Writes[T]): Future[AuditResult] = {

    val eventTags = AuditExtensions.auditHeaderCarrier(hc).toAuditTags() +
      ("transactionName" -> event.transactionName)

    val dataEvent = ExtendedDataEvent(
      auditSource = AppName.fromConfiguration(appNameConfiguration),
      auditType = event.auditType,
      detail = Json.toJson(event.detail),
      tags = eventTags
    )
    logger.info(s"Audit event :- dataEvent.tags :: ${dataEvent.tags} --  auditSource:: ${dataEvent.auditSource}" +
      s" --- detail :: ${dataEvent.detail}")
    auditConnector.sendExtendedEvent(dataEvent)
  }
}
