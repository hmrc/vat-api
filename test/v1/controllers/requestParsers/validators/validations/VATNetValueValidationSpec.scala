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
import v1.models.errors.VATNetValueRuleError

class VATNetValueValidationSpec extends UnitSpec {
  "validate" should {
    val vatNetValue = Some(BigDecimal(50.00))

    "return no errors" when {
      "vatReclaimedCurrPeriod is the largest value" in {

        VATNetValueValidation.validate(Some(BigDecimal(0.00)), Some(BigDecimal(50.00)), vatNetValue) shouldBe List()
      }

      "totalVatDue is the largest value" in {

        VATNetValueValidation.validate(Some(BigDecimal(0.00)), Some(BigDecimal(-50.00)), vatNetValue) shouldBe List()
      }
    }

    "return errors" when {
      "vatReclaimedCurrPeriod is the largest value and the difference between is not equal to netVatDue" in {

        VATNetValueValidation.validate(Some(BigDecimal(-3.00)), Some(BigDecimal(55.00)), vatNetValue) shouldBe List(VATNetValueRuleError)
      }

      "totalVatDue is the largest value and the difference between is not equal to netVatDue" in {

        VATNetValueValidation.validate(Some(BigDecimal(58.00)), Some(BigDecimal(2.00)), vatNetValue) shouldBe List(VATNetValueRuleError)
      }
    }

  }

}
