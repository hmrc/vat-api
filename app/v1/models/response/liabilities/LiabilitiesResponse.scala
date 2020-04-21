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

import java.time.LocalDate

import play.api.libs.json._
import v1.models.response.common.TaxPeriod

case class LiabilitiesResponse(liabilities: Seq[Liability])

object LiabilitiesResponse {

  implicit val writes: OWrites[LiabilitiesResponse] = Json.writes[LiabilitiesResponse]

  //retrieve all transactions, filter out any particular payments, then return a model only if there's data
  implicit def reads(implicit to: String): Reads[LiabilitiesResponse] = {
    (JsPath \ "financialTransactions").read[Seq[JsValue]]
      .map(_.filter(json => {
        json.validate[ChargeTypeWrapper].isSuccess &&
          json.as[ChargeTypeWrapper].chargeType.toLowerCase != "hybrid payments" &&
          json.as[ChargeTypeWrapper].chargeType.toLowerCase != "payment on account"
        }
      ).map(_.as[Liability]))
      .map { liabilities =>
        liabilities.filter { liability =>
          paymentCheck(liability) && dateCheck(liability.taxPeriod, to)
        }
      }
    }.map(LiabilitiesResponse(_))

  //filter particular payments
  private def paymentCheck(liability: Liability): Boolean = {
    val liabilityType = liability.`type`.toLowerCase
    liabilityType != "payment on account" && liabilityType != "hybrid payments"
  }

  //filter the payments that have response to date beyond the request to date
  private def dateCheck(taxPeriod: Option[TaxPeriod], requestToDate: String): Boolean = {
    val toDate = taxPeriod.fold(None: Option[LocalDate]){l => Some(LocalDate.parse(l.to))}
    toDate.fold(true){ desTo => desTo.compareTo(LocalDate.parse(requestToDate)) <= 0
    }
  }

  case class ChargeTypeWrapper(chargeType: String)
  object ChargeTypeWrapper{
    implicit val reads: Reads[ChargeTypeWrapper] = Json.reads[ChargeTypeWrapper]
  }
}