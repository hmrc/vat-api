/*
 * Copyright 2022 HM Revenue & Customs
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

import support.UnitSpec
import v1.models.errors.VATTotalValueRuleError

class VATTotalValueValidationSpec extends UnitSpec {
  "validate" should {

    val totalVatDue = Some(BigDecimal(50.00))

    "return no errors" when {
      "vatDueSales and vatDueAcquisitions combined are equal to totalVatDue" in {

        VATTotalValueValidation.validate(Some(BigDecimal(0.00)), Some(BigDecimal(50.00)), totalVatDue) shouldBe List()
      }

      "return errors" when {
        "vatDueSales and vatDueAcquisitions combined are not equal to totalVatDue" in {

          VATTotalValueValidation.validate(Some(BigDecimal(58.00)), Some(BigDecimal(2.00)), totalVatDue) shouldBe List(VATTotalValueRuleError)
        }
      }

    }
  }
}
