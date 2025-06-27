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

package v1.models.request.penalties

import play.api.libs.json.{Json, OWrites}

case class FinancialRequestHIP(
                                taxRegime: String,
                                taxpayerInformation: TaxpayerInformation,
                                targetedSearch: TargetedSearch,
                                selectionCriteria: SelectionCriteria,
                                dataEnrichment: DataEnrichment
                              )

case class TaxpayerInformation(idType: String, idNumber: String)
case class TargetedSearch(searchType: Option[String], searchItem: Option[String])
case class SelectionCriteria(
                              dateRange: Option[DateRange],
                              includeClearedItems: Option[Boolean],
                              includeStatisticalItems: Option[Boolean],
                              includePaymentOnAccount: Option[Boolean]
                            )
case class DateRange(fromDate: Option[String], toDate: Option[String])
case class DataEnrichment(
                           addRegimeTotalisation: Option[Boolean],
                           addLockInformation: Option[Boolean],
                           addPenaltyDetails: Option[Boolean],
                           addPostedInterestDetails: Option[Boolean],
                           addAccruingInterestDetails: Option[Boolean]
                         )

object FinancialRequestHIP {
  implicit val taxpayerInformationWrites: OWrites[TaxpayerInformation] = Json.writes[TaxpayerInformation]
  implicit val targetedSearchWrites: OWrites[TargetedSearch] = Json.writes[TargetedSearch]
  implicit val dateRangeWrites: OWrites[DateRange] = Json.writes[DateRange]
  implicit val selectionCriteriaWrites: OWrites[SelectionCriteria] = Json.writes[SelectionCriteria]
  implicit val dataEnrichmentWrites: OWrites[DataEnrichment] = Json.writes[DataEnrichment]

  implicit val writes: OWrites[FinancialRequestHIP] = Json.writes[FinancialRequestHIP]
}