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

package uk.gov.hmrc.vatapi.resources.wrappers

import play.api.libs.json.{JsValue, Json}
import uk.gov.hmrc.domain.Vrn
import uk.gov.hmrc.http.HttpResponse
import uk.gov.hmrc.vatapi.UnitSpec
import uk.gov.hmrc.vatapi.resources.Jsons

class FinancialDataResponseSpec extends UnitSpec {
  val vrn = Vrn("123456789")

  val emptyJson: JsValue = Json.parse("""{}""")

  "FinancialDataResponse for liabilities" should {
    "wrap empty response" in {
      val response = FinancialDataResponse(HttpResponse(200, Some(emptyJson)))

      val liabilities = response.getLiabilities(vrn)
      liabilities.left.get.msg contains  "Json format from DES doesn't match the FinancialData model"
    }


    "wrap invalid json response" in {
      val response = FinancialDataResponse(HttpResponse(200, Some(Jsons.FinancialData.oneLiability)))

      val liabilities = response.getLiabilities(vrn)
      liabilities.left.get.msg contains  "Json format from DES doesn't match the FinancialData model"
    }

    "wrap valid response" in {
      val response = FinancialDataResponse(HttpResponse(200, Some(Jsons.FinancialData.singleLiabilityDesResponse)))

      val liabilities = response.getLiabilities(vrn)
      liabilities.right.get.liabilities.head.`type` shouldBe "VAT Return Debit Charge"
    }
  }

  "FinancialDataResponse for payments" should {
    "wrap empty response" in {
      val response = FinancialDataResponse(HttpResponse(200, Some(emptyJson)))

      val payments = response.getPayments(vrn)
      payments.left.get.msg contains  "Json format from DES doesn't match the FinancialData model"
    }


    "wrap invalid json response" in {
      val response = FinancialDataResponse(HttpResponse(200, Some(Jsons.FinancialData.onePayment)))

      val payments = response.getPayments(vrn)
      payments.left.get.msg contains  "Json format from DES doesn't match the FinancialData model"
    }

    "wrap valid response" in {
      val response = FinancialDataResponse(HttpResponse(200, Some(Jsons.FinancialData.singlePaymentDesResponse)))

      val payments = response.getPayments(vrn)
      payments.right.get.payments.head.amount shouldBe 1534.65
    }
  }
}