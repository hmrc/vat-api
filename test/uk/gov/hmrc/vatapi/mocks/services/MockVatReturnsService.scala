/*
 * Copyright 2018 HM Revenue & Customs
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

package uk.gov.hmrc.vatapi.mocks.services

import org.mockito.Matchers
import org.mockito.Mockito.{reset, when}
import org.scalatest.BeforeAndAfterEach
import org.scalatest.mockito.MockitoSugar
import uk.gov.hmrc.domain.Vrn
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.test.UnitSpec
import uk.gov.hmrc.vatapi.models.des
import uk.gov.hmrc.vatapi.resources.wrappers.VatReturnResponse
import uk.gov.hmrc.vatapi.services.VatReturnsService

import scala.concurrent.{ExecutionContext, Future}

trait MockVatReturnsService extends UnitSpec with MockitoSugar with BeforeAndAfterEach {

  val mockVatReturnsService: VatReturnsService = mock[VatReturnsService]

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockVatReturnsService)
  }

  def setupVatReturnSubmission(vrn: Vrn, submission: des.VatReturnDeclaration)(response: VatReturnResponse): Unit =
    when(mockVatReturnsService
      .submit(
        Matchers.eq(vrn),
        Matchers.any[des.VatReturnDeclaration]())(Matchers.any[HeaderCarrier](), Matchers.any[ExecutionContext]()))
      .thenReturn(Future.successful(response))
}
