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
import v1.models.errors.{InvalidDateFromError, InvalidDateToError, RuleDateRangeInvalidError, VrnFormatError}
import v1.models.request.liability.LiabilityRawData

class RetrieveLiabilitiesValidatorSpec extends UnitSpec  {

  val validator = new LiabilitiesValidator()

  private val validVrn = "123456789"
  private val invalidVrn = "thisIsNotAVrn"
  private val validFrom = "2020-01-01"
  private val validTo =  "2020-12-31"

  "running a validation" should {
    "return no errors" when {
      "a valid request" in {
        validator.validate(LiabilityRawData(validVrn, Some(validFrom), Some(validTo))) shouldBe Nil
      }
    }

    "return VrnFormatError error" when {
      "an invalid Vrn is supplied" in {
        validator.validate(LiabilityRawData(invalidVrn, Some(validFrom), Some(validTo))) shouldBe List(VrnFormatError)
      }

      //maintain order of preference to match legacy validation
      "an invalid Vrn, and invalid dates are supplied" in {
        validator.validate(LiabilityRawData(invalidVrn, Some("invalidFromDate"), Some("invalidToDate"))) shouldBe List(VrnFormatError)
      }
    }

    "return only FromDateFormatError error" when {
      "an invalid from date format is supplied" in {
        validator.validate(LiabilityRawData(validVrn, Some("12-31-2020"), Some(validTo))) shouldBe List(InvalidDateFromError)
      }

      //maintain order of preference to match legacy validation
      "an invalid from date and invalid to date are supplied" in {
        validator.validate(LiabilityRawData(validVrn, Some("12-31-2020"), Some("invalidDateTo"))) shouldBe List(InvalidDateFromError)
      }
    }

    "return only ToDateFormatError error" when {
      "an invalid to date format is supplied" in {
        validator.validate(LiabilityRawData(validVrn, Some(validFrom), Some("12-31-2020"))) shouldBe List(InvalidDateToError)
      }
    }

    "return RuleDateRangeError error" when {
      "invalid date range is supplied" in {
        validator.validate(LiabilityRawData(validVrn, Some("2018-01-01"), Some("2019-01-01"))) shouldBe List(RuleDateRangeInvalidError)
      }
    }
  }
}
