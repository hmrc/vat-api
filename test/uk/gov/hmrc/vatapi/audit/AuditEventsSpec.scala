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

package uk.gov.hmrc.vatapi.audit

import org.scalatestplus.play.OneAppPerSuite
import uk.gov.hmrc.vatapi.UnitSpec
import uk.gov.hmrc.vatapi.assets.TestConstants.NRSResponse._

class AuditEventsSpec extends UnitSpec with OneAppPerSuite {

  "nrsAudit event" should {
    "return valid AuditEvent" when {
      "proper NRS data is supplied" in {
      val auditEvent = AuditEvents.nrsAudit(generateVrn, nrsData, "NO_TOKEN", "X-COID")
      assert(auditEvent.transactionName == "submit-vat-return")
      assert(auditEvent.auditType == "submitToNonRepudiationStore")
    }
    }
  }
}
