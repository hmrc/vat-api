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

import play.api.libs.json.{Format, Json, OFormat, Reads}
import uk.gov.hmrc.vatapi.models.EnumJson
import uk.gov.hmrc.vatapi.models.des.DesErrorCode.DesErrorCode

case class DesError(code: DesErrorCode, reason: String)

object DesError {
  implicit val format: OFormat[DesError] = Json.format[DesError]
  implicit val reads: Reads[DesError] = Json.reads[DesError]
}

object DesErrorCode extends Enumeration {
  type DesErrorCode = Value

  val INVALID_VRN,
  INVALID_ARN,
  INVALID_PAYLOAD,
  INVALID_PERIODKEY,
  DUPLICATE_SUBMISSION,
  DATE_RANGE_TOO_LARGE,
  SERVER_ERROR,
  SERVICE_UNAVAILABLE,
  INVALID_IDNUMBER,
  INVALID_DATETO,
  INVALID_DATEFROM,
  NOT_FOUND,
  VRN_NOT_FOUND,
  NOT_FOUND_VRN,
  INVALID_SUBMISSION,
  INVALID_IDENTIFIER,
  INVALID_IDTYPE,
  INVALID_STATUS,
  INVALID_REGIME,
  INVALID_DATE_TO,
  INVALID_DATE_FROM,
  INVALID_DATE_RANGE,
  NOT_FOUND_BPKEY,
  INVALID_REGIMETYPE,
  INVALID_ONLYOPENITEMS,
  INVALID_INCLUDELOCKS,
  INVALID_CALCULATEACCRUEDINTEREST,
  INVALID_CUSTOMERPAYMENTINFORMATION,
  INVALID_DATA
  = Value

  implicit val format: Format[DesErrorCode] = EnumJson.enumFormat(DesErrorCode,
    Some(s"Recognized DesErrorCode values: ${DesErrorCode.values.mkString(", ")}"))
}
