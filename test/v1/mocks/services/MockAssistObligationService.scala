/*
 * Copyright 2026 HM Revenue & Customs
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
import uk.gov.hmrc.http.HeaderCarrier
import utils.EndpointLogContext
import v1.controllers.UserRequest
import v1.models.request.submit.SubmitRequest
import v1.models.response.obligations.Obligation
import v1.services.{ AssistObligationService, ServiceOutcome }

import java.time.LocalDate
import scala.concurrent.{ ExecutionContext, Future }

trait MockAssistObligationService extends MockFactory {

  val mockAssistObligationService: AssistObligationService = mock[AssistObligationService]

  object MockAssistObligationService {

    def retrieveOpenObligation(request: SubmitRequest): CallHandler[Future[ServiceOutcome[Obligation]]] =
      (
        mockAssistObligationService
          .retrieveOpenObligation(_: SubmitRequest, _: LocalDate)(
            _: HeaderCarrier,
            _: ExecutionContext,
            _: EndpointLogContext,
            _: UserRequest[_],
            _: String
          )
        )
        .expects(request, *, *, *, *, *, *)
  }
}
