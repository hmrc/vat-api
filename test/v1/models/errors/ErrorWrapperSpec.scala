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

package v1.models.errors

import play.api.libs.json.Json
import support.UnitSpec

class ErrorWrapperSpec extends UnitSpec {

  val correlationId = "X-123"

  "Rendering a error response with one error" should {
    val error = ErrorWrapper(None, VrnFormatError, None)

    val json = Json.parse(
      """
        |{
        |   "code": "VRN_INVALID",
        |   "message": "The provided Vrn is invalid"
        |}
      """.stripMargin
    )

    "generate the correct JSON" in {
      Json.toJson(error) shouldBe json
    }
  }

  "Rendering a error response with one custom error" should {
    val error = ErrorWrapper(None, PeriodKeyFormatError, None)

    val json = Json.parse(
      """
        |{
        |  "code": "INVALID_REQUEST",
        |  "message": "Invalid request",
        |  "errors": [
        |      {
        |        "code": "PERIOD_KEY_INVALID",
        |        "message": "Invalid period key"
        |      }
        |    ]
        |}
      """.stripMargin
    )

    "generate the correct JSON" in {
      Json.toJson(error) shouldBe json
    }
  }

  "Rendering a error response with one error and an empty sequence of errors" should {
    val error = ErrorWrapper(None, VrnFormatError, Some(Seq.empty))

    val json = Json.parse(
      """
        |{
        |   "code": "VRN_INVALID",
        |   "message": "The provided Vrn is invalid"
        |}
      """.stripMargin
    )

    "generate the correct JSON" in {
      Json.toJson(error) shouldBe json
    }
  }

  "Rendering a error response with multiple errors including a custom error" should {
    val error = ErrorWrapper(None, BadRequestError,
      Some (
        Seq(
          VrnFormatError,
          PeriodKeyFormatError
        )
      )
    )

    val json = Json.parse(
      """
        |{
        |   "code": "INVALID_REQUEST",
        |   "message": "Invalid request",
        |   "errors": [
        |       {
        |         "code": "VRN_INVALID",
        |         "message": "The provided Vrn is invalid"
        |       },
        |       {
        |         "code": "PERIOD_KEY_INVALID",
        |         "message": "Invalid period key"
        |       }
        |   ]
        |}
      """.stripMargin
    )

    "generate the correct JSON" in {
      Json.toJson(error) shouldBe json
    }
  }
}
