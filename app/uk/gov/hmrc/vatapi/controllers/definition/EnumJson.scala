/*
 * Copyright 2019 HM Revenue & Customs
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

package uk.gov.hmrc.vatapi.controllers.definition

import play.api.libs.json._
import uk.gov.hmrc.vatapi.models.ErrorCode

object EnumJson {

  def enumReads[E <: Enumeration](enum: E, valueMissingMessage: Option[String] = None): Reads[E#Value] = new Reads[E#Value] {

    def defaultValueMissingMessage(s: String)= s"Enumeration expected of type: '${enum.getClass}', but it does not contain '$s'"

    def reads(json: JsValue): JsResult[E#Value] = json match {
      case JsString(s) =>
        try {
          JsSuccess(enum.withName(s))
        } catch {
          case _: NoSuchElementException =>
            JsError(JsPath, JsonValidationError(valueMissingMessage.getOrElse(defaultValueMissingMessage(s)), ErrorCode.INVALID_VALUE))
        }
      case _ => JsError(JsPath(), JsonValidationError("String value expected", ErrorCode.INVALID_TYPE))
    }
  }

  implicit def enumWrites[E <: Enumeration]: Writes[E#Value] = new Writes[E#Value] {
    def writes(v: E#Value): JsValue = JsString(v.toString)
  }

  implicit def enumFormat[E <: Enumeration](enum: E, valueMissingMessage: Option[String] = None): Format[E#Value] = {
    Format(enumReads(enum, valueMissingMessage), enumWrites)
  }

}
