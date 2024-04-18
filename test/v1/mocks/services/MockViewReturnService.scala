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

package v1.mocks.services

import org.scalamock.handlers.CallHandler
import org.scalamock.scalatest.MockFactory
import org.scalatest.TestSuite
import uk.gov.hmrc.http.HeaderCarrier
import utils.EndpointLogContext
import v1.controllers.UserRequest
import v1.models.request.viewReturn.ViewRequest
import v1.models.response.viewReturn.ViewReturnResponse
import v1.services.{ServiceOutcome, ViewReturnService}

import scala.concurrent.{ExecutionContext, Future}

trait MockViewReturnService extends TestSuite with MockFactory {

  val mockViewReturnService: ViewReturnService = mock[ViewReturnService]

  object MockViewReturnService {

    def viewReturn(request: ViewRequest): CallHandler[Future[ServiceOutcome[ViewReturnResponse]]] = {
      (mockViewReturnService
        .viewReturn(_ : ViewRequest)(_: HeaderCarrier, _: ExecutionContext, _: EndpointLogContext, _: UserRequest[_], _: String))
        .expects(request, *, *, *, *, *)
    }
  }

}
