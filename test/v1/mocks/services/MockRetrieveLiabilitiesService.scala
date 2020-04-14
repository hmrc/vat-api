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

package v1.mocks.services

import org.scalamock.handlers.CallHandler
import org.scalamock.scalatest.MockFactory
import uk.gov.hmrc.http.HeaderCarrier
import utils.EndpointLogContext
import v1.models.request.liability.LiabilityRequest
import v1.models.response.liability.LiabilityResponse
import v1.services.{RetrieveLiabilitiesService, ServiceOutcome}

import scala.concurrent.{ExecutionContext, Future}

trait MockRetrieveLiabilitiesService extends MockFactory {

  val mockRetrieveLiabilitiesService: RetrieveLiabilitiesService = mock[RetrieveLiabilitiesService]

  object MockRetrieveLiabilitiesService {

    def retrieveLiabilities(request: LiabilityRequest): CallHandler[Future[ServiceOutcome[LiabilityResponse]]] = {
      (mockRetrieveLiabilitiesService
        .retrieveLiabilities(_ : LiabilityRequest)(_: HeaderCarrier, _: ExecutionContext, _: EndpointLogContext))
        .expects(request, *, *, *)
    }
  }

}
