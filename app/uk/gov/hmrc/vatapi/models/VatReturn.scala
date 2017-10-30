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

case class VatReturn(periodKey: String,
                     vatDueSales: Amount,
                     vatDueAcquisitions: Amount,
                     totalVatDue: Amount,
                     vatReclaimedCurrPeriod: Amount,
                     netVatDue: Amount,
                     totalValueSalesExVAT: Amount,
                     totalValuePurchasesExVAT: Amount,
                     totalValueGoodsSuppliedExVAT: Amount,
                     totalAcquisitionsExVAT: Amount)

object VatReturn {

  val periodKeyValidator: Reads[String] = Reads
    .of[String]
    .filter(JsonValidationError("period key should be a 4 character string",
                                ErrorCode.INVALID_PERIOD_KEY))(_.length == 4)

  implicit val writes: OWrites[VatReturn] = Json.writes[VatReturn]

  implicit val reads: Reads[VatReturn] = (
    (__ \ "periodKey").read[String](periodKeyValidator) and
      (__ \ "vatDueSales").read[Amount](vatAmountValidator) and
      (__ \ "vatDueAcquisitions").read[Amount](vatAmountValidator) and
      (__ \ "totalVatDue").read[Amount](vatAmountValidator) and
      (__ \ "vatReclaimedCurrPeriod").read[Amount](vatAmountValidator) and
      (__ \ "netVatDue").read[Amount](vatNonNegativeAmountValidator) and
      (__ \ "totalValueSalesExVAT").read[Amount](vatWholeAmountValidator) and
      (__ \ "totalValuePurchasesExVAT")
        .read[Amount](vatWholeAmountValidator) and
      (__ \ "totalValueGoodsSuppliedExVAT")
        .read[Amount](vatWholeAmountValidator) and
      (__ \ "totalAcquisitionsExVAT").read[Amount](vatWholeAmountValidator)
  )(VatReturn.apply _)
    .validate(
      Seq(
        Validation[VatReturn](
          JsPath \ "totalVatDue",
          vatReturn =>
            vatReturn.totalVatDue == vatReturn.vatDueSales + vatReturn.vatDueAcquisitions,
          JsonValidationError(
            "totalVatDue should be equal to vatDueSales + vatDueAcquisitions",
            ErrorCode.VAT_TOTAL_VALUE)
        ),
        Validation[VatReturn](
          JsPath \ "netVatDue",
          vatReturn =>
            vatReturn.netVatDue == (vatReturn.totalVatDue - vatReturn.vatReclaimedCurrPeriod).abs,
          JsonValidationError(
            "netVatDue should be the difference between the largest and the smallest values among totalVatDue and vatReclaimedCurrPeriod",
            ErrorCode.VAT_NET_VALUE)
        )
      )
    )

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
            totalValueGoodsSuppliedExVAT =
              vatReturn.totalValueGoodsSuppliedExVAT,
            totalAcquisitionsExVAT = vatReturn.totalAcquisitionsExVAT
          ))
    }
}

case class VatReturns(vatReturns: Seq[VatReturn])

object VatReturns {
  implicit val writes: Writes[VatReturns] = Json.writes[VatReturns]
}
