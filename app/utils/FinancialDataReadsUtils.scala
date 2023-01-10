/*
 * Copyright 2023 HM Revenue & Customs
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

package utils

import java.time.LocalDate

import play.api.libs.json.{JsValue, Json, Reads}
import v1.models.response.common.TaxPeriod

trait FinancialDataReadsUtils {

  def filterNotArrayReads[T](filterName: String, notMatching: Seq[String])
                            (implicit rds: Reads[Seq[T]]): Reads[Seq[T]] = (json: JsValue) => {
    json
      .validate[Seq[JsValue]]
      .flatMap(
        readJson =>
          Json
            .toJson(readJson.filterNot {
              element =>
                (element \ filterName).asOpt[String].exists(item => notMatching.contains(item.toLowerCase()))
            })
            .validate[Seq[T]])
  }

  def dateCheck(taxPeriod: Option[TaxPeriod], requestToDate: LocalDate): Boolean = {
    val toDate = taxPeriod.fold(None: Option[LocalDate]) { l => Some(LocalDate.parse(l.to)) }
    toDate.fold(true) { desTo => desTo.compareTo(requestToDate) <= 0
    }
  }
}
