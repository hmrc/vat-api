/*
 * Copyright 2023 HM Revenue & Customs
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

import play.api.libs.json.{JsError, Json}
import support.UnitSpec
import v1.models.response.common.TaxPeriod
import v1.models.response.payments.PaymentsResponse.Payment

class PaymentSpec extends UnitSpec {

  private val desJson = Json.parse(
    s"""
       |{
       |   "chargeType":"VAT Return Debit Charge",
       |   "mainType":"VAT Return Charge",
       |   "periodKey":"15AD",
       |   "periodKeyDescription":"February 2018",
       |   "taxPeriodFrom":"2017-02-01",
       |   "taxPeriodTo":"2017-02-28",
       |   "businessPartner":"0100062914",
       |   "contractAccountCategory":"33",
       |   "contractAccount":"000917000429",
       |   "contractObjectType":"ZVAT",
       |   "contractObject":"00000018000000000104",
       |   "sapDocumentNumber":"003390002284",
       |   "sapDocumentNumberItem":"0001",
       |   "chargeReference":"XQ002750002150",
       |   "mainTransaction":"4700",
       |   "subTransaction":"1174",
       |   "originalAmount":15.65,
       |   "outstandingAmount":10.65,
       |   "clearedAmount":5.0,
       |   "accruedInterest":0,
       |   "items": [
       |      {
       |         "subItem":"000",
       |         "dueDate":"2017-02-11",
       |         "amount":15.0,
       |         "clearingDate":"2017-02-11",
       |         "clearingReason":"01",
       |         "outgoingPaymentMethod":"A",
       |         "paymentLock":"a",
       |         "clearingLock":"A",
       |         "interestLock":"C",
       |         "dunningLock":"1",
       |         "returnFlag":true,
       |         "paymentReference":"a",
       |         "paymentAmount":5.0,
       |         "paymentMethod":"A",
       |         "paymentLot":"081203010024",
       |         "paymentLotItem":"000001",
       |         "clearingSAPDocument":"3350000212",
       |         "statisticalDocument":"A"
       |      }
       |   ]
       |}
      """.stripMargin
  )

  private val multipleItemDesJson = Json.parse(
    """
      |{
      |   "chargeType":"VAT Return Debit Charge",
      |   "mainType":"VAT Return Charge",
      |   "periodKey":"15AC",
      |   "periodKeyDescription":"March 2018",
      |   "taxPeriodFrom":"2017-03-01",
      |   "taxPeriodTo":"2017-03-25",
      |   "businessPartner":"0100062914",
      |   "contractAccountCategory":"33",
      |   "contractAccount":"000917000429",
      |   "contractObjectType":"ZVAT",
      |   "contractObject":"00000018000000000105",
      |   "sapDocumentNumber":"003390002284",
      |   "sapDocumentNumberItem":"0001",
      |   "chargeReference":"XQ002750002151",
      |   "mainTransaction":"4700",
      |   "subTransaction":"1174",
      |   "originalAmount":1050.00,
      |   "outstandingAmount":0,
      |   "clearedAmount":1050.00,
      |   "accruedInterest":0,
      |   "items":[
      |      {
      |         "subItem":"000",
      |         "dueDate":"2017-03-01",
      |         "amount":40.00,
      |         "clearingDate":"2017-03-11",
      |         "clearingReason":"01",
      |         "outgoingPaymentMethod":"A",
      |         "paymentLock":"a",
      |         "clearingLock":"A",
      |         "interestLock":"C",
      |         "dunningLock":"1",
      |         "returnFlag":true,
      |         "paymentReference":"a",
      |         "paymentAmount":50.00,
      |         "paymentMethod":"A",
      |         "paymentLot":"081203010024",
      |         "paymentLotItem":"000001",
      |         "clearingSAPDocument":"3350000241",
      |         "statisticalDocument":"A"
      |      },
      |      {
      |         "subItem":"001",
      |         "dueDate":"2017-03-02",
      |         "amount":1001.00,
      |         "clearingDate":"2017-03-12",
      |         "clearingReason":"01",
      |         "outgoingPaymentMethod":"A",
      |         "paymentLock":"a",
      |         "clearingLock":"A",
      |         "interestLock":"C",
      |         "dunningLock":"1",
      |         "returnFlag":true,
      |         "paymentReference":"a",
      |         "paymentAmount":1000.00,
      |         "paymentMethod":"A",
      |         "paymentLot":"081203010024",
      |         "paymentLotItem":"000001",
      |         "clearingSAPDocument":"3350000242",
      |         "statisticalDocument":"A"
      |      }
      |   ]
      |}
    """.stripMargin)

  private val paymentModel = Payment(
    taxPeriod = Some(TaxPeriod(from = "2017-02-01", to = "2017-02-28")),
    `type` = "VAT Return Debit Charge",
    paymentItems = Some(Seq(
      PaymentItem(amount = Some(5.00), received = Some("2017-02-11"), paymentLot = Some("081203010024"), paymentLotItem = Some("000001")),
    ))
  )

  private val multipleItemPaymentModel =
    Payment(
      taxPeriod = Some(TaxPeriod("2017-03-01", "2017-03-25")),
      `type` = "VAT Return Debit Charge",
      paymentItems = Some(Seq(
        PaymentItem(amount = Some(50.00), received = Some("2017-03-11"), paymentLot = Some("081203010024"), paymentLotItem = Some("000001")),
        PaymentItem(amount = Some(1000.00), received = Some("2017-03-12"), paymentLot = Some("081203010024"), paymentLotItem = Some("000001"))
      ))
    )

  "Payment" when {
    "read from valid JSON" should {
      "produce the expected Payment object" in {
        desJson.as[Payment] shouldBe paymentModel
      }
    }

    "read from valid JSON with multiple payment items" should {
      "produce the expected Payment object" in {
        multipleItemDesJson.as[Payment] shouldBe multipleItemPaymentModel
      }
    }

    "read from valid JSON with missing optional fields" should {
      "read those fields as 'None'" in {

        val mandatoryOnlyDesJson = Json.parse(
          s"""
             |{
             |   "chargeType":"VAT Return Debit Charge"
             |}
            """.stripMargin
        )

        val mandatoryOnlyPaymentModel: Payment =
          Payment(
            taxPeriod = None,
            `type` = "VAT Return Debit Charge",
            paymentItems = None
          )

        mandatoryOnlyDesJson.as[Payment] shouldBe mandatoryOnlyPaymentModel
      }
    }

    "read from valid JSON with 'empty' payment items" should {
      "filter out payment items with no data" in {

        val invalidItemsDesJson = Json.parse(
          s"""
             |{
             |   "chargeType":"VAT Return Debit Charge",
             |   "taxPeriodFrom":"2017-02-01",
             |   "taxPeriodTo":"2017-02-28",
             |   "items": [
             |      {
             |         "clearingDate":"2017-02-11",
             |         "paymentAmount":5.0,
             |         "paymentLot":"081203010024",
             |         "paymentLotItem":"000001"
             |      },
             |      {
             |         "paymentAmount":-15.2,
             |         "paymentLot":"01234",
             |         "paymentLotItem":"0001"
             |      },
             |      {
             |         "subItem":"001",
             |         "dueDate":"2017-03-02",
             |         "amount":1001.00,
             |         "paymentLot":"6789",
             |         "paymentLotItem":"0002"
             |      }
             |
             |   ]
             |}
            """.stripMargin
        )

        val invalidItemsPaymentModel: Payment =
          Payment(
            taxPeriod = Some(TaxPeriod("2017-02-01", "2017-02-28")),
            `type` = "VAT Return Debit Charge",
            paymentItems = Some(Seq(
              PaymentItem(amount = Some(5.00), received = Some("2017-02-11"), paymentLot = Some("081203010024"), paymentLotItem = Some("000001")),
              PaymentItem(amount = Some(-15.2), received = None,  paymentLot = Some("01234"), paymentLotItem = Some("0001"))
            ))
          )

        invalidItemsDesJson.as[Payment] shouldBe invalidItemsPaymentModel
      }
    }

    "read from valid JSON all 'empty' payment items" should {
      "return the 'paymentItems' field as 'None'" in {

        val noValidItemsDesJson = Json.parse(
          s"""
             |{
             |   "chargeType":"VAT Return Debit Charge",
             |   "taxPeriodFrom":"2017-02-01",
             |   "taxPeriodTo":"2017-02-28",
             |   "items": [
             |      {
             |         "subItem": "003",
             |         "dueDate": "2017-07-12",
             |         "amount": -4003.45
             |      },
             |      {
             |         "subItem": "002",
             |         "dueDate": "2017-05-20",
             |         "amount": 304.03
             |      },
             |      {
             |         "subItem": "001",
             |         "dueDate": "2017-03-02",
             |         "amount": 1001.00
             |      }
             |
             |   ]
             |}
            """.stripMargin
        )

        val noValidItemsPaymentModel: Payment =
          Payment(
            taxPeriod = Some(TaxPeriod("2017-02-01", "2017-02-28")),
            `type` = "VAT Return Debit Charge",
            paymentItems = None
          )

        noValidItemsDesJson.as[Payment] shouldBe noValidItemsPaymentModel
      }
    }

    "read from invalid JSON" should {
      "produce a JsError" in {

        val desJson = Json.parse(
          s"""
             |{
             |    "idType": "VRN"
             |}
             |""".stripMargin)

        desJson.validate[Payment] shouldBe a[JsError]
      }
    }
  }
}
