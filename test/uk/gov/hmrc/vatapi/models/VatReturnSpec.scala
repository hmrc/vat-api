/*
 * Copyright 2017 HM Revenue & Customs
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

import uk.gov.hmrc.vatapi.UnitSpec
import uk.gov.hmrc.vatapi.resources.JsonSpec
import play.api.libs.json.Json.toJson

class VatReturnSpec extends UnitSpec with JsonSpec {

  "VatReturn" should {
    "round trip" in {
      roundTripJson(
        VatReturn(
          periodKey = "#001",
          vatDueSales = 500.00,
          vatDueAcquisitions = 100.30,
          totalVatDue = 600.30,
          vatReclaimedCurrPeriod = -450.00,
          netVatDue = 1050.30,
          totalValueSalesExVAT = 1000,
          totalValuePurchasesExVAT = 200.00,
          totalValueGoodsSuppliedExVAT = 100.00,
          totalAcquisitionsExVAT = 540.00
        ))
    }
  }

  "reject VAT returns with values greater than 50 billion" in {
    assertValidationErrorWithCode(
      VatReturn(
        periodKey = "#001",
        vatDueSales = BigDecimal("50000000000"),
        vatDueAcquisitions = 100.30,
        totalVatDue = 350.00,
        vatReclaimedCurrPeriod = -450.00,
        netVatDue = 2000.00,
        totalValueSalesExVAT = 1000,
        totalValuePurchasesExVAT = 200.00,
        totalValueGoodsSuppliedExVAT = 100.00,
        totalAcquisitionsExVAT = 540.00
      ),
      "/vatDueSales",
      ErrorCode.INVALID_MONETARY_AMOUNT
    )
  }

  "reject VAT returns with negative amounts where non-negative amounts are expected" in {
    assertValidationErrorWithCode(
      VatReturn(
        periodKey = "#001",
        vatDueSales = 10.00,
        vatDueAcquisitions = 100.30,
        totalVatDue = 350.00,
        vatReclaimedCurrPeriod = -450.00,
        netVatDue = -2000.00,
        totalValueSalesExVAT = 1000,
        totalValuePurchasesExVAT = 200.00,
        totalValueGoodsSuppliedExVAT = 100.00,
        totalAcquisitionsExVAT = 540.00
      ),
      "/netVatDue",
      ErrorCode.INVALID_MONETARY_AMOUNT
    )
  }

  "reject VAT returns with decimal amounts where whole amounts are expected" in {
    assertValidationErrorWithCode(
      VatReturn(
        periodKey = "#001",
        vatDueSales = 10.00,
        vatDueAcquisitions = 100.30,
        totalVatDue = 350.00,
        vatReclaimedCurrPeriod = -450.00,
        netVatDue = 2000.00,
        totalValueSalesExVAT = 1000,
        totalValuePurchasesExVAT = 200.35,
        totalValueGoodsSuppliedExVAT = 100.00,
        totalAcquisitionsExVAT = 540.00
      ),
      "/totalValuePurchasesExVAT",
      ErrorCode.INVALID_MONETARY_AMOUNT
    )
  }

  "reject VAT returns with amounts with more than 2 decimal places" in {
    assertValidationErrorWithCode(
      VatReturn(
        periodKey = "#001",
        vatDueSales = 500.00,
        vatDueAcquisitions = 100.305,
        totalVatDue = 350.00,
        vatReclaimedCurrPeriod = -450.00,
        netVatDue = 2000.00,
        totalValueSalesExVAT = 1000,
        totalValuePurchasesExVAT = 200.00,
        totalValueGoodsSuppliedExVAT = 100.00,
        totalAcquisitionsExVAT = 540.00
      ),
      "/vatDueAcquisitions",
      ErrorCode.INVALID_MONETARY_AMOUNT
    )
  }

  "reject VAT returns in which the totalVatDue is not equal to vatDueSales + vatDueAcquisitions and netVatDue is not equal to totalVatDue - vatReclaimedCurrPeriod" in {
    assertValidationErrorsWithCode[VatReturn](
      toJson(
        VatReturn(
          periodKey = "#001",
          vatDueSales = 200.00,
          vatDueAcquisitions = 100.00,
          totalVatDue = 400.00,
          vatReclaimedCurrPeriod = 100.00,
          netVatDue = 500.00,
          totalValueSalesExVAT = 1000,
          totalValuePurchasesExVAT = 200.00,
          totalValueGoodsSuppliedExVAT = 100.00,
          totalAcquisitionsExVAT = 540.00
        )
      ),
      Map("/totalVatDue" -> Seq(ErrorCode.VAT_TOTAL_VALUE),
          "/netVatDue" -> Seq(ErrorCode.VAT_NET_VALUE))
    )
  }

}
