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

package v1.models.response.payments

import play.api.libs.functional.syntax._
import play.api.libs.json._

case class Payment(taxPeriod: Option[TaxPeriod],
                   `type`: String,
                   paymentItem: Option[Seq[PaymentItem]])

object Payment {

  implicit val writes: Writes[Payment] = Json.writes[Payment]

  implicit val reads: Reads[Payment] = (
    TaxPeriod.reads and
      (JsPath \ "chargeType").read[String] and
      (JsPath \ "items").readNullable[Seq[PaymentItem]]
    )(Payment.apply _)

}
