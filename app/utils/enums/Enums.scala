/*
 * Copyright 2022 HM Revenue & Customs
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

package utils.enums

import cats.Show
import play.api.libs.json._
import utils.enums.Values.MkValues

import scala.reflect.ClassTag

object Shows {
  implicit def toStringShow[E]: Show[E] = Show.show(_.toString)
}

object Enums {
  private def typeName[E: ClassTag]: String = implicitly[ClassTag[E]].runtimeClass.getSimpleName

  def parser[E: MkValues](implicit ev: Show[E] = Shows.toStringShow[E]): PartialFunction[String, E] =
    implicitly[MkValues[E]].values.map(e => ev.show(e) -> e).toMap

  def reads[E: MkValues: ClassTag](implicit ev: Show[E] = Shows.toStringShow[E]): Reads[E] =
    implicitly[Reads[String]].collect(JsonValidationError(s"error.expected.$typeName"))(parser)

  def writes[E: MkValues](implicit ev: Show[E] = Shows.toStringShow[E]): Writes[E] = Writes[E](e => JsString(ev.show(e)))

  def format[E: MkValues: ClassTag](implicit ev: Show[E] = Shows.toStringShow[E]): Format[E] =
    Format(reads, writes)
}
