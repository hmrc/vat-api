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

import java.time.LocalDate

import play.api.libs.json.{JsError, JsValue, Json}
import support.UnitSpec
import v1.models.response.common.TaxPeriod
import v1.models.response.payments.PaymentsResponse.Payment

class PaymentsResponseSpec extends UnitSpec {

  implicit val to: LocalDate = LocalDate.parse("2017-12-01")

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
      |            }
      |         ]
      |      },
      |      {
      |         "chargeType":"VAT Return Debit Charge",
      |         "mainType":"VAT Return Charge",
      |         "periodKey":"15AD",
      |         "periodKeyDescription":"February 2018",
      |         "items":[
      |            {
      |               "clearingDate":"2017-03-19",
      |               "paymentAmount":-25.0
      |            }
      |         ]
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
       |      },
       |      {
       |         "amount": -25,
       |         "received": "2017-03-19"
       |      }
       |   ]
       |}
    """.stripMargin
  )

  val paymentsResponseModel: PaymentsResponse =
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
          taxPeriod = None,
          `type` = "VAT Return Debit Charge",
          paymentItems = Some(Seq(
            PaymentItem(amount = Some(-25.00), received = Some("2017-03-19"))
          ))
        )
      )
    )

  "PaymentResponse" when {
    "read from valid JSON" should {
      "produce the expected PaymentResponse object" in {
        desJson.as[PaymentsResponse] shouldBe paymentsResponseModel
      }
    }

    "read from valid JSON with some unsupported items" should {

      val filteredPaymentsResponseModel: PaymentsResponse =
        PaymentsResponse(
          Seq(
            Payment(
              taxPeriod = Some(TaxPeriod(from = "2017-04-20", to = "2017-09-28")),
              `type` = "VAT Return Debit Charge",
              paymentItems = Some(Seq(
                PaymentItem(amount = Some(-25.00), received = Some("2017-03-19"))
              ))
            )
          )
        )

      "filter away any payments with an unsupported `type`" in {
        val invalidChargeTypeDesJson: JsValue = Json.parse(
          """
            |{
            |   "idType":"VRN",
            |   "idNumber":"100062914",
            |   "regimeType":"VATC",
            |   "processingDate":"2017-05-13T09:30:00.000Z",
            |   "financialTransactions":[
            |      {
            |         "chargeType":"payment on account",
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
            |            }
            |         ]
            |      },
            |      {
            |         "chargeType":"VAT Return Debit Charge",
            |         "mainType":"VAT Return Charge",
            |         "periodKey":"15AD",
            |         "periodKeyDescription":"February 2018",
            |         "taxPeriodFrom":"2017-04-20",
            |         "taxPeriodTo":"2017-09-28",
            |         "items":[
            |            {
            |               "clearingDate":"2017-03-19",
            |               "paymentAmount":-25.0
            |            }
            |         ]
            |      }
            |   ]
            |}
          """.stripMargin
        )

        invalidChargeTypeDesJson.as[PaymentsResponse] shouldBe filteredPaymentsResponseModel
      }

      "filter away any payments with an unsupported date" in {
        val invalidDateDesJson: JsValue = Json.parse(
          """
            |{
            |   "idType":"VRN",
            |   "idNumber":"100062914",
            |   "regimeType":"VATC",
            |   "processingDate":"2017-05-13T09:30:00.000Z",
            |   "financialTransactions":[
            |      {
            |         "chargeType":"not payment on account",
            |         "taxPeriodFrom":"2017-02-01",
            |         "taxPeriodTo":"2017-12-28",
            |         "items":[
            |            {
            |               "clearingDate":"2017-02-11",
            |               "paymentAmount":5.0
            |            },
            |            {
            |               "clearingDate":"2017-04-11",
            |               "paymentAmount":10.0
            |            }
            |         ]
            |      },
            |      {
            |         "chargeType":"VAT Return Debit Charge",
            |         "mainType":"VAT Return Charge",
            |         "periodKey":"15AD",
            |         "periodKeyDescription":"February 2018",
            |         "taxPeriodFrom":"2017-04-20",
            |         "taxPeriodTo":"2017-09-28",
            |         "items":[
            |            {
            |               "clearingDate":"2017-03-19",
            |               "paymentAmount":-25.0
            |            }
            |         ]
            |      }
            |   ]
            |}
          """.stripMargin
        )

        invalidDateDesJson.as[PaymentsResponse] shouldBe filteredPaymentsResponseModel
      }

      "filter away any payments without paymentItems" in {
        val noPaymentAmountDesJson: JsValue = Json.parse(
          """
            |{
            |   "idType":"VRN",
            |   "idNumber":"100062914",
            |   "regimeType":"VATC",
            |   "processingDate":"2017-05-13T09:30:00.000Z",
            |   "financialTransactions":[
            |      {
            |         "chargeType":"not payment on account",
            |         "taxPeriodFrom":"2017-02-01",
            |         "taxPeriodTo":"2017-12-28"
            |      },
            |      {
            |         "chargeType":"VAT Return Debit Charge",
            |         "mainType":"VAT Return Charge",
            |         "periodKey":"15AD",
            |         "periodKeyDescription":"February 2018",
            |         "taxPeriodFrom":"2017-04-20",
            |         "taxPeriodTo":"2017-09-28",
            |         "items":[
            |            {
            |               "clearingDate":"2017-03-19",
            |               "paymentAmount":-25.0
            |            },
            |            {
            |               "clearingDate":"2017-04-19"
            |            }
            |         ]
            |      }
            |   ]
            |}
          """.stripMargin
        )

        noPaymentAmountDesJson.as[PaymentsResponse] shouldBe filteredPaymentsResponseModel
      }

      "filter away any payments that don't have paymentAmount but have a clearingDate" in {
        val noPaymentItemsDesJson: JsValue = Json.parse(
          """
            |{
            |   "idType":"VRN",
            |   "idNumber":"100062914",
            |   "regimeType":"VATC",
            |   "processingDate":"2017-05-13T09:30:00.000Z",
            |   "financialTransactions":[
            |      {
            |         "chargeType":"not payment on account",
            |         "taxPeriodFrom":"2017-02-01",
            |         "taxPeriodTo":"2017-12-28"
            |      },
            |      {
            |         "chargeType":"VAT Return Debit Charge",
            |         "mainType":"VAT Return Charge",
            |         "periodKey":"15AD",
            |         "periodKeyDescription":"February 2018",
            |         "taxPeriodFrom":"2017-04-20",
            |         "taxPeriodTo":"2017-09-28",
            |         "items":[
            |            {
            |               "clearingDate":"2017-03-19",
            |               "paymentAmount":-25.0
            |            }
            |         ]
            |      }
            |   ]
            |}
          """.stripMargin
        )

        noPaymentItemsDesJson.as[PaymentsResponse] shouldBe filteredPaymentsResponseModel
      }

      "apply all three filters correctly" in {
        val multipleUnsupportedDesJson: JsValue = Json.parse(
          """
            |{
            |   "idType":"VRN",
            |   "idNumber":"100062914",
            |   "regimeType":"VATC",
            |   "processingDate":"2017-05-13T09:30:00.000Z",
            |   "financialTransactions":[
            |      {
            |         "chargeType":"payment on account",
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
            |            }
            |         ]
            |      },
            |      {
            |         "chargeType":"not payment on account",
            |         "taxPeriodFrom":"2017-02-01",
            |         "taxPeriodTo":"2017-12-28",
            |         "items":[
            |            {
            |               "clearingDate":"2017-02-11",
            |               "paymentAmount":5.0
            |            },
            |            {
            |               "clearingDate":"2017-04-11",
            |               "paymentAmount":10.0
            |            }
            |         ]
            |      },
            |      {
            |         "chargeType":"payment on account",
            |         "taxPeriodFrom":"2017-02-01",
            |         "taxPeriodTo":"2017-02-28"
            |      }
            |   ]
            |}
          """.stripMargin
        )

        multipleUnsupportedDesJson.as[PaymentsResponse] shouldBe PaymentsResponse(Seq.empty[Payment])
      }
    }

    "read from invalid JSON" should {
      "produce a JsError" in {
        val invalidJson: JsValue = Json.parse(
          """
            |{
            |  "chargeType": false
            |}
          """.stripMargin
        )

        invalidJson.validate[PaymentsResponse] shouldBe a[JsError]
      }
    }

    "read from JSON where 'chargeType' is missing in an item" in {

      val desJson = Json.parse(
        s"""
           |{
           |    "idType": "VRN",
           |    "idNumber": "XQIT00000000001",
           |    "regimeType": "VATC",
           |    "processingDate": "2017-03-07T09:30:00.000Z",
           |    "financialTransactions": [
           |       {
           |            "taxPeriodFrom": "2017-01-01",
           |            "taxPeriodTo": "2017-04-05"
           |        }
           |    ]
           |}
            """.stripMargin
      )

      desJson.validate[PaymentsResponse] shouldBe a[JsError]
    }

    "written to JSON" should {
      "produce the expected JsObject" in {
        Json.toJson(paymentsResponseModel) shouldBe mtdJson
      }
    }
  }
}
