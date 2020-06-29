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

package v1.models.response.obligations

import play.api.libs.functional.syntax._
import play.api.libs.json.{JsPath, Json, OWrites, Reads}

case class Obligation(periodKey: String,
                      start: String,
                      end: String,
                      due: String,
                      status: String,
                      received: Option[String])

object Obligation {

  implicit val writes: OWrites[Obligation] = Json.writes[Obligation]

  implicit val reads: Reads[Obligation] = (
    (JsPath \ "periodKey").read[String] and
      (JsPath \ "inboundCorrespondenceFromDate").read[String] and
      (JsPath \ "inboundCorrespondenceToDate").read[String] and
      (JsPath \ "inboundCorrespondenceDueDate").read[String] and
      (JsPath \ "status").read[String] and
      (JsPath \ "inboundCorrespondenceDateReceived").readNullable[String]
    )(Obligation.apply _)
}
