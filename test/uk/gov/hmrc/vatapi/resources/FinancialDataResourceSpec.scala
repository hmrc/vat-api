/*
 * Copyright 2019 HM Revenue & Customs
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
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.mvc.Http.MimeTypes
import uk.gov.hmrc.http.HttpResponse
import uk.gov.hmrc.vatapi.audit.AuditEvents
import uk.gov.hmrc.vatapi.auth.Organisation
import uk.gov.hmrc.vatapi.mocks.MockAuditService
import uk.gov.hmrc.vatapi.mocks.auth.MockAuthorisationService
import uk.gov.hmrc.vatapi.mocks.connectors.MockFinancialDataConnector
import uk.gov.hmrc.vatapi.models.audit.AuditResponse
import uk.gov.hmrc.vatapi.models.{ErrorResult, Errors, FinancialDataQueryParams}
import uk.gov.hmrc.vatapi.resources.wrappers.{FinancialDataResponse, Response}
import v2.models.audit.AuditError

import scala.concurrent.Future

class FinancialDataResourceSpec extends ResourceSpec
  with MockFinancialDataConnector
  with MockAuthorisationService
  with MockAuditService {

  val authContext = Organisation(None)

  class Setup {
    val testFinancialDataResource = new FinancialDataResource(mockFinancialDataConnector, mockAuthorisationService, mockAppContext, mockAuditService)
    mockAuthAction(vrn).thenReturn(Future.successful(Right(authContext)))
  }

  val queryParams = FinancialDataQueryParams(now.minusDays(100), now.minusDays(20))

  "retrieveLiabilities" should {
    "return a 200 and the correct financial data json" when {
      "for a given valid period" in new Setup {
        val desResponse = FinancialDataResponse(HttpResponse(OK, Some(Jsons.FinancialData.singleLiabilityDesResponse)))

        MockFinancialDataConnector.get(vrn, queryParams)
          .returns(Future.successful(desResponse))

        val expectedResponseBody = Json.toJson(Jsons.FinancialData.oneLiability)
        val result = testFinancialDataResource.retrieveLiabilities(vrn, queryParams)(FakeRequest())
        status(result) shouldBe OK
        contentType(result) shouldBe Some(MimeTypes.JSON)
        contentAsJson(result) shouldBe expectedResponseBody

        val auditResponse = AuditResponse(OK, None, Some(expectedResponseBody))
        MockAuditService.verifyAudit(AuditEvents.retrieveVatLiabilitiesAudit(desResponse.getCorrelationId(),
          authContext.affinityGroup, None, auditResponse))
      }
    }

    "return a 404" when {
      "no data for the period" in new Setup {
        val desResponse = FinancialDataResponse(HttpResponse(OK, Some(Jsons.FinancialData.missingLiabilityDesResponse)))

        MockFinancialDataConnector.get(vrn, queryParams)
          .returns(Future.successful(desResponse))

        val expectedResponseBody = Json.toJson(Jsons.FinancialData.oneLiability)
        val result = testFinancialDataResource.retrieveLiabilities(vrn, queryParams)(FakeRequest())
        status(result) shouldBe NOT_FOUND
        contentAsJson(result) shouldBe Json.toJson(Errors.NotFound)

        val auditResponse = AuditResponse(NOT_FOUND, Some(Seq(AuditError(Errors.NotFound.code))), None)
        MockAuditService.verifyAudit(AuditEvents.retrieveVatLiabilitiesAudit(desResponse.getCorrelationId(),
          authContext.affinityGroup, None, auditResponse))
      }
    }

    "return a 500 with error message" when {
      "when DES backend fails to respond" in new Setup {

        MockFinancialDataConnector.get(vrn, queryParams)
          .returns(Future.failed(new Exception("INTERNAL_ERROR")))

        val result = testFinancialDataResource.retrieveLiabilities(vrn, queryParams)(FakeRequest())
        status(result) shouldBe INTERNAL_SERVER_ERROR

        val auditResponse = AuditResponse(INTERNAL_SERVER_ERROR, Some(Seq(AuditError(Errors.InternalServerError.code))), None)
        MockAuditService.verifyAudit(AuditEvents.retrieveVatLiabilitiesAudit(Response.defaultCorrelationId,
          authContext.affinityGroup, None, auditResponse))
      }
    }
  }

  "retrievePayments" should {
    "return a 200 and the correct financial data json" when {
      "for a given valid period" in new Setup {
        val desResponse = FinancialDataResponse(HttpResponse(OK, Some(Jsons.FinancialData.singlePaymentDesResponse)))

        MockAuditService.audit()
          .returns(EitherT[Future, ErrorResult, Unit](Future.successful(Right(()))))

        MockFinancialDataConnector.get(vrn, queryParams)
          .returns(Future.successful(desResponse))

        val expectedResponseBody = Json.toJson(Jsons.FinancialData.onePayment)
        val result = testFinancialDataResource.retrievePayments(vrn, queryParams)(FakeRequest())
        status(result) shouldBe OK
        contentType(result) shouldBe Some(MimeTypes.JSON)
        contentAsJson(result) shouldBe expectedResponseBody

        val auditResponse = AuditResponse(OK, None, Some(expectedResponseBody))
        MockAuditService.verifyAudit(AuditEvents.retrieveVatPaymentsAudit(desResponse.getCorrelationId(),
          authContext.affinityGroup, None, auditResponse))
      }
    }

    "return a 404" when {
      "no data for the period" in new Setup {
        val desResponse = FinancialDataResponse(HttpResponse(OK, Some(Jsons.FinancialData.missingPaymentDesResponse)))

        MockAuditService.audit()
          .returns(EitherT[Future, ErrorResult, Unit](Future.successful(Right(()))))

        MockFinancialDataConnector.get(vrn, queryParams)
          .returns(Future.successful(desResponse))

        val expectedResponseBody = Json.toJson(Jsons.FinancialData.onePayment)
        val result = testFinancialDataResource.retrievePayments(vrn, queryParams)(FakeRequest())
        status(result) shouldBe NOT_FOUND
        contentAsJson(result) shouldBe Json.toJson(Errors.NotFound)

        val auditResponse = AuditResponse(NOT_FOUND, Some(Seq(AuditError(Errors.NotFound.code))), None)
        MockAuditService.verifyAudit(AuditEvents.retrieveVatPaymentsAudit(desResponse.getCorrelationId(),
          authContext.affinityGroup, None, auditResponse))
      }
    }

    "return a 500 with error message" when {
      "when DES backend fails to respond" in new Setup {

        MockFinancialDataConnector.get(vrn, queryParams)
          .returns(Future.failed(new Exception("INTERNAL_ERROR")))

        val result = testFinancialDataResource.retrievePayments(vrn, queryParams)(FakeRequest())
        status(result) shouldBe INTERNAL_SERVER_ERROR

        val auditResponse = AuditResponse(INTERNAL_SERVER_ERROR, Some(Seq(AuditError(Errors.InternalServerError.code))), None)
        MockAuditService.verifyAudit(AuditEvents.retrieveVatPaymentsAudit(Response.defaultCorrelationId,
          authContext.affinityGroup, None, auditResponse))
      }
    }
  }
}
