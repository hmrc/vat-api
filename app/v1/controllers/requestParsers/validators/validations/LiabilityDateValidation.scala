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

import v1.models.errors.{InvalidDateToError, MtdError}

import scala.util.{Success, Try}

object LiabilityDateValidation {

  def validate(toDate: String, error: MtdError): List[MtdError] = Try {
    LocalDate.parse(toDate, dateFormat)
  } match {
    case Success(date) if extraDateValidation(error,date) => NoValidationErrors
    case _ => List(error)
  }

  private def extraDateValidation(error: MtdError, date: LocalDate) = {
    error match {
      case InvalidDateToError => !date.isAfter(LocalDate.now())
      case invalidDateFromError@_ => date.isAfter(LocalDate.parse("2016-04-06"))
    }
  }
}
