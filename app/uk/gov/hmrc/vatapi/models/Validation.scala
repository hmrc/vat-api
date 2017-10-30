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

package uk.gov.hmrc.vatapi.models

import play.api.libs.json._

case class Validation[T](path: JsPath,
                         validator: T => Boolean,
                         validationError: JsonValidationError)

object Validation {
  implicit class ErrorAccumulatingValidator[T](reads: Reads[T]) {
    def validate(validations: Seq[Validation[T]]): Reads[T] =
      reads.flatMap { t =>
        Reads[T] { _ =>
          val errors = validations.foldLeft(
            Seq.empty[(JsPath, Seq[JsonValidationError])]) {
            case (errs, Validation(path, validator, err)) =>
              if (validator(t)) errs else errs :+ path -> Seq(err)
          }
          if (errors.isEmpty) JsSuccess(t) else JsError(errors)
        }
      }
  }
}
