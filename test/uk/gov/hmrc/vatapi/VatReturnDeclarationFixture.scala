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

package uk.gov.hmrc.vatapi

import play.api.libs.json.{JsObject, Json}
import uk.gov.hmrc.vatapi.models.VatReturnDeclaration

object VatReturnDeclarationFixture {

  val vatReturnDeclaration = VatReturnDeclaration(
    periodKey = "#001",
    vatDueSales = 7000,
    vatDueAcquisitions = 3000,
    totalVatDue = 10000,
    vatReclaimedCurrPeriod = 1000,
    netVatDue = 9000,
    totalValueSalesExVAT = 1000,
    totalValuePurchasesExVAT = 200,
    totalValueGoodsSuppliedExVAT = 100,
    totalAcquisitionsExVAT = 540,
    finalised = true
  )

  val vatReturnDeclarationJson: JsObject = {
    Json.obj(
      "periodKey" -> "#001",
      "vatDueSales" -> 7000,
      "vatDueAcquisitions" -> 3000,
      "totalVatDue" -> 10000,
      "vatReclaimedCurrPeriod" -> 1000,
      "netVatDue" -> 9000,
      "totalValueSalesExVAT" -> 1000,
      "totalValuePurchasesExVAT" -> 200,
      "totalValueGoodsSuppliedExVAT" -> 100,
      "totalAcquisitionsExVAT" -> 540,
      "finalised" -> true
    )
  }
}
