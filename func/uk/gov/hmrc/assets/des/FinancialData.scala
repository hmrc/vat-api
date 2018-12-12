package uk.gov.hmrc.assets.des

import play.api.libs.json.{JsValue, Json}

object FinancialData {

  val vatHybrid: JsValue = Json.parse(
    """
      |{
      |   "idType":"VRN",
      |   "idNumber":"888871052",
      |   "regimeType":"VATC",
      |   "processingDate":"2018-11-26T00:00:00.000Z",
      |   "financialTransactions":[
      |      {
      |         "chargeType":"Balance brought forward",
      |         "periodKey":"BFWD",
      |         "originalAmount":0
      |      },
      |      {
      |         "chargeType":"VAT RETURN DEBIT CHARGE",
      |         "originalAmount":1000,
      |         "periodKey":"0318",
      |         "taxPeriodFrom":"2017-01-01",
      |         "taxPeriodTo":"2017-03-31"
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
    """.stripMargin)

  val vatHybridNoPOA: JsValue = Json.parse(
    """
      |{
      |   "idType":"VRN",
      |   "idNumber":"888871052",
      |   "regimeType":"VATC",
      |   "processingDate":"2018-11-26T00:00:00.000Z",
      |   "financialTransactions":[
      |      {
      |         "chargeType":"Balance brought forward",
      |         "periodKey":"BFWD",
      |         "originalAmount":0
      |      },
      |      {
      |         "chargeType":"VAT RETURN DEBIT CHARGE",
      |         "originalAmount":1000,
      |         "periodKey":"0318",
      |         "taxPeriodFrom":"2017-01-01",
      |         "taxPeriodTo":"2017-03-31"
      |      }
      |   ]
      |}
    """.stripMargin)

  val oneLiability: JsValue = Json.parse("""{
      "idType": "MTDBSA",
      "idNumber": "XQIT00000000001",
      "regimeType": "ITSA",
      "processingDate": "2017-03-07T09:30:00.000Z",
      "financialTransactions": [{
        "chargeType": "VAT",
        "mainType": "2100",
        "periodKey": "13RL",
        "periodKeyDescription": "abcde",
        "taxPeriodFrom": "2017-01-01",
        "taxPeriodTo": "2017-03-31",
        "businessPartner": "6622334455",
        "contractAccountCategory": "02",
        "contractAccount": "D",
        "contractObjectType": "ABCD",
        "contractObject": "00000003000000002757",
        "sapDocumentNumber": "1040000872",
        "sapDocumentNumberItem": "XM00",
        "chargeReference": "XM002610011594",
        "mainTransaction": "1234",
        "subTransaction": "5678",
        "originalAmount": 463872,
        "outstandingAmount": 463872,
        "accruedInterest": 10000,
        "items": [{
          "subItem": "001",
          "dueDate": "2018-04-02",
          "amount": 463872
        }]
      }]
    }""".stripMargin)

  val minLiability: JsValue = Json.parse("""{
      "processingDate": "2017-03-07T09:30:00.000Z",
      "financialTransactions": [{
        "chargeType": "VAT",
        "originalAmount": 463872,
        "items": []
      }]
    }""".stripMargin)

  val multipleLiabilities: JsValue = Json.parse(
    """
      |{
      |    "idType": "MTDBSA",
      |    "idNumber": "XQIT00000000001",
      |    "regimeType": "ITSA",
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
      |        },
      |        {
      |            "chargeType": "VAT CA Charge",
      |            "mainType": "VAT Central Assessment",
      |            "periodKey": "15AA",
      |            "periodKeyDescription": "August 2017",
      |            "taxPeriodFrom": "2017-08-01",
      |            "taxPeriodTo": "2017-08-31",
      |            "businessPartner": "0100062914",
      |            "contractAccountCategory": "33",
      |            "contractAccount": "000917000429",
      |            "contractObjectType": "ZVAT",
      |            "contractObject": "00000018000000000104",
      |            "sapDocumentNumber": "003580002691",
      |            "sapDocumentNumberItem": "0001",
      |            "chargeReference": "XZ003100015596",
      |            "mainTransaction": "4720",
      |            "subTransaction": "1174",
      |            "originalAmount": 8493.38,
      |            "outstandingAmount": 7493.38,
      |            "items": [{
      |                "subItem": "000",
      |                "dueDate": "2017-10-07",
      |                "amount": 8493.38,
      |                "clearingDate": "2017-10-07",
      |                "clearingReason": "01",
      |                "outgoingPaymentMethod": "A",
      |                "paymentLock": "a",
      |                "clearingLock": "A",
      |                "interestLock": "C",
      |                "dunningLock": "1",
      |                "returnFlag": true,
      |                "paymentReference": "a",
      |                "paymentMethod": "A",
      |                "paymentLot": "081203010024",
      |                "paymentLotItem": "000001",
      |                "clearingSAPDocument": "3350000253",
      |                "statisticalDocument": "A"
      |            }]
      |        }
      |    ]
      |}"""".stripMargin)

  val multipleLiabilitiesWithPaymentOnAccount: JsValue = Json.parse(
    """
      |{
      |    "idType": "MTDBSA",
      |    "idNumber": "XQIT00000000001",
      |    "regimeType": "ITSA",
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
      |            "chargeType": "Payment on account",
      |            "mainType": "VAT Return Charge",
      |            "periodKey": "15AD",
      |            "periodKeyDescription": "April 2017",
      |            "taxPeriodFrom": "2017-04-02",
      |            "taxPeriodTo": "2017-05-01",
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
      |            "originalAmount": 10.00,
      |            "outstandingAmount": 10.00,
      |            "accruedInterest": 10000,
      |            "items": [{
      |                "subItem": "001",
      |                "dueDate": "2017-06-09",
      |                "amount": 10.00
      |            }]
      |        },
      |        {
      |            "chargeType": "VAT CA Charge",
      |            "mainType": "VAT Central Assessment",
      |            "periodKey": "15AA",
      |            "periodKeyDescription": "August 2017",
      |            "taxPeriodFrom": "2017-08-01",
      |            "taxPeriodTo": "2017-08-31",
      |            "businessPartner": "0100062914",
      |            "contractAccountCategory": "33",
      |            "contractAccount": "000917000429",
      |            "contractObjectType": "ZVAT",
      |            "contractObject": "00000018000000000104",
      |            "sapDocumentNumber": "003580002691",
      |            "sapDocumentNumberItem": "0001",
      |            "chargeReference": "XZ003100015596",
      |            "mainTransaction": "4720",
      |            "subTransaction": "1174",
      |            "originalAmount": 8493.38,
      |            "outstandingAmount": 7493.38,
      |            "items": [{
      |                "subItem": "000",
      |                "dueDate": "2017-10-07",
      |                "amount": 8493.38,
      |                "clearingDate": "2017-10-07",
      |                "clearingReason": "01",
      |                "outgoingPaymentMethod": "A",
      |                "paymentLock": "a",
      |                "clearingLock": "A",
      |                "interestLock": "C",
      |                "dunningLock": "1",
      |                "returnFlag": true,
      |                "paymentReference": "a",
      |                "paymentMethod": "A",
      |                "paymentLot": "081203010024",
      |                "paymentLotItem": "000001",
      |                "clearingSAPDocument": "3350000253",
      |                "statisticalDocument": "A"
      |            }]
      |        }
      |    ]
      |}"""".stripMargin)

  val liabilitiesOverlapping: JsValue = Json.parse("""{
      "idType": "MTDBSA",
      "idNumber": "XQIT00000000001",
      "regimeType": "ITSA",
      "processingDate": "2017-03-07T09:30:00.000Z",
      "financialTransactions": [
      {
        "chargeType": "VAT",
        "mainType": "2100",
        "periodKey": "13RL",
        "periodKeyDescription": "abcde",
        "taxPeriodFrom": "2017-01-01",
        "taxPeriodTo": "2017-03-31",
        "businessPartner": "6622334455",
        "contractAccountCategory": "02",
        "contractAccount": "D",
        "contractObjectType": "ABCD",
        "contractObject": "00000003000000002757",
        "sapDocumentNumber": "1040000872",
        "sapDocumentNumberItem": "XM00",
        "chargeReference": "XM002610011594",
        "mainTransaction": "1234",
        "subTransaction": "5678",
        "originalAmount": 463872,
        "outstandingAmount": 463872,
        "accruedInterest": 10000,
        "items": [{
          "subItem": "001",
          "dueDate": "2018-04-02",
          "amount": 463872
        }]
      },{
         "chargeType": "VAT",
         "mainType": "2100",
         "periodKey": "13RL",
         "periodKeyDescription": "abcde",
         "taxPeriodFrom": "2017-6-01",
         "taxPeriodTo": "2018-07-31",
         "businessPartner": "6622334455",
         "contractAccountCategory": "02",
         "contractAccount": "D",
         "contractObjectType": "ABCD",
         "contractObject": "00000003000000002757",
         "sapDocumentNumber": "1040000872",
         "sapDocumentNumberItem": "XM00",
         "chargeReference": "XM002610011594",
         "mainTransaction": "1234",
         "subTransaction": "5678",
         "originalAmount": 463872,
         "outstandingAmount": 463872,
         "accruedInterest": 10000,
         "items": [{
           "subItem": "001",
           "dueDate": "2018-04-02",
           "amount": 463872
         }
        ]
       }
      ]
    }""".stripMargin)

  val emptyLiabilities: JsValue = Json.parse(
    """
      |{
      |    "idType": "VRN",
      |    "idNumber": "100062914",
      |    "regimeType": "VATC",
      |    "processingDate": "2017-05-13T09:30:00.000Z",
      |    "financialTransactions": [
      |    ]
      | }
    """.stripMargin)

  val onePayment: JsValue = Json.parse(
    """
      |{
      |    "idType": "VRN",
      |    "idNumber": "100062914",
      |    "regimeType": "VATC",
      |    "processingDate": "2017-05-13T09:30:00.000Z",
      |    "financialTransactions": [
      |      {
      |         "chargeType": "VAT Return Debit Charge",
      |         "mainType": "VAT Return Charge",
      |         "periodKey": "15AD",
      |         "periodKeyDescription": "January 2018",
      |         "taxPeriodFrom": "2017-01-01",
      |         "taxPeriodTo": "2017-01-30",
      |         "businessPartner": "0100062914",
      |         "contractAccountCategory": "33",
      |         "contractAccount": "000917000429",
      |         "contractObjectType": "ZVAT",
      |         "contractObject": "00000018000000000104",
      |         "sapDocumentNumber": "003390002284",
      |         "sapDocumentNumberItem": "0001",
      |         "chargeReference": "XQ002750002150",
      |         "mainTransaction": "4700",
      |         "subTransaction": "1174",
      |         "originalAmount": 1534.65,
      |         "outstandingAmount": 0,
      |         "clearedAmount": 1534.65,
      |         "accruedInterest": 0,
      |         "items": [
      |            {
      |               "subItem": "000",
      |               "dueDate": "2017-01-12",
      |               "amount": 1000.65,
      |               "clearingDate": "2017-02-12",
      |               "clearingReason": "01",
      |               "outgoingPaymentMethod": "A",
      |               "paymentLock": "a",
      |               "clearingLock": "A",
      |               "interestLock": "C",
      |               "dunningLock": "1",
      |               "returnFlag": true,
      |               "paymentReference": "a",
      |               "paymentAmount": 1534.65,
      |               "paymentMethod": "A",
      |               "paymentLot": "081203010024",
      |               "paymentLotItem": "000001",
      |               "clearingSAPDocument": "3350000253",
      |               "statisticalDocument": "A"
      |            }
      |         ]
      |      }
      |   ]
      |}
      |
      """.stripMargin)

  val overlappingPayment: JsValue = Json.parse(
    """
      |{
      |    "idType": "VRN",
      |    "idNumber": "100062914",
      |    "regimeType": "VATC",
      |    "processingDate": "2017-05-13T09:30:00.000Z",
      |    "financialTransactions": [
      |      {
      |         "chargeType": "VAT Return Debit Charge",
      |         "mainType": "VAT Return Charge",
      |         "periodKey": "15AD",
      |         "periodKeyDescription": "January 2018",
      |         "taxPeriodFrom": "2017-01-01",
      |         "taxPeriodTo": "2017-01-30",
      |         "businessPartner": "0100062914",
      |         "contractAccountCategory": "33",
      |         "contractAccount": "000917000429",
      |         "contractObjectType": "ZVAT",
      |         "contractObject": "00000018000000000104",
      |         "sapDocumentNumber": "003390002284",
      |         "sapDocumentNumberItem": "0001",
      |         "chargeReference": "XQ002750002150",
      |         "mainTransaction": "4700",
      |         "subTransaction": "1174",
      |         "originalAmount": 1534.65,
      |         "outstandingAmount": 0,
      |         "clearedAmount": 1534.65,
      |         "accruedInterest": 0,
      |         "items": [
      |            {
      |               "subItem": "000",
      |               "dueDate": "2017-01-12",
      |               "amount": 1000.65,
      |               "clearingDate": "2017-02-12",
      |               "clearingReason": "01",
      |               "outgoingPaymentMethod": "A",
      |               "paymentLock": "a",
      |               "clearingLock": "A",
      |               "interestLock": "C",
      |               "dunningLock": "1",
      |               "returnFlag": true,
      |               "paymentReference": "a",
      |               "paymentAmount": 1534.65,
      |               "paymentMethod": "A",
      |               "paymentLot": "081203010024",
      |               "paymentLotItem": "000001",
      |               "clearingSAPDocument": "3350000253",
      |               "statisticalDocument": "A"
      |            }
      |         ]
      |      },
      |       {
      |         "chargeType": "VAT Return Debit Charge",
      |         "mainType": "VAT Return Charge",
      |         "periodKey": "15AD",
      |         "periodKeyDescription": "January 2018",
      |         "taxPeriodFrom": "2017-05-01",
      |         "taxPeriodTo": "2017-08-30",
      |         "businessPartner": "0100062914",
      |         "contractAccountCategory": "33",
      |         "contractAccount": "000917000429",
      |         "contractObjectType": "ZVAT",
      |         "contractObject": "00000018000000000104",
      |         "sapDocumentNumber": "003390002284",
      |         "sapDocumentNumberItem": "0001",
      |         "chargeReference": "XQ002750002150",
      |         "mainTransaction": "4700",
      |         "subTransaction": "1174",
      |         "originalAmount": 1534.65,
      |         "outstandingAmount": 0,
      |         "clearedAmount": 1534.65,
      |         "accruedInterest": 0,
      |         "items": [
      |            {
      |               "subItem": "000",
      |               "dueDate": "2017-01-12",
      |               "amount": 1000.65,
      |               "clearingDate": "2017-02-12",
      |               "clearingReason": "01",
      |               "outgoingPaymentMethod": "A",
      |               "paymentLock": "a",
      |               "clearingLock": "A",
      |               "interestLock": "C",
      |               "dunningLock": "1",
      |               "returnFlag": true,
      |               "paymentReference": "a",
      |               "paymentAmount": 1534.65,
      |               "paymentMethod": "A",
      |               "paymentLot": "081203010024",
      |               "paymentLotItem": "000001",
      |               "clearingSAPDocument": "3350000253",
      |               "statisticalDocument": "A"
      |            }
      |         ]
      |      }
      |   ]
      |}
      |
      """.stripMargin)

  val minPayment: JsValue = Json.parse("""{
      "processingDate": "2017-03-07T09:30:00.000Z",
      "financialTransactions": [{
        "chargeType": "VAT",
        "originalAmount": 463872,
        "items": [
          {
            "paymentAmount": 123456
          }
        ]
      }]
    }""".stripMargin)

  val multiplePayments: JsValue = Json.parse(
    """
      |{
      |    "idType": "VRN",
      |    "idNumber": "100062914",
      |    "regimeType": "VATC",
      |    "processingDate": "2017-05-13T09:30:00.000Z",
      |    "financialTransactions": [
      |      {
      |         "chargeType": "VAT Return Debit Charge",
      |         "mainType": "VAT Return Charge",
      |         "periodKey": "15AD",
      |         "periodKeyDescription": "February 2018",
      |         "taxPeriodFrom": "2017-02-01",
      |         "taxPeriodTo": "2017-02-28",
      |         "businessPartner": "0100062914",
      |         "contractAccountCategory": "33",
      |         "contractAccount": "000917000429",
      |         "contractObjectType": "ZVAT",
      |         "contractObject": "00000018000000000104",
      |         "sapDocumentNumber": "003390002284",
      |         "sapDocumentNumberItem": "0001",
      |         "chargeReference": "XQ002750002150",
      |         "mainTransaction": "4700",
      |         "subTransaction": "1174",
      |         "originalAmount": 15.65,
      |         "outstandingAmount": 10.65,
      |         "clearedAmount": 5.0,
      |         "accruedInterest": 0,
      |         "items": [
      |            {
      |               "subItem": "000",
      |               "dueDate": "2017-02-11",
      |               "amount": 15.0,
      |               "clearingDate": "2017-02-11",
      |               "clearingReason": "01",
      |               "outgoingPaymentMethod": "A",
      |               "paymentLock": "a",
      |               "clearingLock": "A",
      |               "interestLock": "C",
      |               "dunningLock": "1",
      |               "returnFlag": true,
      |               "paymentReference": "a",
      |               "paymentAmount": 5.0,
      |               "paymentMethod": "A",
      |               "paymentLot": "081203010024",
      |               "paymentLotItem": "000001",
      |               "clearingSAPDocument": "3350000212",
      |               "statisticalDocument": "A"
      |            }
      |         ]
      |      },
      |      {
      |         "chargeType": "VAT Return Debit Charge",
      |         "mainType": "VAT Return Charge",
      |         "periodKey": "15AC",
      |         "periodKeyDescription": "March 2018",
      |         "taxPeriodFrom": "2017-03-01",
      |         "taxPeriodTo": "2017-03-25",
      |         "businessPartner": "0100062914",
      |         "contractAccountCategory": "33",
      |         "contractAccount": "000917000429",
      |         "contractObjectType": "ZVAT",
      |         "contractObject": "00000018000000000105",
      |         "sapDocumentNumber": "003390002284",
      |         "sapDocumentNumberItem": "0001",
      |         "chargeReference": "XQ002750002151",
      |         "mainTransaction": "4700",
      |         "subTransaction": "1174",
      |         "originalAmount": 1050.00,
      |         "outstandingAmount": 0,
      |         "clearedAmount": 1050.00,
      |         "accruedInterest": 0,
      |         "items": [
      |            {
      |               "subItem": "000",
      |               "dueDate": "2017-03-01",
      |               "amount": 40.00,
      |               "clearingDate": "2017-03-11",
      |               "clearingReason": "01",
      |               "outgoingPaymentMethod": "A",
      |               "paymentLock": "a",
      |               "clearingLock": "A",
      |               "interestLock": "C",
      |               "dunningLock": "1",
      |               "returnFlag": true,
      |               "paymentReference": "a",
      |               "paymentAmount": 50.00,
      |               "paymentMethod": "A",
      |               "paymentLot": "081203010024",
      |               "paymentLotItem": "000001",
      |               "clearingSAPDocument": "3350000241",
      |               "statisticalDocument": "A"
      |            },
      |            {
      |               "subItem": "001",
      |               "dueDate": "2017-03-02",
      |               "amount": 1001.00,
      |               "clearingDate": "2017-03-12",
      |               "clearingReason": "01",
      |               "outgoingPaymentMethod": "A",
      |               "paymentLock": "a",
      |               "clearingLock": "A",
      |               "interestLock": "C",
      |               "dunningLock": "1",
      |               "returnFlag": true,
      |               "paymentReference": "a",
      |               "paymentAmount": 1000.00,
      |               "paymentMethod": "A",
      |               "paymentLot": "081203010024",
      |               "paymentLotItem": "000001",
      |               "clearingSAPDocument": "3350000242",
      |               "statisticalDocument": "A"
      |            }
      |         ]
      |      },
      |      {
      |         "chargeType": "VAT Return Debit Charge",
      |         "mainType": "VAT Return Charge",
      |         "periodKey": "15AD",
      |         "periodKeyDescription": "August 2017",
      |         "taxPeriodFrom": "2017-08-01",
      |         "taxPeriodTo": "2017-12-20",
      |         "businessPartner": "0100062914",
      |         "contractAccountCategory": "33",
      |         "contractAccount": "000917000429",
      |         "contractObjectType": "ZVAT",
      |         "contractObject": "00000018000000000106",
      |         "sapDocumentNumber": "003390002284",
      |         "sapDocumentNumberItem": "0001",
      |         "chargeReference": "XQ002750002152",
      |         "mainTransaction": "4700",
      |         "subTransaction": "1174",
      |         "originalAmount": 4000.0,
      |         "accruedInterest": 10.0,
      |         "items": [
      |            {
      |               "subItem": "000",
      |               "dueDate": "2017-08-05",
      |               "amount": 322.00,
      |               "clearingReason": "01",
      |               "clearingDate": "2017-08-05",
      |               "outgoingPaymentMethod": "A",
      |               "paymentLock": "a",
      |               "clearingLock": "A",
      |               "interestLock": "C",
      |               "dunningLock": "1",
      |               "returnFlag": true,
      |               "paymentReference": "a",
      |               "paymentAmount": 321.00,
      |               "paymentMethod": "A",
      |               "paymentLot": "081203010024",
      |               "paymentLotItem": "000001",
      |               "clearingSAPDocument": "3350000254",
      |               "statisticalDocument": "A"
      |            },
      |            {
      |               "subItem": "001",
      |               "dueDate": "2017-04-02",
      |               "amount": 90.00,
      |               "clearingReason": "01",
      |               "outgoingPaymentMethod": "A",
      |               "paymentLock": "a",
      |               "clearingLock": "A",
      |               "interestLock": "C",
      |               "dunningLock": "1",
      |               "returnFlag": true,
      |               "paymentReference": "a",
      |               "paymentAmount": 91.00,
      |               "paymentMethod": "A",
      |               "paymentLot": "081203010024",
      |               "paymentLotItem": "000002",
      |               "clearingSAPDocument": "3350000255",
      |               "statisticalDocument": "A"
      |            },
      |            {
      |               "subItem": "002",
      |               "dueDate": "2017-09-01",
      |               "amount": 6.00,
      |               "clearingDate": "2017-09-12",
      |               "clearingReason": "01",
      |               "outgoingPaymentMethod": "A",
      |               "paymentLock": "a",
      |               "clearingLock": "A",
      |               "interestLock": "C",
      |               "dunningLock": "1",
      |               "returnFlag": true,
      |               "paymentReference": "a",
      |               "paymentAmount": 5.00,
      |               "paymentMethod": "A",
      |               "paymentLot": "081203010024",
      |               "paymentLotItem": "000003",
      |               "clearingSAPDocument": "3350000256",
      |               "statisticalDocument": "A"
      |            }
      |         ]
      |      },
      |       {
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
    """.stripMargin)
}


