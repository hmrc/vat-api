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

import play.api.libs.functional.syntax._
import play.api.libs.json._

case class SubmitRequestBody(periodKey: Option[String],
                             vatDueSales: Option[BigDecimal],
                             vatDueAcquisitions: Option[BigDecimal],
                             vatDueTotal: Option[BigDecimal],
                             vatReclaimedCurrPeriod: Option[BigDecimal],
                             vatDueNet: Option[BigDecimal],
                             totalValueSalesExVAT: Option[BigDecimal],
                             totalValuePurchasesExVAT: Option[BigDecimal],
                             totalValueGoodsSuppliedExVAT: Option[BigDecimal],
                             totalAllAcquisitionsExVAT: Option[BigDecimal],
                             finalised: Option[Boolean],
                             receivedAt: Option[String],
                             agentReference: Option[String]) {
}


object SubmitRequestBody {

  implicit val reads: Reads[SubmitRequestBody] = (
    (JsPath \ "periodKey").readNullable[String] and
      (JsPath \ "vatDueSales").readNullable[BigDecimal] and
      (JsPath \ "vatDueAcquisitions").readNullable[BigDecimal] and
      (JsPath \ "totalVatDue").readNullable[BigDecimal] and
      (JsPath \ "vatReclaimedCurrPeriod").readNullable[BigDecimal] and
      (JsPath \ "netVatDue").readNullable[BigDecimal] and
      (JsPath \ "totalValueSalesExVAT").readNullable[BigDecimal] and
      (JsPath \ "totalValuePurchasesExVAT").readNullable[BigDecimal] and
      (JsPath \ "totalValueGoodsSuppliedExVAT").readNullable[BigDecimal] and
      (JsPath \ "totalAcquisitionsExVAT").readNullable[BigDecimal] and
      (JsPath \ "finalised").readNullable[Boolean] and
      Reads.pure(None) and
      Reads.pure(None)
    ) (SubmitRequestBody.apply _)

  implicit val writes: OWrites[SubmitRequestBody] = new OWrites[SubmitRequestBody] {
    def writes(response: SubmitRequestBody): JsObject = {

      response.periodKey.fold(Json.obj())(value => Json.obj("periodKey" -> value)) ++
        response.vatDueSales.fold(Json.obj())(value => Json.obj("vatDueSales" -> value)) ++
        response.vatDueAcquisitions.fold(Json.obj())(value => Json.obj("vatDueAcquisitions" -> value)) ++
        response.vatDueTotal.fold(Json.obj())(value => Json.obj("vatDueTotal" -> value)) ++
        response.vatReclaimedCurrPeriod.fold(Json.obj())(value => Json.obj("vatReclaimedCurrPeriod" -> value)) ++
        response.vatDueNet.fold(Json.obj())(value => Json.obj("vatDueNet" -> value)) ++
        response.totalValueSalesExVAT.fold(Json.obj())(value => Json.obj("totalValueSalesExVAT" -> value)) ++
        response.totalValuePurchasesExVAT.fold(Json.obj())(value => Json.obj("totalValuePurchasesExVAT" -> value)) ++
        response.totalValueGoodsSuppliedExVAT.fold(Json.obj())(value => Json.obj("totalValueGoodsSuppliedExVAT" -> value)) ++
        response.totalAllAcquisitionsExVAT.fold(Json.obj())(value => Json.obj("totalAllAcquisitionsExVAT" -> value)) ++
        response.agentReference.fold(Json.obj())(value => Json.obj("agentReference" -> value)) ++
        response.receivedAt.fold(Json.obj())(value => Json.obj("receivedAt" -> value))
    }
  }
}