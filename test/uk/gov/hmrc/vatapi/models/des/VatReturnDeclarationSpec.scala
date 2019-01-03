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

package uk.gov.hmrc.vatapi.models.des

import org.joda.time.DateTime
import uk.gov.hmrc.vatapi.UnitSpec
import uk.gov.hmrc.vatapi.assets.TestConstants

class VatReturnDeclarationSpec extends UnitSpec {
  "Des VatReturnDeclaration" should {
    "correctly convert to Json" in {

      val receivedAt = new DateTime(2018,12,11,9,8,7).toDateTimeISO

      import TestConstants.VatReturn._
      import TestConstants.testArn


      val expectedDesVATReturnJsonString =
        s"""{
           |"periodKey":"#001",
           |"vatDueSales":-3600.15,
           |"vatDueAcquisitions":12000.05,
           |"vatDueTotal":8399.90,
           |"vatReclaimedCurrPeriod":124.15,
           |"vatDueNet":8275.75,
           |"totalValueSalesExVAT":1000.00,
           |"totalValuePurchasesExVAT":200.00,
           |"totalValueGoodsSuppliedExVAT":100.00,
           |"totalAllAcquisitionsExVAT":540.00,
           |"agentReferenceNumber":"$testArn",
           |"receivedAt":"2018-12-11T09:08:07.000Z"
           |}""".stripMargin.replaceAll("\\s", "")

      desVatReturnDeclaration(receivedAt).toJsonString shouldBe expectedDesVATReturnJsonString
    }
  }
}