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

package uk.gov.hmrc.vatapi.mocks.connectors

import org.scalatest.Suite
import uk.gov.hmrc.domain.Vrn
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.vatapi.connectors.VatReturnsConnector
import uk.gov.hmrc.vatapi.mocks.Mock
import uk.gov.hmrc.vatapi.models.des.VatReturnDeclaration
import uk.gov.hmrc.vatapi.resources.wrappers.VatReturnResponse

import scala.concurrent.{ExecutionContext, Future}

trait MockVatReturnsConnector extends Mock {
  _: Suite =>

  val mockVatReturnsConnector: VatReturnsConnector = mock[VatReturnsConnector]

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockVatReturnsConnector)
  }

  def setupVatReturnSubmission(vrn: Vrn, submission: VatReturnDeclaration)(response: VatReturnResponse): Unit =
    when(mockVatReturnsConnector
      .post(
        eqTo(vrn),
        any[VatReturnDeclaration]())(any[HeaderCarrier](), any[ExecutionContext]()))
      .thenReturn(Future.successful(response))

  def retrieveVatReturn(vrn: Vrn, periodKey: String)(response: VatReturnResponse) =
    when(mockVatReturnsConnector
      .query(
        eqTo(vrn),
        any[String]())(any[HeaderCarrier](), any[ExecutionContext]()))
      .thenReturn(Future.successful(response))

  def retrieveVatReturnFailed(vrn: Vrn, periodKey: String) =
    when(mockVatReturnsConnector
      .query(
        eqTo(vrn),
        any[String]())(any[HeaderCarrier](), any[ExecutionContext]()))
      .thenReturn(Future.failed(new Exception("DES FAILED")))

}
