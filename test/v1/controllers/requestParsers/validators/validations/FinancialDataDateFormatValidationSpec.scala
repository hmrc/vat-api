/*
 * Copyright 2021 HM Revenue & Customs
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
import v1.models.errors.{FinancialDataInvalidDateFromError, FinancialDataInvalidDateToError}

class FinancialDataDateFormatValidationSpec extends UnitSpec{
  "validate" should {
    "return an empty list" when {
      "passed a valid date" in {
        FinancialDataDateFormatValidation.validate("2019-02-02", FinancialDataInvalidDateFromError) shouldBe List()
      }
      "a 'from' date is on the minimum supported date" in {
        FinancialDataDateFormatValidation.validate("2016-04-06", FinancialDataInvalidDateFromError) shouldBe List()
      }
      "a 'to' date is today" in {
        lazy val dateTomorrow = LocalDate.now().format(dateFormat)
        FinancialDataDateFormatValidation.validate(dateTomorrow, FinancialDataInvalidDateFromError) shouldBe List()
      }
    }

    "return a list containing an error" when {
      "passed a date with an invalid month" in {
        FinancialDataDateFormatValidation.validate("2019-13-02", FinancialDataInvalidDateFromError)shouldBe
          List(FinancialDataInvalidDateFromError)
      }
      "passed a date with an invalid day" in {
        FinancialDataDateFormatValidation.validate("2019-02-32", FinancialDataInvalidDateFromError) shouldBe
          List(FinancialDataInvalidDateFromError)
      }
      "passed a date with an invalid year" in {
        FinancialDataDateFormatValidation.validate("201-02-02", FinancialDataInvalidDateFromError) shouldBe
          List(FinancialDataInvalidDateFromError)
      }
      "passed a date with an invalid separator" in {
        FinancialDataDateFormatValidation.validate("2012.02-02", FinancialDataInvalidDateFromError) shouldBe
          List(FinancialDataInvalidDateFromError)
      }
      "passed a date written as text" in {
        FinancialDataDateFormatValidation.validate("2nd Feb 2012", FinancialDataInvalidDateFromError) shouldBe
          List(FinancialDataInvalidDateFromError)
      }
      "a 'from' date is before the minimum supported date" in {
        FinancialDataDateFormatValidation.validate("2016-04-05", FinancialDataInvalidDateFromError) shouldBe
          List(FinancialDataInvalidDateFromError)
      }
      "a 'to' date is in the future" in {
        val dateTomorrow: String = LocalDate.now().plusDays(1).format(dateFormat)
        FinancialDataDateFormatValidation.validate(dateTomorrow, FinancialDataInvalidDateToError) shouldBe
          List(FinancialDataInvalidDateToError)
      }
    }
  }
}
