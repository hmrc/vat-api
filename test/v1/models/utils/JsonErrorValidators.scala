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

package v1.models.utils

import play.api.libs.json._
import support.UnitSpec

trait JsonErrorValidators {
  _: UnitSpec =>

  type JsError = (JsPath, Seq[JsonValidationError])
  type JsErrors = Seq[JsError]

  object JsonError {
    val NUMBER_OR_STRING_FORMAT_EXCEPTION = "error.expected.jsnumberorjsstring"
    val NUMBER_FORMAT_EXCEPTION = "error.expected.numberformatexception"
    val BOOLEAN_FORMAT_EXCEPTION = "error.expected.jsboolean"
    val STRING_FORMAT_EXCEPTION = "error.expected.jsstring"
    val JSNUMBER_FORMAT_EXCEPTION = "error.expected.jsnumber"
    val JSARRAY_FORMAT_EXCEPTION = "error.expected.jsarray"
    val PATH_MISSING_EXCEPTION = "error.path.missing"
  }

  implicit class JsErrorOps(err: JsError){
    def path: JsPath = err._1
  }

  implicit class AnyToJson[T : Writes](a: T){
    def toJson: JsValue = Json.toJson(a)
  }

  implicit class JsResultOps[T](res: JsResult[T]) {
    def errors: JsErrors = res match {
      case JsError(jsErrors) => jsErrors
      case JsSuccess(_, _) => fail("A JSON error was expected")
    }
  }

  implicit class JsValueOps(json: JsValue){
    def removeProperty(path: JsPath): JsValue = {
      path.prune(json).fold(
        invalid = errs => fail(s"an error occurred when reading $path: $errs"),
        valid = x => x
      )
    }
  }

  def testMandatoryProperty[A : Reads](json: JsValue)(property: String): Unit = {
    s"the JSON is missing the required property $property" should {

      val jsPath: JsPath = property.split("/").filterNot(_ == "").foldLeft(JsPath())(_ \ _)
      val jsResult = json.removeProperty(jsPath).validate[A]

      "only throw one error" in {
        jsResult.errors.size shouldBe 1
      }

      lazy val jsError = jsResult.errors.head

      "throw the error against the correct property" in {
        jsError.path shouldBe jsPath
      }

      "throw a missing path error" in {
        filterErrorByPath(jsPath, jsError).message shouldBe JsonError.PATH_MISSING_EXCEPTION
      }
    }
  }

  def testPropertyType[T](json: JsValue)(path: String, replacement: JsValue, expectedError: String)
                         (implicit rds: Reads[T]): Unit = {

    val jsPath = path.split("/").filterNot(_ == "").foldLeft(JsPath())(_ \ _)

    lazy val jsResult = {
      val amendedJson: JsValue = jsPath.json.pickBranch.reads(json).fold(
        invalid = errs => fail(s"an error occurred when reading $path : $errs"),
        valid = _ => overwriteJsonProperty(jsPath, json, replacement)
      )
      rds.reads(amendedJson)
    }

    s"the JSON has the wrong data type for path $path" should {

      "only throw one error" in {
        jsResult.errors.size shouldBe 1
      }

      lazy val jsError = jsResult.errors.head

      "throw the error against the correct property" in {
        jsError.path shouldBe jsPath
      }

      "throw an invalid type error" in {
        filterErrorByPath(jsPath, jsError).message shouldBe expectedError
      }
    }
  }

  private def overwriteJsonProperty(path: JsPath, json: JsValue, replacement: JsValue): JsValue = {
    val updateReads: Reads[JsObject] = __.json.update(path.json.put(replacement))
    json.as[JsObject](updateReads)
  }

  private def filterErrorByPath(jsPath: JsPath, jsError: JsError): JsonValidationError = {
    jsError match {
      case (path, err :: Nil) if jsError.path == path => err
      case (path, _ :: Nil)=> fail(s"single error returned but path $path does not match $jsPath")
      case (path, errs @ _ :: _)=> fail(s"multiple errors returned for $path but only 1 required : $errs")
    }
  }
}
