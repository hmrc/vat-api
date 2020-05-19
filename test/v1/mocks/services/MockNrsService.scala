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

import org.joda.time.DateTime
import org.scalamock.handlers.CallHandler
import org.scalamock.scalatest.MockFactory
import uk.gov.hmrc.http.HeaderCarrier
import v1.controllers.UserRequest
import v1.models.errors.ErrorWrapper
import v1.models.nrs.response.NrsResponse
import v1.models.request.submit.SubmitRequest
import v1.services.NrsService

import scala.concurrent.{ExecutionContext, Future}

trait MockNrsService extends MockFactory {

  val mockNrsService: NrsService = mock[NrsService]

  object MockNrsService {

    def submitNrs(request: SubmitRequest, dateTime: DateTime): CallHandler[Future[Either[ErrorWrapper, NrsResponse]]] = {
      (mockNrsService
        .submitNrs(_ : SubmitRequest, _: DateTime)(_: UserRequest[_], _: HeaderCarrier, _: ExecutionContext))
        .expects(request, *, *, *, *)
    }
  }

}
