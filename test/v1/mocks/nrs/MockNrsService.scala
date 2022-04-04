/*
 * Copyright 2022 HM Revenue & Customs
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
import uk.gov.hmrc.http.HeaderCarrier
import v1.controllers.UserRequest
import v1.models.request.submit.SubmitRequest
import v1.nrs.NrsService
import v1.nrs.models.response.NrsResponse
import java.time.{LocalDateTime, OffsetDateTime}

import scala.concurrent.{ExecutionContext, Future}

trait MockNrsService extends MockFactory {

  val mockNrsService: NrsService = mock[NrsService]

  object MockNrsService {

    def submit(request: SubmitRequest, nrsId: String, dateTime: OffsetDateTime): CallHandler[Future[Option[NrsResponse]]] = {
      (mockNrsService
        .submit(_: SubmitRequest, _: String, _: OffsetDateTime)(_: UserRequest[_], _: HeaderCarrier, _: ExecutionContext, _: String))
        .expects(request, *, *, *, *, *, *)
    }
  }

}
