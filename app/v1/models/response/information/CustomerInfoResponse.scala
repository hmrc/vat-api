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

package v1.models.response.information

import play.api.libs.functional.syntax._
import play.api.libs.json._

case class CustomerInfoResponse(
                                 customerDetails: Option[CustomerDetails],
                                 flatRateScheme: Option[FlatRateScheme],
                               )

object CustomerInfoResponse {
  implicit val reads: Reads[CustomerInfoResponse] = (
    (__ \ "approvedInformation" \ "customerDetails").readNullable[CustomerDetails] and
      (__ \ "approvedInformation" \ "flatRateScheme").readNullable[FlatRateScheme]
    )(CustomerInfoResponse.apply _)

  implicit val writes: Writes[CustomerInfoResponse] = new Writes[CustomerInfoResponse] {
    def writes(cir: CustomerInfoResponse): JsValue = {
      val json = Json.obj(
        "customerDetails" -> cir.customerDetails.filterNot {
          case CustomerDetails(None) => true
          case _ => false
        },
        "flatRateScheme" -> cir.flatRateScheme.filterNot {
          case FlatRateScheme(None, None) => true
          case _ => false
        }
      ).fields.filterNot(_._2 == JsNull)
      JsObject(json)
    }
  }
}

case class FlatRateScheme(frsCategory: Option[String], startDate: Option[String])

object FlatRateScheme {
  implicit val reads: Reads[FlatRateScheme] = (
    (__ \ "FRSCategory").readNullable[String] and
      (__ \ "startDate").readNullable[String]
    )(FlatRateScheme.apply _)

  implicit val writes: Writes[FlatRateScheme] = new Writes[FlatRateScheme] {
    def writes(frs: FlatRateScheme): JsValue = {
      val json = Json.obj(
        "frsCategory" -> frs.frsCategory,
        "startDate" -> frs.startDate
      ).fields.filterNot(_._2 == JsNull)

      if (json.isEmpty) JsNull else JsObject(json)
    }
  }
}

case class CustomerDetails(effectiveRegistrationDate: Option[String])

object CustomerDetails {
  implicit val reads: Reads[CustomerDetails] = ((__ \ "effectiveRegistrationDate").readNullable[String].map(CustomerDetails.apply))
  implicit val writes: Writes[CustomerDetails] = Json.writes[CustomerDetails]
}

case class CustomerInfoDataError(code: String, reason: String)

object CustomerInfoDataError {
  implicit val format: OFormat[CustomerInfoDataError] = Json.format[CustomerInfoDataError]
}

