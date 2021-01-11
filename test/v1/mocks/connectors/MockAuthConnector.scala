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

package v1.mocks.connectors

import org.scalamock.handlers.CallHandler
import org.scalamock.scalatest.MockFactory
import uk.gov.hmrc.auth.core.AuthConnector
import uk.gov.hmrc.auth.core.authorise.Predicate
import uk.gov.hmrc.auth.core.retrieve.Retrieval
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.{ExecutionContext, Future}

trait MockAuthConnector extends MockFactory {

  val mockAuthConnector: AuthConnector = mock[AuthConnector]

  object MockAuthConnector {

    def authorised[A](predicate: Predicate, retrievals: Retrieval[A]): CallHandler[Future[A]] = {
      (mockAuthConnector.authorise[A](_: Predicate, _: Retrieval[A])(_: HeaderCarrier, _: ExecutionContext))
        .expects(predicate, retrievals, *, *)
    }
  }

}
