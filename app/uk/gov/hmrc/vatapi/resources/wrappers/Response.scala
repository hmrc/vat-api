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

import play.api.Logger
import play.api.http.Status
import play.api.libs.json.JsValue
import uk.gov.hmrc.http.HttpResponse
import uk.gov.hmrc.vatapi.models.Errors
import uk.gov.hmrc.vatapi.models.des.DesError
import uk.gov.hmrc.vatapi.models.des.DesErrorCode.{DesErrorCode, _}
import uk.gov.hmrc.vatapi.resources.{AuthRequest, VatResult}
import uk.gov.hmrc.vatapi.utils.pagerDutyLogging.{Endpoint, PagerDutyLogging}

import scala.PartialFunction.{apply => _, _}
import scala.util.{Failure, Success, Try}

object Response {
  val defaultCorrelationId = "No Correlation ID"

  def getCorrelationId(httpResponse: HttpResponse): String =
    httpResponse.header("CorrelationId").getOrElse(defaultCorrelationId)
}

trait Response {

  val logger: Logger = Logger(this.getClass)
  val status: Int = underlying.status

  def underlying: HttpResponse

  def filter[A](pf: PartialFunction[Int, VatResult])(implicit endpoint: Endpoint, request: AuthRequest[A]): VatResult = {
    val statusPrefix: Int = status / 100
    statusPrefix match {
      case 4 | 5 =>
        val message = s"DES error occurred. User type: ${request.authContext.affinityGroup}\n" +
          s"Status code: ${underlying.status}\nBody: ${underlying.body}"

        PagerDutyLogging.logError(endpoint.toLoggerMessage, message, statusPrefix, logger.error(_))

        (pf orElse errorMappings orElse standardErrorMapping) (status)
      case _ => (pf andThen addCorrelationHeader) (status)
    }
  }

  private def addCorrelationHeader(result: VatResult) =
    underlying
      .header("CorrelationId")
      .fold(result)(correlationId => result.withHeaders("X-CorrelationId" -> correlationId))

  def errorMappings: PartialFunction[Int, VatResult] = empty

  private def standardErrorMapping: PartialFunction[Int, VatResult] = {
    case 404 => VatResult.FailureEmptyBody(Status.NOT_FOUND, Errors.NotFound)
    case 500 if errorCodeIsOneOf(SERVER_ERROR) => VatResult.Failure(Status.INTERNAL_SERVER_ERROR, Errors.InternalServerError)
    case 503 if errorCodeIsOneOf(SERVICE_UNAVAILABLE) => VatResult.Failure(Status.INTERNAL_SERVER_ERROR, Errors.InternalServerError)
    case _ => VatResult.Failure(Status.INTERNAL_SERVER_ERROR, Errors.InternalServerError)
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

  def getCorrelationId: String = Response.getCorrelationId(underlying)
}

