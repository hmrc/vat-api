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
import play.api.libs.json.{JsValue, Json}
import play.api.test.FakeRequest
import play.mvc.Http.MimeTypes
import uk.gov.hmrc.http.HttpResponse
import uk.gov.hmrc.vatapi.mocks.MockAuditService
import uk.gov.hmrc.vatapi.mocks.auth.MockAuthorisationService
import uk.gov.hmrc.vatapi.mocks.connectors.MockObligationsConnector
import uk.gov.hmrc.vatapi.models.{ErrorResult, Errors, ObligationsQueryParams}
import uk.gov.hmrc.vatapi.resources.wrappers.ObligationsResponse

import scala.concurrent.Future

class ObligationsResourceSpec extends ResourceSpec
  with MockObligationsConnector
  with MockAuthorisationService
  with MockAuditService {

  class Setup {
    val testObligationResource = new ObligationsResource (mockObligationsConnector, mockAuthorisationService, mockAppContext, mockAuditService)
    mockAuthAction(vrn)
  }

  val queryParams = ObligationsQueryParams(Some(now.minusDays(7)), Some(now), Some("O"))
  val queryParamsWithNoStatus = ObligationsQueryParams(Some(now.minusDays(7)), Some(now))
  val desObligationsJson: JsValue = Jsons.Obligations.desResponse(vrn)
  val desObligationsNoDetailsJson: JsValue = Jsons.Obligations.desResponseWithoutObligationDetails(vrn)
  val clientObligationsJson: JsValue = Jsons.Obligations()

  "retrieveObligations" should {
    "return a 200 and the correct obligations json" when {
      "DES returns a 200 response with the correct obligations body" in new Setup {
        val desResponse = ObligationsResponse(HttpResponse(200, Some(desObligationsJson)))

        MockAuditService.audit()
          .returns(EitherT[Future, ErrorResult, Unit](Future.successful(Right(()))))

        MockObligationsConnector.get(vrn, queryParams)
          .returns(Future.successful(desResponse))

        val result = testObligationResource.retrieveObligations(vrn, queryParams)(FakeRequest())
        status(result) shouldBe 200
        contentType(result) shouldBe Some(MimeTypes.JSON)
        contentAsJson(result) shouldBe clientObligationsJson
      }
    }

    "return a 200 and the correct obligations json for no status" when {
      "DES returns a 200 response with the correct obligations body" in new Setup {
        val desResponse = ObligationsResponse(HttpResponse(200, Some(desObligationsJson)))

        MockAuditService.audit()
          .returns(EitherT[Future, ErrorResult, Unit](Future.successful(Right(()))))

        MockObligationsConnector.get(vrn, queryParamsWithNoStatus)
          .returns(Future.successful(desResponse))

        val result = testObligationResource.retrieveObligations(vrn, queryParamsWithNoStatus)(FakeRequest())
        status(result) shouldBe 200
        contentType(result) shouldBe Some(MimeTypes.JSON)
        contentAsJson(result) shouldBe clientObligationsJson
      }
    }

    "return a 404 with no body" when {
      "DES returns a 200 response with the correct obligations body but the obligationDetails are empty" in new Setup {
        val desResponse = ObligationsResponse(HttpResponse(200, Some(desObligationsNoDetailsJson)))

        MockObligationsConnector.get(vrn, queryParams)
          .returns(Future.successful(desResponse))

        val result = testObligationResource.retrieveObligations(vrn, queryParams)(FakeRequest())
        status(result) shouldBe 404
        contentType(result) shouldBe None
      }
    }

    "return a 500 with a json body" when {
      "DES returns a 200 response but the body does not match the expected obligations format" in new Setup {
        val invalidDesResponse = ObligationsResponse(HttpResponse(200, Some(Json.obj("invalid" -> "des json"))))

        MockObligationsConnector.get(vrn, queryParams)
          .returns(Future.successful(invalidDesResponse))

        val result = testObligationResource.retrieveObligations(vrn, queryParams)(FakeRequest())
        status(result) shouldBe 500
        contentType(result) shouldBe Some(MimeTypes.JSON)
        contentAsJson(result) shouldBe Json.toJson(Errors.InternalServerError)
      }
    }

    "return a 500 with a json body for a failure" when {
      "DES returns some unexpected error" in new Setup {

        MockObligationsConnector.get(vrn, queryParams)
          .returns(Future.failed(new Exception("Connection refused error")))

        val result = testObligationResource.retrieveObligations(vrn, queryParams)(FakeRequest())
        status(result) shouldBe 500
        contentType(result) shouldBe Some(MimeTypes.JSON)
        contentAsJson(result) shouldBe Json.toJson(Errors.InternalServerError)
      }
    }

    "DES returns a 200 response with a non-json body" in new Setup {
      val nonJsonDesResponse = ObligationsResponse(HttpResponse(200, responseString = Some("non-json")))

      MockObligationsConnector.get(vrn, queryParams)
        .returns(Future.successful(nonJsonDesResponse))

      val result = testObligationResource.retrieveObligations(vrn, queryParams)(FakeRequest())
      status(result) shouldBe 500
      contentType(result) shouldBe Some(MimeTypes.JSON)
      contentAsJson(result) shouldBe Json.toJson(Errors.InternalServerError)
    }

    "DES returns a 200 response with an empty body" in new Setup {
      val nonJsonDesResponse = ObligationsResponse(HttpResponse(200))

      MockObligationsConnector.get(vrn, queryParams)
        .returns(Future.successful(nonJsonDesResponse))

      val result = testObligationResource.retrieveObligations(vrn, queryParams)(FakeRequest())
      status(result) shouldBe 500
      contentType(result) shouldBe Some(MimeTypes.JSON)
      contentAsJson(result) shouldBe Json.toJson(Errors.InternalServerError)
    }
  }
}
