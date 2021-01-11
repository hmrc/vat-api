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

import v1.controllers.requestParsers.validators.validations.{FinancialDataDateFormatValidation, FinancialDataDateRangeValidation, VrnValidation}
import v1.models.errors.{FinancialDataInvalidDateFromError, FinancialDataInvalidDateToError, MtdError}
import v1.models.request.liabilities.LiabilitiesRawData

class LiabilitiesValidator extends Validator[LiabilitiesRawData]  {

  private val validationSet = List(vrnFormatValidation, fromDateFormatValidation, toDateFormatValidation, dateRangeValidation)

  private def vrnFormatValidation: LiabilitiesRawData => List[List[MtdError]] = (data: LiabilitiesRawData) => {
    List(
      VrnValidation.validate(data.vrn)
    )
  }

  private def fromDateFormatValidation: LiabilitiesRawData => List[List[MtdError]] = (data: LiabilitiesRawData) => {
    List(
      data.from match {
        case None => List(FinancialDataInvalidDateFromError)
        case Some(from) => FinancialDataDateFormatValidation.validate(from, FinancialDataInvalidDateFromError)
      }
    )
  }

  private def toDateFormatValidation: LiabilitiesRawData => List[List[MtdError]] = (data: LiabilitiesRawData) => {
    List(
      data.to match {
        case None => List(FinancialDataInvalidDateToError)
        case Some(to) => FinancialDataDateFormatValidation.validate(to, FinancialDataInvalidDateToError)
      }
    )
  }

  private def dateRangeValidation: LiabilitiesRawData => List[List[MtdError]] = (data: LiabilitiesRawData) => {
    List(
      FinancialDataDateRangeValidation.validate(data.from.get, data.to.get)
    )
  }

  override def validate(data: LiabilitiesRawData): List[MtdError] = {
    run(validationSet, data).distinct
  }
}
