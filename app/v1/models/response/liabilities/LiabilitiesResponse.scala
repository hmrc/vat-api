/*
 * Copyright 2022 HM Revenue & Customs
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
import utils.FinancialDataReadsUtils

case class LiabilitiesResponse(liabilities: Seq[Liability])

object LiabilitiesResponse extends FinancialDataReadsUtils {

  private val unsupportedChargeTypes: Seq[String] = Seq("hybrid payments", "payment on account")

  implicit val writes: OWrites[LiabilitiesResponse] = Json.writes[LiabilitiesResponse]

  implicit def reads(implicit to: LocalDate): Reads[LiabilitiesResponse] =
    (JsPath \ "financialTransactions")
      .read(filterNotArrayReads[Liability](filterName = "chargeType", notMatching = unsupportedChargeTypes))
      .map(_.filter(liability => dateCheck(liability.taxPeriod, to)))
      .map(LiabilitiesResponse(_))
}
