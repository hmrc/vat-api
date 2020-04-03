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

import v1.models.errors.MtdError

import scala.util.{Failure, Success, Try}

object DateFormatValidation {
  def validate(date: String, error: MtdError): List[MtdError] = Try {
    LocalDate.parse(date, dateFormat)
  } match {
    case Success(_) => NoValidationErrors
    case Failure(_) => List(error)
  }
}
