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

package v1.models.audit

import play.api.libs.json.Json
import support.UnitSpec
import v1.fixtures.audit.AuditResponseFixture._
class AuditResponseSpec extends UnitSpec {

  "AuditResponse" when {
    "written to JSON with a body" should {
      "produce the expected JsObject" in {
        Json.toJson(auditResponseModelWithBody) shouldBe successAuditResponseWithBody
      }
    }

    "written to JSON with no body" should {
      "produce the expected JsObject" in {
        Json.toJson(auditResponseModelWithoutBody) shouldBe successAuditResponse
      }
    }

    "written to JSON with Audit Errors" should {
      "produce the expected JsObject" in {
        Json.toJson(auditResponseModelWithErrors) shouldBe auditResponseJsonWithErrors
      }
    }
  }
}

