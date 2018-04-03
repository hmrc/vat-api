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

package uk.gov.hmrc.vatapi.models.des

import org.joda.time.DateTime
import play.api.libs.json._
import uk.gov.hmrc.vatapi.UnitSpec

class VatReturnDeclarationSpec extends UnitSpec {
  "VatReturnDeclaration" should {
    "correctly convert to Json" in {

      val receivedAt = new DateTime(2018,12,11,9,8,7)

      val json: JsObject = Json.obj(
        "periodKey" -> JsString("#001"),
        "vatDueSales" -> JsNumber(50.00),
        "vatDueAcquisitions" -> JsNumber(100.30),
        "vatDueTotal" -> JsNumber(50.00),
        "vatReclaimedCurrPeriod" -> JsNumber(40.00),
        "vatDueNet" -> JsNumber(110.30),
        "totalValueSalesExVAT" -> JsString("1000.00"),
        "totalValuePurchasesExVAT" -> JsString("200.00"),
        "totalValueGoodsSuppliedExVAT" -> JsString("100.00"),
        "totalAllAcquisitionsExVAT" -> JsString("540.00"),
        "receivedAt" -> JsString(receivedAt.toString("yyyy-MM-dd'T'HH:mm:ss.SSSZ"))
      )

      val model = VatReturnDeclaration(
        "#001", 50.00, 100.30, 50.00, 40.00, 110.30, "1000.00", "200.00", "100.00", "540.00", None, receivedAt
      )

      Json.toJson(model) shouldBe json
    }
  }
}
