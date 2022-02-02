/*
 * Copyright 2022 HM Revenue & Customs
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

package v1.models.response.payments

import play.api.libs.json.{JsError, JsObject, JsValue, Json}
import support.UnitSpec

class PaymentItemSpec extends UnitSpec {

  val desJson: JsValue = Json.parse(
    """
      |{
      |  "paymentAmount" : 100.2,
      |  "clearingDate" : "2017-01-01"
      |}
    """.stripMargin
  )

  val invalidDesJson: JsValue = Json.parse(
    """
      |{
      |  "paymentAmount" : 100.2,
      |  "clearingDate" : false
      |}
    """.stripMargin
  )

  val mtdJson: JsValue = Json.parse(
    """
      |{
      |  "amount" : 100.2,
      |  "received" : "2017-01-01"
      |}
    """.stripMargin
  )

  val paymentItemModel: PaymentItem =
    PaymentItem(amount = Some(100.2), received = Some("2017-01-01"))

  "PaymentItem" when {
    "read from valid JSON" should {
      "produce the expected PaymentItem object" in {
        desJson.as[PaymentItem] shouldBe paymentItemModel
      }

      "handle missing optional fields" in {
        JsObject.empty.as[PaymentItem] shouldBe PaymentItem.empty
      }

      "error on invalid json" in {
        invalidDesJson.validate[PaymentItem] shouldBe a[JsError]
      }
    }

    "written to JSON" should {
      "produce the expected Js Object" in {
        Json.toJson(paymentItemModel) shouldBe mtdJson
      }

      "not write empty fields" in {

        val emptyPaymentItemModel: PaymentItem =
          PaymentItem(amount = None, received = None)

        Json.toJson(emptyPaymentItemModel) shouldBe JsObject.empty
      }
    }
  }
}
