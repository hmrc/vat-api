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

import uk.gov.hmrc.vatapi.UnitSpec
import uk.gov.hmrc.vatapi.resources.JsonSpec

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
        )
      )
    }

  }

}
