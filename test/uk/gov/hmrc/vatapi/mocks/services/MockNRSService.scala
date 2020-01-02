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

package uk.gov.hmrc.vatapi.mocks.services

import org.joda.time.DateTime
import play.api.libs.json.Json
import uk.gov.hmrc.domain.Vrn
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.test.UnitSpec
import uk.gov.hmrc.vatapi.httpparsers.NrsSubmissionHttpParser.NrsSubmissionOutcome
import uk.gov.hmrc.vatapi.mocks.Mock
import uk.gov.hmrc.vatapi.models.{Metadata, NRSSubmission, SearchKeys, VatReturnDeclaration}
import uk.gov.hmrc.vatapi.resources.AuthRequest
import uk.gov.hmrc.vatapi.services.NRSService

import scala.concurrent.{ExecutionContext, Future}

trait MockNRSService extends UnitSpec with Mock {

  val mockNrsService: NRSService = mock[NRSService]

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockNrsService)
  }

  def setupNrsSubmission(vrn: Vrn, submission: VatReturnDeclaration)(response: NrsSubmissionOutcome): Unit = {
    when(mockNrsService
      .submit(
        eqTo(vrn),
        any[NRSSubmission]())(any[HeaderCarrier](), any[ExecutionContext](), any[AuthRequest[_]]()))
      .thenReturn(Future.successful(response))

    when(mockNrsService.convertToNrsSubmission(
      eqTo(vrn),
      eqTo(submission)
    )(any[AuthRequest[_]]()))
      .thenReturn(
        NRSSubmission("", Metadata("", "", "", None, DateTime.now(), None, "", Json.obj(), SearchKeys()))
      )
  }
}
