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

package v1.models.response.liability

import play.api.libs.json.Json
import support.UnitSpec

class LiabilityResponseSpec extends UnitSpec {

  implicit val to: String = "2017-12-01"

  "LiabilityResponse" should {
    "read from the downstream model" when {

      "all fields are present in a single list" in {

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
             |            "taxPeriodTo": "2017-04-05",
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

        val liabilityResponse = LiabilityResponse(Seq(
          Liability(
            taxPeriod = Some(
              TaxPeriod(
                from = "2017-01-01",
                to = "2017-04-05"
              )
            ),
            `type` = "VAT",
            originalAmount = 463872,
            outstandingAmount = Some(463872),
            due = Some("2017-03-08")
          )
        ))

        desJson.as[LiabilityResponse] shouldBe liabilityResponse
      }

      "multiple liabilities are returned" in {

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
                   |            "taxPeriodTo": "2017-04-05",
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
                   |        },
                   |        {
                   |            "chargeType": "VAT Return Debit Charge",
                   |            "mainType": "VAT Return Charge",
                   |            "periodKey": "15AD",
                   |            "periodKeyDescription": "April 2017",
                   |            "taxPeriodFrom": "2017-04-01",
                   |            "taxPeriodTo": "2017-04-30",
                   |            "businessPartner": "0100062914",
                   |            "contractAccountCategory": "42",
                   |            "contractAccount": "000917000429",
                   |            "contractObjectType": "ZVAT",
                   |            "contractObject": "00000018000000000104",
                   |            "sapDocumentNumber": "003390002284",
                   |            "sapDocumentNumberItem": "0001",
                   |            "chargeReference": "XQ002750002150",
                   |            "mainTransaction": "4700",
                   |            "subTransaction": "1174",
                   |            "originalAmount": 15.00,
                   |            "outstandingAmount": 15.00,
                   |            "accruedInterest": 10000,
                   |            "items": [{
                   |                "subItem": "001",
                   |                "dueDate": "2017-06-09",
                   |                "amount": 15.00
                   |            }]
                   |        }
                   |    ]
                   |}
                   |""".stripMargin)

              val liabilityResponse = LiabilityResponse(Seq(
                Liability(
                  taxPeriod = Some(
                    TaxPeriod(
                      from = "2017-01-01",
                      to = "2017-04-05"
                    )
                  ),
                  `type` = "VAT",
                  originalAmount = 463872,
                  outstandingAmount = Some(463872),
                  due = Some("2017-03-08")
                ),
                Liability(
                  taxPeriod = Some(
                    TaxPeriod(
                      from = "2017-04-01",
                      to = "2017-04-30"
                    )
                  ),
                  `type` = "VAT Return Debit Charge",
                  originalAmount = 15.00,
                  outstandingAmount = Some(15.00),
                  due = Some("2017-06-09")
                )
              ))

              desJson.as[LiabilityResponse] shouldBe liabilityResponse
            }

      "one liability is returned without items:dueDate" in {

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
             |            "taxPeriodTo": "2017-04-05",
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
             |            "accruedInterest": 10000
             |        }
             |    ]
             |}
             |""".stripMargin)

        val liabilityResponse = LiabilityResponse(Seq(
          Liability(
            taxPeriod = Some(
              TaxPeriod(
                from = "2017-01-01",
                to = "2017-04-05"
              )
            ),
            `type` = "VAT",
            originalAmount = 463872,
            outstandingAmount = Some(463872),
            due = None
          )
        ))

        desJson.as[LiabilityResponse] shouldBe liabilityResponse
      }

      "multiple liabilities are returned with the minimum amount of optional fields" in {

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
             |            "accruedInterest": 10000
             |        },
             |        {
             |            "chargeType": "VAT",
             |            "mainType": "2100",
             |            "periodKey": "13RL",
             |            "periodKeyDescription": "abcde",
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
             |            "accruedInterest": 10000
             |        }
             |    ]
             |}
             |""".stripMargin)

        val liabilityResponse = LiabilityResponse(Seq(
          Liability(
            taxPeriod = None,
            `type` = "VAT",
            originalAmount = 463872,
            outstandingAmount = None,
            due = None
          ),
          Liability(
            taxPeriod = None,
            `type` = "VAT",
            originalAmount = 463872,
            outstandingAmount = None,
            due = None
          )
        ))

        desJson.as[LiabilityResponse] shouldBe liabilityResponse
      }

      "not parse incorrect json" in {

        val desJson = Json.parse(
          s"""
             |{
             |    "idType": "VRN"
             |}
             |""".stripMargin)

        desJson.asOpt[LiabilityResponse] shouldBe None
      }

      "multiple liabilities are returned, but 'payment on account' should be filtered out" in {

        val desJson = Json.parse(
          s"""
             |{
             |    "idType": "VRN",
             |    "idNumber": "XQIT00000000001",
             |    "regimeType": "VATC",
             |    "processingDate": "2017-03-07T09:30:00.000Z",
             |    "financialTransactions": [{
             |            "chargeType": "Payment on account",
             |            "mainType": "2100",
             |            "periodKey": "13RL",
             |            "periodKeyDescription": "abcde",
             |            "taxPeriodFrom": "2017-01-01",
             |            "taxPeriodTo": "2017-04-05",
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
             |        },
             |        {
             |            "chargeType": "VAT Return Debit Charge",
             |            "mainType": "VAT Return Charge",
             |            "periodKey": "15AD",
             |            "periodKeyDescription": "April 2017",
             |            "taxPeriodFrom": "2017-04-01",
             |            "taxPeriodTo": "2017-04-30",
             |            "businessPartner": "0100062914",
             |            "contractAccountCategory": "42",
             |            "contractAccount": "000917000429",
             |            "contractObjectType": "ZVAT",
             |            "contractObject": "00000018000000000104",
             |            "sapDocumentNumber": "003390002284",
             |            "sapDocumentNumberItem": "0001",
             |            "chargeReference": "XQ002750002150",
             |            "mainTransaction": "4700",
             |            "subTransaction": "1174",
             |            "originalAmount": 15.00,
             |            "outstandingAmount": 15.00,
             |            "accruedInterest": 10000,
             |            "items": [{
             |                "subItem": "001",
             |                "dueDate": "2017-06-09",
             |                "amount": 15.00
             |            }]
             |        }
             |    ]
             |}
             |""".stripMargin)

        val liabilityResponse = LiabilityResponse(Seq(
          Liability(
            taxPeriod = Some(
              TaxPeriod(
                from = "2017-04-01",
                to = "2017-04-30"
              )
            ),
            `type` = "VAT Return Debit Charge",
            originalAmount = 15.00,
            outstandingAmount = Some(15.00),
            due = Some("2017-06-09")
          )
        ))

        desJson.as[LiabilityResponse] shouldBe liabilityResponse
      }

      "multiple liabilities are returned, but 'Hybrid Payments' should be filtered out" in {

        val desJson = Json.parse(
          s"""
             |{
             |    "idType": "VRN",
             |    "idNumber": "XQIT00000000001",
             |    "regimeType": "VATC",
             |    "processingDate": "2017-03-07T09:30:00.000Z",
             |    "financialTransactions": [{
             |            "chargeType": "Hybrid Payments",
             |            "mainType": "2100",
             |            "periodKey": "13RL",
             |            "periodKeyDescription": "abcde",
             |            "taxPeriodFrom": "2017-01-01",
             |            "taxPeriodTo": "2017-04-05",
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
             |        },
             |        {
             |            "chargeType": "VAT Return Debit Charge",
             |            "mainType": "VAT Return Charge",
             |            "periodKey": "15AD",
             |            "periodKeyDescription": "April 2017",
             |            "taxPeriodFrom": "2017-04-01",
             |            "taxPeriodTo": "2017-04-30",
             |            "businessPartner": "0100062914",
             |            "contractAccountCategory": "42",
             |            "contractAccount": "000917000429",
             |            "contractObjectType": "ZVAT",
             |            "contractObject": "00000018000000000104",
             |            "sapDocumentNumber": "003390002284",
             |            "sapDocumentNumberItem": "0001",
             |            "chargeReference": "XQ002750002150",
             |            "mainTransaction": "4700",
             |            "subTransaction": "1174",
             |            "originalAmount": 15.00,
             |            "outstandingAmount": 15.00,
             |            "accruedInterest": 10000,
             |            "items": [{
             |                "subItem": "001",
             |                "dueDate": "2017-06-09",
             |                "amount": 15.00
             |            }]
             |        }
             |    ]
             |}
             |""".stripMargin)

        val liabilityResponse = LiabilityResponse(Seq(
          Liability(
            taxPeriod = Some(
              TaxPeriod(
                from = "2017-04-01",
                to = "2017-04-30"
              )
            ),
            `type` = "VAT Return Debit Charge",
            originalAmount = 15.00,
            outstandingAmount = Some(15.00),
            due = Some("2017-06-09")
          )
        ))

        desJson.as[LiabilityResponse] shouldBe liabilityResponse
      }

      "multiple liabilities are returned, but 'Payment on account' and 'Hybrid Payments' should be filtered out" in {

        val desJson = Json.parse(
          s"""
             |{
             |    "idType": "VRN",
             |    "idNumber": "XQIT00000000001",
             |    "regimeType": "VATC",
             |    "processingDate": "2017-03-07T09:30:00.000Z",
             |    "financialTransactions": [{
             |            "chargeType": "Hybrid Payments",
             |            "mainType": "2100",
             |            "periodKey": "13RL",
             |            "periodKeyDescription": "abcde",
             |            "taxPeriodFrom": "2017-01-01",
             |            "taxPeriodTo": "2017-04-05",
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
             |        },
             |        {
             |            "chargeType": "VAT Return Debit Charge",
             |            "mainType": "VAT Return Charge",
             |            "periodKey": "15AD",
             |            "periodKeyDescription": "April 2017",
             |            "taxPeriodFrom": "2017-04-01",
             |            "taxPeriodTo": "2017-04-30",
             |            "businessPartner": "0100062914",
             |            "contractAccountCategory": "42",
             |            "contractAccount": "000917000429",
             |            "contractObjectType": "ZVAT",
             |            "contractObject": "00000018000000000104",
             |            "sapDocumentNumber": "003390002284",
             |            "sapDocumentNumberItem": "0001",
             |            "chargeReference": "XQ002750002150",
             |            "mainTransaction": "4700",
             |            "subTransaction": "1174",
             |            "originalAmount": 15.00,
             |            "outstandingAmount": 15.00,
             |            "accruedInterest": 10000,
             |            "items": [{
             |                "subItem": "001",
             |                "dueDate": "2017-06-09",
             |                "amount": 15.00
             |            }]
             |        },
             |        {
             |            "chargeType": "Payment on account",
             |            "mainType": "2100",
             |            "periodKey": "13RL",
             |            "periodKeyDescription": "abcde",
             |            "taxPeriodFrom": "2017-01-01",
             |            "taxPeriodTo": "2017-04-05",
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

        val liabilityResponse = LiabilityResponse(Seq(
          Liability(
            taxPeriod = Some(
              TaxPeriod(
                from = "2017-04-01",
                to = "2017-04-30"
              )
            ),
            `type` = "VAT Return Debit Charge",
            originalAmount = 15.00,
            outstandingAmount = Some(15.00),
            due = Some("2017-06-09")
          )
        ))

        desJson.as[LiabilityResponse] shouldBe liabilityResponse
      }

      "filter out all transactions and return None if they only consist of un-allowed charge types" in {

        val desJson = Json.parse(
          s"""
             |{
             |    "idType": "VRN",
             |    "idNumber": "XQIT00000000001",
             |    "regimeType": "VATC",
             |    "processingDate": "2017-03-07T09:30:00.000Z",
             |    "financialTransactions": [{
             |            "chargeType": "Hybrid Payments",
             |            "mainType": "2100",
             |            "periodKey": "13RL",
             |            "periodKeyDescription": "abcde",
             |            "taxPeriodFrom": "2017-01-01",
             |            "taxPeriodTo": "2017-04-05",
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
             |        },
             |        {
             |            "chargeType": "payment on account",
             |            "mainType": "VAT Return Charge",
             |            "periodKey": "15AD",
             |            "periodKeyDescription": "April 2017",
             |            "taxPeriodFrom": "2017-04-01",
             |            "taxPeriodTo": "2017-04-30",
             |            "businessPartner": "0100062914",
             |            "contractAccountCategory": "42",
             |            "contractAccount": "000917000429",
             |            "contractObjectType": "ZVAT",
             |            "contractObject": "00000018000000000104",
             |            "sapDocumentNumber": "003390002284",
             |            "sapDocumentNumberItem": "0001",
             |            "chargeReference": "XQ002750002150",
             |            "mainTransaction": "4700",
             |            "subTransaction": "1174",
             |            "originalAmount": 15.00,
             |            "outstandingAmount": 15.00,
             |            "accruedInterest": 10000,
             |            "items": [{
             |                "subItem": "001",
             |                "dueDate": "2017-06-09",
             |                "amount": 15.00
             |            }]
             |        },
             |        {
             |            "chargeType": "Payment on account",
             |            "mainType": "2100",
             |            "periodKey": "13RL",
             |            "periodKeyDescription": "abcde",
             |            "taxPeriodFrom": "2017-01-01",
             |            "taxPeriodTo": "2017-04-05",
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

        desJson.asOpt[LiabilityResponse] shouldBe None
      }
    }

    "filter a result that fails the date check" in {

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

      desJson.asOpt[LiabilityResponse] shouldBe None
    }

    "use the writes format correctly" in {

      val liabilitiesJson = Json.parse(

        s"""
           |{
           |	"liabilities": [{
           |		"taxPeriod": {
           |			"from": "2017-01-01",
           |			"to": "2017-04-05"
           |		},
           |		"type": "VAT",
           |		"originalAmount": 463872,
           |		"outstandingAmount": 463872,
           |		"due": "2017-03-08"
           |	}]
           |}
           |""".stripMargin)

      val liabilityResponse = LiabilityResponse(Seq(
        Liability(
          taxPeriod = Some(
            TaxPeriod(
              from = "2017-01-01",
              to = "2017-04-05"
            )
          ),
          `type` = "VAT",
          originalAmount = 463872,
          outstandingAmount = Some(463872),
          due = Some("2017-03-08")
        )
      ))

      Json.toJson(liabilityResponse) shouldBe liabilitiesJson
    }
  }
}
