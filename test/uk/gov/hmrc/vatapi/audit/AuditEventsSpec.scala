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

package uk.gov.hmrc.vatapi.audit

import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import uk.gov.hmrc.domain.Vrn
import uk.gov.hmrc.vatapi.UnitSpec
import uk.gov.hmrc.vatapi.assets.TestConstants.NRSResponse._
import uk.gov.hmrc.vatapi.models.audit.{AuditDetail, AuditEvent, AuditResponse}

class AuditEventsSpec extends UnitSpec with GuiceOneAppPerSuite {

  val xCorrelationId = "testXCorrelationId"
  val userType = "testUserType"
  val arn = "testArn"
  val clientId = "testClientId"

  val vrn = Vrn("someVrn")
  val auditResponse = AuditResponse(200, None, None)

  "agentRef" should {
    "return a valid key value" when {
      "arn is passed" in {
        val arn = "Arn123"
        AuditEvents.agentRef(Some(arn)) shouldBe Map("agentReferenceNumber" -> arn)
      }

      "none is passed" in {
        AuditEvents.agentRef(None) shouldBe Map.empty[String, String]
      }
    }
  }

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
        val auditEvent = AuditEvents.submitVatReturn(xCorrelationId, userType, Some(nrSubmissionId), clientId, Some(arn), auditResponse)
        val expected = AuditEvent(
          "submitVatReturn",
          "submit-vat-return",
          AuditDetail(userType, Some(arn), xCorrelationId, auditResponse, clientId, Some(nrSubmissionId))
        )

        auditEvent shouldBe expected
      }
    }

    "return a valid AuditEvent without optional values" when {
      "all values are supplied except arn" in {
        val auditEvent = AuditEvents.submitVatReturn(xCorrelationId, userType, None, clientId, None, auditResponse)
        val expected = AuditEvent(
          "submitVatReturn",
          "submit-vat-return",
          AuditDetail(userType, None, xCorrelationId, auditResponse, clientId, None)
        )

        auditEvent shouldBe expected
      }
      "all values are supplied except nrSubmissionId" in {
        val auditEvent = AuditEvents.submitVatReturn(xCorrelationId, userType, None, clientId, Some(arn), auditResponse)
        val expected = AuditEvent(
          "submitVatReturn",
          "submit-vat-return",
          AuditDetail(userType, Some(arn), xCorrelationId, auditResponse, clientId, None)
        )

        auditEvent shouldBe expected
      }
    }
  }

  "retrieveVatObligations" should {

    "return a valid AuditEvent" when {
      "all values are supplied" in {
        val auditEvent = AuditEvents.retrieveVatObligationsAudit(xCorrelationId, userType, Some(arn), clientId, auditResponse)
        val expected = AuditEvent(
          "retrieveVatObligations",
          "retrieve-vat-obligations",
          AuditDetail(userType, Some(arn), xCorrelationId, auditResponse, clientId)
        )
        auditEvent shouldBe expected
      }
    }

    "return a valid AuditEvent without optional values" when {
      "all values are supplied except arn" in {
        val auditEvent = AuditEvents.retrieveVatObligationsAudit(xCorrelationId, userType, None, clientId, auditResponse)
        val expected = AuditEvent(
          "retrieveVatObligations",
          "retrieve-vat-obligations",
          AuditDetail(userType, None, xCorrelationId, auditResponse, clientId)
        )

        auditEvent shouldBe expected
      }
    }
  }

  "retrieveVatReturns" should {

    "return a valid AuditEvent" when {
      "all values are supplied" in {
        val auditEvent = AuditEvents.retrieveVatReturnsAudit(xCorrelationId, userType, Some(arn), clientId, auditResponse)
        val expected = AuditEvent(
          "retrieveVatReturns",
          "retrieve-vat-returns",
          AuditDetail(userType, Some(arn), xCorrelationId, auditResponse, clientId)
        )
        auditEvent shouldBe expected
      }
    }

    "return a valid AuditEvent without optional values" when {
      "all values are supplied except arn" in {
        val auditEvent = AuditEvents.retrieveVatReturnsAudit(xCorrelationId, userType, None, clientId, auditResponse)
        val expected = AuditEvent(
          "retrieveVatReturns",
          "retrieve-vat-returns",
          AuditDetail(userType, None, xCorrelationId, auditResponse, clientId)
        )

        auditEvent shouldBe expected
      }
    }
  }

  "retrieveVatLiabilities" should {

    "return a valid AuditEvent" when {
      "all values are supplied" in {
        val auditEvent = AuditEvents.retrieveVatLiabilitiesAudit(xCorrelationId, userType, Some(arn), clientId, auditResponse)
        val expected = AuditEvent(
          "retrieveVatLiabilities",
          "retrieve-vat-liabilities",
          AuditDetail(userType, Some(arn), xCorrelationId, auditResponse, clientId)
        )
        auditEvent shouldBe expected
      }
    }

    "return a valid AuditEvent without optional values" when {
      "all values are supplied except arn" in {
        val auditEvent = AuditEvents.retrieveVatLiabilitiesAudit(xCorrelationId, userType, None, clientId, auditResponse)
        val expected = AuditEvent(
          "retrieveVatLiabilities",
          "retrieve-vat-liabilities",
          AuditDetail(userType, None, xCorrelationId, auditResponse, clientId)
        )

        auditEvent shouldBe expected
      }
    }
  }

  "retrieveVatPayments" should {

    "return a valid AuditEvent" when {
      "all values are supplied" in {
        val auditEvent = AuditEvents.retrieveVatPaymentsAudit(xCorrelationId, userType, Some(arn), clientId, auditResponse)
        val expected = AuditEvent(
          "retrieveVatPayments",
          "retrieve-vat-payments",
          AuditDetail(userType, Some(arn), xCorrelationId, auditResponse, clientId)
        )
        auditEvent shouldBe expected
      }
    }

    "return a valid AuditEvent without optional values" when {
      "all values are supplied except arn" in {
        val auditEvent = AuditEvents.retrieveVatPaymentsAudit(xCorrelationId, userType, None, clientId, auditResponse)
        val expected = AuditEvent(
          "retrieveVatPayments",
          "retrieve-vat-payments",
          AuditDetail(userType, None, xCorrelationId, auditResponse, clientId)
        )

        auditEvent shouldBe expected
      }
    }
  }
}
