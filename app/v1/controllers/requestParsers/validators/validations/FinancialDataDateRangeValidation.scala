/*
 * Copyright 2024 HM Revenue & Customs
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

import v1.models.errors.{FinancialDataInvalidDateRangeError, MtdError}

object FinancialDataDateRangeValidation {

  def validate(from: String, to: String): List[MtdError] = {
    val fmtFrom = LocalDate.parse(from, dateFormat)
    val fmtTo = LocalDate.parse(to, dateFormat)
    List(
      checkIfDateRangeIsIncorrect(fmtFrom, fmtTo)
    ).flatten
  }

  private def checkIfDateRangeIsIncorrect(from: LocalDate, to: LocalDate): List[MtdError] = {

    if(!from.isBefore(to) || from.plusYears(1).minusDays(1).isBefore(to)) {
      List(FinancialDataInvalidDateRangeError)
    } else Nil
  }
}
