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

import play.api.Logger
import play.api.libs.json.JsValue
import play.api.libs.json.Json.toJson
import play.api.mvc.Result
import play.api.mvc.Results._
import uk.gov.hmrc.http.HttpResponse
import uk.gov.hmrc.vatapi.models.Errors
import uk.gov.hmrc.vatapi.models.des.DesError
import uk.gov.hmrc.vatapi.models.des.DesErrorCode.{DesErrorCode, _}
import uk.gov.hmrc.vatapi.resources.AuthRequest

import scala.PartialFunction.{apply => _, _}
import scala.util.{Failure, Success, Try}

trait Response {

  val logger: Logger = Logger(this.getClass)
  val status: Int = underlying.status

  def underlying: HttpResponse

  def filter[A](pf: PartialFunction[Int, Result])(implicit request: AuthRequest[A]): Result =
    status / 100 match {
      case 4 | 5 =>
        logger.error(s"DES error occurred. User type: ${request.authContext.affinityGroup}\n" +
          s"Status code: ${underlying.status}\nBody: ${underlying.body}")
        (pf orElse errorMappings orElse standardErrorMapping) (status)
      case _ => (pf andThen addCorrelationHeader) (status)
    }

  private def addCorrelationHeader(result: Result) =
    underlying
      .header("CorrelationId")
      .fold(result)(correlationId => result.withHeaders("X-CorrelationId" -> correlationId))

  def errorMappings: PartialFunction[Int, Result] = empty

  private def standardErrorMapping: PartialFunction[Int, Result] = {
    case 404 => NotFound
    case 500 if errorCodeIsOneOf(SERVER_ERROR) => InternalServerError(toJson(Errors.InternalServerError))
    case 503 if errorCodeIsOneOf(SERVICE_UNAVAILABLE) => InternalServerError(toJson(Errors.InternalServerError))
    case _ => InternalServerError(toJson(Errors.InternalServerError))
  }

  def errorCodeIsOneOf(errorCodes: DesErrorCode*): Boolean = jsonOrError match {
    case Right(json) => json.asOpt[DesError].exists(errorCode => errorCodes.contains(errorCode.code))
    case Left(_) => false
  }

  def jsonOrError: Either[Throwable, JsValue] = {
    Try(underlying.json) match {
      case Success(null) => Left(new RuntimeException)
      case Success(json) => Right(json)
      case Failure(e) => Left(e)
    }
  }

  def getCorrelationId(): String = {
    underlying.header("CorrelationId").getOrElse("No Correlation ID")
  }

}

