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

import play.api.libs.json.{JsValue, Json}
import support.UnitSpec
import v1.models.response.common.TaxPeriod
import v1.models.response.payments.PaymentsResponse.Payment

class PaymentsResponseSpec extends UnitSpec {

  implicit val to: String = "2017-12-01"

  val desJson: JsValue = Json.parse(
    """
      |{
      |   "idType":"VRN",
      |   "idNumber":"100062914",
      |   "regimeType":"VATC",
      |   "processingDate":"2017-05-13T09:30:00.000Z",
      |   "financialTransactions":[
      |      {
      |         "chargeType":"VAT Return Debit Charge",
      |         "taxPeriodFrom":"2017-02-01",
      |         "taxPeriodTo":"2017-02-28",
      |         "items":[
      |            {
      |               "clearingDate":"2017-02-11",
      |               "paymentAmount":5.0
      |            },
      |            {
      |               "clearingDate":"2017-04-11",
      |               "paymentAmount":10.0
      |            },
      |         ]
      |      },
      |      {
      |         "chargeType":"VAT Return Debit Charge",
      |         "mainType":"VAT Return Charge",
      |         "periodKey":"15AD",
      |         "periodKeyDescription":"February 2018",
      |         "taxPeriodFrom":"2017-04-20",
      |         "taxPeriodTo":"2017-09-28",
      |      }
      |   ]
      |}
    """.stripMargin
  )

  val mtdJson: JsValue = Json.parse(
    """
       |{
       |   "payments":[
       |      {
       |         "amount":5,
       |         "received":"2017-02-11"
       |      },
       |      {
       |         "amount":10,
       |         "received":"2017-04-11"
       |      }
       |   ]
       |}
    """.stripMargin
  )

  val paymentsResponse: PaymentsResponse =
    PaymentsResponse(
      Seq(
        Payment(
          taxPeriod = Some(TaxPeriod(from = "2017-02-01", to = "2017-02-28")),
          `type` = "VAT Return Debit Charge",
          paymentItems = Some(Seq(
            PaymentItem(amount = Some(5.00), received = Some("2017-02-11")),
            PaymentItem(amount = Some(10.00), received = Some("2017-04-11"))
          ))
        ),
        Payment(
          taxPeriod = Some(TaxPeriod(from = "2017-04-20", to = "2017-09-28")),
          `type` = "VAT Return Debit Charge",
          paymentItems = None
        )
      )
    )

  "PaymentResponse" when {
    "read from valid JSON" should {
      "produce the expected PaymentResponse object" in {
        desJson.as[PaymentsResponse] shouldBe paymentsResponse
      }
    }

    "read from valid JSON with some unsupported items" should {
      "filter away any payments with an unsupported `type`" in {

      }

      "filter away any payments with an unsupported date" in {

      }

      "filter away any payments without paymentItems" in {

      }

      "apply all three filters correctly" in {

      }
    }

    "read from valid JSON with only unsupported items" should {
      "return an empty sequence of payments" in {

      }
    }

    "read from invalid JSON" should {
      "produce a JsError" in {

      }
    }

    "written to JSON" should {
      "produce the expected JsObject" in {
        Json.toJson(paymentsResponse) shouldBe mtdJson
      }
    }
  }



  "PaymentResponse" should {
    "return payments for all charge types other than `Payment on account`" when {

      "multiple financialTransaction types received" in {
        val desJson = Json.parse(
          """
            |{
            |   "idType":"VRN",
            |   "idNumber":"100062914",
            |   "regimeType":"VATC",
            |   "processingDate":"2017-05-13T09:30:00.000Z",
            |   "financialTransactions":[
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
            |      },
            |      {
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
            |      },
            |      {
            |         "chargeType":"Payment on account",
            |         "items":[
            |            {
            |               "subItem":"000",
            |               "amount":-10
            |            },
            |            {
            |               "subItem":"001",
            |               "dueDate":"2017-04-01",
            |               "amount":-10,
            |               "clearingDate":"2017-11-27",
            |               "paymentAmount":-10,
            |               "paymentMethod":"DIRECT DEBIT"
            |            }
            |         ],
            |         "periodKey":"0318",
            |         "taxPeriodFrom":"2017-01-01",
            |         "taxPeriodTo":"2017-03-31"
            |      }
            |   ]
            |}
            |""".stripMargin)

        val paymentResponse = PaymentsResponse(Seq(Payment(
          taxPeriod = Some(
            TaxPeriod(
              from = "2017-02-01",
              to = "2017-02-28"
            )
          ),
          `type` = "VAT Return Debit Charge",
          Some(Seq(PaymentItem(amount = Some(5.00),
            received = Some("2017-02-11")
          )))),
          Payment(
            taxPeriod = Some(
              TaxPeriod(
                from = "2017-03-01",
                to = "2017-03-25"
              )
            ),
            `type` = "VAT Return Debit Charge",
            Some(Seq(PaymentItem(Some(50.00),Some("2017-03-11")), PaymentItem(Some(1000.00),Some("2017-03-12"))
            )))))

        desJson.as[PaymentsResponse] shouldBe paymentResponse
      }
    }

    "return no payments" when {
      "transactions received only consists `Payment on account` charge types" in {

        val desJson = Json.parse(
          s"""
             |{
             |    "idType": "VRN",
             |    "idNumber": "XQIT00000000001",
             |    "regimeType": "VATC",
             |    "processingDate": "2017-03-07T09:30:00.000Z",
             |    "financialTransactions": [{
             |         "chargeType":"Payment on account",
             |         "items":[
             |            {
             |               "subItem":"000",
             |               "amount":-10
             |            },
             |            {
             |               "subItem":"001",
             |               "dueDate":"2017-04-01",
             |               "amount":-10,
             |               "clearingDate":"2017-11-27",
             |               "paymentAmount":-10,
             |               "paymentMethod":"DIRECT DEBIT"
             |            }
             |         ],
             |         "periodKey":"0318",
             |         "taxPeriodFrom":"2017-01-01",
             |         "taxPeriodTo":"2017-03-31"
             |      }
             |    ]
             |}
             |""".stripMargin)

        desJson.as[PaymentsResponse] shouldBe PaymentsResponse(Seq.empty[Payment])
      }
    }

    "return no payments" when {
      "received to date is out of request date range" in {

        val desJson = Json.parse(
          s"""
             |{
             |    "idType": "VRN",
             |    "idNumber": "XQIT00000000001",
             |    "regimeType": "VATC",
             |    "processingDate": "2017-03-07T09:30:00.000Z",
             |    "financialTransactions": [{
             |            "chargeType": "VAT",
             |            "mainType": "2100",
             |            "periodKey": "13RL",
             |            "periodKeyDescription": "abcde",
             |            "taxPeriodFrom": "2017-01-01",
             |            "taxPeriodTo": "2017-12-12",
             |            "businessPartner": "6622334455",
             |            "contractAccountCategory": "02",
             |            "contractAccount": "D",
             |            "contractObjectType": "ABCD",
             |            "contractObject": "00000003000000002757",
             |            "sapDocumentNumber": "1040000872",
             |            "sapDocumentNumberItem": "XM00",
             |            "chargeReference": "XM002610011594",
             |            "mainTransaction": "1234",
             |            "subTransaction": "5678",
             |            "originalAmount": 463872,
             |            "outstandingAmount": 463872,
             |            "accruedInterest": 10000,
             |            "items": [{
             |                "subItem": "001",
             |                "dueDate": "2017-03-08",
             |                "amount": 463872
             |            }]
             |        }
             |    ]
             |}
             |""".stripMargin)

        desJson.as[PaymentsResponse] shouldBe PaymentsResponse(Seq.empty[Payment])
      }
    }

    "return mtd formatted json" when {
      "use the writes format correctly" in {

        val mtdJson = Json.parse(
          s"""
             |{
             |   "payments":[{
             |         "amount":5,
             |         "received":"2017-02-11"
             |   },
             |   {
             |         "amount":10,
             |         "received":"2017-04-11"
             |   }]
             |}
             |""".stripMargin)

        val paymentResponse = PaymentsResponse(Seq(Payment(
          taxPeriod = Some(
            TaxPeriod(
              from = "2017-02-01",
              to = "2017-02-28"
            )
          ),
          `type` = "VAT Return Debit Charge",
          Some(Seq(PaymentItem(amount = Some(5.00),
            received = Some("2017-02-11")
          ), PaymentItem(amount = Some(10.00),
            received = Some("2017-04-11")
          ))))))

        Json.toJson(paymentResponse) shouldBe mtdJson
      }
    }

    "return empty json" when {
      "use the writes format correctly" in {

        val mtdJson = Json.parse(
          s"""
             |{
             |   "payments":[]
             |}
             |""".stripMargin)

        val paymentResponse = PaymentsResponse(Seq(Payment(
          taxPeriod = Some(
            TaxPeriod(
              from = "2017-02-01",
              to = "2017-02-28"
            )
          ),
          `type` = "VAT Return Debit Charge",None)))

        Json.toJson(paymentResponse) shouldBe mtdJson
      }
    }
  }
}
