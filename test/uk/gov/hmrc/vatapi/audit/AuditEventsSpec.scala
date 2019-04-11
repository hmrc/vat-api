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

package uk.gov.hmrc.vatapi.audit

import org.scalatestplus.play.OneAppPerSuite
import uk.gov.hmrc.vatapi.UnitSpec
import uk.gov.hmrc.vatapi.assets.TestConstants.NRSResponse._
import uk.gov.hmrc.vatapi.models.audit.AuditEvent

class AuditEventsSpec extends UnitSpec with OneAppPerSuite {

  val xCorrelationId = "testXCorrelationId"
  val userType = "testUserType"
  val arn = "testArn"

  "nrsAudit event" should {
    "return valid AuditEvent" when {

      val vrn = generateVrn

      "proper NRS data is supplied" in {
        val auditEvent = AuditEvents.nrsAudit(vrn, nrsClientData, "NO_TOKEN")
        val expected = AuditEvent(
          "submitToNonRepudiationStore",
          "submit-vat-return",
          Map(
            "vrn" -> vrn.vrn,
            "authorization" -> "NO_TOKEN",
            "nrSubmissionID" -> nrsClientData.nrSubmissionId,
            "correlationId" -> ""
          )
        )

        auditEvent shouldBe expected
      }
    }
  }

  "submitVatReturn" should {

    val nrSubmissionId = "testNrSubmissionId"

    "return a valid AuditEvent" when {
      "all values are supplied" in {
        val auditEvent = AuditEvents.submitVatReturn(xCorrelationId, userType, Some(nrSubmissionId), Some(arn))
        val expected = AuditEvent(
          "submitVatReturn",
          "submit-vat-return",
          Map(
            "X-CorrelationId" -> xCorrelationId,
            "userType" -> userType,
            "nrSubmissionId" -> nrSubmissionId,
            "agentReferenceNumber" -> arn
          )
        )

        auditEvent shouldBe expected
      }
    }

    "return a valid AuditEvent without optional values" when {
      "all values are supplied except arn" in {
        val auditEvent = AuditEvents.submitVatReturn(xCorrelationId, userType, Some(nrSubmissionId), None)
        val expected = AuditEvent(
          "submitVatReturn",
          "submit-vat-return",
          Map(
            "X-CorrelationId" -> xCorrelationId,
            "userType" -> userType,
            "nrSubmissionId" -> nrSubmissionId
          )
        )

        auditEvent shouldBe expected
      }
      "all values are supplied except nrSubmissionId" in {
        val auditEvent = AuditEvents.submitVatReturn(xCorrelationId, userType, None, Some(arn))
        val expected = AuditEvent(
          "submitVatReturn",
          "submit-vat-return",
          Map(
            "X-CorrelationId" -> xCorrelationId,
            "userType" -> userType,
            "agentReferenceNumber" -> arn
          )
        )

        auditEvent shouldBe expected
      }
    }
  }

  "retrieveVatObligations" should {

    "return a valid AuditEvent" when {
      "all values are supplied" in {
        val auditEvent = AuditEvents.retrieveVatObligationsAudit(xCorrelationId, userType, Some(arn))
        val expected = AuditEvent(
          "retrieveVatObligations",
          "retrieve-vat-obligations",
          Map(
            "X-CorrelationId" -> xCorrelationId,
            "userType" -> userType,
            "agentReferenceNumber" -> arn
          )
        )
        auditEvent shouldBe expected
      }
    }

    "return a valid AuditEvent without optional values" when {
      "all values are supplied except arn" in {
        val auditEvent = AuditEvents.retrieveVatObligationsAudit(xCorrelationId, userType, None)
        val expected = AuditEvent(
          "retrieveVatObligations",
          "retrieve-vat-obligations",
          Map(
            "X-CorrelationId" -> xCorrelationId,
            "userType" -> userType
          )
        )

        auditEvent shouldBe expected
      }
    }
  }

  "retrieveVatReturns" should {

    "return a valid AuditEvent" when {
      "all values are supplied" in {
        val auditEvent = AuditEvents.retrieveVatReturnsAudit(xCorrelationId, userType, Some(arn))
        val expected = AuditEvent(
          "retrieveVatReturns",
          "retrieve-vat-returns",
          Map(
            "X-CorrelationId" -> xCorrelationId,
            "userType" -> userType,
            "agentReferenceNumber" -> arn
          )
        )
        auditEvent shouldBe expected
      }
    }

    "return a valid AuditEvent without optional values" when {
      "all values are supplied except arn" in {
        val auditEvent = AuditEvents.retrieveVatReturnsAudit(xCorrelationId, userType, None)
        val expected = AuditEvent(
          "retrieveVatReturns",
          "retrieve-vat-returns",
          Map(
            "X-CorrelationId" -> xCorrelationId,
            "userType" -> userType
          )
        )

        auditEvent shouldBe expected
      }
    }
  }

  "retrieveVatLiabilities" should {

    "return a valid AuditEvent" when {
      "all values are supplied" in {
        val auditEvent = AuditEvents.retrieveVatLiabilitiesAudit(xCorrelationId, userType, Some(arn))
        val expected = AuditEvent(
          "retrieveVatLiabilities",
          "retrieve-vat-liabilities",
          Map(
            "X-CorrelationId" -> xCorrelationId,
            "userType" -> userType,
            "agentReferenceNumber" -> arn
          )
        )
        auditEvent shouldBe expected
      }
    }

    "return a valid AuditEvent without optional values" when {
      "all values are supplied except arn" in {
        val auditEvent = AuditEvents.retrieveVatLiabilitiesAudit(xCorrelationId, userType, None)
        val expected = AuditEvent(
          "retrieveVatLiabilities",
          "retrieve-vat-liabilities",
          Map(
            "X-CorrelationId" -> xCorrelationId,
            "userType" -> userType
          )
        )

        auditEvent shouldBe expected
      }
    }
  }

  "retrieveVatPayments" should {

    "return a valid AuditEvent" when {
      "all values are supplied" in {
        val auditEvent = AuditEvents.retrieveVatPaymentsAudit(xCorrelationId, userType, Some(arn))
        val expected = AuditEvent(
          "retrieveVatPayments",
          "retrieve-vat-payments",
          Map(
            "X-CorrelationId" -> xCorrelationId,
            "userType" -> userType,
            "agentReferenceNumber" -> arn
          )
        )
        auditEvent shouldBe expected
      }
    }

    "return a valid AuditEvent without optional values" when {
      "all values are supplied except arn" in {
        val auditEvent = AuditEvents.retrieveVatPaymentsAudit(xCorrelationId, userType, None)
        val expected = AuditEvent(
          "retrieveVatPayments",
          "retrieve-vat-payments",
          Map(
            "X-CorrelationId" -> xCorrelationId,
            "userType" -> userType
          )
        )

        auditEvent shouldBe expected
      }
    }
  }
}
