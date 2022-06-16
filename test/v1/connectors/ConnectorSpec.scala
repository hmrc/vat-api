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

package v1.connectors

import play.api.http.{HeaderNames, MimeTypes, Status}
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import support.UnitSpec
import uk.gov.hmrc.http.{HeaderCarrier, RequestId}
import v1.controllers.UserRequest
import v1.models.auth.UserDetails

import scala.concurrent.ExecutionContext

trait ConnectorSpec extends UnitSpec
  with Status
  with MimeTypes
  with HeaderNames {

  lazy val baseUrl = "http://test-BaseUrl"
  implicit val correlationId: String = "a1e8057e-fbbc-47a8-a8b4-78d9f015c253"
  implicit val userRequest: UserRequest[AnyContentAsEmpty.type] = UserRequest(UserDetails("Individual",None,"id"),FakeRequest())

  val otherHeaders: Seq[(String, String)] = Seq(
    "Gov-Test-Scenario" -> "DEFAULT",
    "AnotherHeader" -> "HeaderValue"
  )

  val dummyDesHeaderCarrierConfig: HeaderCarrier.Config =
    HeaderCarrier.Config(
      Seq("^not-test-BaseUrl?$".r),
      Seq.empty[String],
      Some("vat-api")
    )

  val dummyHeaderCarrierConfig: HeaderCarrier.Config =
    HeaderCarrier.Config(
      Seq("^not-test-BaseUrl?$".r),
      Seq.empty[String],
      Some("vat-api")
    )

  val requiredDesHeaders: Seq[(String, String)] = Seq(
    "Environment" -> "des-environment",
    "Authorization" -> "Bearer des-token",
    "User-Agent" -> "vat-api",
    "CorrelationId" -> correlationId,
    "Gov-Test-Scenario" -> "DEFAULT"
  )

  val requiredDesHeadersPost: Seq[(String, String)] = Seq(
    "Environment" -> "des-environment",
    "Authorization" -> "Bearer des-token",
    "User-Agent" -> "vat-api",
    "OriginatorID" -> "MDTP",
    "CorrelationId" -> correlationId,
    "Gov-Test-Scenario" -> "DEFAULT"
  )

  val allowedDesHeaders: Seq[String] = Seq(
    "Accept",
    "Gov-Test-Scenario",
    "Content-Type",
    "Location",
    "X-Request-Timestamp",
    "X-Session-Id"
  )

  implicit val hc: HeaderCarrier = HeaderCarrier(requestId = Some(RequestId("123")), otherHeaders = otherHeaders)
  implicit val ec: ExecutionContext = scala.concurrent.ExecutionContext.global
}