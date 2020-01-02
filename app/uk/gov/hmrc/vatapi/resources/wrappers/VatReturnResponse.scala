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

package uk.gov.hmrc.vatapi.resources.wrappers

import play.api.http.Status
import play.api.libs.json.{JsError, JsSuccess, JsValue}
import uk.gov.hmrc.http.HttpResponse
import uk.gov.hmrc.vatapi.httpparsers.NRSData
import uk.gov.hmrc.vatapi.models.des.DesErrorCode._
import uk.gov.hmrc.vatapi.models.{DesTransformError, DesTransformValidator, Errors, VatReturn, des}
import uk.gov.hmrc.vatapi.resources.VatResult

case class VatReturnResponse(underlying: HttpResponse) extends Response {

  var nrsData: NRSData = _

  def vatReturnOrError: Either[DesTransformError, VatReturn] = {

    def deserialise(js: JsValue) = js.validate[des.VatReturn] match {
      case JsError(errors) => Left(ParseError(s"Json format from DES doesn't match the VatReturn model: $errors"))
      case JsSuccess(vatReturn, _) => DesTransformValidator[des.VatReturn, VatReturn].from(vatReturn)
    }

    jsonOrError match {
      case Left(e) =>
        logger.error(s"[VatReturnResponse][vatReturnOrError] Non json response from DES : $e")
        Left(ParseError(s"Unable to parse the response from DES as Json: $e"))
      case Right(js) =>
        deserialise(js)
    }
  }

  def vatSubmissionReturnOrError: Either[DesTransformError, JsValue] = {
    jsonOrError match {
      case Left(e) =>
        logger.error(s"[VatReturnResponse][vatReturnOrError] Non json response from DES : $e")
        Left(ParseError(s"Unable to parse the response from DES as Json: $e"))
      case Right(js) => Right(js)
    }
  }

  def withNrsData(data: NRSData): VatReturnResponse = {
    nrsData = data;
    this
  }

  override def errorMappings: PartialFunction[Int, VatResult] = {
    case 400 if errorCodeIsOneOf(INVALID_VRN) => VatResult.Failure(Status.BAD_REQUEST, Errors.VrnInvalid)
    case 400 if errorCodeIsOneOf(INVALID_ARN) => VatResult.Failure(Status.INTERNAL_SERVER_ERROR, Errors.InternalServerError)
    case 400 if errorCodeIsOneOf(INVALID_ORIGINATOR_ID) => VatResult.Failure(Status.INTERNAL_SERVER_ERROR, Errors.InternalServerError)
    case 400 if errorCodeIsOneOf(INVALID_PAYLOAD) => VatResult.Failure(Status.BAD_REQUEST, Errors.InvalidRequest)
    case 400 if errorCodeIsOneOf(INVALID_PERIODKEY) => VatResult.Failure(Status.BAD_REQUEST, Errors.InvalidPeriodKey)
    case 400 if errorCodeIsOneOf(INVALID_SUBMISSION) =>
      logger.info(s"[VatReturnResponse][errorMappings] Des returned error with status 400 and errorCode INVALID_SUBMISSION")
      VatResult.Failure(Status.INTERNAL_SERVER_ERROR, Errors.InternalServerError)
    case 403 if errorCodeIsOneOf(DATE_RANGE_TOO_LARGE) => VatResult.Failure(Status.FORBIDDEN, Errors.businessError(Errors.DateRangeTooLarge))
    case 403 if errorCodeIsOneOf(VRN_NOT_FOUND, NOT_FOUND_VRN) => VatResult.Failure(Status.INTERNAL_SERVER_ERROR, Errors.InternalServerError)
    case 403 if errorCodeIsOneOf(INVALID_IDENTIFIER) => VatResult.Failure(Status.NOT_FOUND, Errors.InvalidPeriodKey)
    case 403 if errorCodeIsOneOf(INVALID_INPUTDATA) => VatResult.Failure(Status.FORBIDDEN, Errors.InvalidRequest)
    case 403 if errorCodeIsOneOf(TAX_PERIOD_NOT_ENDED) => VatResult.Failure(Status.FORBIDDEN, Errors.TaxPeriodNotEnded)
    case 409 if errorCodeIsOneOf(DUPLICATE_SUBMISSION) => VatResult.Failure(Status.FORBIDDEN, Errors.businessError(Errors.DuplicateVatSubmission))

  }
}

case class ParseError(msg: String) extends DesTransformError
