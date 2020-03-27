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

package v1.controllers.requestParsers.validators

import support.UnitSpec
import v1.models.errors.{FormatPeriodKeyError, VrnFormatError}
import v1.models.request.viewReturn.ViewRawData

class ViewReturnValidatorSpec extends UnitSpec {

  val validator = new ViewReturnValidator()

  private val validVrn = "123456789"
  private val invalidVrn = "thisIsNotAVrn"
  private val validPeriodKey = "AB12"
  private val invalidPeriodKey = "thisStringIsTooLongToBeAPeriodKey"

  "running a validation" should {
    "return no errors" when {
      "a valid request" in {
        validator.validate(ViewRawData(validVrn, validPeriodKey)) shouldBe Nil
      }
    }

    "return VrnFormatError error" when {
      "an invalid Vrn is supplied" in {
        validator.validate(ViewRawData(invalidVrn, validPeriodKey)) shouldBe List(VrnFormatError)
      }
    }

    "return PeriodKeyFormatError error" when {
      "an invalid Period Key is supplied" in {
        validator.validate(ViewRawData(validVrn, invalidPeriodKey)) shouldBe List(FormatPeriodKeyError)
      }
    }

    "return only VrnFormatError error" when {
      "an invalid Vrn and invalid Period Key are supplied" in {
        validator.validate(ViewRawData(invalidVrn, invalidPeriodKey)) shouldBe List(VrnFormatError)
      }
    }
  }
}
