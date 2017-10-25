/*
 * Copyright 2017 HM Revenue & Customs
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

package uk.gov.hmrc.vatapi.models.des

import org.joda.time.{DateTime, DateTimeZone}
import play.api.libs.json.{Json, Reads, Writes}
import uk.gov.hmrc.vatapi.models
import uk.gov.hmrc.vatapi.models.Amount
import uk.gov.hmrc.vatapi.models.dateFormat

case class VatReturn(periodKey: String,
                     vatDueSales: Amount,
                     vatDueAcquisitions: Amount,
                     vatDueTotal: Amount,
                     vatReclaimedCurrPeriod: Amount,
                     vatDueNet: Amount,
                     totalValueSalesExVAT: Amount,
                     totalValuePurchasesExVAT: Amount,
                     totalValueGoodsSuppliedExVAT: Amount,
                     totalAcquisitionsExVAT: Amount,
                     agentReferenceNumber: Option[String] = None,
                     receivedAt: DateTime)

object VatReturn {
  implicit val writes: Writes[VatReturn] = Json.writes[VatReturn]
  implicit val reads: Reads[VatReturn] = Json.reads[VatReturn]

  def from(vatReturn: models.VatReturn): VatReturn =
    VatReturn(
      periodKey = vatReturn.periodKey,
      vatDueSales = vatReturn.vatDueSales,
      vatDueAcquisitions = vatReturn.vatDueAcquisitions,
      vatDueTotal = vatReturn.totalVatDue,
      vatReclaimedCurrPeriod = vatReturn.vatReclaimedCurrPeriod,
      vatDueNet = vatReturn.netVatDue,
      totalValueSalesExVAT = vatReturn.totalAcquisitionsExVAT,
      totalValuePurchasesExVAT = vatReturn.totalValuePurchasesExVAT,
      totalValueGoodsSuppliedExVAT = vatReturn.totalValueGoodsSuppliedExVAT,
      totalAcquisitionsExVAT = vatReturn.totalAcquisitionsExVAT,
      receivedAt = DateTime.now(DateTimeZone.UTC)
    )

}
