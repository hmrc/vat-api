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

package v1.controllers.requestParsers.validators.validations

import play.api.libs.json.{Json, Reads}
import support.UnitSpec
import v1.models.errors.MtdError
import v1.models.utils.JsonErrorValidators

class JsonFormatValidationSpec extends UnitSpec with JsonErrorValidators {

  case class TestDataObject(fieldOne: String, fieldTwo: String)

  implicit val testDataObjectReads: Reads[TestDataObject] = Json.reads[TestDataObject]

  val someError = MtdError("SOME_CODE", "some message")

  "validate" should {
    "return no errors" when {
      "when a valid JSON object with all the necessary fields is supplied" in {

        val validJson = Json.parse("""{ "fieldOne" : "Something", "fieldTwo" : "SomethingElse" }""")

        val validationResult = JsonFormatValidation.validate[TestDataObject](validJson, someError)
        validationResult shouldBe empty
      }
    }

    "return an error " when {
      "when a required field is missing" in {

        // fieldTwo is missing
        val json = Json.parse("""{ "fieldOne" : "Something" }""")

        val validationResult = JsonFormatValidation.validate[TestDataObject](json, someError)
        validationResult shouldBe List(someError)
      }

    }

  }
}
