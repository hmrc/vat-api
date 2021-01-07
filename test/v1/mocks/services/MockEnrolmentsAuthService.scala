/*
 * Copyright 2021 HM Revenue & Customs
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

package v1.mocks.services

import org.scalamock.handlers.CallHandler
import org.scalamock.scalatest.MockFactory
import uk.gov.hmrc.auth.core.authorise.Predicate
import uk.gov.hmrc.http.HeaderCarrier
import v1.models.auth.UserDetails
import v1.models.outcomes.AuthOutcome
import v1.services.EnrolmentsAuthService

import scala.concurrent.{ExecutionContext, Future}

trait MockEnrolmentsAuthService extends MockFactory {

  val mockEnrolmentsAuthService: EnrolmentsAuthService = mock[EnrolmentsAuthService]

  object MockEnrolmentsAuthService {

    def authoriseUser(): Unit = {
      (mockEnrolmentsAuthService.authorised(_: Predicate, _: Boolean)(_: HeaderCarrier, _: ExecutionContext))
        .expects(*, *, *, *)
        .returns(Future.successful(Right(UserDetails("Individual", None, "client-Id"))))
    }

    def authorised(predicate: Predicate): CallHandler[Future[AuthOutcome]] = {
      (mockEnrolmentsAuthService.authorised(_: Predicate, _: Boolean)(_: HeaderCarrier, _: ExecutionContext))
        .expects(predicate, *, *, *)
    }
  }

}
