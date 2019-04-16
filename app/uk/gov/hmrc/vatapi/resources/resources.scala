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

package uk.gov.hmrc.vatapi

import cats.data.EitherT
import cats.implicits._
import play.api.Logger
import play.api.http.Status
import play.api.libs.json._
import play.api.mvc.Result
import play.api.mvc.Results.InternalServerError
import uk.gov.hmrc.vatapi.models.{AuthorisationErrorResult, ErrorResult, Errors, GenericErrorResult, InternalServerErrorResult, JsonValidationErrorResult, ValidationErrorResult}
import uk.gov.hmrc.vatapi.resources.wrappers.Response

import scala.concurrent.{ExecutionContext, Future}

package object resources {

  type BusinessResult[T] = EitherT[Future, ErrorResult, T]
  val GovTestScenarioHeader = "Gov-Test-Scenario"

  def unhandledResponse(status: Int, logger: Logger): Result = {
    logger.error(s"Unhandled response from DES. Status code: $status. Returning 500 to client.")
    InternalServerError(Json.toJson(Errors.InternalServerError("An internal server error occurred")))
  }

  def handleErrors(errorResult: ErrorResult): VatResult = {
    errorResult match {
      case GenericErrorResult(message) => VatResult.Failure(Status.BAD_REQUEST, Errors.badRequest(message))
      case JsonValidationErrorResult(errors) => VatResult.Failure(Status.BAD_REQUEST,Errors.badRequest(errors))
      case ValidationErrorResult(error) => VatResult.Failure(Status.BAD_REQUEST, Errors.badRequest(error))
      case AuthorisationErrorResult(error) => VatResult.Failure(Status.FORBIDDEN, error)
      case InternalServerErrorResult(error) => VatResult.Failure(Status.INTERNAL_SERVER_ERROR, Errors.InternalServerError(error))
    }
  }

  def validateJson[T](json: JsValue)(implicit reads: Reads[T], ec: ExecutionContext): BusinessResult[T] =
    BusinessResult {
      for {
        errors <- json.validate[T].asEither.left
      } yield JsonValidationErrorResult(errors)
    }

  def validate[T](value: T)(validate: PartialFunction[T, Errors.Error])(implicit ec: ExecutionContext): BusinessResult[T] =
    if (validate.isDefinedAt(value)) BusinessResult.failure(ValidationErrorResult(validate(value)))
    else BusinessResult.success(value)

  def authorise[T](value: T)(auth: PartialFunction[T, Errors.Error])(implicit ec: ExecutionContext): BusinessResult[T] =
    if (auth.isDefinedAt(value)) BusinessResult.failure(AuthorisationErrorResult(Errors.businessError(auth(value))))
    else BusinessResult.success(value)

  def execute[T](torun: Unit => Future[T])(implicit ec: ExecutionContext): BusinessResult[T] =
    BusinessResult {
      for {
        result <- torun(())
      } yield Right(result)
    }

  def fromDes[R <: Response](result: BusinessResult[R]): DesBusinessResult[R] = DesBusinessResult(result)

  object BusinessResult {

    def apply[T](eventuallyErrorOrResult: Future[Either[ErrorResult, T]]): BusinessResult[T] =
      new EitherT(eventuallyErrorOrResult)

    def apply[T](errorOrResult: Either[ErrorResult, T])(implicit ec: ExecutionContext): BusinessResult[T] =
      EitherT.fromEither(errorOrResult)

    def success[T](value: T)(implicit ec: ExecutionContext): BusinessResult[T] = EitherT.fromEither(Right(value))

    def failure[T](error: ErrorResult)(implicit ec: ExecutionContext): BusinessResult[T] = EitherT.fromEither(Left(error))

  }

}
