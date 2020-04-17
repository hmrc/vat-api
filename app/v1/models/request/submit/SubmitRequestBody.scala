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

package v1.models.request.submit

import play.api.libs.json.{Json, OWrites, Reads}

case class SubmitRequestBody(periodKey: String,
                             vatDueSales: BigDecimal,
                             vatDueAcquisitions: BigDecimal,
                             totalVatDue: BigDecimal,
                             vatReclaimedCurrPeriod: BigDecimal,
                             netVatDue: BigDecimal,
                             totalValueSalesExVAT: BigDecimal,
                             totalValuePurchasesExVAT: BigDecimal,
                             totalValueGoodsSuppliedExVAT: BigDecimal,
                             totalAcquisitionsExVAT: BigDecimal,
                             finalised: Boolean)

object SubmitRequestBody {

  implicit val reads: Reads[SubmitRequestBody] = Json.reads[SubmitRequestBody]
  implicit val writes: OWrites[SubmitRequestBody] = Json.writes[SubmitRequestBody]

}