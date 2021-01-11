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

package v1.controllers.requestParsers.validators

import java.time.LocalDate
import java.time.format.DateTimeFormatter

import support.UnitSpec
import v1.models.errors.{FinancialDataInvalidDateFromError, FinancialDataInvalidDateRangeError, FinancialDataInvalidDateToError, VrnFormatError}
import v1.models.request.payments.PaymentsRawData

class PaymentsValidatorSpec extends UnitSpec {

  val validator = new PaymentsValidator()

  private val validVrn = "123456789"
  private val invalidVrn = "thisIsNotAVrn"
  private val validFrom = "2019-01-01"
  private val validTo =  "2019-12-31"

  private val dateTimeFormatter = DateTimeFormatter ofPattern "yyyy-MM-dd"

  "running a validation" should {
    "return no errors" when {
      "a valid request" in {
        validator.validate(PaymentsRawData(validVrn, Some(validFrom), Some(validTo))) shouldBe Nil
      }

      "a 'to' date is today" in {
        val todayDate = LocalDate.now().format(dateTimeFormatter)
        val yesterdayDate = LocalDate.now().minusDays(1).format(dateTimeFormatter)
        validator.validate(PaymentsRawData(validVrn, Some(yesterdayDate), Some(todayDate))) shouldBe List()
      }

      "a 'from' date is on the minimum supported date" in {
        validator.validate(PaymentsRawData(validVrn, Some("2016-04-06"), Some("2016-04-07"))) shouldBe List()
      }
    }

    "return VrnFormatError error" when {
      "an invalid Vrn is supplied" in {
        validator.validate(PaymentsRawData(invalidVrn, Some(validFrom), Some(validTo))) shouldBe List(VrnFormatError)
      }

      //maintain order of preference to match legacy validation
      "an invalid Vrn, and invalid dates are supplied" in {
        validator.validate(PaymentsRawData(invalidVrn, Some("invalidFromDate"), Some("invalidToDate"))) shouldBe List(VrnFormatError)
      }
    }

    "return only FinancialDataInvalidDateFromError error" when {
      "an invalid from date format is supplied" in {
        validator.validate(PaymentsRawData(validVrn, Some("12-31-2020"), Some(validTo))) shouldBe List(FinancialDataInvalidDateFromError)
      }

      //maintain order of preference to match legacy validation
      "an invalid from date and invalid to date are supplied" in {
        validator.validate(PaymentsRawData(validVrn, Some("12-31-2020"), Some("invalidDateTo"))) shouldBe List(FinancialDataInvalidDateFromError)
      }

      "a 'from' date is before the minimum supported date" in {
        validator.validate(PaymentsRawData(validVrn, Some("2016-04-05"), Some("2019-01-01"))) shouldBe List(FinancialDataInvalidDateFromError)
      }
    }

    "return only FinancialDataInvalidDateToError error" when {
      "an invalid to date format is supplied" in {
        validator.validate(PaymentsRawData(validVrn, Some(validFrom), Some("12-31-2020"))) shouldBe List(FinancialDataInvalidDateToError)
      }

      "a 'to' date is in the future" in {
        val tomorrowDate = LocalDate.now().plusDays(1).format(dateTimeFormatter)
        validator.validate(PaymentsRawData(validVrn, Some("2018-01-01"), Some(tomorrowDate))) shouldBe List(FinancialDataInvalidDateToError)
      }
    }

    "return RuleDateRangeError error" when {
      "invalid date range is supplied" in {
        validator.validate(PaymentsRawData(validVrn, Some("2018-01-01"), Some("2019-01-01"))) shouldBe List(FinancialDataInvalidDateRangeError)
      }
    }
  }
}
