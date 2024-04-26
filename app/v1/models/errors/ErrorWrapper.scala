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

package v1.models.errors

import play.api.libs.json.{JsObject, JsValue, Json, Writes}
import v1.models.audit.AuditError

case class ErrorWrapper(correlationId: String, error: MtdError, errors: Option[Seq[MtdError]] = None) {

  private def allErrors: Seq[MtdError] = errors match {
    case Some(seq) => seq
    case None      => Seq(error)
  }

  def auditErrors: Seq[AuditError] =
    allErrors.map(error => AuditError(error.code))
}

object ErrorWrapper {

  val allErrors: Seq[MtdError] => Seq[JsValue] = {
    case mtdError :: Nil => mtdErrors(mtdError)
    case mtdError :: rest => mtdErrors(mtdError) ++ allErrors(rest)
  }

  private val mtdErrors : MtdError => Seq[JsValue] = {
    case MtdError(_, _, Some(customJson)) =>
      customJson.asOpt[MtdErrorWrapper] match {
        case Some(e) => mtdErrorWrapper(e)
        case _ => Seq(customJson)
      }
    case _@o => Seq(Json.toJson(o))
  }

  private val mtdErrorWrapper: MtdErrorWrapper => Seq[JsValue]= wrapper => wrapper.errors match {
    case Some(errors) if errors.nonEmpty => errors.map(error => Json.toJson(error))
    case _ => Seq(Json.toJson(wrapper))
  }

  implicit val writes: Writes[ErrorWrapper] = (errorResponse: ErrorWrapper) => {

    val singleJson: JsObject = Json.toJson(errorResponse.error).as[JsObject]

    errorResponse.errors match {
      case Some(errors) if errors.nonEmpty => singleJson + ("errors" -> Json.toJson(allErrors(errors)))
      case _ => singleJson
    }
  }
}
