/*
 * Copyright 2025 HM Revenue & Customs
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

package v1.models.response.financialData

import play.api.libs.functional.syntax._
import play.api.libs.json._

sealed trait FinancialDataHIPErrorResponse

case class TechnicalError(code: String, message: String, logId: String) extends FinancialDataHIPErrorResponse
case class BusinessError(processingDate: String, code: String, text: String) extends FinancialDataHIPErrorResponse
case class BusinessErrors(errors: List[BusinessError]) extends FinancialDataHIPErrorResponse

object TechnicalError {
  implicit val reads: Reads[TechnicalError] = (
    (__ \"error" \ "code").read[String] and
      (__ \"error" \ "message").read[String] and
      (__ \"error" \ "logId").read[String]
  )(TechnicalError.apply _)
}

object BusinessError {
  implicit val reads: Reads[BusinessError] = Json.reads[BusinessError]
}

object BusinessErrors {
  implicit val reads: Reads[BusinessErrors] = (__ \ "errors").read[List[BusinessError]].map(BusinessErrors.apply)
}

object FinancialDataHIPErrorResponse {
  implicit val reads: Reads[FinancialDataHIPErrorResponse] =
    TechnicalError.reads.widen[FinancialDataHIPErrorResponse]
      .orElse(BusinessErrors.reads.widen[FinancialDataHIPErrorResponse])
}