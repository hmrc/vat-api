/*
 * Copyright 2023 HM Revenue & Customs
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

package v1.controllers.requestParsers.validators

import play.api.libs.json.{JsLookupResult, JsSuccess, Reads}
import v1.controllers.requestParsers.validators.validations.{MandatoryFieldValidation, _}
import v1.models.errors.{MtdError, NumericFormatRuleError, StringFormatRuleError, UnMappedPlayRuleError}
import v1.models.request.submit.{SubmitRawData, SubmitRequestBody}

class SubmitReturnValidator extends Validator[SubmitRawData] {

  private val validationSet = List(vrnFormatValidation, jsonValidation, responseFieldValidation, tierFourValidation, finalisedValueValidation)

  private def jsonValidation: SubmitRawData => List[List[MtdError]] = (data: SubmitRawData) => {

    List(
      JsonValidation.validate(data.body)
    )
  }

  private def responseFieldValidation: SubmitRawData => List[List[MtdError]] = (data: SubmitRawData) => {
    //mandatoryFieldValidation
    val body = data.body.asJson.get
    val minNetVatDue = BigDecimal(0.00)
    val maxNetVatDue = BigDecimal(99999999999.99)
    val minRegularValue = BigDecimal(-9999999999999.99)
    val maxRegularValue = BigDecimal(9999999999999.99)
    val minNonDecRegularValue = BigDecimal(-9999999999999.0)
    val maxNonDecRegularValue = BigDecimal(9999999999999.0)

    List(
      MandatoryFieldValidation.validate(body \ "periodKey", "periodKey"),
      MandatoryFieldValidation.validate(body \ "vatDueSales", "vatDueSales"),
      MandatoryFieldValidation.validate(body \ "vatDueAcquisitions", "vatDueAcquisitions"),
      MandatoryFieldValidation.validate(body \ "totalVatDue", "totalVatDue"),
      MandatoryFieldValidation.validate(body \ "vatReclaimedCurrPeriod", "vatReclaimedCurrPeriod"),
      MandatoryFieldValidation.validate(body \ "netVatDue", "netVatDue"),
      MandatoryFieldValidation.validate(body \ "totalValueSalesExVAT", "totalValueSalesExVAT"),
      MandatoryFieldValidation.validate(body \ "totalValuePurchasesExVAT", "totalValuePurchasesExVAT"),
      MandatoryFieldValidation.validate(body \ "totalValueGoodsSuppliedExVAT", "totalValueGoodsSuppliedExVAT"),
      MandatoryFieldValidation.validate(body \ "totalAcquisitionsExVAT", "totalAcquisitionsExVAT"),
      MandatoryFieldValidation.validate(body \ "finalised", "finalised"),


      //monataryformat validation
      DecimalMonetaryValueFormatValidation.validate(getFieldFromBody[BigDecimal](body \ "vatDueSales"), "vatDueSales", minRegularValue, maxRegularValue),
      DecimalMonetaryValueFormatValidation.validate(getFieldFromBody[BigDecimal](body \ "vatDueAcquisitions"), "vatDueAcquisitions", minRegularValue, maxRegularValue),
      DecimalMonetaryValueFormatValidation.validate(getFieldFromBody[BigDecimal](body \ "totalVatDue"), "totalVatDue", minRegularValue, maxRegularValue),
      DecimalMonetaryValueFormatValidation.validate(getFieldFromBody[BigDecimal](body \ "vatReclaimedCurrPeriod"), "vatReclaimedCurrPeriod", minRegularValue, maxRegularValue),
      NonNegativeDecimalMonetaryValueFormatValidation.validate(getFieldFromBody[BigDecimal](body \ "netVatDue"), "netVatDue", minNetVatDue, maxNetVatDue),
      NonDecimalMonetaryValueFormatValidation.validate(getFieldFromBody[BigDecimal](body \ "totalValueSalesExVAT"), "totalValueSalesExVAT", minNonDecRegularValue, maxNonDecRegularValue),
      NonDecimalMonetaryValueFormatValidation.validate(getFieldFromBody[BigDecimal](body \ "totalValuePurchasesExVAT"), "totalValuePurchasesExVAT", minNonDecRegularValue, maxNonDecRegularValue),
      NonDecimalMonetaryValueFormatValidation.validate(getFieldFromBody[BigDecimal](body \ "totalValueGoodsSuppliedExVAT"), "totalValueGoodsSuppliedExVAT", minNonDecRegularValue, maxNonDecRegularValue),
      NonDecimalMonetaryValueFormatValidation.validate(getFieldFromBody[BigDecimal](body \ "totalAcquisitionsExVAT"), "totalAcquisitionsExVAT", minNonDecRegularValue, maxNonDecRegularValue),


      //type Validation

      JsonFormatValidation.validate[String](body \ "periodKey", StringFormatRuleError),
      JsonFormatValidation.validate[BigDecimal](body \ "vatDueSales", NumericFormatRuleError.withFieldName("vatDueSales")),
      JsonFormatValidation.validate[BigDecimal](body \ "vatDueAcquisitions", NumericFormatRuleError.withFieldName("vatDueAcquisitions")),
      JsonFormatValidation.validate[BigDecimal](body \ "totalVatDue", NumericFormatRuleError.withFieldName("totalVatDue")),
      JsonFormatValidation.validate[BigDecimal](body \ "vatReclaimedCurrPeriod", NumericFormatRuleError.withFieldName("vatReclaimedCurrPeriod")),
      JsonFormatValidation.validate[BigDecimal](body \ "netVatDue", NumericFormatRuleError.withFieldName("netVatDue")),
      JsonFormatValidation.validate[BigDecimal](body \ "totalValueSalesExVAT", NumericFormatRuleError.withFieldName("totalValueSalesExVAT")),
      JsonFormatValidation.validate[BigDecimal](body \ "totalValuePurchasesExVAT", NumericFormatRuleError.withFieldName("totalValuePurchasesExVAT")),
      JsonFormatValidation.validate[BigDecimal](body \ "totalValueGoodsSuppliedExVAT", NumericFormatRuleError.withFieldName("totalValueGoodsSuppliedExVAT")),
      JsonFormatValidation.validate[BigDecimal](body \ "totalAcquisitionsExVAT", NumericFormatRuleError.withFieldName("totalAcquisitionsExVAT")),
      JsonFormatValidation.validate[Boolean](body \ "finalised", UnMappedPlayRuleError),

      //periodKey format validation

      BodyPeriodKeyValidation.validate(getFieldFromBody[String](body \ "periodKey")),

      //MonetaryValue Validation

      DecimalMonetaryValueRangeValidation.validate(getFieldFromBody[BigDecimal](body \ "vatDueSales"), "vatDueSales", minRegularValue, maxRegularValue),
      DecimalMonetaryValueRangeValidation.validate(getFieldFromBody[BigDecimal](body \ "vatDueAcquisitions"), "vatDueAcquisitions", minRegularValue, maxRegularValue),
      DecimalMonetaryValueRangeValidation.validate(getFieldFromBody[BigDecimal](body \ "totalVatDue"), "totalVatDue", minRegularValue, maxRegularValue),
      DecimalMonetaryValueRangeValidation.validate(getFieldFromBody[BigDecimal](body \ "vatReclaimedCurrPeriod"), "vatReclaimedCurrPeriod", minRegularValue, maxRegularValue),
      DecimalMonetaryValueRangeValidation.validateNonNegative(getFieldFromBody[BigDecimal](body \ "netVatDue"), "netVatDue", minNetVatDue, maxNetVatDue),
      DecimalMonetaryValueRangeValidation.validate(getFieldFromBody[BigDecimal](body \ "totalValueSalesExVAT"), "totalValueSalesExVAT", BigInt(-9999999999999L), BigInt(9999999999999L)),
      DecimalMonetaryValueRangeValidation.validate(getFieldFromBody[BigDecimal](body \ "totalValuePurchasesExVAT"), "totalValuePurchasesExVAT", BigInt(-9999999999999L), BigInt(9999999999999L)),
      DecimalMonetaryValueRangeValidation.validate(getFieldFromBody[BigDecimal](body \ "totalValueGoodsSuppliedExVAT"), "totalValueGoodsSuppliedExVAT", BigInt(-9999999999999L), BigInt(9999999999999L)),
      DecimalMonetaryValueRangeValidation.validate(getFieldFromBody[BigDecimal](body \ "totalAcquisitionsExVAT"), "totalAcquisitionsExVAT", BigInt(-9999999999999L), BigInt(9999999999999L))
    )
  }

  private def tierFourValidation: SubmitRawData => List[List[MtdError]] = (data: SubmitRawData) => {

    val body = data.body.asJson.get.as[SubmitRequestBody]

    List(
      VATNetValueValidation.validate(body.totalVatDue, body.vatReclaimedCurrPeriod, body.netVatDue),
      VATTotalValueValidation.validate(body.vatDueSales, body.vatDueAcquisitions, body.totalVatDue)
    )
  }

  private def vrnFormatValidation: SubmitRawData => List[List[MtdError]] = (data: SubmitRawData) => {
    List(
      VrnValidation.validate(data.vrn)
    )
  }

  private def finalisedValueValidation: SubmitRawData => List[List[MtdError]] = (data: SubmitRawData) => {
    val body: SubmitRequestBody = data.body.asJson.get.as[SubmitRequestBody]

    List(
      FinalisedValueValidation.validate(body.finalised)
    )
  }

  private def getFieldFromBody[A](field: JsLookupResult)(implicit reads: Reads[A]): Option[A] = {

    if (field.isDefined) {
      field.validate[A] match {
        case JsSuccess(_, _) => Some(field.get.as[A])
        case _ => None
      }
    }
    else None
  }

  override def validate(data: SubmitRawData): List[MtdError] = {
    run(validationSet, data).distinct
  }
}
