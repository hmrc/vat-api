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

package v1.models.nrs.response

import play.api.libs.json.{JsPath, Reads}

case class NrsResponse(nrSubmissionId: String, cadesTSignature: String, timestamp: String)

object NrsResponse {

  val deprecatedString: String = "This has been deprecated - DO NOT USE"
  val empty: NrsResponse = NrsResponse("", deprecatedString, "")

  implicit val reads: Reads[NrsResponse] = (JsPath \ "nrSubmissionId").read[String].map(NrsResponse(_, deprecatedString, ""))
}
