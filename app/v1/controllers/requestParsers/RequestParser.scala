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

package v1.controllers.requestParsers

import v1.controllers.requestParsers.validators.Validator
import v1.models.errors.{BadRequestError, ErrorWrapper}
import v1.models.request.RawData

trait RequestParser[Raw <: RawData, Request] {

  val validator: Validator[Raw]

  protected def requestFor(data: Raw): Request

  def parseRequest(data: Raw): Either[ErrorWrapper, Request] = {
    validator.validate(data) match {
      case Nil => Right(requestFor(data))
      case err :: Nil => Left(ErrorWrapper(None, err, None))
      case errs => Left(ErrorWrapper(None, BadRequestError, Some(errs)))
    }
  }
}
