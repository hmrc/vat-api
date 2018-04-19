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
import play.api.libs.json.{JsError, JsSuccess, JsValue}
import play.api.mvc.Result
import play.api.mvc.Results.{BadRequest, InternalServerError}
import uk.gov.hmrc.http.HttpResponse
import uk.gov.hmrc.vatapi.httpparsers.NRSData
import uk.gov.hmrc.vatapi.models.des.DesErrorCode._
import uk.gov.hmrc.vatapi.models.{DesTransformError, DesTransformValidator, Errors, VatReturn, des}
import uk.gov.hmrc.vatapi.resources.VatReturnsResource.Forbidden

case class VatReturnResponse(underlying: HttpResponse) extends Response {

  def vatReturnOrError: Either[DesTransformError, VatReturn] = {

    def deserialise(js: JsValue) = js.validate[des.VatReturn] match {
      case JsError(errors) => Left(ParseError(s"Json format from DES doesn't match the VatReturn model: $errors"))
      case JsSuccess(vatReturn, _) => DesTransformValidator[des.VatReturn, VatReturn].from(vatReturn)
    }

    jsonOrError match {
      case Left(e) =>
        logger.error(s"[VatReturnResponse][vatReturnOrError] Non json response from DES : ${underlying.body}")
        Left(ParseError(s"Unable to parse the response from DES as Json: ${e.getMessage}"))
      case Right(js) =>
        logger.info(s"[VatReturnResponse][vatReturnOrError] Json response body from DES : ${js}")
        deserialise(js)
    }
  }

  var nrsData: NRSData = _
  def withNrsData(data: NRSData): VatReturnResponse = {nrsData = data; this}

  override def errorMappings: PartialFunction[Int, Result] = {
    case 400 if errorCodeIsOneOf(INVALID_VRN) => BadRequest(toJson(Errors.VrnInvalid))
    case 400 if errorCodeIsOneOf(INVALID_ARN) => InternalServerError(toJson(Errors.InternalServerError))
    case 400 if errorCodeIsOneOf(INVALID_PAYLOAD) => BadRequest(toJson(Errors.InvalidRequest))
    case 400 if errorCodeIsOneOf(INVALID_PERIODKEY) => BadRequest(toJson(Errors.InvalidPeriodKey))
    case 400 if errorCodeIsOneOf(INVALID_SUBMISSION) => BadRequest(toJson(Errors.InvalidVatSubmission))
    case 409 if errorCodeIsOneOf(DUPLICATE_SUBMISSION) => Forbidden(toJson(Errors.businessError(Errors.DuplicateVatSubmission)))
    case 403 if errorCodeIsOneOf(DATE_RANGE_TOO_LARGE) => Forbidden(toJson(Errors.businessError(Errors.DateRangeTooLarge)))
    case 403 if errorCodeIsOneOf(VRN_NOT_FOUND) => InternalServerError(toJson(Errors.InternalServerError))
    case 403 if errorCodeIsOneOf(NOT_FOUND_VRN) => InternalServerError(toJson(Errors.InternalServerError))
  }
}

case class ParseError(msg: String) extends DesTransformError
