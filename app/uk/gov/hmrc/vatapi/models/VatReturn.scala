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

package uk.gov.hmrc.vatapi.models

import play.api.libs.json._

case class VatReturn(
                      periodKey: String,
                      vatDueSales: Amount,
                      vatDueAcquisitions: Amount,
                      totalVatDue: Amount,
                      vatReclaimedCurrPeriod: Amount,
                      netVatDue: Amount,
                      totalValueSalesExVAT: Amount,
                      totalValuePurchasesExVAT: Amount,
                      totalValueGoodsSuppliedExVAT: Amount,
                      totalAcquisitionsExVAT: Amount
                    )

object VatReturn {

  implicit val writes: OWrites[VatReturn] = Json.writes[VatReturn]
  implicit val reads: Reads[VatReturn] = Json.reads[VatReturn]

  implicit val from: DesTransformValidator[des.VatReturn, VatReturn] =
    new DesTransformValidator[des.VatReturn, VatReturn] {
      def from(vatReturn: des.VatReturn): Either[DesTransformError, VatReturn] =
        Right(
          VatReturn(
            periodKey = vatReturn.periodKey,
            vatDueSales = vatReturn.vatDueSales,
            vatDueAcquisitions = vatReturn.vatDueAcquisitions,
            totalVatDue = vatReturn.vatDueTotal,
            vatReclaimedCurrPeriod = vatReturn.vatReclaimedCurrPeriod,
            netVatDue = vatReturn.vatDueNet,
            totalValueSalesExVAT = vatReturn.totalValueSalesExVAT,
            totalValuePurchasesExVAT = vatReturn.totalValuePurchasesExVAT,
            totalValueGoodsSuppliedExVAT = vatReturn.totalValueGoodsSuppliedExVAT,
            totalAcquisitionsExVAT = vatReturn.totalAllAcquisitionsExVAT
          )
        )
    }
}
