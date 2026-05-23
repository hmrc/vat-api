/*
 * Copyright 2023 HM Revenue & Customs
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

package v1.controllers

import play.api.libs.json.{JsValue, Json}
import play.api.mvc._
import uk.gov.hmrc.auth.core.Enrolment
import uk.gov.hmrc.http.HeaderCarrier
import utils.IdGenerator
import v1.mocks.services.MockEnrolmentsAuthService
import v1.models.errors.{DownstreamError, ForbiddenDownstreamError, LegacyUnauthorisedError, MtdError}
import v1.services.EnrolmentsAuthService

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class AuthorisedControllerSpec extends ControllerBaseSpec {

  trait Test extends MockEnrolmentsAuthService {

    val hc: HeaderCarrier = HeaderCarrier()
    val authorisedController: TestController = new TestController()

    val mockIdGenerator: IdGenerator = new IdGenerator {
      override def getUid: String = "test-correlation-id"
    }

    class TestController extends AuthorisedController(cc) {
      override val authService: EnrolmentsAuthService = mockEnrolmentsAuthService
      override val idGenerator: IdGenerator = mockIdGenerator

      def action(vrn: String): Action[AnyContent] = authorisedAction(vrn).async {
        Future.successful(Ok(Json.obj()))
      }
    }
  }

  val vrn: String = "123456789"

  "calling an action" when {
    "a user is properly authorised" should {
      "return a 200 success response" in new Test {
        MockEnrolmentsAuthService.authoriseUser()
        private val result = authorisedController.action(vrn)(fakeGetRequest)
        status(result) shouldBe OK
      }
    }

    "the enrolments auth service returns an error" must {
      "map to the correct result" when {

        val predicate: Enrolment =
          Enrolment("HMRC-MTD-VAT")
            .withIdentifier("VRN", vrn)
            .withDelegatedAuthRule("mtd-vat-auth")

        def serviceErrors(mtdError: MtdError, expectedStatus: Int, expectedBody: JsValue): Unit = {
          s"a ${mtdError.code} error is returned from the enrolments auth service" in new Test {

            MockEnrolmentsAuthService.authorised(predicate)
              .returns(Future.successful(Left(mtdError)))

            private val actualResult = authorisedController.action(vrn)(fakeGetRequest)
            status(actualResult) shouldBe expectedStatus
            contentAsJson(actualResult) shouldBe expectedBody
          }
        }

        object unexpectedError extends MtdError(code = "UNEXPECTED_ERROR", message = "This is an unexpected error")

        val authServiceErrors =
          Seq(
            (LegacyUnauthorisedError, FORBIDDEN, Json.toJson(LegacyUnauthorisedError)),
            (ForbiddenDownstreamError, FORBIDDEN, Json.toJson(DownstreamError)),
            (unexpectedError, INTERNAL_SERVER_ERROR, Json.toJson(DownstreamError))
          )

        authServiceErrors.foreach(args => (serviceErrors _).tupled(args))
      }
    }
  }
}

