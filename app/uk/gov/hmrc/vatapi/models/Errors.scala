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

package uk.gov.hmrc.vatapi.models

import play.api.libs.json._
import uk.gov.hmrc.vatapi.models.ErrorCode.ErrorCode

object Errors {

  implicit val errorDescWrites: Writes[Error] = Json.writes[Error]
  implicit val badRequestWrites: Writes[BadRequest] = new Writes[BadRequest] {
    override def writes(req: BadRequest): JsValue = {
      Json.obj("code" -> req.code, "message" -> req.message, "errors" -> req.errors)
    }
  }

  implicit val businessErrorWrites: Writes[BusinessError] = new Writes[BusinessError] {
    override def writes(req: BusinessError) =
      Json.obj("code" -> req.code, "message" -> req.message, "errors" -> req.errors)
  }

  implicit val internalServerErrorWrites: Writes[InternalServerError] = new Writes[InternalServerError] {
    override def writes(req: InternalServerError): JsValue =
      Json.obj("code" -> req.code, "message" -> req.message)
  }

  case class Error(code: String, message: String, path: Option[String])

  case class BadRequest(errors: Seq[Error], message: String) {
    val code = "INVALID_REQUEST"
  }

  case class BusinessError(errors: Seq[Error], message: String) {
    val code = "BUSINESS_ERROR"
  }

  case class InternalServerError(message: String) {
    val code = "INTERNAL_SERVER_ERROR"
  }

  object VrnInvalid extends Error("VRN_INVALID", "The provided VRN is invalid", None)
  object InvalidDateFrom extends Error("DATE_FROM_INVALID", "The provided from date is invalid", None)
  object InvalidDateTo extends Error("DATE_TO_INVALID", "The provided to date is invalid", None)
  object InvalidPeriodKey extends Error("PERIOD_KEY_INVALID", "Invalid period key", None)
  object InvalidRequest extends Error("INVALID_REQUEST", "Invalid request", None)
  object InternalServerError extends Error("INTERNAL_SERVER_ERROR", "An internal server error occurred", None)
  object NotFinalisedDeclaration extends Error("NOT_FINALISED", "The return cannot be accepted without a declaration it is finalised.", Some("/finalised"))
  object NotFound extends Error("NOT_FOUND", "The remote endpoint has indicated that no data can be found", None)
  object DateRangeTooLarge extends Error("DATE_RANGE_TOO_LARGE", "The date of the requested return cannot be further than four years from the current date.", None)
  object DuplicateVatSubmission extends Error("DUPLICATE_SUBMISSION", "The VAT return was already submitted for the given period.", None)

  object ClientNotSubscribed extends Error("CLIENT_NOT_SUBSCRIBED", "The client is not subscribed to MTD", None)
  object AgentNotAuthorized extends Error("AGENT_NOT_AUTHORIZED", "The agent is not authorized", None)
  object AgentNotSubscribed extends Error("AGENT_NOT_SUBSCRIBED", "The agent is not subscribed to agent services", None)
  object BadToken extends Error("UNAUTHORIZED", "Bearer token is missing or not authorized", None)

  def badRequest(validationErrors: JsonValidationErrors) = BadRequest(flattenValidationErrors(validationErrors), "Invalid request")
  def badRequest(error: Error) = BadRequest(Seq(error), "Invalid request")
  def badRequest(message: String) = BadRequest(Seq.empty, message)

  def businessError(error: Error): BusinessError = businessError(Seq(error))
  def businessError(errors: Seq[Error]): BusinessError = BusinessError(errors, "Business validation error")

  private def flattenValidationErrors(validationErrors: JsonValidationErrors): Seq[Error] = {
    validationErrors.flatMap { validationError =>
      val (path, errors) = validationError
      errors.map { err =>
        err.args match {
          case Seq(head, _*) if head.isInstanceOf[ErrorCode] =>
            Error(head.asInstanceOf[ErrorCode].toString, err.message, Some(path.toString()))
          case _ => convertErrorMessageToCode(err, path.toString())
        }
      }
    }
  }

  /*
   * Converts a Play error without an error code into an Error that contains an error code
   * based on the content of the error message.
   */
  private def convertErrorMessageToCode(playError: JsonValidationError, errorPath: String): Error = {
    playError.message match {
      case "error.expected.jodadate.format" => Error("INVALID_DATE", "please provide a date in ISO format (i.e. YYYY-MM-DD)", Some(errorPath))
      case "error.path.missing" => Error("MANDATORY_FIELD_MISSING", "a mandatory field is missing", Some(errorPath))
      case "error.expected.numberformatexception" => Error("INVALID_NUMERIC_VALUE", "please provide a numeric field", Some(errorPath))
      case "error.expected.jsstring" => Error("INVALID_STRING_VALUE", "please provide a string field", Some(errorPath))
      case _ => Error("UNMAPPED_PLAY_ERROR", playError.message, Some(errorPath))
    }
  }


}
