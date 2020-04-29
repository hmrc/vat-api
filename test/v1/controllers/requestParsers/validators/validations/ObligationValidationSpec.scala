/*
 * Copyright 2020 HM Revenue & Customs
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
import v1.models.errors.{InvalidFromError, InvalidStatusError, InvalidToError, RuleMissingDateRangeError}
import v1.models.request.obligations.ObligationsRawData

class ObligationValidationSpec extends UnitSpec {

  private val validVRN = "123456789"
  private val validFromDate = "2017-01-01"
  private val validToDate = "2017-03-31"

  private val invalidFrom = "2017-34-45"
  private val invalidTo = "2017-14-31"

  "validate" should {
    "return no errors" when {
      "no from or to date is supplied but status is set to 'O'" in {
        val validationResult = ObligationParameterFormatValidation.validate(ObligationsRawData(validVRN, None, None, Some("O")))
        validationResult.isEmpty shouldBe true
      }

      "all parameters are supplied" in {
        val validationResult = ObligationParameterFormatValidation.validate(ObligationsRawData(validVRN, Some(validFromDate), Some(validToDate), Some("O")))
        validationResult.isEmpty shouldBe true
      }
    }

    "return an error" when {

      "invalid FROM and TO are supplied" in {
        val validationResult = ObligationParameterFormatValidation.validate(ObligationsRawData(validVRN, Some(invalidFrom), Some(invalidTo), Some("O")))
        validationResult.isEmpty shouldBe false
        validationResult.length shouldBe 1
        validationResult.head shouldBe InvalidFromError
      }

      "no FROM or TO date is supplied but status is set to 'F'" in {
        val validationResult = ObligationParameterFormatValidation.validate(ObligationsRawData(validVRN, None, None, Some("F")))
        validationResult.isEmpty shouldBe false
        validationResult.length shouldBe 1
        validationResult.head shouldBe RuleMissingDateRangeError
      }

      "no FROM or TO date is supplied and status is invalid" in {
        val validationResult = ObligationParameterFormatValidation.validate(ObligationsRawData(validVRN, None, None, Some("NotAStatus")))
        validationResult.isEmpty shouldBe false
        validationResult.length shouldBe 1
        validationResult.head shouldBe RuleMissingDateRangeError
      }

      "FROM date is supplied but no TO date" in {
        val validationResult = ObligationParameterFormatValidation.validate(ObligationsRawData(validVRN, Some(validFromDate), None, Some("F")))
        validationResult.isEmpty shouldBe false
        validationResult.length shouldBe 1
        validationResult.head shouldBe InvalidToError
      }

      "TO date is supplied but no FROM date" in {
        val validationResult = ObligationParameterFormatValidation.validate(ObligationsRawData(validVRN, None, Some(validToDate), Some("F")))
        validationResult.isEmpty shouldBe false
        validationResult.length shouldBe 1
        validationResult.head shouldBe InvalidFromError
      }

    }
  }
}
