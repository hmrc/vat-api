/*
 * Copyright 2021 HM Revenue & Customs
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

import v1.models.errors.{MtdError, VATNetValueRuleError}

object VATNetValueValidation {

  private def checkNetValue(totalVatDue: BigDecimal, vatReclaimed:BigDecimal, vatNetValue:BigDecimal): List[MtdError] = {
    val sortedList = List(totalVatDue, vatReclaimed).sorted

    if ((sortedList(1) - sortedList(0)) == vatNetValue) List() else List(VATNetValueRuleError)
  }

  def validate(totalVatDue: Option[BigDecimal], vatReclaimed: Option[BigDecimal], vatNetValue: Option[BigDecimal]): List[MtdError] = {

    (totalVatDue, vatReclaimed, vatNetValue) match {
      case (Some(totalVateDue), Some(vatReclaimed), Some(vatNetValue)) => checkNetValue(totalVateDue, vatReclaimed, vatNetValue)
      case _ => List()
    }
  }
}
