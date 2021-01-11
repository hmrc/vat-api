/*
 * Copyright 2021 HM Revenue & Customs
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

class NrsAuditDetailSpec extends UnitSpec {

  val modelSuccess = NrsAuditDetail("1234567", "Bearer test", Some("1234"), None, "")

  val modelError = NrsAuditDetail("1234567", "Bearer test", None, Some(
    Json.parse("""{
      |"test":"test"
      |}""".stripMargin)), "")

  val jsonSuccess: JsValue = Json.parse(
    """{
      |"vrn":"1234567",
      | "authorization":"Bearer test",
      | "nrSubmissionID":"1234",
      | "correlationId":""
      |}""".stripMargin)

  val jsonError: JsValue = Json.parse(
    """
      |{"vrn":"1234567","authorization":"Bearer test","request":{"test":"test"},"correlationId":""}
      |""".stripMargin)
  
  "NrsAuditDetail" when {
    "written to JSON (success)" should {
      "produce the expected JsObject" in {
        Json.toJson(modelSuccess) shouldBe jsonSuccess
      }
    }

    "written to JSON (error)" should {
      "produce the expected JsObject" in {
        Json.toJson(modelError) shouldBe jsonError
      }
    }
  }
}
