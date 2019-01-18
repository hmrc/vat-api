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

package uk.gov.hmrc.vatapi.resources.wrappers
import play.api.libs.json.Json.toJson
import play.api.libs.json.{JsError, JsSuccess, JsValue}
import play.api.mvc.Result
import play.api.mvc.Results.{BadRequest, InternalServerError}
import uk.gov.hmrc.domain.Vrn
import uk.gov.hmrc.http.HttpResponse
import uk.gov.hmrc.vatapi.models.des.DesErrorCode._
import uk.gov.hmrc.vatapi.models.{DesTransformError, DesTransformValidator, Errors, Liabilities, Payments, des}
import uk.gov.hmrc.vatapi.resources.VatReturnsResource.NotFound

case class FinancialDataResponse(underlying: HttpResponse) extends Response {

  def getLiabilities(vrn: Vrn): Either[DesTransformError, Liabilities] = {

    def deserialise(js: JsValue) = js.validate[des.FinancialData] match {
      case JsError(errors) => Left(ParseError(s"[FinancialDataResponse][getLiabilities - deserialise] Json format from DES doesn't match the FinancialData model:  $errors"))
      case JsSuccess(financialData, _) =>
        DesTransformValidator[des.FinancialData, Liabilities].from(financialData)
    }
    jsonOrError match {
      case Right(js) =>
        logger.debug(s"[FinancialDataResponse][getLiabilities - jsonOrError] Json response body from DES : ${js}")
        deserialise(js)
      case Left(e) =>
        logger.error(s"[FinancialDataResponse][getLiabilities - jsonOrError] Non json response from DES : ${e.getMessage}")
        Left(ParseError(s"Unable to parse the response from DES as Json: $e"))
    }
  }

  def getPayments(vrn: Vrn): Either[DesTransformError, Payments] = {
    def deserialise(js: JsValue) = js.validate[des.FinancialData] match {
      case JsError(errors) => Left(ParseError(s"[FinancialDataResponse][getPayments - deserialise] Json format from DES doesn't match the FinancialData model: $errors"))
      case JsSuccess(financialData, _) => DesTransformValidator[des.FinancialData, Payments].from(financialData)
    }
    jsonOrError match {
      case Right(js) =>
        logger.debug(s"[FinancialDataResponse][getPayments - jsonOrError] Json response body from DES : ${js}")
        deserialise(js)
      case Left(e) =>
        logger.error(s"[FinancialDataResponse][getPayments - jsonOrError] Non json response from DES : ${e.getMessage}")
        Left(ParseError(s"Unable to parse the response from DES as Json: $e"))
    }
  }

  override def errorMappings: PartialFunction[Int, Result] = {
    case 400 if errorCodeIsOneOf(INVALID_IDNUMBER) => BadRequest(toJson(Errors.VrnInvalid))
    case 400 if errorCodeIsOneOf(INVALID_DATEFROM) => BadRequest(toJson(Errors.InvalidDateFrom))
    case 400 if errorCodeIsOneOf(INVALID_DATETO) => BadRequest(toJson(Errors.InvalidDateTo))
    case 400 if errorCodeIsOneOf(NOT_FOUND) => NotFound(toJson(Errors.NotFound))
    case 400 if errorCodeIsOneOf(INVALID_IDTYPE, INVALID_ONLYOPENITEMS, INVALID_REGIMETYPE,
      INVALID_INCLUDELOCKS, INVALID_CALCULATEACCRUEDINTEREST, INVALID_CUSTOMERPAYMENTINFORMATION
    ) => InternalServerError(toJson(Errors.InternalServerError))
    case 404 if errorCodeIsOneOf(NOT_FOUND) => NotFound(toJson(Errors.NotFound))
    case 422 if errorCodeIsOneOf(INVALID_DATA) => BadRequest(toJson(Errors.InvalidData))
  }

}