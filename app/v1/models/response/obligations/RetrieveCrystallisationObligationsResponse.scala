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

import play.api.libs.json.{JsArray, JsResult, JsValue, Json, Reads}


case class ObligationDetails(status: String, inboundCorrespondenceFromDate: String, inboundCorrespondenceToDate: String, inboundCorrespondenceDateReceived: Option[String], inboundCorrespondenceDueDate: String, periodKey: String)

object ObligationDetails {
  implicit val reads: Reads[ObligationDetails] = Json.reads[ObligationDetails]
}

case class RetrieveCrystallisationObligationsResponse(obligationDetails: Seq[ObligationDetails])

object RetrieveCrystallisationObligationsResponse {
  implicit val reads: Reads[RetrieveCrystallisationObligationsResponse] =
    (json: JsValue) =>
      JsResult.applicativeJsResult.pure[RetrieveCrystallisationObligationsResponse](RetrieveCrystallisationObligationsResponse(
        (json \ "obligations" \\ "obligationDetails").foldLeft(List[ObligationDetails]())((_, v) => {
            v.asInstanceOf[JsArray].value.map(v => v.as[ObligationDetails]).toList
          })
      ))
}


