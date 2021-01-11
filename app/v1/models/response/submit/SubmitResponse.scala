/*
 * Copyright 2021 HM Revenue & Customs
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

package v1.models.response.submit

import org.joda.time.DateTime
import play.api.libs.functional.syntax._
import play.api.libs.json._
import utils.DateUtils._

case class SubmitResponse(processingDate: DateTime,
                          formBundleNumber: String,
                          paymentIndicator: Option[String],
                          chargeRefNumber: Option[String])

object SubmitResponse {

  implicit val reads: Reads[SubmitResponse] = (
    ((__ \ "processingDate").read[DateTime](dateTimeFormat) or
      (__ \ "processingDate").read[DateTime](defaultDateTimeFormat)) and
      (__ \ "formBundleNumber").read[String] and
      (__ \ "paymentIndicator").readNullable[String] and
      (__ \ "chargeRefNumber").readNullable[String]
    ) (SubmitResponse.apply _)

  implicit val dateFormats: Format[DateTime] = dateTimeFormat
  implicit val writes: Writes[SubmitResponse] = Json.writes[SubmitResponse]

}
