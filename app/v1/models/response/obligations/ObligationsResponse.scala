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

import play.api.libs.json.{JsPath, Json, OWrites, Reads}

case class ObligationsResponse(obligations: Seq[Obligation])

object ObligationsResponse {

  //bound case class to allow us to read from multiple lists of obligation details to merge together later
  case class Detail(obligations: Seq[Obligation])

  object Detail {
    implicit val reads: Reads[Detail] = (JsPath \ "obligationDetails").read[Seq[Obligation]].map(Detail(_))
  }

  implicit val writes: OWrites[ObligationsResponse] = Json.writes[ObligationsResponse]

  implicit val reads: Reads[ObligationsResponse] =
    (JsPath \ "obligations").read[Seq[Detail]].map(det => ObligationsResponse(det.flatMap(_.obligations)))
}