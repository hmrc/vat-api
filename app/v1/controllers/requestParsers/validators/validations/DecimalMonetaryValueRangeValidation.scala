/*
 * Copyright 2024 HM Revenue & Customs
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

package v1.controllers.requestParsers.validators.validations

import v1.models.errors.{InvalidMonetaryValueError, MtdError}

object DecimalMonetaryValueRangeValidation {

  def validate(field: Option[BigDecimal], fieldName: String, minValue: BigDecimal, maxValue: BigDecimal): List[MtdError] = {

    val error = InvalidMonetaryValueError.withFieldName(fieldName, minValue, maxValue)

    field match {
      case Some(value) if value > maxValue || value < minValue  =>
        List(error)
      case _ => List()
    }
  }

  def validateNonNegative(field: Option[BigDecimal], fieldName: String, minValue: BigDecimal, maxValue: BigDecimal): List[MtdError] = {

    val error = InvalidMonetaryValueError.withFieldNameAndNonNegative(fieldName)

    field match {
      case Some(value) if value > maxValue || value < minValue  =>
        List(error)
      case _ => List()
    }
  }

  def validate(field:Option[BigDecimal], fieldName: String, minValue: BigInt, maxValue: BigInt): List[MtdError] = {

    val error = InvalidMonetaryValueError.withFieldName(fieldName)

    field match {
      case Some(value) if value > BigDecimal(maxValue) || value < BigDecimal(minValue)  =>
        List(error)
      case _ => List()
    }
  }
}
