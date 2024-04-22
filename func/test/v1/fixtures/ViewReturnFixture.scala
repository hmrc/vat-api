/*
 * Copyright 2021 HM Revenue & Customs
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

package v1.fixtures

import play.api.libs.json.{JsValue, Json}
import v1.models.response.viewReturn.ViewReturnResponse

trait ViewReturnFixture {

  val viewReturnDesJson: JsValue = Json.parse(
    """
      |{
      |    "periodKey": "A001",
      |    "vatDueSales": 1234567890123.23,
      |    "vatDueAcquisitions": -9876543210912.87,
      |    "vatDueTotal": 1234567890112.23,
      |    "vatReclaimedCurrPeriod": -1234567890122.23,
      |    "vatDueNet": 2345678901.12,
      |    "totalValueSalesExVAT": 1234567890123.00,
      |    "totalValuePurchasesExVAT": 1234567890123.00,
      |    "totalValueGoodsSuppliedExVAT": 1234567890123.00,
      |    "totalAllAcquisitionsExVAT": -1234567890123.00
      |}
    """.stripMargin
  )

  val viewReturnMtdJson: JsValue = Json.parse(
    """
      |{
      |    "periodKey": "A001",
      |    "vatDueSales": 1234567890123.23,
      |    "vatDueAcquisitions": -9876543210912.87,
      |    "totalVatDue": 1234567890112.23,
      |    "vatReclaimedCurrPeriod": -1234567890122.23,
      |    "netVatDue": 2345678901.12,
      |    "totalValueSalesExVAT": 1234567890123.00,
      |    "totalValuePurchasesExVAT": 1234567890123.00,
      |    "totalValueGoodsSuppliedExVAT": 1234567890123.00,
      |    "totalAcquisitionsExVAT": -1234567890123.00
      |}
    """.stripMargin
  )

  val viewReturnResponse: ViewReturnResponse =
    ViewReturnResponse(
      periodKey = "A001",
      vatDueSales = 1234567890123.23,
      vatDueAcquisitions = -9876543210912.87,
      totalVatDue = 1234567890112.23,
      vatReclaimedCurrPeriod = -1234567890122.23,
      netVatDue = 2345678901.12,
      totalValueSalesExVAT = 1234567890123.00,
      totalValuePurchasesExVAT = 1234567890123.00,
      totalValueGoodsSuppliedExVAT = 1234567890123.00,
      totalAcquisitionsExVAT = -1234567890123.00
    )
}
