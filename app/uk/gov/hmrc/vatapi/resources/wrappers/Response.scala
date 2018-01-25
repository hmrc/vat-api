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

/*
 * Copyright 2017 HM Revenue & Customs
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

import play.api.Logger
import play.api.libs.json.JsValue
import play.api.libs.json.Json.toJson
import play.api.mvc.Results._
import play.api.mvc.{Request, Result}
import uk.gov.hmrc.http.HttpResponse
import uk.gov.hmrc.vatapi.models.Errors
import uk.gov.hmrc.vatapi.models.des.DesError
import uk.gov.hmrc.vatapi.models.des.DesErrorCode.{DesErrorCode, _}
import uk.gov.hmrc.vatapi.resources.VatReturnsResource.Forbidden

trait Response {
  val logger: Logger = Logger(this.getClass)

  def underlying: HttpResponse

  def json: JsValue = underlying.json

  val status: Int = underlying.status


  def filter[A](pf: PartialFunction[Int, Result])(implicit request: Request[A]): Result =
    status / 100 match {
      case 4 | 5 =>
        logResponse()
        (pf orElse errorMapping) (status)
      case _ => (pf andThen addCorrelationHeader) (status)
    }

  private def logResponse(): Unit =
    logger.error(s"DES error occurred with status code ${underlying.status} and body ${underlying.body}")

  private def addCorrelationHeader(result: Result) =
    underlying
      .header("CorrelationId")
      .fold(result)(correlationId => result.withHeaders("X-CorrelationId" -> correlationId))

  private def errorMapping: PartialFunction[Int, Result] = {
    case 400 if errorCodeIsOneOf(INVALID_VRN) => BadRequest(toJson(Errors.VrnInvalid))
    case 400 if errorCodeIsOneOf(INVALID_ARN) => InternalServerError(toJson(Errors.InternalServerError))
    case 400 if errorCodeIsOneOf(INVALID_PAYLOAD) => BadRequest(toJson(Errors.InvalidRequest))
    case 400 if errorCodeIsOneOf(INVALID_PERIODKEY) => BadRequest(toJson(Errors.InvalidPeriodKey))
    case 400 if errorCodeIsOneOf(DUPLICATE_SUBMISSION) =>  Forbidden(toJson(Errors.businessError(Errors.DuplicateVatSubmission)))
    case 403 if errorCodeIsOneOf(DATE_RANGE_TOO_LARGE) =>  Forbidden(toJson(Errors.businessError(Errors.DateRangeTooLarge)))
    case 404 => NotFound
    case 500 if errorCodeIsOneOf(SERVER_ERROR) => InternalServerError(toJson(Errors.InternalServerError))
    case 503 if errorCodeIsOneOf(SERVICE_UNAVAILABLE) => InternalServerError(toJson(Errors.InternalServerError))
    case _ => InternalServerError(toJson(Errors.InternalServerError))
  }

  def errorCodeIsOneOf(errorCodes: DesErrorCode*): Boolean =
    json.asOpt[DesError].exists(errorCode => errorCodes.contains(errorCode.code))
}

