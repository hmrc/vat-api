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

package uk.gov.hmrc.vatapi.resources

import org.joda.time.DateTime
import play.api.http.MimeTypes
import play.api.libs.json.{JsValue, Json}
import play.api.test.FakeRequest
import uk.gov.hmrc.http.HttpResponse
import uk.gov.hmrc.vatapi.VatReturnDeclarationFixture
import uk.gov.hmrc.vatapi.assets.TestConstants
import uk.gov.hmrc.vatapi.audit.AuditEvents
import uk.gov.hmrc.vatapi.auth.Organisation
import uk.gov.hmrc.vatapi.httpparsers.NRSData
import uk.gov.hmrc.vatapi.mocks.MockAuditService
import uk.gov.hmrc.vatapi.mocks.connectors.MockVatReturnsConnector
import uk.gov.hmrc.vatapi.mocks.orchestrators.MockVatReturnsOrchestrator
import uk.gov.hmrc.vatapi.models.Errors.TaxPeriodNotEnded
import uk.gov.hmrc.vatapi.models.audit.AuditResponse
import uk.gov.hmrc.vatapi.models.des.{DesError, DesErrorCode, VatReturn}
import uk.gov.hmrc.vatapi.models.{Errors, InternalServerErrorResult}
import uk.gov.hmrc.vatapi.resources.wrappers.{Response, VatReturnResponse}
import v2.models.audit.AuditError

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future


class VatReturnsResourceSpec extends ResourceSpec
  with MockVatReturnsConnector
  with MockVatReturnsOrchestrator
  with MockAuditService {

  val authContext = Organisation(None)

  class Setup {
    val resource = new VatReturnsResource(
      mockVatReturnsConnector,
      mockVatReturnsOrchestrator,
      mockAuthorisationService,
      mockAuditService,
      cc
    )
  }

  val vatReturnsDeclaration = VatReturnDeclarationFixture.vatReturnDeclaration
  val vatReturnDeclarationJson = VatReturnDeclarationFixture.vatReturnDeclarationJson

  val desVatReturn = VatReturn(periodKey = "#001",
    vatDueSales = -3600.15,
    vatDueAcquisitions = 12000.05,
    vatDueTotal = 8399.90,
    vatReclaimedCurrPeriod = 124.15,
    vatDueNet = 8275.75,
    totalValueSalesExVAT = 1000,
    totalValuePurchasesExVAT = 200,
    totalValueGoodsSuppliedExVAT = 100,
    totalAllAcquisitionsExVAT = 100,
    agentReferenceNumber = Some("MK001"),
    receivedAt = Some(DateTime.parse("2018-02-14T09:32:15Z")))

  val clientVatReturnJson = Json.parse(
    """{
      |  "periodKey": "#001",
      |  "vatDueSales": -3600.15,
      |  "vatDueAcquisitions": 12000.05,
      |  "totalVatDue": 8399.9,
      |  "vatReclaimedCurrPeriod": 124.15,
      |  "netVatDue": 8275.75,
      |  "totalValueSalesExVAT": 1000,
      |  "totalValuePurchasesExVAT": 200,
      |  "totalValueGoodsSuppliedExVAT": 100,
      |  "totalAcquisitionsExVAT": 100
      |}""".stripMargin)

  val nrsSubmissionId = "test-sub-id"
  val nrsTimestamp = "test-timestamp"
  val nrsData = NRSData(nrsSubmissionId, "This has been deprecated - DO NOT USE", nrsTimestamp)

  val vatReturnResponseJson = Json.toJson(TestConstants.VatReturn.vatReturnsDes)
  val vatReturnResponse = VatReturnResponse(HttpResponse(200, Some(vatReturnResponseJson))).withNrsData(nrsData)
  val duplicateSubmissionResponse =
    VatReturnResponse(HttpResponse(
      CONFLICT,
      responseJson = Some(Json.toJson(DesError(DesErrorCode.DUPLICATE_SUBMISSION, "The VAT return was already submitted for the given period.")))
    ))
  val taxPeriodNotEndedResponse =
    VatReturnResponse(HttpResponse(
      FORBIDDEN,
      responseJson = Some(Json.toJson(DesError(DesErrorCode.TAX_PERIOD_NOT_ENDED, "The remote endpoint has indicated that the submission is for an tax period that has not ended.")))
    ))

  "submitVatReturn" should {
    "return a 201 with the correct response and headers" when {
      "the orchestrator returns a valid response" in new Setup {

        mockAuthActionWithNrs(vrn).thenReturn(Future.successful(Right(authContext)))
        MockVatReturnsOrchestrator.submitVatReturn(vrn, vatReturnsDeclaration)
          .returns(Future.successful(Right(vatReturnResponse)))

        val request = FakeRequest().withBody[JsValue](vatReturnDeclarationJson)
        val result = resource.submitVatReturn(vrn)(request)

        status(result) shouldBe CREATED
        contentType(result) shouldBe Some(JSON)
        contentAsJson(result) shouldBe vatReturnResponseJson

        val headersMap = headers(result)
        headersMap("Receipt-ID") shouldBe nrsSubmissionId
        headersMap("Receipt-Timestamp") shouldBe nrsTimestamp
        headersMap("Receipt-Signature") shouldBe "This has been deprecated - DO NOT USE"

        val auditResponse = AuditResponse(CREATED, None, Some(vatReturnResponseJson))
        MockAuditService.verifyAudit(AuditEvents.submitVatReturn(vatReturnResponse.getCorrelationId,
          authContext.affinityGroup, Some(nrsSubmissionId), None, auditResponse))
      }
    }

    "return a 500 with the message" when {
      "the orchestrator returns error from NRS submission" in new Setup {

        mockAuthActionWithNrs(vrn).thenReturn(Future.successful(Right(authContext)))
        MockVatReturnsOrchestrator.submitVatReturn(vrn, vatReturnsDeclaration)
          .returns(Future.successful(Left(InternalServerErrorResult(Errors.InternalServerError.message))))

        val request = FakeRequest().withBody[JsValue](vatReturnDeclarationJson)
        val result = resource.submitVatReturn(vrn)(request)

        status(result) shouldBe INTERNAL_SERVER_ERROR

        val auditResponse = AuditResponse(INTERNAL_SERVER_ERROR, Some(Seq(AuditError(Errors.InternalServerError.code))), None)
        MockAuditService.verifyAudit(AuditEvents.submitVatReturn(Response.defaultCorrelationId,
          authContext.affinityGroup, None, None, auditResponse))
      }
    }

    "return a 403 duplication submission" when {
      "re-submit the same vat return" in new Setup {

        mockAuthActionWithNrs(vrn).thenReturn(Future.successful(Right(authContext)))
        MockVatReturnsOrchestrator.submitVatReturn(vrn, vatReturnsDeclaration)
          .returns(Future.successful(Right(duplicateSubmissionResponse)))

        val request = FakeRequest().withBody[JsValue](vatReturnDeclarationJson)
        val result = resource.submitVatReturn(vrn)(request)

        status(result) shouldBe FORBIDDEN

        val auditResponse = AuditResponse(FORBIDDEN, Some(Seq(AuditError(Errors.DuplicateVatSubmission.code))), None)
        MockAuditService.verifyAudit(AuditEvents.submitVatReturn(duplicateSubmissionResponse.getCorrelationId,
          authContext.affinityGroup, None, None, auditResponse))
      }
    }
    "return a 403 tax period not ended error" when {
      "des indicates that the return has been sent too early" in new Setup {

        mockAuthActionWithNrs(vrn).thenReturn(Future.successful(Right(authContext)))
        MockVatReturnsOrchestrator.submitVatReturn(vrn, vatReturnsDeclaration)
          .returns(Future.successful(Right(taxPeriodNotEndedResponse)))

        val request = FakeRequest().withBody[JsValue](vatReturnDeclarationJson)
        val result = resource.submitVatReturn(vrn)(request)

        status(result) shouldBe FORBIDDEN
        contentAsJson(result) shouldBe Json.toJson(TaxPeriodNotEnded)

        val auditResponse = AuditResponse(FORBIDDEN, Some(Seq(AuditError(Errors.TaxPeriodNotEnded.code))), None)
        MockAuditService.verifyAudit(AuditEvents.submitVatReturn(taxPeriodNotEndedResponse.getCorrelationId,
          authContext.affinityGroup, None, None, auditResponse))
      }
    }
    "return an INTERNAL_SERVER_ERROR" when {
      "backend failed to respond" in new Setup {

        mockAuthActionWithNrs(vrn).thenReturn(Future.successful(Right(authContext)))
        MockVatReturnsOrchestrator.submitVatReturn(vrn, vatReturnsDeclaration)
          .returns(Future.failed(new Exception("DES FAILED")))

        val request = FakeRequest().withBody[JsValue](vatReturnDeclarationJson)
        val result = resource.submitVatReturn(vrn)(request)

        status(result) shouldBe INTERNAL_SERVER_ERROR

        val auditResponse = AuditResponse(INTERNAL_SERVER_ERROR, Some(Seq(AuditError(Errors.InternalServerError.code))), None)
        MockAuditService.verifyAudit(AuditEvents.submitVatReturn(Response.defaultCorrelationId,
          authContext.affinityGroup, None, None, auditResponse))
      }
    }
  }

  "retrieveVatReturn" should {
    "return a 200 " when {
      "a valid vrn and period key is supplied" in new Setup {

        mockAuthAction(vrn).thenReturn(Future.successful(Right(authContext)))
        val successResponse = VatReturnResponse(HttpResponse(OK, responseJson =
          Some(Json.toJson(desVatReturn))))
        retrieveVatReturn(vrn, "#001")(successResponse)

        val result = resource.retrieveVatReturns(vrn, "#001")(FakeRequest())
        status(result) shouldBe OK
        contentType(result) shouldBe Some(MimeTypes.JSON)
        contentAsJson(result) shouldBe clientVatReturnJson

        val auditResponse = AuditResponse(OK, None, Some(clientVatReturnJson))
        MockAuditService.verifyAudit(AuditEvents.retrieveVatReturnsAudit(successResponse.getCorrelationId,
          authContext.affinityGroup, None, auditResponse))
      }
    }

    "return a 500" when {
      "des backend return 200 with empty data" in new Setup {

        mockAuthAction(vrn).thenReturn(Future.successful(Right(authContext)))
        val successResponse = VatReturnResponse(HttpResponse(OK, responseJson =
          Some(Json.toJson(""))))
        retrieveVatReturnFailed(vrn, "#001")

        val result = resource.retrieveVatReturns(vrn, "#001")(FakeRequest())
        status(result) shouldBe INTERNAL_SERVER_ERROR

        val auditResponse = AuditResponse(INTERNAL_SERVER_ERROR, Some(Seq(AuditError(Errors.InternalServerError.code))), None)
        MockAuditService.verifyAudit(AuditEvents.retrieveVatReturnsAudit(successResponse.getCorrelationId,
          authContext.affinityGroup, None, auditResponse))
      }
    }

    "return an 400" when {
      "period key invalid" in new Setup {

        mockAuthAction(vrn).thenReturn(Future.successful(Right(authContext)))
        val result = resource.retrieveVatReturns(vrn, "xxxxx")(FakeRequest())
        status(result) shouldBe BAD_REQUEST

        val auditResponse = AuditResponse(BAD_REQUEST, Some(Seq(AuditError(Errors.InvalidPeriodKey.code))), None)
        MockAuditService.verifyAudit(AuditEvents.retrieveVatReturnsAudit(Response.defaultCorrelationId,
          authContext.affinityGroup, None, auditResponse))
      }
    }

    "return a failure error code" when {
      "des backend returns an error code" in new Setup {

        mockAuthAction(vrn).thenReturn(Future.successful(Right(authContext)))
        val failureResponse = VatReturnResponse(HttpResponse(BAD_REQUEST, responseJson =
          Some(Json.parse("""{"code" : "INVALID_VRN", "reason": ""}"""))))
        retrieveVatReturn(vrn, "#001")(failureResponse)

        val result = resource.retrieveVatReturns(vrn, "#001")(FakeRequest())
        status(result) shouldBe BAD_REQUEST
        contentType(result) shouldBe Some(MimeTypes.JSON)
        contentAsJson(result) shouldBe Json.toJson(Errors.VrnInvalid)

        val auditResponse = AuditResponse(BAD_REQUEST, Some(Seq(AuditError(Errors.VrnInvalid.code))), None)
        MockAuditService.verifyAudit(AuditEvents.retrieveVatReturnsAudit(failureResponse.getCorrelationId,
          authContext.affinityGroup, None, auditResponse))
      }
    }
  }
}
