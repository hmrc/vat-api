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

package uk.gov.hmrc.vatapi.mocks

import org.mockito.stubbing.OngoingStubbing
import org.scalatest.Suite
import uk.gov.hmrc.vatapi.models.audit.AuditEvent
import uk.gov.hmrc.vatapi.resources.BusinessResult
import uk.gov.hmrc.vatapi.services.AuditService

trait MockAuditService extends Mock { _: Suite =>

  val mockAuditService: AuditService = mock[AuditService]

  object MockAuditService {
    def audit[T](): OngoingStubbing[BusinessResult[Unit]] = {
      when(mockAuditService.audit[T](any())(any(), any(), any(), any()))
    }
    def audit[T](event: AuditEvent[T]): OngoingStubbing[BusinessResult[Unit]] = {
      when(mockAuditService.audit[T](eqTo(event))(any(), any(), any(), any()))
    }

    def verifyAudit[T](event: AuditEvent[T]) = {
      verify(mockAuditService).audit(eqTo(event))(any(), any(), any(), any())
    }
  }

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockAuditService)
  }
}
