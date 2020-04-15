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

package v1.fixtures.audit

import play.api.libs.json.{JsValue, Json}
import v1.fixtures.audit.AuditResponseFixture._
import v1.models.audit.AuditDetail
import v1.models.auth.UserDetails

object AuditDetailFixture {

  val userType: String = "Agent"
  val agentReferenceNumber: Option[String] = Some("012345678")
  val correlationId = "a1e8057e-fbbc-47a8-a8b478d9f015c253"
  val userDetails = UserDetails("Agent", agentReferenceNumber, "id")

  val auditDetailModelSuccess: AuditDetail =
    AuditDetail(
      userType = userType,
      agentReferenceNumber = agentReferenceNumber,
      response = auditResponseModelWithoutBody,
      `X-CorrelationId` = correlationId
    )

  val auditDetailModelError: AuditDetail =
    auditDetailModelSuccess.copy(
      response = auditResponseModelWithErrors
    )

  val auditDetailJsonSuccess: JsValue = Json.parse(
    s"""
       |{
       |   "userType" : "$userType",
       |   "agentReferenceNumber" : "${agentReferenceNumber.get}",
       |   "response":{
       |     "httpStatus": ${auditResponseModelWithoutBody.httpStatus}
       |   },
       |   "X-CorrelationId": "$correlationId"
       |}
    """.stripMargin
  )

  val auditDetailJsonError: JsValue = Json.parse(
    s"""
       |{
       |   "userType" : "$userType",
       |   "agentReferenceNumber" : "${agentReferenceNumber.get}",
       |   "response": $auditResponseJsonWithErrors,
       |   "X-CorrelationId": "$correlationId"
       |}
     """.stripMargin
  )
}
