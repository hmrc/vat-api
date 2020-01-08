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

import play.api.libs.json.{JsValue, Json}
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import play.mvc.Http.MimeTypes
import uk.gov.hmrc.auth.core.Enrolments
import uk.gov.hmrc.http.HttpResponse
import uk.gov.hmrc.vatapi.audit.AuditEvents
import uk.gov.hmrc.vatapi.auth.Organisation
import uk.gov.hmrc.vatapi.mocks.MockAuditService
import uk.gov.hmrc.vatapi.mocks.auth.MockAuthorisationService
import uk.gov.hmrc.vatapi.mocks.connectors.MockObligationsConnector
import uk.gov.hmrc.vatapi.models.audit.AuditResponse
import uk.gov.hmrc.vatapi.models.{Errors, ObligationsQueryParams}
import uk.gov.hmrc.vatapi.resources.wrappers.{ObligationsResponse, Response}
import v2.models.audit.AuditError

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class ObligationsResourceSpec extends ResourceSpec
  with MockObligationsConnector
  with MockAuthorisationService
  with MockAuditService {

  val queryParams = ObligationsQueryParams(Some(now.minusDays(7)), Some(now), Some("O"))
  val queryParamsWithNoStatus = ObligationsQueryParams(Some(now.minusDays(7)), Some(now))
  val desObligationsJson: JsValue = Jsons.Obligations.desResponse(vrn)
  val desObligationsNoDetailsJson: JsValue = Jsons.Obligations.desResponseWithoutObligationDetails(vrn)

  val enrolments = Enrolments(Set.empty)
  val arn = "someAgentRefNo"
  val clientId = "someClientId"
  val clientObligationsJson: JsValue = Jsons.Obligations()

  val authContext = Organisation(None)

  class Setup {
    val testObligationResource = new ObligationsResource(mockObligationsConnector, mockAuthorisationService, mockAuditService, cc)
    mockAuthAction(vrn).thenReturn(Future.successful(Right(authContext)))

    val fakeRequest: FakeRequest[AnyContentAsEmpty.type] = FakeRequest().withHeaders("X-Client-Id" -> clientId)
  }

  "retrieveObligations" should {
    "return a 200 and the correct obligations json" when {
      "DES returns a 200 response with the correct obligations body" in new Setup {

        val desResponse = ObligationsResponse(HttpResponse(OK, Some(desObligationsJson)))

        MockObligationsConnector.get(vrn, queryParams)
          .returns(Future.successful(desResponse))

        val result = testObligationResource.retrieveObligations(vrn, queryParams)(fakeRequest)
        status(result) shouldBe OK
        contentType(result) shouldBe Some(MimeTypes.JSON)
        contentAsJson(result) shouldBe clientObligationsJson

        val auditResponse = AuditResponse(OK, None, Some(clientObligationsJson))
        MockAuditService.verifyAudit(AuditEvents.retrieveVatObligationsAudit(desResponse.getCorrelationId,
          authContext.affinityGroup, None, clientId, auditResponse))
      }
    }

    "return a 200 and the correct obligations json for no status" when {
      "DES returns a 200 response with the correct obligations body" in new Setup {
        val desResponse = ObligationsResponse(HttpResponse(OK, Some(desObligationsJson)))

        MockObligationsConnector.get(vrn, queryParamsWithNoStatus)
          .returns(Future.successful(desResponse))

        val result = testObligationResource.retrieveObligations(vrn, queryParamsWithNoStatus)(fakeRequest)
        status(result) shouldBe OK
        contentType(result) shouldBe Some(MimeTypes.JSON)
        contentAsJson(result) shouldBe clientObligationsJson

        val auditResponse = AuditResponse(OK, None, Some(clientObligationsJson))
        MockAuditService.verifyAudit(AuditEvents.retrieveVatObligationsAudit(desResponse.getCorrelationId,
          authContext.affinityGroup, None, clientId, auditResponse))
      }
    }

    "return a 404 with a json body" when {
      "DES returns a 200 response with the correct obligations body but the obligationDetails are empty" in new Setup {
        val desResponse = ObligationsResponse(HttpResponse(OK, Some(desObligationsNoDetailsJson)))

        MockObligationsConnector.get(vrn, queryParams)
          .returns(Future.successful(desResponse))

        val result = testObligationResource.retrieveObligations(vrn, queryParams)(fakeRequest)
        status(result) shouldBe NOT_FOUND
        contentType(result) shouldBe Some(MimeTypes.JSON)
        contentAsJson(result) shouldBe Json.toJson(Errors.NotFound)

        val auditResponse = AuditResponse(NOT_FOUND, Some(Seq(AuditError(Errors.NotFound.code))), None)
        MockAuditService.verifyAudit(AuditEvents.retrieveVatObligationsAudit(desResponse.getCorrelationId,
          authContext.affinityGroup, None, clientId, auditResponse))
      }
    }

    "return a 500 with a json body" when {
      "DES returns a 200 response but the body does not match the expected obligations format" in new Setup {
        val invalidDesResponse = ObligationsResponse(HttpResponse(OK, Some(Json.obj("invalid" -> "des json"))))

        MockObligationsConnector.get(vrn, queryParams)
          .returns(Future.successful(invalidDesResponse))

        val result = testObligationResource.retrieveObligations(vrn, queryParams)(fakeRequest)
        status(result) shouldBe INTERNAL_SERVER_ERROR
        contentType(result) shouldBe Some(MimeTypes.JSON)
        contentAsJson(result) shouldBe Json.toJson(Errors.InternalServerError)

        val auditResponse = AuditResponse(INTERNAL_SERVER_ERROR, Some(Seq(AuditError(Errors.InternalServerError.code))), None)
        MockAuditService.verifyAudit(AuditEvents.retrieveVatObligationsAudit(invalidDesResponse.getCorrelationId,
          authContext.affinityGroup, None, clientId, auditResponse))
      }
    }

    "return a 500 with a json body for a failure" when {
      "DES returns some unexpected error" in new Setup {

        MockObligationsConnector.get(vrn, queryParams)
          .returns(Future.failed(new Exception("Connection refused error")))

        val result = testObligationResource.retrieveObligations(vrn, queryParams)(fakeRequest)
        status(result) shouldBe INTERNAL_SERVER_ERROR
        contentType(result) shouldBe Some(MimeTypes.JSON)
        contentAsJson(result) shouldBe Json.toJson(Errors.InternalServerError)

        val auditResponse = AuditResponse(INTERNAL_SERVER_ERROR, Some(Seq(AuditError(Errors.InternalServerError.code))), None)
        MockAuditService.verifyAudit(AuditEvents.retrieveVatObligationsAudit(Response.defaultCorrelationId,
          authContext.affinityGroup, None, clientId, auditResponse))
      }
    }

    "DES returns a 200 response with a non-json body" in new Setup {
      val nonJsonDesResponse = ObligationsResponse(HttpResponse(OK, responseString = Some("non-json")))

      MockObligationsConnector.get(vrn, queryParams)
        .returns(Future.successful(nonJsonDesResponse))

      val result = testObligationResource.retrieveObligations(vrn, queryParams)(fakeRequest)
      status(result) shouldBe INTERNAL_SERVER_ERROR
      contentType(result) shouldBe Some(MimeTypes.JSON)
      contentAsJson(result) shouldBe Json.toJson(Errors.InternalServerError)

      val auditResponse = AuditResponse(INTERNAL_SERVER_ERROR, Some(Seq(AuditError(Errors.InternalServerError.code))), None)
      MockAuditService.verifyAudit(AuditEvents.retrieveVatObligationsAudit(nonJsonDesResponse.getCorrelationId,
        authContext.affinityGroup, None, clientId, auditResponse))
    }

    "DES returns a 200 response with an empty body" in new Setup {
      val nonJsonDesResponse = ObligationsResponse(HttpResponse(OK))

      MockObligationsConnector.get(vrn, queryParams)
        .returns(Future.successful(nonJsonDesResponse))

      val result = testObligationResource.retrieveObligations(vrn, queryParams)(fakeRequest)
      status(result) shouldBe INTERNAL_SERVER_ERROR
      contentType(result) shouldBe Some(MimeTypes.JSON)
      contentAsJson(result) shouldBe Json.toJson(Errors.InternalServerError)
    }

    "return a failure error code" when {
      "des backend returns an error code" in new Setup {
        val failureResponse = ObligationsResponse(HttpResponse(BAD_REQUEST,
          Some(Json.parse("""{"code" : "INVALID_VRN", "reason": ""}"""))))

        MockObligationsConnector.get(vrn, queryParams)
          .returns(Future.successful(failureResponse))

        val result = testObligationResource.retrieveObligations(vrn, queryParams)(fakeRequest)
        status(result) shouldBe INTERNAL_SERVER_ERROR
        contentType(result) shouldBe Some(MimeTypes.JSON)
        contentAsJson(result) shouldBe Json.toJson(Errors.InternalServerError)

        val auditResponse = AuditResponse(INTERNAL_SERVER_ERROR, Some(Seq(AuditError(Errors.InternalServerError.code))), None)
        MockAuditService.verifyAudit(AuditEvents.retrieveVatObligationsAudit(failureResponse.getCorrelationId,
          authContext.affinityGroup, None, clientId, auditResponse))
      }
    }
  }
}
