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

package v1.controllers.validators.validations

import support.UnitSpec
import v1.controllers.requestParsers.validators.validations.DateRangeValidation
import v1.models.errors.{ RuleDateRangeInvalidError}

class DateRangeValidationSpec extends UnitSpec {

  val from2020 = "2020-01-01"
  val to2021 = "2021-01-01"
  val from2022 = "2022-01-01" // 732 days from validFrom
  val from2018 = "2018-04-05"
  val to2019 = "2019-02-20"
  val to2022Plus1Day = "2022-01-02" // 733 days from validFrom

  "validate" should {
    "return an empty list" when {

      "passed valid from and to dates" in {
        DateRangeValidation.validate(from2020, to2021) shouldBe List()
      }
      "passed the maximum date range" in {
        DateRangeValidation.validate(from2020, from2022) shouldBe List()
      }

    }
    "return a list containing an error" when {

      "passed a from date which is the same as the to date" in {
        DateRangeValidation.validate(to2021, to2021) shouldBe List(RuleDateRangeInvalidError)
      }
      "passed an invalid date range" in {
        DateRangeValidation.validate(from2020, to2022Plus1Day) shouldBe List(RuleDateRangeInvalidError)
      }

    }
  }

}
