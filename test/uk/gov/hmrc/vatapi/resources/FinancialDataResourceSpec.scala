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
import play.api.test.FakeRequest
import play.mvc.Http.MimeTypes
import uk.gov.hmrc.http.HttpResponse
import uk.gov.hmrc.vatapi.mocks.MockAuditService
import uk.gov.hmrc.vatapi.mocks.auth.MockAuthorisationService
import uk.gov.hmrc.vatapi.mocks.connectors.MockFinancialDataConnector
import uk.gov.hmrc.vatapi.models.{ErrorResult, FinancialDataQueryParams}
import uk.gov.hmrc.vatapi.resources.wrappers.FinancialDataResponse

import scala.concurrent.Future

class FinancialDataResourceSpec extends ResourceSpec
  with MockFinancialDataConnector
  with MockAuthorisationService
  with MockAuditService {

  class Setup {
    val testFinalcialDataResource = new FinancialDataResource (      mockFinancialDataConnector, mockAuthorisationService, mockAppContext , mockAuditService)
//      override val authService = mockAuthorisationService
//      override val connector = mockFinancialDataConnector
//      override val appContext = mockAppContext
//      override val auditService = mockAuditService
//    }
    mockAuthAction(vrn)
  }

  val queryParams = FinancialDataQueryParams(now.minusDays(100), now.minusDays(20))

  "retrieveLiabilities" should {
    "return a 200 and the correct financial data json" when {
      "for a given valid period" in new Setup {
        val desResponse = FinancialDataResponse(HttpResponse(200, Some(Jsons.FinancialData.singleLiabilityDesResponse)))

        MockAuditService.audit()
          .returns(EitherT[Future, ErrorResult, Unit](Future.successful(Right(()))))

        MockFinancialDataConnector.get(vrn, queryParams)
          .returns(Future.successful(desResponse))

        val result = testFinalcialDataResource.retrieveLiabilities(vrn, queryParams)(FakeRequest())
        status(result) shouldBe 200
        contentType(result) shouldBe Some(MimeTypes.JSON)
      }
    }
  }

  "retrieveLiabilities" should {
    "return a 500 with error message" when {
      "when DES backed is failed to respond" in new Setup {

        MockFinancialDataConnector.get(vrn, queryParams)
          .returns(Future.failed(new Exception("INTERNAL_ERROR")))

        val result = testFinalcialDataResource.retrieveLiabilities(vrn, queryParams)(FakeRequest())
        status(result) shouldBe 500
      }
    }
  }

  "retrievePayments" should {
    "return a 200 and the correct financial data json" when {
      "for a given valid period" in new Setup {
        val desResponse = FinancialDataResponse(HttpResponse(200, Some(Jsons.FinancialData.singlePaymentDesResponse)))

        MockAuditService.audit()
          .returns(EitherT[Future, ErrorResult, Unit](Future.successful(Right(()))))

        MockFinancialDataConnector.get(vrn, queryParams)
          .returns(Future.successful(desResponse))

        val result = testFinalcialDataResource.retrievePayments(vrn, queryParams)(FakeRequest())
        status(result) shouldBe 200
        contentType(result) shouldBe Some(MimeTypes.JSON)
      }
    }
  }

  "retrievePayments" should {
    "return a 500 with error message" when {
      "when DES backed is failed to respond" in new Setup {

        MockFinancialDataConnector.get(vrn, queryParams)
          .returns(Future.failed(new Exception("INTERNAL_ERROR")))

        val result = testFinalcialDataResource.retrievePayments(vrn, queryParams)(FakeRequest())
        status(result) shouldBe 500
      }
    }
  }
}
