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

import play.api.libs.json.{JsError, JsSuccess}
import uk.gov.hmrc.http.HttpResponse
import uk.gov.hmrc.vatapi.models.{
  DesTransformError,
  DesTransformValidator,
  VatReturn,
  VatReturns,
  des
}

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
}

case class ParseError(msg: String) extends DesTransformError