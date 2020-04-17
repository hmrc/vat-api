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

import java.time.LocalDate

import support.UnitSpec
import v1.models.errors.{InvalidDateFromError, InvalidDateToError}

class LiabilityDateValidationSpec extends UnitSpec{
  "validate" should {
    "return an empty list" when {
      "passed a valid date" in {
        LiabilityDateValidation.validate("2019-02-02", InvalidDateToError) shouldBe List()
      }
      "passed a date equal to today's date" in {
        LiabilityDateValidation.validate(LocalDate.now().toString, InvalidDateToError) shouldBe List()
      }
    }
    "return a list containing an error" when {
      "passed a date with an invalid month" in {
        LiabilityDateValidation.validate("2019-13-02", InvalidDateToError) shouldBe List(InvalidDateToError)
      }
      "passed a date with an invalid day" in {
        LiabilityDateValidation.validate("2019-02-32", InvalidDateToError) shouldBe List(InvalidDateToError)
      }
      "passed a date with an invalid year" in {
        LiabilityDateValidation.validate("201-02-02", InvalidDateToError) shouldBe List(InvalidDateToError)
      }
      "passed a date with an invalid separator" in {
        LiabilityDateValidation.validate("2012.02-02", InvalidDateToError) shouldBe List(InvalidDateToError)
      }
      "passed a date written as text" in {
        LiabilityDateValidation.validate("2nd Feb 2012", InvalidDateToError) shouldBe List(InvalidDateToError)
      }
      "passed a date in the future" in {
        LiabilityDateValidation.validate(LocalDate.now().plusDays(1L).toString, InvalidDateToError) shouldBe List(InvalidDateToError)
      }

      "passed a date equal to the old vat minimum from date" in {
        LiabilityDateValidation.validate("2016-04-06", InvalidDateFromError) shouldBe List(InvalidDateFromError)
      }
      "passed a date before the old vat minimum from date" in {
        LiabilityDateValidation.validate("2016-04-05", InvalidDateFromError) shouldBe List(InvalidDateFromError)
      }
    }
  }
}
