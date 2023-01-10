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
import v1.models.errors.InvalidMonetaryValueError

class DecimalMonetaryValueFormatValidationSpec extends UnitSpec {

  "validate" should {
    "return an empty list" when {
      "passed a valid field" in {
        val result = DecimalMonetaryValueFormatValidation.validate(Some(BigDecimal(9999999999999.99)), "vatDueSales", BigDecimal(-9999999999999.99), BigDecimal(9999999999999.99))
        result shouldBe List()
      }
      "a field contains a single decimal places" in {
        val result = DecimalMonetaryValueFormatValidation.validate(Some(BigDecimal(9999999999999.9)), "vatDueSales", BigDecimal(-9999999999999.99), BigDecimal(9999999999999.99))
        result shouldBe List()
      }
    }

    "return a list of errors" when {
      "a field contains too many decimal places" in {
        val result = DecimalMonetaryValueFormatValidation.validate(Some(BigDecimal(9999999999999.999)), "vatDueSales", BigDecimal(-9999999999999.99), BigDecimal(9999999999999.99))
        result shouldBe List(InvalidMonetaryValueError.withFieldName("vatDueSales", BigDecimal(-9999999999999.99), BigDecimal(9999999999999.99)))
      }
    }
  }
}
