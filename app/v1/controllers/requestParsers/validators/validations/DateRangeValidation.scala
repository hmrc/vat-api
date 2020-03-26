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


import java.time.{Duration, LocalDate}

import v1.models.errors.{MtdError, RuleDateRangeInvalidError}

object DateRangeValidation {

  def validate(from: String, to: String): List[MtdError] = {
    val fmtFrom = LocalDate.parse(from, dateFormat)
    val fmtTo = LocalDate.parse(to, dateFormat)
    List(
      checkIfDateRangeIsTooLarge(fmtFrom, fmtTo)
    ).flatten
  }

  private def checkIfDateRangeIsTooLarge(from: LocalDate, to: LocalDate): List[MtdError] = {
    val start = from.atStartOfDay()
    val end = to.atStartOfDay()
    val daysRange = Duration.between(start, end.plusDays(1) /* add day to make inclusive */).toDays
    if(daysRange > maxDateRangeForObligations || daysRange < 1) List(RuleDateRangeInvalidError) else Nil
  }
}
