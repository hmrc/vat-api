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

import cats.data.EitherT
import play.api.libs.json.{JsValue, Json}
import play.api.test.FakeRequest
import uk.gov.hmrc.http.HttpResponse
import uk.gov.hmrc.vatapi.VatReturnDeclarationFixture
import uk.gov.hmrc.vatapi.httpparsers.NRSData
import uk.gov.hmrc.vatapi.mocks.connectors.MockVatReturnsConnector
import uk.gov.hmrc.vatapi.mocks.orchestrators.MockVatReturnsOrchestrator
import uk.gov.hmrc.vatapi.mocks.services.MockAuditService
import uk.gov.hmrc.vatapi.models.ErrorResult
import uk.gov.hmrc.vatapi.resources.wrappers.VatReturnResponse

import scala.concurrent.Future

class VatReturnsResourceSpec extends ResourceSpec
  with MockVatReturnsConnector
  with MockAuditService
  with MockVatReturnsOrchestrator {

  class Setup {
    val resource = new VatReturnsResource{
      override val connector = mockVatReturnsConnector
      override val orchestrator = mockVatReturnsOrchestrator
      override val auditService = mockAuditService
      override val authService = mockAuthorisationService
      override val appContext = mockAppContext
    }
    mockAuthAction(vrn)
  }

  val vatReturnsDeclaration = VatReturnDeclarationFixture.vatReturnDeclaration
  val vatReturnDeclarationJson = VatReturnDeclarationFixture.vatReturnDeclarationJson

  val nrsSubmissionId = "test-sub-id"
  val nrsTimestamp = "test-timestamp"
  val nrsData = NRSData(nrsSubmissionId, "test-cades-ts", nrsTimestamp)

  val vatReturnResponseJson = Json.obj("test" -> "json")
  val vatReturnResponse = VatReturnResponse(HttpResponse(200, Some(vatReturnResponseJson))).withNrsData(nrsData)

  "submitVatReturn" should {
    "return a 201 with the correct response and headers" when {
      "the orchestrator returns a valid response" in new Setup {
        MockVatReturnsOrchestrator.submitVatReturn(vrn, vatReturnsDeclaration)
          .returns(Future.successful(Right(vatReturnResponse)))

        MockAuditService.audit()
          .returns(EitherT[Future, ErrorResult, Unit](Future.successful(Right(()))))

        val request = FakeRequest().withBody[JsValue](vatReturnDeclarationJson)
        val result = resource.submitVatReturn(vrn)(request)

        status(result) shouldBe CREATED
        contentType(result) shouldBe Some(JSON)
        contentAsJson(result) shouldBe vatReturnResponseJson

        val headersMap = headers(result)
        headersMap("Receipt-ID") shouldBe nrsSubmissionId
        headersMap("Receipt-Timestamp") shouldBe nrsTimestamp
        headersMap("Receipt-Signature") shouldBe "NOT CURRENTLY IMPLEMENTED"
      }
    }
  }
}
