package uk.gov.hmrc.assets.des

import play.api.libs.json.Json

object VatReturns {

  val submissionSuccessBody = s"""
                       |{
                       |    "processingDate": "2018-03-01T11:43:43.195Z",
                       |    "paymentIndicator": "BANK",
                       |    "formBundleNumber": "891713832155"
                       |}
                            """.stripMargin


  val successBodyWithoutPaymentIndicator = s"""
                       |{
                       |    "processingDate": "2018-03-01T11:43:43Z",
                       |    "formBundleNumber": "891713832155"
                       |}
                            """.stripMargin

  val retrieveVatReturnsDesSuccessBody = Json.parse(
    """
      |{
      |                      "periodKey": "0001",
      |                      "inboundCorrespondenceFromDate": "2017-01-01",
      |                      "inboundCorrespondenceToDate": "2017-12-31",
      |                      "vatDueSales": 100.25,
      |                      "vatDueAcquisitions": 100.25,
      |                      "vatDueTotal": 200.50,
      |                      "vatReclaimedCurrPeriod": 100.25,
      |                      "vatDueNet": 100.25,
      |                      "totalValueSalesExVAT": 100,
      |                      "totalValuePurchasesExVAT": 100,
      |                      "totalValueGoodsSuppliedExVAT": 100,
      |                      "totalAllAcquisitionsExVAT": 100,
      |                      "receivedAt": "2017-12-18T16:49:20.678Z"
      |                    }
      |""".stripMargin)

  val retrieveVatReturnsDesResponseWithNoReceivedAt = Json.parse(
    """
      |{
      |                      "periodKey": "0001",
      |                      "inboundCorrespondenceFromDate": "2017-01-01",
      |                      "inboundCorrespondenceToDate": "2017-12-31",
      |                      "vatDueSales": 100.25,
      |                      "vatDueAcquisitions": 100.25,
      |                      "vatDueTotal": 200.50,
      |                      "vatReclaimedCurrPeriod": 100.25,
      |                      "vatDueNet": 100.25,
      |                      "totalValueSalesExVAT": 100,
      |                      "totalValuePurchasesExVAT": 100,
      |                      "totalValueGoodsSuppliedExVAT": 100,
      |                      "totalAllAcquisitionsExVAT": 100,
      |                      "receivedAt": "2017-12-18T16:49:20.678Z"
      |                    }
      |""".stripMargin)

  val retrieveVatReturnsMtdSuccessBody = Json.parse(
    """
      |{
      |                      "periodKey": "0001",
      |                      "vatDueSales": 100.25,
      |                      "vatDueAcquisitions": 100.25,
      |                      "totalVatDue": 200.50,
      |                      "vatReclaimedCurrPeriod": 100.25,
      |                      "netVatDue": 100.25,
      |                      "totalValueSalesExVAT": 100,
      |                      "totalValuePurchasesExVAT": 100,
      |                      "totalValueGoodsSuppliedExVAT": 100,
      |                      "totalAcquisitionsExVAT": 100
      |                    }
      |""".stripMargin)
}
