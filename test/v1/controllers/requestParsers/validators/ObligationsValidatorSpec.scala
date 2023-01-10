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

import support.UnitSpec
import v1.models.errors.{InvalidFromError, InvalidStatusError, InvalidToError, RuleDateRangeInvalidError, VrnFormatError}
import v1.models.request.obligations.ObligationsRawData

class ObligationsValidatorSpec extends  UnitSpec{
  val validator = new ObligationsValidator()

  private val validVrn = "123456789"
  private val validFromDate = "2017-01-01"
  private val validToDate = "2017-03-31"

  private val invalidVrn = "NotAVrn"
  private val invalidFrom = "2017-34-45"
  private val invalidTo = "2017-14-31"

  "running a validation" should {
    "return no errors" when {
      "a valid request with all parameters (Status: F)" in {
        validator.validate(ObligationsRawData(validVrn, Some(validFromDate), Some(validToDate), Some("F"))) shouldBe Nil
      }

      "a valid request with omitted parameters (Status: O)" in {
        validator.validate(ObligationsRawData(validVrn, None, None, Some("O"))) shouldBe Nil
      }
    }

    "return an error" when {
      "invalid VRN is supplied" in {
        validator.validate(ObligationsRawData(invalidVrn, Some(validFromDate), Some(validToDate), Some("F"))) shouldBe List(VrnFormatError)
      }

      "invalid from and to dates are supplied, returns InvalidFromError" in {
        validator.validate(ObligationsRawData(validVrn, Some(invalidFrom), Some(validToDate), Some("F"))) shouldBe List(InvalidFromError)
      }

      "invalid from date is supplied" in {
        validator.validate(ObligationsRawData(validVrn, Some(invalidFrom), Some(validToDate), Some("F"))) shouldBe List(InvalidFromError)
      }

      "invalid to date is supplied" in {
        validator.validate(ObligationsRawData(validVrn, Some(validFromDate), Some(invalidTo), Some("F"))) shouldBe List(InvalidToError)
      }

      "no parameters are provided" in {
        validator.validate(ObligationsRawData("", None, None, None)) shouldBe List(VrnFormatError)
      }

      "invalid status is provided" in {
        validator.validate(ObligationsRawData(validVrn, Some(validFromDate), Some(validToDate), Some("NotAStatus"))) shouldBe List(InvalidStatusError)
      }

      "from' date after 'to' date" in {
        validator.validate(ObligationsRawData(validVrn,Some("2018-01-01"), Some("2017-01-01"), Some("F"))) shouldBe List(RuleDateRangeInvalidError)
      }
    }
  }

}
