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
import v1.models.errors.InvalidDateFromError

class DateToValidationSpec extends UnitSpec{
  "validate" should {
    "return an empty list" when {
      "passed a valid date" in {
        DateToValidation.validate("2019-02-02", InvalidDateFromError) shouldBe List()
      }
      "passed a date equal to today's date" in {
        DateToValidation.validate(LocalDate.now().toString, InvalidDateFromError) shouldBe List()
      }
    }
    "return a list containing an error" when {
      "passed a date with an invalid month" in {
        DateToValidation.validate("2019-13-02", InvalidDateFromError) shouldBe List(InvalidDateFromError)
      }
      "passed a date with an invalid day" in {
        DateToValidation.validate("2019-02-32", InvalidDateFromError) shouldBe List(InvalidDateFromError)
      }
      "passed a date with an invalid year" in {
        DateToValidation.validate("201-02-02", InvalidDateFromError) shouldBe List(InvalidDateFromError)
      }
      "passed a date with an invalid separator" in {
        DateToValidation.validate("2012.02-02", InvalidDateFromError) shouldBe List(InvalidDateFromError)
      }
      "passed a date written as text" in {
        DateToValidation.validate("2nd Feb 2012", InvalidDateFromError) shouldBe List(InvalidDateFromError)
      }
      "passed a date in the future" in {
        DateToValidation.validate(LocalDate.now().plusDays(1L).toString, InvalidDateFromError) shouldBe List(InvalidDateFromError)
      }
    }
  }
}
