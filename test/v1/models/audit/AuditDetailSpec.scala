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
import v1.fixtures.audit.GenericAuditDetailFixture._
import v1.fixtures.audit.AuditResponseFixture._

class AuditDetailSpec extends UnitSpec {

  "AuditDetail" when {
    "written to JSON (success)" should {
      "produce the expected JsObject" in {
        Json.toJson(genericAuditDetailModelSuccess) shouldBe genericAuditDetailJsonSuccess
        Json.toJson(AuditDetail(userDetails, nino, correlationId, auditResponseModelWithBody)) shouldBe genericAuditDetailJsonSuccess
      }
    }

    "written to JSON (error)" should {
      "produce the expected JsObject" in {
        Json.toJson(genericAuditDetailModelError) shouldBe genericAuditDetailJsonError
      }
    }
  }
}
