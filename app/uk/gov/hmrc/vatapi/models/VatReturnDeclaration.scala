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

import org.joda.time.DateTime
import play.api.libs.functional.syntax._
import play.api.libs.json._
import uk.gov.hmrc.vatapi.models.Validation._
import uk.gov.hmrc.vatapi.utils.ImplicitCurrencyFormatter._

case class VatReturnDeclaration(
  periodKey: String,
  vatDueSales: Amount,
  vatDueAcquisitions: Amount,
  totalVatDue: Amount,
  vatReclaimedCurrPeriod: Amount,
  netVatDue: Amount,
  totalValueSalesExVAT: Amount,
  totalValuePurchasesExVAT: Amount,
  totalValueGoodsSuppliedExVAT: Amount,
  totalAcquisitionsExVAT: Amount,
  finalised: Boolean
) {

  def toDes(timestamp: DateTime = new DateTime(), arn: Option[String]): des.VatReturnDeclaration =
    des.VatReturnDeclaration(
      periodKey = periodKey,
      vatDueSales = vatDueSales,
      vatDueAcquisitions = vatDueAcquisitions,
      vatDueTotal = totalVatDue,
      vatReclaimedCurrPeriod = vatReclaimedCurrPeriod,
      vatDueNet = netVatDue,
      totalValueSalesExVAT = totalValueSalesExVAT,
      totalValuePurchasesExVAT = totalValuePurchasesExVAT,
      totalValueGoodsSuppliedExVAT = totalValueGoodsSuppliedExVAT,
      totalAllAcquisitionsExVAT = totalAcquisitionsExVAT,
      agentReferenceNumber = arn,
      receivedAt = timestamp
    )

}

object VatReturnDeclaration {

  implicit val writes: OWrites[VatReturnDeclaration] = Json.writes[VatReturnDeclaration]

  private val periodKeyValidator: Reads[String] = Reads
    .of[String]
    .filter(JsonValidationError("period key should be a 4 character string",
                                ErrorCode.PERIOD_KEY_INVALID))(_.length == 4)

  implicit val reads: Reads[VatReturnDeclaration] = (
    (__ \ "periodKey").read[String](periodKeyValidator) and
      (__ \ "vatDueSales").read[Amount](vatAmountValidator) and
      (__ \ "vatDueAcquisitions").read[Amount](vatAmountValidator) and
      (__ \ "totalVatDue").read[Amount](vatAmountValidator) and
      (__ \ "vatReclaimedCurrPeriod").read[Amount](vatAmountValidator) and
      (__ \ "netVatDue").read[Amount](vatNonNegativeAmountValidator) and
      (__ \ "totalValueSalesExVAT").read[Amount](vatAmountValidatorWithZeroDecimals) and
      (__ \ "totalValuePurchasesExVAT")
        .read[Amount](vatAmountValidatorWithZeroDecimals) and
      (__ \ "totalValueGoodsSuppliedExVAT")
        .read[Amount](vatAmountValidatorWithZeroDecimals) and
      (__ \ "totalAcquisitionsExVAT").read[Amount](vatAmountValidatorWithZeroDecimals) and
      (__ \ "finalised").read[Boolean]
  )(VatReturnDeclaration.apply _)
    .validate(
      Seq(
        Validation[VatReturnDeclaration](
          JsPath \ "totalVatDue",
          vatReturn => vatReturn.totalVatDue == vatReturn.vatDueSales + vatReturn.vatDueAcquisitions,
          JsonValidationError(
            "totalVatDue should be equal to vatDueSales + vatDueAcquisitions",
            ErrorCode.VAT_TOTAL_VALUE)
        ),
        Validation[VatReturnDeclaration](
          JsPath \ "netVatDue",
          vatReturn =>
            vatReturn.netVatDue == (vatReturn.totalVatDue - vatReturn.vatReclaimedCurrPeriod).abs,
          JsonValidationError(
            "netVatDue should be the difference between the largest and the smallest values among totalVatDue and vatReclaimedCurrPeriod",
            ErrorCode.VAT_NET_VALUE)
        )
      )
    )

}
