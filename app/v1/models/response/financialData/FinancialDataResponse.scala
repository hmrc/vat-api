/*
 * Copyright 2022 HM Revenue & Customs
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

import play.api.libs.json._
import v1.models.errors.MtdError
import play.api.libs.functional.syntax._

case class LineItemInterestDetails(interestKey: String,
                                   interestStartDate: String)

object LineItemInterestDetails {
  implicit val format: OFormat[LineItemInterestDetails] = Json.format[LineItemInterestDetails]
}

case class LineItemDetail(periodFromDate: String,
                          periodToDate: String,
                          periodKey: String,
                          netDueDate: String,
                          amount: BigDecimal,
                          lineItemInterestDetails: LineItemInterestDetails)

object LineItemDetail {
  implicit val format: OFormat[LineItemDetail] = Json.format[LineItemDetail]
}

case class DocumentDetail(postingDate: String,
                          issueDate: String,
                          documentTotalAmount: BigDecimal,
                          documentClearedAmount: BigDecimal,
                          documentOutstandingAmount: BigDecimal,
                          documentInterestTotal: BigDecimal,
                          lineItemDetails: Seq[LineItemDetail])

object DocumentDetail {
  implicit val reads: Reads[DocumentDetail] = (
    (JsPath \ "postingDate").read[String] and
      (JsPath \ "issueDate").read[String] and
      (JsPath \ "documentTotalAmount").read[BigDecimal] and
      (JsPath \ "documentClearedAmount").read[BigDecimal] and
      (JsPath \ "documentOutstandingAmount").read[BigDecimal] and
      (JsPath \ "documentInterestTotals" \ "interestTotalAmount").read[BigDecimal] and
      (JsPath \ "lineItemDetails").read[Seq[LineItemDetail]]
    )(DocumentDetail.apply _)

  implicit val writes: OWrites[DocumentDetail] = Json.writes[DocumentDetail]
}

case class Totalisation(totalOverdue: BigDecimal,
                        totalNotYetDue: BigDecimal,
                        totalBalance: BigDecimal,
                        totalCredit: BigDecimal,
                        totalCleared: BigDecimal)

object Totalisation {
  implicit val reads: Reads[Totalisation] = (
    (JsPath \ "targetedSearch" \ "totalOverdue").read[BigDecimal] and
      (JsPath \ "targetedSearch" \ "totalNotYetDue").read[BigDecimal] and
      (JsPath \ "targetedSearch" \ "totalBalance").read[BigDecimal] and
      (JsPath \ "targetedSearch" \ "totalCredit").read[BigDecimal] and
      (JsPath \ "targetedSearch" \ "totalCleared").read[BigDecimal]
    )(Totalisation.apply _)

  implicit val writes: OWrites[Totalisation] = Json.writes[Totalisation]
}

case class FinancialDataResponse(totalisation: Totalisation,
                                 documentDetails: Seq[DocumentDetail])

object FinancialDataResponse {
  implicit val format: OFormat[FinancialDataResponse] = Json.format[FinancialDataResponse]
}

case class FinancialDataErrors(
                            failures: List[FinancialDataError]
                          )

object FinancialDataErrors {
  implicit val format: OFormat[FinancialDataErrors] = Json.format[FinancialDataErrors]
}

case class FinancialDataError(
                         code: String,
                         reason: String
                       )

object FinancialDataError {
  implicit val format: OFormat[FinancialDataError] = Json.format[FinancialDataError]
}