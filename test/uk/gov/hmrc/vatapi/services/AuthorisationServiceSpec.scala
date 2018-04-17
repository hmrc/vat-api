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

package uk.gov.hmrc.vatapi.services

import org.joda.time.DateTime
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.mockito.MockitoSugar
import org.scalatestplus.play.OneAppPerSuite
import play.api.http.HeaderNames
import play.api.libs.json.JsResultException
import play.api.libs.json.Json.toJson
import play.api.mvc.Results
import play.api.mvc.Results.Forbidden
import play.api.test.FakeRequest
import uk.gov.hmrc.auth.core.{InsufficientConfidenceLevel, UnsupportedAuthProvider}
import uk.gov.hmrc.domain.Vrn
import uk.gov.hmrc.http.{HeaderCarrier, SessionKeys}
import uk.gov.hmrc.vatapi.UnitSpec
import uk.gov.hmrc.vatapi.assets.TestConstants.Auth._
import uk.gov.hmrc.vatapi.auth.APIAuthorisedFunctions
import uk.gov.hmrc.vatapi.mocks.auth.MockAPIAuthorisedFunctions
import uk.gov.hmrc.vatapi.models.Errors

import scala.concurrent.ExecutionContext
import scala.util.Right


class AuthorisationServiceSpec extends UnitSpec with OneAppPerSuite with MockitoSugar with ScalaFutures with MockAPIAuthorisedFunctions {

  object TestAuthorisationService extends AuthorisationService {
    override val apiAuthorisedFunctions: APIAuthorisedFunctions = mockAPIAuthorisedFunctions
  }

  implicit val hc: HeaderCarrier = HeaderCarrier()
  val testDateTime: DateTime = DateTime.now()

  lazy val fakeRequestWithActiveSession: FakeRequest[_] = FakeRequest().withSession(
    SessionKeys.lastRequestTimestamp -> "1498236506662",
    SessionKeys.authToken -> "Bearer Token"
  ).withHeaders(
    HeaderNames.REFERER -> "/test/url"
  )

  implicit val ec: ExecutionContext = app.injector.instanceOf[ExecutionContext]

  "TestAuthorisationService.authCheck" when {

    val testVrn: Vrn = Vrn("123456789")
    val invalidVrn: Vrn = Vrn("111111111")

    "verify the auth with valid organisation client details" should {
      "should return valid auth enrolments " in {
        setupMockAuthRetrievalSuccess(testAuthOrganisationSuccessResponse)
        extractAwait(TestAuthorisationService.authCheck(testVrn)(hc, fakeRequestWithActiveSession, ec)) shouldBe Right(orgAuthContext)
      }
    }

    "verify the auth with valid individual client details" should {
      "should return valid auth enrolments" in {
        setupMockAuthRetrievalSuccess(testAuthIndividualSuccessResponse)
        extractAwait(TestAuthorisationService.authCheck(testVrn)(hc, fakeRequestWithActiveSession, ec)) shouldBe Right(indAuthContext)
      }
    }

    "verify the auth with invalid client details" should {
      "should reject the client " in {
        setupMockAuthorisationException()
        extractAwait(TestAuthorisationService.authCheck(invalidVrn)(hc, fakeRequestWithActiveSession, ec)).isLeft shouldBe true
      }
    }

    "verify the auth with invalid client confidenceLevel details" should {
      "should reject the client " in {
        setupMockAuthorisationException(new InsufficientConfidenceLevel())
        extractAwait(TestAuthorisationService.authCheck(invalidVrn)(hc, fakeRequestWithActiveSession, ec)) shouldBe
          Left(Forbidden(toJson(Errors.ClientOrAgentNotAuthorized)))
      }
    }

    "verify auth when JSON response from auth.authorise has insufficient data for creating NRS data" should {
      "reject the client" in {
        setupMockAuthorisationException(new JsResultException(errors = Seq()))
        extractAwait(TestAuthorisationService.authCheck(testVrn)(hc, fakeRequestWithActiveSession, ec)) shouldBe
        Left(Forbidden(toJson(Errors.InternalServerError)))
      }
    }

    "verify the auth with unexpected auth error" should {
      "should reject the client " in {
        setupMockAuthorisationException(new UnsupportedAuthProvider)
        extractAwait(TestAuthorisationService.authCheck(invalidVrn)(hc, fakeRequestWithActiveSession, ec)) shouldBe
          Left(Results.InternalServerError(toJson(Errors.InternalServerError("An internal server error occurred"))))
      }
    }
  }
}