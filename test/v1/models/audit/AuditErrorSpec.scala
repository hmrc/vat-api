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

package v1.models.audit

import play.api.libs.json.{JsValue, Json}
import support.UnitSpec

class AuditErrorSpec extends UnitSpec {

  private val auditErrorModel: AuditError = AuditError(errorCode = "FORMAT_NINO")

  private val auditErrorJson: JsValue = Json.parse(
    """
      |{
      |  "errorCode": "FORMAT_NINO"
      |}
    """.stripMargin
  )

  "AuditError" when {
    "read from valid JSON" should {
      "produce the expected AuditError object" in {
        auditErrorJson.as[AuditError] shouldBe auditErrorModel
      }
    }

    "written to JSON" should {
      "produce the expected JsObject" in {
        Json.toJson(auditErrorModel) shouldBe auditErrorJson
      }
    }
  }
}
