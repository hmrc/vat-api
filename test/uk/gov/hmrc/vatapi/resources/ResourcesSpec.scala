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

import play.api.libs.json.{JsPath, JsonValidationError}
import uk.gov.hmrc.vatapi.UnitSpec
import uk.gov.hmrc.vatapi.models.{ErrorCode, Errors}

class ResourcesSpec extends UnitSpec {

  "Errors.badRequest" should {

    "translate Json string validation error to the appropriate error code" in {
      val errors = Seq((JsPath \ "a", Seq(JsonValidationError("error.expected.jsstring"))))
      Errors.badRequest(errors).errors.head.code shouldBe ErrorCode.INVALID_STRING_VALUE.toString
    }

    "translate Json numeric validation error to the appropriate error code" in {
      val errors = Seq((JsPath \ "a", Seq(JsonValidationError("error.expected.numberformatexception"))))
      Errors.badRequest(errors).errors.head.code shouldBe ErrorCode.INVALID_NUMERIC_VALUE.toString
    }
  }
}
