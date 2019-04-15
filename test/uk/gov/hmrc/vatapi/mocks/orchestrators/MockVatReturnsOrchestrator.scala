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

package uk.gov.hmrc.vatapi.mocks.orchestrators

import org.scalatest.Suite
import uk.gov.hmrc.domain.Vrn
import uk.gov.hmrc.vatapi.mocks.Mock
import uk.gov.hmrc.vatapi.models.VatReturnDeclaration
import uk.gov.hmrc.vatapi.orchestrators.VatReturnsOrchestrator

trait MockVatReturnsOrchestrator extends Mock { _: Suite =>

  val mockVatReturnsOrchestrator = mock[VatReturnsOrchestrator]

  object MockVatReturnsOrchestrator {
    def submitVatReturn(vrn: Vrn, vatReturn: VatReturnDeclaration) = {
      when(mockVatReturnsOrchestrator.submitVatReturn(eqTo(vrn), eqTo(vatReturn), any())(any(), any()))
    }
  }

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockVatReturnsOrchestrator)
  }
}
