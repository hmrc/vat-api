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
import v1.models.errors.InvalidMonetaryValueError

class MonetaryValueRangeValidationSpec extends UnitSpec {

  val minRegularValue = BigDecimal(-9999999999999.99)
  val maxRegularValue = BigDecimal(9999999999999.99)
  val minNonDecimalValue = BigDecimal(-9999999999999.99)
  val maxNonDecimalValue = BigDecimal(9999999999999.99)

  "validate" should {
    "return an empty list" when {
      "passed a decimal valid field" in {

        val result = DecimalMonetaryValueRangeValidation.validate(Some(BigDecimal(9999999999999.99)), "vatDueSales",  minRegularValue, maxRegularValue)
        result shouldBe List()
      }

      "passed a valid non-decimal field" in {

        val result = DecimalMonetaryValueRangeValidation.validate(Some(BigDecimal(9999999999999.99)), "vatDueSales",  minRegularValue, maxRegularValue)
        result shouldBe List()
      }
    }

    "return a list of errors" when {
      "the field exceeds the maximum value" in {

        val result = DecimalMonetaryValueRangeValidation.validate(Some(BigDecimal(10000000000000.00)), "vatDueSales",  minRegularValue, maxRegularValue)
        result shouldBe List(InvalidMonetaryValueError.withFieldName("vatDueSales", minRegularValue, maxRegularValue))
      }

      "the field is below the minimum value" in {

        val result = DecimalMonetaryValueRangeValidation.validate(Some(BigDecimal(-10000000000000.00)), "vatDueSales",  minRegularValue, maxRegularValue)
        result shouldBe List(InvalidMonetaryValueError.withFieldName("vatDueSales", minRegularValue, maxRegularValue))
      }
    }
  }
}