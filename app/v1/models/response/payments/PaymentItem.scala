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

package v1.models.response.payments

import play.api.libs.functional.syntax._
import play.api.libs.json.{JsNull, JsPath, Json, OWrites, Reads}

case class PaymentItem(amount: Option[BigDecimal],
                       received: Option[String],
                       paymentLot: Option[String],
                       paymentLotItem: Option[String])

object PaymentItem {
  val empty: PaymentItem = PaymentItem(None, None, None, None)
  implicit val writes: OWrites[PaymentItem] = OWrites[PaymentItem] { paymentItem =>
    Json.obj(
      "amount" -> paymentItem.amount,
      "received" -> paymentItem.received
    ).fields.filterNot(_._2 == JsNull).foldLeft(Json.obj())(_ + _)
  }
  implicit val reads: Reads[PaymentItem] = (
    (JsPath \ "paymentAmount").readNullable[BigDecimal] and
      (JsPath \ "clearingDate").readNullable[String] and
      (JsPath \ "paymentLot").readNullable[String] and
      (JsPath \ "paymentLotItem").readNullable[String]
    ) (PaymentItem.apply _)
}
