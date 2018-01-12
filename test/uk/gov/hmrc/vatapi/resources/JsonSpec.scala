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

package uk.gov.hmrc.vatapi.resources

import play.api.libs.json._
import uk.gov.hmrc.vatapi.UnitSpec
import uk.gov.hmrc.vatapi.models.ErrorCode.ErrorCode

trait JsonSpec extends UnitSpec {

  def roundTripJson[T](json: T)(implicit format: Format[T]): Unit = {
    val write = Json.toJson(json)
    val read = write.validate[T]
    read.asOpt shouldEqual Some(json)
  }

  def assertJsonIs[T](input: T, expectedOutput: T)(
      implicit format: Format[T]): Unit = {
    val write = Json.toJson(input)
    val read = write.validate[T]

    read.asOpt shouldEqual Some(expectedOutput)
  }

  def assertValidationPasses[T](o: T)(implicit format: Format[T]): Unit = {
    val json = Json.toJson(o)
    json
      .validate[T]
      .fold(
        invalid => fail(invalid.seq.mkString(", ")),
        valid => valid shouldEqual o
      )
  }

  def assertJsonValidationPasses[T: Format](json: JsValue): Unit = {
    json
      .validate[T]
      .fold(
        invalid => fail(invalid.seq.mkString(", ")),
        _ => succeed
      )
  }

  def assertValidationErrorWithCode[T: Format](obj: T,
                                               path: String,
                                               error: ErrorCode): Unit =
    assertValidationErrorsWithCode[T](Json.toJson(obj), Map(path -> Seq(error)))

  def assertValidationErrorWithMessage[T: Format](obj: T,
                                                  path: String,
                                                  message: String): Unit =
    assertValidationErrorsWithMessage[T](Json.toJson(obj),
                                         Map(path -> Seq(message)))

  def assertValidationErrorsWithCode[T: Format](
      value: JsValue,
      pathAndCode: Map[String, Seq[ErrorCode]]): Unit = {

    value.validate[T].asEither match {
      case Left(errs) =>
        errs.groupBy(_._1).toSeq.map {
          case (p, e) =>
            p.toString -> e
              .flatMap(_._2)
              .map(_.args.head.asInstanceOf[ErrorCode])
              .reverse
        } should contain theSameElementsAs pathAndCode.toSeq
      case Right(_) =>
        fail(
          s"Provided object passed json validation. Was expected to fail for the paths: ${pathAndCode.toSeq}")
    }
  }

  def assertValidationErrorsWithMessage[T: Format](
      value: JsValue,
      pathAndMessage: Map[String, Seq[String]]): Unit = {
    val expectedError = pathAndMessage.map {
      case (path, msg) => path -> Seq(JsonValidationError(msg))
    }.toSeq

    value.validate[T].asEither match {
      case Left(errs) =>
        errs.map {
          case (p, e) =>
            p.toString -> e.map(x => JsonValidationError(x.message))
        } should contain theSameElementsAs expectedError
      case Right(_) =>
        fail(
          s"Provided object passed json validation. Was expected to fail for the paths: ${expectedError
            .map(_._1)}")
    }
  }
}
