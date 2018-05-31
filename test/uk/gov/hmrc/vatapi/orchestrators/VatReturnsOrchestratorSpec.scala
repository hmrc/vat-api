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

package uk.gov.hmrc.vatapi.orchestrators

import cats.data.EitherT
import org.scalatest.EitherValues
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.mockito.MockitoSugar
import org.scalatestplus.play.OneAppPerSuite
import play.api.http.Status.{BAD_REQUEST, OK}
import play.api.libs.json.Json
import play.api.test.FakeRequest
import uk.gov.hmrc.domain.Vrn
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}
import uk.gov.hmrc.vatapi.UnitSpec
import uk.gov.hmrc.vatapi.assets.TestConstants.Auth._
import uk.gov.hmrc.vatapi.assets.TestConstants.NRSResponse._
import uk.gov.hmrc.vatapi.assets.TestConstants.VatReturn._
import uk.gov.hmrc.vatapi.httpparsers.NrsError
import uk.gov.hmrc.vatapi.mocks.services.{MockAuditService, MockNRSService, MockVatReturnsService}
import uk.gov.hmrc.vatapi.models.des.{DesError, DesErrorCode}
import uk.gov.hmrc.vatapi.models.{ErrorResult, Errors, InternalServerErrorResult, VatReturnDeclaration}
import uk.gov.hmrc.vatapi.resources.AuthRequest
import uk.gov.hmrc.vatapi.resources.wrappers.VatReturnResponse
import uk.gov.hmrc.vatapi.services.{NRSService, VatReturnsService}

import scala.concurrent.Future

class VatReturnsOrchestratorSpec extends UnitSpec with OneAppPerSuite with MockitoSugar with ScalaFutures with EitherValues
  with MockNRSService
  with MockVatReturnsService
  with MockAuditService {

  object TestVatReturnsOrchestrator extends VatReturnsOrchestrator {
    override val nrsService: NRSService = mockNrsService
    override val vatReturnsService: VatReturnsService = mockVatReturnsService
    override val auditService = mockAuditService
  }

  implicit val hc: HeaderCarrier = HeaderCarrier()
  implicit val req: AuthRequest[_] = new AuthRequest(orgAuthContextWithNrsData, FakeRequest().withHeaders(("Authorization", "Bearer test-bearer-token")))

  val vatReturnSuccessResponse = VatReturnResponse(HttpResponse(OK, responseJson = Some(Json.toJson(vatReturnsDes))))
  val vatReturninvalidPayloadResponse =
    VatReturnResponse(HttpResponse(
      BAD_REQUEST,
      responseJson = Some(Json.toJson(DesError(DesErrorCode.INVALID_PAYLOAD, "Submission has not passed validation. Invalid parameter Payload.")))
    ))

  "VatReturnsOrchestrator.submit" when {

    lazy val testVrn: Vrn = Vrn("123456789")
    def result(submission: VatReturnDeclaration): Future[Either[ErrorResult, VatReturnResponse]] =
      TestVatReturnsOrchestrator.submitVatReturn(testVrn, submission)

    "an unsuccessful response is returned from NRS Service" should {
      "return an NRS Failure" in {
        setupNrsSubmission(testVrn, vatReturnDeclaration)(Left(NrsError))
        extractAwait(result(vatReturnDeclaration)) shouldBe Left(InternalServerErrorResult(Errors.InternalServerError.message))
      }
    }

    "a successful response is returned from NRS Service" should {
      "retrieve a VatReturnsResponse from VatReturnsService" in {
        MockAuditService.audit()
          .returns(EitherT[Future, ErrorResult, Unit](Future.successful(Right(()))))
        setupNrsSubmission(testVrn, vatReturnDeclaration)(Right(nrsData))
        setupVatReturnSubmission(testVrn, desVatReturnDeclaration(timestamp))(vatReturnSuccessResponse)
        extractAwait(result(vatReturnDeclaration)) shouldBe Right(vatReturnSuccessResponse)
      }
    }
  }
}
