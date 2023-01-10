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

package v1.controllers.requestParsers.validators.validations

import java.time.LocalDate

import v1.models.errors.{FinancialDataInvalidDateFromError, FinancialDataInvalidDateToError, MtdError}

object FinancialDataDateFormatValidation {

  private def parseDate(date: String): LocalDate = LocalDate.parse(date, dateFormat)
  private val minimumSupportedDate: String = "2016-04-06"

  def validate(date: String, error: MtdError): List[MtdError] =
    DateFormatValidation.validate(date, error) match {
      case NoValidationErrors => error match {
        case FinancialDataInvalidDateFromError => validateFrom(date)
        case FinancialDataInvalidDateToError => validateTo(date)
        case _ => NoValidationErrors
      }
      case _ => List(error)
    }

  private def validateFrom(fromDate: String): List[MtdError] = {
    if (parseDate(minimumSupportedDate).isAfter(parseDate(fromDate))) List(FinancialDataInvalidDateFromError)
    else NoValidationErrors
  }

  private def validateTo(toDate: String): List[MtdError] = {
    if (LocalDate.now().isBefore(parseDate(toDate))) List(FinancialDataInvalidDateToError)
    else NoValidationErrors
  }
}
