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

package v1.models.response.liabilities

import play.api.libs.functional.syntax._
import play.api.libs.json.{JsPath, Json, OWrites, Reads}
import utils.NestedJsonReads
import v1.models.response.common.TaxPeriod

case class Liability(taxPeriod: Option[TaxPeriod],
                     `type`: String,
                     originalAmount: BigDecimal,
                     outstandingAmount: Option[BigDecimal],
                     due: Option[String])

object Liability extends NestedJsonReads {

  implicit val writes: OWrites[Liability] = Json.writes[Liability]

  implicit val reads: Reads[Liability] = (
    TaxPeriod.reads and
      (JsPath \ "chargeType").read[String] and
      (JsPath \ "originalAmount").read[BigDecimal] and
      (JsPath \ "outstandingAmount").readNullable[BigDecimal] and
      (JsPath \ "items" \\ "dueDate").readNestedNullable[String]
    )(Liability.apply _)
}
