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

package v1.models.response.liability

import play.api.libs.functional.syntax._
import play.api.libs.json.{JsPath, JsValue, Json, OWrites, Reads}

case class Liability(taxPeriod: Option[TaxPeriod],
                     `type`: String,
                     originalAmount: BigDecimal,
                     outstandingAmount: Option[BigDecimal],
                     due: Option[String])

object Liability {

  implicit val writes: OWrites[Liability] = Json.writes[Liability]

  private val dueDateReads = {
    ((JsPath \\ "items").readNullable[Seq[JsValue]]).flatMap {
      case Some(_) => (JsPath \\ "items" \\ "dueDate").readNullable[String]
      case None => (JsPath \\ "items").readNullable[String]
    }
  }

  implicit val reads: Reads[Liability] = (
    TaxPeriod.reads and
      (JsPath \ "chargeType").read[String] and
      (JsPath \ "originalAmount").read[BigDecimal] and
      (JsPath \ "outstandingAmount").readNullable[BigDecimal] and
      dueDateReads
    )(Liability.apply _)
}
