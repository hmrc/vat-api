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

package uk.gov.hmrc.vatapi.resources

import org.scalatest.mockito.MockitoSugar
import org.scalatest.{Matchers, OptionValues, WordSpec}
import play.api.Configuration
import play.api.http.{HeaderNames, MimeTypes, Status}
import play.api.test.{DefaultAwaitTimeout, ResultExtractors}
import uk.gov.hmrc.domain.Vrn
import uk.gov.hmrc.vatapi.TestUtils
import uk.gov.hmrc.vatapi.config.AppContext
import uk.gov.hmrc.vatapi.mocks.auth.MockAuthorisationService

trait ResourceSpec extends WordSpec
  with Matchers
  with OptionValues
  with MockitoSugar
  with TestUtils
  with ResultExtractors
  with HeaderNames
  with Status
  with DefaultAwaitTimeout
  with MimeTypes
  with MockAuthorisationService {

  val vrn: Vrn = generateVrn

  val mockAppContext = mock[AppContext]

  def mockAuthAction(vrn: Vrn, authEnabled: Boolean = false) = {
    val config = Configuration("auth.enabled" -> authEnabled)
    when(mockAppContext.featureSwitch)
      .thenReturn(Some(config))

    MockAuthorisationService.authCheck(vrn)
  }
}
