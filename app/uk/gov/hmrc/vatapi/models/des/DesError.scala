/*
 * Copyright 2018 HM Revenue & Customs
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

package uk.gov.hmrc.vatapi.models.des

import play.api.libs.json.{Format, Json, Reads}
import uk.gov.hmrc.vatapi.models.EnumJson
import uk.gov.hmrc.vatapi.models.des.DesErrorCode.DesErrorCode

case class MultiDesError(failures: Seq[DesError])

object MultiDesError {
  implicit val reads: Reads[MultiDesError] = Json.reads[MultiDesError]
}

case class DesError(code: DesErrorCode, reason: String)

object DesError {
  implicit val reads: Reads[DesError] = Json.reads[DesError]
}

object DesErrorCode extends Enumeration {
  type DesErrorCode = Value

  val INVALID_VRN,
  INVALID_PAYLOAD,
  NOT_FOUND_NINO,
  NOT_FOUND,
  CONFLICT,
  SERVER_ERROR,
  SERVICE_UNAVAILABLE,
  INVALID_PERIOD,
  INVALID_PERIOD_KEY,
  INVALID_ORIGINATOR_ID,
  INVALID_REQUEST,
  INVALID_BUSINESSID,
  INVALID_TAX_YEAR,
  NOT_FOUND_TAX_YEAR,
  INVALID_TYPE,
  INVALID_IDENTIFIER,
  INVALID_CALCID,
  INVALID_INCOME_SOURCE,
  INVALID_DATE_FROM,
  INVALID_DATE_TO,
  INVALID_STATUS,
  NOT_CONTIGUOUS_PERIOD,
  OVERLAPS_IN_PERIOD,
  NOT_ALIGN_PERIOD,
  INVALID_INCOMESOURCEID,
  NOT_FOUND_INCOME_SOURCE,
  BOTH_EXPENSES_SUPPLIED,
  NOT_ALLOWED_CONSOLIDATED_EXPENSES,
  NOT_UNDER_16,
  NOT_OVER_STATE_PENSION,
  MISSING_EXEMPTION_INDICATOR,
  MISSING_EXEMPTION_REASON,
  INVALID_DATE_RANGE = Value

  implicit val format: Format[DesErrorCode] = EnumJson.enumFormat(DesErrorCode,
    Some(s"Recognized DesErrorCode values: ${DesErrorCode.values.mkString(", ")}"))
}
