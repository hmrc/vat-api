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
import play.api.Logger
import play.api.libs.json.Json.toJson
import play.api.libs.json.{JsError, JsSuccess, JsValue}
import play.api.mvc.Result
import play.api.mvc.Results.BadRequest
import uk.gov.hmrc.domain.Vrn
import uk.gov.hmrc.http.HttpResponse
import uk.gov.hmrc.vatapi.models.des.DesErrorCode._
import uk.gov.hmrc.vatapi.models.{DesTransformError, DesTransformValidator, Errors, Liabilities, Payments, des}
import uk.gov.hmrc.vatapi.resources.VatReturnsResource.{Forbidden, NotFound}

case class FinancialDataResponse(underlying: HttpResponse) extends Response {



  def getLiabilities(vrn: Vrn): Either[DesTransformError, Liabilities] = {

    def deserialise(js: JsValue) = js.validate[des.FinancialData] match {
      case JsError(errors) => Left(ParseError(s"Unable to parse the response from DES as Json: $errors"))
      case JsSuccess(financialData, _) => DesTransformValidator[des.FinancialData, Liabilities].from(financialData)
    }
    jsonOrError match {
      case Right(js) => deserialise(js)
      case Left(e) => Left(ParseError(s"Unable to parse the response from DES as Json: $e"))
    }
  }

  def getPayments(vrn: Vrn): Either[DesTransformError, Payments] = {
    def deserialise(js: JsValue) = js.validate[des.FinancialData] match {
      case JsError(errors) => Left(ParseError(s"Unable to parse the response from DES as Json: $errors"))
      case JsSuccess(financialData, _) => DesTransformValidator[des.FinancialData, Payments].from(financialData)
    }
    jsonOrError match {
      case Right(js) => deserialise(js)
      case Left(e) => Left(ParseError(s"Unable to parse the response from DES as Json: $e"))
    }
  }

  override def errorMappings: PartialFunction[Int, Result] = {
    case 400 if errorCodeIsOneOf(INVALID_VRN) => BadRequest(toJson(Errors.VrnInvalid))
    case 400 if errorCodeIsOneOf(INVALID_DATEFROM) => BadRequest(toJson(Errors.InvalidDateFrom))
    case 400 if errorCodeIsOneOf(INVALID_DATETO) => BadRequest(toJson(Errors.InvalidDateTo))
    case 400 if errorCodeIsOneOf(NOT_FOUND) => NotFound(toJson(Errors.NotFound))
  }

}