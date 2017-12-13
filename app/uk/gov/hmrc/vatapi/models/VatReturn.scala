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

package uk.gov.hmrc.vatapi.models

import play.api.libs.functional.syntax._
import play.api.libs.json._
import uk.gov.hmrc.vatapi.models.Validation._
import org.joda.time.LocalDate
import uk.gov.hmrc.vatapi.models.dateTimeFormat

case class VatReturn(
  period: Period,
  vatDueSales: Amount,
  vatDueAcquisitions: Amount,
  totalVatDue: Amount,
  vatReclaimedCurrPeriod: Amount,
  netVatDue: Amount,
  totalValueSalesExVAT: Amount,
  totalValuePurchasesExVAT: Amount,
  totalValueGoodsSuppliedExVAT: Amount,
  totalAcquisitionsExVAT: Amount,
  received: LocalDate
)

object VatReturn {

  implicit val writes: OWrites[VatReturn] = Json.writes[VatReturn]
  implicit val reads: Reads[VatReturn] = Json.reads[VatReturn]

  implicit val from: DesTransformValidator[des.VatReturn, VatReturn] =
    new DesTransformValidator[des.VatReturn, VatReturn] {
      def from(vatReturn: des.VatReturn): Either[DesTransformError, VatReturn] =
        Right(
          VatReturn(
            period = Period(
              key   = vatReturn.periodKey,
              start = LocalDate.parse(vatReturn.inboundCorrespondenceFromDate),
              end   = LocalDate.parse(vatReturn.inboundCorrespondenceToDate)
            ),
            vatDueSales = vatReturn.vatDueSales,
            vatDueAcquisitions = vatReturn.vatDueAcquisitions,
            totalVatDue = vatReturn.vatDueTotal,
            vatReclaimedCurrPeriod = vatReturn.vatReclaimedCurrPeriod,
            netVatDue = vatReturn.vatDueNet,
            totalValueSalesExVAT = vatReturn.totalValueSalesExVAT,
            totalValuePurchasesExVAT = vatReturn.totalValuePurchasesExVAT,
            totalValueGoodsSuppliedExVAT = vatReturn.totalValueGoodsSuppliedExVAT,
            totalAcquisitionsExVAT = vatReturn.totalAcquisitionsExVAT,
            received = vatReturn.receivedAt.toLocalDate
          )
        )
    }
}

case class VatReturns(vatReturns: Seq[VatReturn])

object VatReturns {
  implicit val writes: Writes[VatReturns] = Json.writes[VatReturns]
}
