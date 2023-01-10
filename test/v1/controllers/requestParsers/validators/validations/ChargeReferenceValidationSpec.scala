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

package v1.controllers.requestParsers.validators.validations

import support.UnitSpec
import v1.models.errors.FinancialInvalidSearchItem
import v1.models.utils.JsonErrorValidators

class ChargeReferenceValidationSpec extends UnitSpec with JsonErrorValidators {

  "validate" should {
    "return no errors" when {
      "when a valid Vrn is supplied" in {
        val validationResult = ChargeReferenceValidation.validate("XR123456789012")
        validationResult.isEmpty shouldBe true
      }
    }

    "return an error" when {
      "when a Charge Reference is supplied with the first character i not an X" in {
        val validationResult = ChargeReferenceValidation.validate("BR123456789012")
        validationResult.isEmpty shouldBe false
        validationResult.length shouldBe 1
        validationResult.head shouldBe FinancialInvalidSearchItem
      }
      "when a Charge Reference is supplied has too many Letters" in {
        val validationResult = ChargeReferenceValidation.validate("XRP23456789012")
        validationResult.isEmpty shouldBe false
        validationResult.length shouldBe 1
        validationResult.head shouldBe FinancialInvalidSearchItem
      }
      "when an invalid Charge Reference is supplied" in {
        val validationResult = ChargeReferenceValidation.validate("thisIsNotAChargeReference")
        validationResult.isEmpty shouldBe false
        validationResult.length shouldBe 1
        validationResult.head shouldBe FinancialInvalidSearchItem
      }
    }
  }
}
