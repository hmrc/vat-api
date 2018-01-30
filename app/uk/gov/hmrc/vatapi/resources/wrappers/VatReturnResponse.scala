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

package uk.gov.hmrc.vatapi.resources.wrappers

import play.api.libs.json.Json.toJson
import play.api.libs.json.{JsError, JsSuccess}
import play.api.mvc.Result
import play.api.mvc.Results.{BadRequest, InternalServerError}
import uk.gov.hmrc.http.HttpResponse
import uk.gov.hmrc.vatapi.models.des.DesErrorCode.{DATE_RANGE_TOO_LARGE, DUPLICATE_SUBMISSION, INVALID_ARN, INVALID_PAYLOAD, INVALID_PERIODKEY, INVALID_VRN}
import uk.gov.hmrc.vatapi.models.{DesTransformError, DesTransformValidator, Errors, VatReturn, des}
import uk.gov.hmrc.vatapi.resources.VatReturnsResource.Forbidden

case class VatReturnResponse(underlying: HttpResponse) extends Response {

  def vatReturn: Option[des.VatReturnsDES] =
    json.asOpt[des.VatReturnsDES] match {
      case Some(vatReturn) => Some(vatReturn)
      case None => logger.error(s"The response from DES does not match the expected format. JSON: [$json]")
                   None
    }

  def retrieve: Either[DesTransformError, VatReturn] =
    json.validate[des.VatReturn] match {
      case JsError(errors) => Left(ParseError(s"Unable to parse the response from DES as Json: $errors"))
      case JsSuccess(vatReturn, _) =>  DesTransformValidator[des.VatReturn, VatReturn].from(vatReturn)
    }

  override def errorMappings: PartialFunction[Int, Result] = {
    case 400 if errorCodeIsOneOf(INVALID_VRN) => BadRequest(toJson(Errors.VrnInvalid))
    case 400 if errorCodeIsOneOf(INVALID_ARN) => InternalServerError(toJson(Errors.InternalServerError))
    case 400 if errorCodeIsOneOf(INVALID_PAYLOAD) => BadRequest(toJson(Errors.InvalidRequest))
    case 400 if errorCodeIsOneOf(INVALID_PERIODKEY) => BadRequest(toJson(Errors.InvalidPeriodKey))
    case 400 if errorCodeIsOneOf(DUPLICATE_SUBMISSION) => Forbidden(toJson(Errors.businessError(Errors.DuplicateVatSubmission)))
    case 403 if errorCodeIsOneOf(DATE_RANGE_TOO_LARGE) => Forbidden(toJson(Errors.businessError(Errors.DateRangeTooLarge)))
  }
}

case class ParseError(msg: String) extends DesTransformError