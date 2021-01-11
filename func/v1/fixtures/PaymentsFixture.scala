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
import uk.gov.hmrc.domain.Vrn
import v1.models.request.payments.{PaymentsRawData, PaymentsRequest}
import v1.models.response.common.TaxPeriod
import v1.models.response.payments.PaymentsResponse.Payment
import v1.models.response.payments.{PaymentItem, PaymentsResponse}

trait PaymentsFixture {

  val vrn: String = "123456789"
  val toDate: String = "2017-01-01"
  val fromDate: String = "2018-01-01"

  val correlationId: String = "X-ID"

  val rawData: PaymentsRawData =
    PaymentsRawData(vrn = vrn, from = Some(fromDate), to = Some(toDate))

  val request: PaymentsRequest =
    PaymentsRequest(vrn = Vrn(vrn), from = fromDate, to = toDate)

  val paymentsDesJson: JsValue = Json.parse(
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
      |               "paymentAmount":15.0,
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
      |               "paymentAmount":40.00,
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
      |               "paymentAmount":1001.00,
      |               "paymentMethod":"A",
      |               "paymentLot":"081203010024",
      |               "paymentLotItem":"000001",
      |               "clearingSAPDocument":"3350000242",
      |               "statisticalDocument":"A"
      |            }
      |         ]
      |      },
      |      {
      |         "chargeType":"VAT Return Debit Charge",
      |         "mainType":"VAT Return Charge",
      |         "periodKey":"15AD",
      |         "periodKeyDescription":"August 2017",
      |         "taxPeriodFrom":"2017-08-01",
      |         "taxPeriodTo":"2017-12-20",
      |         "businessPartner":"0100062914",
      |         "contractAccountCategory":"33",
      |         "contractAccount":"000917000429",
      |         "contractObjectType":"ZVAT",
      |         "contractObject":"00000018000000000106",
      |         "sapDocumentNumber":"003390002284",
      |         "sapDocumentNumberItem":"0001",
      |         "chargeReference":"XQ002750002152",
      |         "mainTransaction":"4700",
      |         "subTransaction":"1174",
      |         "originalAmount":4000.0,
      |         "accruedInterest":10.0,
      |         "items":[
      |            {
      |               "subItem":"000",
      |               "dueDate":"2017-08-05",
      |               "amount":322.00,
      |               "clearingReason":"01",
      |               "clearingDate":"2017-08-05",
      |               "outgoingPaymentMethod":"A",
      |               "paymentLock":"a",
      |               "clearingLock":"A",
      |               "interestLock":"C",
      |               "dunningLock":"1",
      |               "returnFlag":true,
      |               "paymentReference":"a",
      |               "paymentAmount":322.00,
      |               "paymentMethod":"A",
      |               "paymentLot":"081203010024",
      |               "paymentLotItem":"000001",
      |               "clearingSAPDocument":"3350000254",
      |               "statisticalDocument":"A"
      |            },
      |            {
      |               "subItem":"001",
      |               "dueDate":"2017-04-02",
      |               "amount":90.00,
      |               "clearingReason":"01",
      |               "outgoingPaymentMethod":"A",
      |               "paymentLock":"a",
      |               "clearingLock":"A",
      |               "interestLock":"C",
      |               "dunningLock":"1",
      |               "returnFlag":true,
      |               "paymentReference":"a",
      |               "paymentAmount":90.00,
      |               "paymentMethod":"A",
      |               "paymentLot":"081203010024",
      |               "paymentLotItem":"000002",
      |               "clearingSAPDocument":"3350000255",
      |               "statisticalDocument":"A"
      |            },
      |            {
      |               "subItem":"002",
      |               "dueDate":"2017-09-01",
      |               "amount":6.00,
      |               "clearingDate":"2017-09-12",
      |               "clearingReason":"01",
      |               "outgoingPaymentMethod":"A",
      |               "paymentLock":"a",
      |               "clearingLock":"A",
      |               "interestLock":"C",
      |               "dunningLock":"1",
      |               "returnFlag":true,
      |               "paymentReference":"a",
      |               "paymentAmount":6.00,
      |               "paymentMethod":"A",
      |               "paymentLot":"081203010024",
      |               "paymentLotItem":"000003",
      |               "clearingSAPDocument":"3350000256",
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
    """.stripMargin
  )

  val paymentsResponse: PaymentsResponse =
    PaymentsResponse(
      payments = Seq(
        Payment(
          taxPeriod = Some(TaxPeriod(from = "2017-02-01", to = "2017-02-28")),
          `type` = "VAT Return Debit Charge",
          paymentItems = Some(Seq(
            PaymentItem(amount = Some(15.0), received = Some("2017-02-11"))
          ))
        ),
        Payment(
          taxPeriod = Some(TaxPeriod(from = "2017-03-01", to = "2017-03-25")),
          `type` = "VAT Return Debit Charge",
          paymentItems = Some(Seq(
            PaymentItem(amount = Some(40.00), received = Some("2017-03-11")),
            PaymentItem(amount = Some(1001.00), received = Some("2017-03-12"))
          ))
        ),
        Payment(
          taxPeriod = Some(TaxPeriod(from = "2017-08-01", to = "2017-12-20")),
          `type` = "VAT Return Debit Charge",
          paymentItems = Some(Seq(
            PaymentItem(Some(322.00), Some("2017-08-05")),
            PaymentItem(Some(90.00), None),
            PaymentItem(Some(6.00), Some("2017-09-12"))
          ))
        )
      )
    )

  val paymentsMtdJson: JsValue = Json.parse(
    """
      |{
      |   "payments":[
      |      {
      |         "amount":15,
      |         "received":"2017-02-11"
      |      },
      |      {
      |         "amount":40,
      |         "received":"2017-03-11"
      |      },
      |      {
      |         "amount":1001,
      |         "received":"2017-03-12"
      |      },
      |      {
      |         "amount":322,
      |         "received":"2017-08-05"
      |      },
      |      {
      |         "amount":90
      |      },
      |      {
      |         "amount":6,
      |         "received":"2017-09-12"
      |      }
      |   ]
      |}
    """.stripMargin
  )

  val unsupportedPaymentsDesJson: JsValue = Json.parse(
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
      |         "taxPeriodFrom":"2019-01-01",
      |         "taxPeriodTo":"2019-02-01",
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
      |         "accruedInterest":0
      |      },
      |      {
      |         "chargeType":"VAT Return Debit Charge",
      |         "mainType":"VAT Return Charge",
      |         "periodKey":"15AC",
      |         "periodKeyDescription":"March 2018",
      |         "taxPeriodFrom":"2019-02-02",
      |         "taxPeriodTo":"2019-03-01",
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
      |               "paymentAmount":40.00,
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
      |               "paymentAmount":1001.00,
      |               "paymentMethod":"A",
      |               "paymentLot":"081203010024",
      |               "paymentLotItem":"000001",
      |               "clearingSAPDocument":"3350000242",
      |               "statisticalDocument":"A"
      |            }
      |         ]
      |      },
      |      {
      |         "chargeType":"VAT Return Debit Charge",
      |         "mainType":"VAT Return Charge",
      |         "periodKey":"15AD",
      |         "periodKeyDescription":"August 2017",
      |         "taxPeriodFrom":"2019-03-02",
      |         "taxPeriodTo":"2019-04-01",
      |         "businessPartner":"0100062914",
      |         "contractAccountCategory":"33",
      |         "contractAccount":"000917000429",
      |         "contractObjectType":"ZVAT",
      |         "contractObject":"00000018000000000106",
      |         "sapDocumentNumber":"003390002284",
      |         "sapDocumentNumberItem":"0001",
      |         "chargeReference":"XQ002750002152",
      |         "mainTransaction":"4700",
      |         "subTransaction":"1174",
      |         "originalAmount":4000.0,
      |         "accruedInterest":10.0,
      |         "items":[
      |            {
      |               "subItem":"000",
      |               "dueDate":"2017-08-05",
      |               "amount":322.00,
      |               "clearingReason":"01",
      |               "outgoingPaymentMethod":"A",
      |               "paymentLock":"a",
      |               "clearingLock":"A",
      |               "interestLock":"C",
      |               "dunningLock":"1",
      |               "returnFlag":true,
      |               "paymentReference":"a",
      |               "paymentMethod":"A",
      |               "paymentLot":"081203010024",
      |               "paymentLotItem":"000001",
      |               "clearingSAPDocument":"3350000254",
      |               "statisticalDocument":"A"
      |            },
      |            {
      |               "subItem":"001",
      |               "dueDate":"2017-04-02",
      |               "amount":90.00,
      |               "clearingReason":"01",
      |               "outgoingPaymentMethod":"A",
      |               "paymentLock":"a",
      |               "clearingLock":"A",
      |               "interestLock":"C",
      |               "dunningLock":"1",
      |               "returnFlag":true,
      |               "paymentReference":"a",
      |               "paymentMethod":"A",
      |               "paymentLot":"081203010024",
      |               "paymentLotItem":"000002",
      |               "clearingSAPDocument":"3350000255",
      |               "statisticalDocument":"A"
      |            },
      |            {
      |               "subItem":"002",
      |               "dueDate":"2017-09-01",
      |               "amount":6.00,
      |               "clearingReason":"01",
      |               "outgoingPaymentMethod":"A",
      |               "paymentLock":"a",
      |               "clearingLock":"A",
      |               "interestLock":"C",
      |               "dunningLock":"1",
      |               "returnFlag":true,
      |               "paymentReference":"a",
      |               "paymentMethod":"A",
      |               "paymentLot":"081203010024",
      |               "paymentLotItem":"000003",
      |               "clearingSAPDocument":"3350000256",
      |               "statisticalDocument":"A"
      |            }
      |         ]
      |      },
      |      {
      |         "chargeType":"Payment on account",
      |         "periodKey":"0318",
      |         "taxPeriodFrom":"2019-04-02",
      |         "taxPeriodTo":"2019-05-01",
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
      |         ]
      |      }
      |   ]
      |}
    """.stripMargin
  )
}
