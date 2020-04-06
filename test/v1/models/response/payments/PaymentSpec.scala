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

package v1.models.response.payments

import play.api.libs.json.Json
import support.UnitSpec
import v1.models.response.common.TaxPeriod

class PaymentSpec extends UnitSpec {

  private val desJson = Json.parse(
    s"""
       |      {
       |         "chargeType":"VAT Return Debit Charge",
       |         "mainType":"VAT Return Charge",
       |         "periodKey":"15AD",
       |         "periodKeyDescription":"February 2018",
       |         "taxPeriodFrom":"2017-02-01",
       |         "taxPeriodTo":"2017-02-28",
       |         "businessPartner":"0100062914",
       |         "contractAccountCategory":"33",
       |         "contractAccount":"000917000429",
       |         "contractObjectType":"ZVAT",
       |         "contractObject":"00000018000000000104",
       |         "sapDocumentNumber":"003390002284",
       |         "sapDocumentNumberItem":"0001",
       |         "chargeReference":"XQ002750002150",
       |         "mainTransaction":"4700",
       |         "subTransaction":"1174",
       |         "originalAmount":15.65,
       |         "outstandingAmount":10.65,
       |         "clearedAmount":5.0,
       |         "accruedInterest":0,
       |         "items":[
       |            {
       |               "subItem":"000",
       |               "dueDate":"2017-02-11",
       |               "amount":15.0,
       |               "clearingDate":"2017-02-11",
       |               "clearingReason":"01",
       |               "outgoingPaymentMethod":"A",
       |               "paymentLock":"a",
       |               "clearingLock":"A",
       |               "interestLock":"C",
       |               "dunningLock":"1",
       |               "returnFlag":true,
       |               "paymentReference":"a",
       |               "paymentAmount":5.0,
       |               "paymentMethod":"A",
       |               "paymentLot":"081203010024",
       |               "paymentLotItem":"000001",
       |               "clearingSAPDocument":"3350000212",
       |               "statisticalDocument":"A"
       |            }
       |         ]
       |      }
       |""".stripMargin)

  private val payment = Payment(
    taxPeriod = Some(
      TaxPeriod(
        from = "2017-02-01",
        to = "2017-02-28"
      )
    ),
    `type` = "VAT Return Debit Charge",
    Some(Seq(PaymentItem(amount = Some(5.00),
      received = Some("2017-02-11")
    )))
  )

  private val desMultiplePaymentItemsJson = Json.parse(
    """
      |{
      |         "chargeType":"VAT Return Debit Charge",
      |         "mainType":"VAT Return Charge",
      |         "periodKey":"15AC",
      |         "periodKeyDescription":"March 2018",
      |         "taxPeriodFrom":"2017-03-01",
      |         "taxPeriodTo":"2017-03-25",
      |         "businessPartner":"0100062914",
      |         "contractAccountCategory":"33",
      |         "contractAccount":"000917000429",
      |         "contractObjectType":"ZVAT",
      |         "contractObject":"00000018000000000105",
      |         "sapDocumentNumber":"003390002284",
      |         "sapDocumentNumberItem":"0001",
      |         "chargeReference":"XQ002750002151",
      |         "mainTransaction":"4700",
      |         "subTransaction":"1174",
      |         "originalAmount":1050.00,
      |         "outstandingAmount":0,
      |         "clearedAmount":1050.00,
      |         "accruedInterest":0,
      |         "items":[
      |            {
      |               "subItem":"000",
      |               "dueDate":"2017-03-01",
      |               "amount":40.00,
      |               "clearingDate":"2017-03-11",
      |               "clearingReason":"01",
      |               "outgoingPaymentMethod":"A",
      |               "paymentLock":"a",
      |               "clearingLock":"A",
      |               "interestLock":"C",
      |               "dunningLock":"1",
      |               "returnFlag":true,
      |               "paymentReference":"a",
      |               "paymentAmount":50.00,
      |               "paymentMethod":"A",
      |               "paymentLot":"081203010024",
      |               "paymentLotItem":"000001",
      |               "clearingSAPDocument":"3350000241",
      |               "statisticalDocument":"A"
      |            },
      |            {
      |               "subItem":"001",
      |               "dueDate":"2017-03-02",
      |               "amount":1001.00,
      |               "clearingDate":"2017-03-12",
      |               "clearingReason":"01",
      |               "outgoingPaymentMethod":"A",
      |               "paymentLock":"a",
      |               "clearingLock":"A",
      |               "interestLock":"C",
      |               "dunningLock":"1",
      |               "returnFlag":true,
      |               "paymentReference":"a",
      |               "paymentAmount":1000.00,
      |               "paymentMethod":"A",
      |               "paymentLot":"081203010024",
      |               "paymentLotItem":"000001",
      |               "clearingSAPDocument":"3350000242",
      |               "statisticalDocument":"A"
      |            }
      |         ]
      |      }
      |
      |""".stripMargin)

  private val mulpiplePaymentsObj = Payment(
    Some(TaxPeriod("2017-03-01","2017-03-25")),"VAT Return Debit Charge",
    Some(List(PaymentItem(Some(50.00),Some("2017-03-11")),
      PaymentItem(Some(1000.00),Some("2017-03-12")))))

  "Payment" should {
    "return payment object" when {

      "read a json having single payment item" in {

        desJson.as[Payment] shouldBe payment
      }

      "read a json having multiple payment items" in {

        desMultiplePaymentItemsJson.as[Payment] shouldBe mulpiplePaymentsObj
      }

      "all fields are present in a single list" in {

        desJson.as[Payment] shouldBe payment
      }

      "one payment is returned without items:received" in {

        val desJsonWithOnlyMandatoryFields = Json.parse(
          s"""
             |      {
             |         "chargeType":"VAT Return Debit Charge",
             |         "mainType":"VAT Return Charge",
             |         "periodKey":"15AD",
             |         "periodKeyDescription":"February 2018",
             |         "taxPeriodFrom":"2017-02-01",
             |         "taxPeriodTo":"2017-02-28",
             |         "items":[
             |            {
             |               "subItem":"000",
             |               "dueDate":"2017-02-11",
             |               "amount":15.0,
             |               "returnFlag":true,
             |               "paymentReference":"a",
             |               "paymentAmount":5.0,
             |               "paymentMethod":"A",
             |               "paymentLot":"081203010024",
             |               "paymentLotItem":"000001"
             |            }
             |         ]
             |      }
             |""".stripMargin)

        val payment = Payment(
          taxPeriod = Some(
            TaxPeriod(
              from = "2017-02-01",
              to = "2017-02-28"
            )
          ),
          `type` = "VAT Return Debit Charge",
          Some(Seq(PaymentItem(amount = Some(5.00),
            received = None
          )))
        )

        desJsonWithOnlyMandatoryFields.as[Payment] shouldBe payment
      }

      "not parse incorrect json" in {

        val desJson = Json.parse(
          s"""
             |{
             |    "idType": "VRN"
             |}
             |""".stripMargin)

        desJson.asOpt[Payment] shouldBe None
      }

    }

    "use the writes format correctly" in {

      val mtdJson = Json.parse(
        s"""
           |{
           |	"taxPeriod": {
           |		"from": "2017-02-01",
           |		"to": "2017-02-28"
           |	},
           |	"type": "VAT Return Debit Charge",
           |	"paymentItem": [{
           |		"amount": 5,
           |		"received": "2017-02-11"
           |	}]
           |}
           |""".stripMargin)

      Json.toJson(payment) shouldBe mtdJson
    }
  }
}
