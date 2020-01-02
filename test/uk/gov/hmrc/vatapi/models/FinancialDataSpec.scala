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

package uk.gov.hmrc.vatapi.models

import org.joda.time.LocalDate
import uk.gov.hmrc.vatapi.UnitSpec
import uk.gov.hmrc.vatapi.resources.JsonSpec
import uk.gov.hmrc.vatapi.support.{FinancialData => TestFinancialData}

class FinancialDataSpec extends UnitSpec with JsonSpec {

  "des.FinancialData" should {
    "round trip" in {
      roundTripJson(
        TestFinancialData.testFinancialData
      )
    }
  }

  "models.FinancialData" should {

    "retrieve a full liabilities model where liabilities exist from the des.FinancialData model" when {

      val testLiability = Liabilities(
        Seq(Liability(
          Some(TaxPeriod(
            from = LocalDate.parse("1977-08-13"),
            to = LocalDate.parse("1977-08-14")
          )),
          `type` = "VAT",
          originalAmount = 10000,
          outstandingAmount = Some(10000),
          due = Some(LocalDate.parse("1967-08-13"))
        ))
      )
      val testLiabilities =
        Liabilities(
          Seq(Liability(
            Some(TaxPeriod(
              from = LocalDate.parse("1977-08-13"),
              to = LocalDate.parse("1977-08-14")
            )),
            `type` = "VAT",
            originalAmount = 10000,
            outstandingAmount = Some(10000),
            due = Some(LocalDate.parse("1967-08-13"))
          ),
          Liability(
            Some(TaxPeriod(
              from = LocalDate.parse("1967-08-13"),
              to = LocalDate.parse("1967-08-14")
            )),
            `type` = "VAT",
            originalAmount = 10000,
            outstandingAmount = Some(10000),
            due = Some(LocalDate.parse("1967-08-13"))
          )
        )
      )

      "transactions exist with liabilities and payments" in {
        val result = Liabilities.from.from(TestFinancialData.testFinancialData)

        result.isRight shouldBe true
        result shouldBe Right(testLiabilities)
      }
      "transactions exist with just liabilities" in {
        val result = Liabilities.from.from(TestFinancialData.justLiabilities)

        result.isRight shouldBe true
        result shouldBe Right(testLiability)
      }
    }

    "retrieve a partial liabilities model where liabilities exist in the des.FinancialData model with just mandatory values" in {
      val testLiability = Right(Liabilities(
        Seq(Liability(
          `type` = "VAT",
          originalAmount = 10000
        ))
      ))

      val result = Liabilities.from.from(TestFinancialData.minimumLiabilityData)

      result.isRight shouldBe true
      result shouldBe testLiability
    }

    "return nothing when no liabilities exist from the des.FinancialData model" in {
      val result = Liabilities.from.from(TestFinancialData.noLiabilities)

      result.isRight
      result shouldBe Right(Liabilities(Seq()))
    }

    "return the correct liabilities payment error when the Json can't be transformed" in {
      val result = Liabilities.from.from(TestFinancialData.badLiabilityModel)

      result.isLeft shouldBe true
      result.left.get.msg should startWith ("[Liabilities] Unable to parse the Json from DES model")
    }

    "return no liabilities when only Hybrid payments exist in the des.FinancialData model" in {
      val result = Liabilities.from.from(TestFinancialData.testFinancialDataHybridPaymentsOnly)

      result.isRight shouldBe true
      result shouldBe Right(Liabilities(Seq()))
    }

    "retrieve a payments model where both payments exist" when {
      val testPayment = Right(Payments(
        Seq(Payment(
          amount = 10000,
          received = Some(LocalDate.parse("1967-08-13"))
        ))
      ))

      "transactions exist with liabilities and payments" in {
        val result = Payments.from.from(TestFinancialData.testFinancialData)

        result.isRight shouldBe true
        result shouldBe testPayment
      }
      "transactions exist with just payments" in {
        val result = Payments.from.from(TestFinancialData.justPayments)

        result.isRight shouldBe true
        result shouldBe testPayment
      }
      "transactions exist with liabilities, payments and hybrid payments" in {
        val result = Payments.from.from(TestFinancialData.allPaymentsAndLiabilities)

        result.isRight shouldBe true
        result shouldBe testPayment
      }
    }

    "retrieve a partial payments model where payments exist in the des.FinancialData model with just mandatory values" in {
      val testLiability = Right(Payments(
        Seq(Payment(
          amount = 10000
        ))
      ))

      val result = Payments.from.from(TestFinancialData.minimumPaymentData)

      result.isRight shouldBe true
      result shouldBe testLiability
    }

    "return nothing when no payments exist from the des.FinancialData model" in {
      val result = Payments.from.from(TestFinancialData.justLiabilities)

      result.isRight shouldBe true
      result shouldBe Right(Payments(Seq()))
    }

    "return nothing when only POA payments exist from the des.FinancialData model" in {
      val result = Payments.from.from(TestFinancialData.testFinancialDataPOAOnly)

      result.isRight shouldBe true
      result shouldBe Right(Payments(Seq()))
    }

    "return the correct payment error when the Json can't be transformed" in {
      val result = Payments.from.from(TestFinancialData.badPaymentModel)

      result.isLeft shouldBe true
      result.left.get.msg shouldBe "[Payments] Unable to parse the Json from DES model"

    }
  }

}
