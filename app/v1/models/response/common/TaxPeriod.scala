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

package v1.models.response.common

import play.api.libs.functional.syntax._
import play.api.libs.json._

case class TaxPeriod(from: String, to: String)

object TaxPeriod {

  implicit val writes: OWrites[TaxPeriod] = Json.writes[TaxPeriod]

  private val readPeriod: Reads[TaxPeriod] = (
    (JsPath \ "taxPeriodFrom").read[String] and
      (JsPath \ "taxPeriodTo").read[String])(TaxPeriod.apply _)

  implicit val reads: Reads[Option[TaxPeriod]] = { json =>
    (json \ "taxPeriodFrom", json \ "taxPeriodTo") match {
      case (from, to) if from.isDefined && to.isDefined => json.validateOpt[TaxPeriod](readPeriod)
      case _ => JsResult.applicativeJsResult.pure(None)
    }
  }

}
