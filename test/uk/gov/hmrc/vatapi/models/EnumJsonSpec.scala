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

package uk.gov.hmrc.vatapi.models

import org.scalatestplus.play.guice.GuiceOneAppPerTest
import play.api.libs.json._
import uk.gov.hmrc.vatapi.UnitSpec

class EnumJsonSpec extends UnitSpec with GuiceOneAppPerTest {

  val json = Json.parse(
    """{
      |"INVALID_VALUE_LENGTH": "Enum json spec"
      |}""".stripMargin)

  val jsString = JsString("INVALID_VALUE_LENGTH")

  "enumReads" should {
    "return a valid error msg" when {
      "you pass json not of type JsString" in {
        EnumJson.enumReads(ErrorCode, None).reads(json) shouldBe JsError(__ , JsonValidationError("String value expected", ErrorCode.INVALID_TYPE))
      }

      "the error code is not in enum" in {
        EnumJson.enumReads(ErrorCode, None).reads(jsString) shouldBe
          JsError(__ , JsonValidationError(s"Enumeration expected of type: '${ErrorCode.getClass}', but it does not contain 'INVALID_VALUE_LENGTH'",
            ErrorCode.INVALID_VALUE))
      }
    }
  }
}
