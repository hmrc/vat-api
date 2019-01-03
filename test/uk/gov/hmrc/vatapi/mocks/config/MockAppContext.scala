/*
 * Copyright 2019 HM Revenue & Customs
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

package uk.gov.hmrc.vatapi.mocks.config

import org.mockito.stubbing.OngoingStubbing
import org.scalatest.Suite
import uk.gov.hmrc.vatapi.config.AppContext
import uk.gov.hmrc.vatapi.mocks.Mock

trait MockAppContext extends Mock { _: Suite =>

  val mockAppContext: AppContext = mock[AppContext]

  object MockAppContext {
    def desUrl: OngoingStubbing[String] = when(mockAppContext.desUrl)
    def desToken: OngoingStubbing[String] = when(mockAppContext.desToken)
    def desEnv: OngoingStubbing[String] = when(mockAppContext.desEnv)
    def vatHybridFeatureEnabled: OngoingStubbing[Boolean] = when(mockAppContext.vatHybridFeatureEnabled)
  }

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockAppContext)
  }
}
