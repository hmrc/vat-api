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

package v1.mocks.connectors

import org.scalamock.handlers.CallHandler
import org.scalamock.scalatest.MockFactory
import uk.gov.hmrc.http.HeaderCarrier
import v1.connectors.{NrsConnector, NrsOutcome}
import v1.models.nrs.request.NrsSubmission
import v1.models.nrs.response.NrsResponse

import scala.concurrent.{ExecutionContext, Future}

trait MockNrsConnector extends MockFactory {

  val mockNrsConnector: NrsConnector = mock[NrsConnector]

  object MockNrsConnector {

    def submitNrs(body: NrsSubmission): CallHandler[Future[NrsOutcome[NrsResponse]]] = {
      (mockNrsConnector
        .submitNrs(_: NrsSubmission, _: String)(_: HeaderCarrier, _: ExecutionContext))
        .expects(body, *, *, *)
    }
  }

}
