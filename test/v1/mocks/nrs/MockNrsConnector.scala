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

package v1.mocks.nrs

import org.scalamock.handlers.CallHandler
import org.scalamock.scalatest.MockFactory
import org.scalatest.TestSuite
import play.api.mvc.Request
import uk.gov.hmrc.http.HeaderCarrier
import v1.nrs.models.request.NrsSubmission
import v1.nrs.{NrsConnector, NrsOutcome}

import scala.concurrent.Future

trait MockNrsConnector extends TestSuite with MockFactory {

  val mockNrsConnector: NrsConnector = mock[NrsConnector]

  object MockNrsConnector {

    def submitNrs(body: NrsSubmission): CallHandler[Future[NrsOutcome]] = {
      (mockNrsConnector
        .submit(_: NrsSubmission)(_: HeaderCarrier, _: String, _: Request[_]))
        .expects(body, *, *, *)
    }
  }

}
