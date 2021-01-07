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

package v1.models.response.viewReturn

import play.api.libs.functional.syntax._
import play.api.libs.json.{JsPath, Json, OWrites, Reads}

case class ViewReturnResponse(periodKey: String,
                              vatDueSales: BigDecimal,
                              vatDueAcquisitions: BigDecimal,
                              totalVatDue: BigDecimal,
                              vatReclaimedCurrPeriod: BigDecimal,
                              netVatDue: BigDecimal,
                              totalValueSalesExVAT: BigDecimal,
                              totalValuePurchasesExVAT: BigDecimal,
                              totalValueGoodsSuppliedExVAT: BigDecimal,
                              totalAcquisitionsExVAT: BigDecimal)

object ViewReturnResponse {
  implicit val writes: OWrites[ViewReturnResponse] = Json.writes[ViewReturnResponse]
  implicit val reads: Reads[ViewReturnResponse] = (
    (JsPath \ "periodKey").read[String] and
      (JsPath \ "vatDueSales").read[BigDecimal] and
      (JsPath \ "vatDueAcquisitions").read[BigDecimal] and
      (JsPath \ "vatDueTotal").read[BigDecimal] and
      (JsPath \ "vatReclaimedCurrPeriod"). read[BigDecimal] and
      (JsPath \ "vatDueNet").read[BigDecimal] and
      (JsPath \ "totalValueSalesExVAT").read[BigDecimal] and
      (JsPath \ "totalValuePurchasesExVAT").read[BigDecimal] and
      (JsPath \ "totalValueGoodsSuppliedExVAT").read[BigDecimal] and
      (JsPath \ "totalAllAcquisitionsExVAT").read[BigDecimal]
    )(ViewReturnResponse.apply _)
}
