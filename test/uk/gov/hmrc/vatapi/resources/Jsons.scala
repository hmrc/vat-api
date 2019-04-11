/*
 * Copyright 2019 HM Revenue & Customs
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

package uk.gov.hmrc.vatapi.resources

import org.joda.time.LocalDate
import play.api.libs.json.{JsValue, Json}
import sun.util.resources.LocaleData
import uk.gov.hmrc.domain.Vrn
import uk.gov.hmrc.vatapi.models.{Liabilities, Liability, Payment, Payments, TaxPeriod}

object Jsons {

  object Errors {

    private def error(error: (String, String)) = {
      s"""
         |    {
         |      "code": "${error._1}",
         |      "path": "${error._2}"
         |    }
         """.stripMargin
    }

    private def errorWithMessage(code: String, message: String) =
      s"""
         |{
         |  "code": "$code",
         |  "message": "$message"
         |}
       """.stripMargin

    val invalidVrn: String =
      errorWithMessage("INVALID_NINO", "Submission has not passed validation. Invalid parameter NINO.")
    val ninoInvalid: String = errorWithMessage("NINO_INVALID", "The provided Nino is invalid")
    val invalidPayload: String =
      errorWithMessage("INVALID_PAYLOAD", "Submission has not passed validation. Invalid PAYLOAD.")
    val invalidRequest: String = errorWithMessage("INVALID_REQUEST", "Invalid request")
    val ninoNotFound: String =
      errorWithMessage("NOT_FOUND_NINO", "The remote endpoint has indicated that no data can be found.")
    val desNotFound: String =
      errorWithMessage("NOT_FOUND", "The remote endpoint has indicated that no data can be found.")
    val duplicateTradingName: String = errorWithMessage("CONFLICT", "Duplicated trading name.")
    val notFound: String = errorWithMessage("NOT_FOUND", "Resource was not found")
    val invalidPeriod: String = businessErrorWithMessage(
      "INVALID_PERIOD" -> "Periods should be contiguous and have no gaps between one another")
    val overlappingPeriod: String = businessErrorWithMessage(
      "OVERLAPPING_PERIOD" -> "Period overlaps with existing periods.")
    val nonContiguousPeriod: String = businessErrorWithMessage(
      "NOT_CONTIGUOUS_PERIOD" -> "Periods should be contiguous.")
    val misalignedPeriod: String = businessErrorWithMessage(
      "MISALIGNED_PERIOD" -> "Period is not within the accounting period.")
    val misalignedAndOverlappingPeriod: String = businessErrorWithMessage(
      "MISALIGNED_PERIOD" -> "Period is not within the accounting period.",
      "OVERLAPPING_PERIOD" -> "Period overlaps with existing periods.")
    val invalidOriginatorId: String =
      errorWithMessage("INVALID_ORIGINATOR_ID", "Submission has not passed validation. Invalid header Originator-Id.")
    val internalServerError: String = errorWithMessage("INTERNAL_SERVER_ERROR", "An internal server error occurred")
    val invalidCalcId: String = errorWithMessage("INVALID_CALCID", "Submission has not passed validation")
    val unauthorised: String = errorWithMessage("UNAUTHORIZED", "Bearer token is missing or not authorized")
    val clientNotSubscribed: String = errorWithMessage("CLIENT_NOT_SUBSCRIBED", "The client is not subscribed to MTD")
    val agentNotAuthorised: String = errorWithMessage("AGENT_NOT_AUTHORIZED", "The agent is not authorized")
    val agentNotSubscribed: String =
      errorWithMessage("AGENT_NOT_SUBSCRIBED", "The agent is not subscribed to agent services")

    def invalidRequest(errors: (String, String)*): String = {
      s"""
         |{
         |  "code": "INVALID_REQUEST",
         |  "message": "Invalid request",
         |  "errors": [
         |    ${errors.map { error }.mkString(",")}
         |  ]
         |}
         """.stripMargin
    }

    def businessError(errors: (String, String)*): String = {
      s"""
         |{
         |  "code": "BUSINESS_ERROR",
         |  "message": "Business validation error",
         |  "errors": [
         |    ${errors.map { error }.mkString(",")}
         |  ]
         |}
         """.stripMargin
    }

    def businessErrorWithMessage(errors: (String, String)*): String = {
      s"""
         |{
         |  "code": "BUSINESS_ERROR",
         |  "message": "Business validation error",
         |  "errors": [
         |    ${errors
        .map {
          case (code, msg) => errorWithMessage(code, msg)
        }
        .mkString(",")}
         |  ]
         |}
         """.stripMargin
    }
  }

  object Obligations {
    def apply(firstMet: String = "F",
              secondMet: String = "O",
              thirdMet: String = "O",
              fourthMet: String = "O"): JsValue = {
      Json.parse(s"""
                    |{
                    |  "obligations": [
                    |    {
                    |      "start": "2017-04-06",
                    |      "end": "2017-07-05",
                    |      "due": "2017-08-05",
                    |      "status": "$firstMet",
                    |      "received": "2017-08-01",
                    |      "periodKey": "#001"
                    |    },
                    |    {
                    |      "start": "2017-07-06",
                    |      "end": "2017-10-05",
                    |      "due": "2017-11-05",
                    |      "status": "$secondMet",
                    |      "periodKey": "#002"
                    |    },
                    |    {
                    |      "start": "2017-10-06",
                    |      "end": "2018-01-05",
                    |      "due": "2018-02-05",
                    |      "status": "$thirdMet",
                    |      "periodKey": "#003"
                    |    },
                    |    {
                    |      "start": "2018-01-06",
                    |      "end": "2018-04-05",
                    |      "due": "2018-05-06",
                    |      "status": "$fourthMet",
                    |      "periodKey": "#004"
                    |    }
                    |  ]
                    |}
         """.stripMargin)
    }

    def desResponse(vrn: Vrn): JsValue = Json.parse(
        s"""
           |{
           |  "obligations": [
           |  {
           |    "identification": {
           |        "incomeSourceType": "A",
           |        "referenceNumber": "$vrn",
           |        "referenceType": "VRN"
           |    },
           |    "obligationDetails": [
           |    {
           |      "status": "F",
           |      "inboundCorrespondenceFromDate": "2017-04-06",
           |      "inboundCorrespondenceToDate": "2017-07-05",
           |      "inboundCorrespondenceDateReceived": "2017-08-01",
           |      "inboundCorrespondenceDueDate": "2017-08-05",
           |      "periodKey": "#001"
           |    },
           |    {
           |      "status": "O",
           |      "inboundCorrespondenceFromDate": "2017-07-06",
           |      "inboundCorrespondenceToDate": "2017-10-05",
           |      "inboundCorrespondenceDueDate": "2017-11-05",
           |      "periodKey": "#002"
           |    },
           |    {
           |      "status": "O",
           |      "inboundCorrespondenceFromDate": "2017-10-06",
           |      "inboundCorrespondenceToDate": "2018-01-05",
           |      "inboundCorrespondenceDueDate": "2018-02-05",
           |      "periodKey": "#003"
           |    },
           |    {
           |      "status": "O",
           |      "inboundCorrespondenceFromDate": "2018-01-06",
           |      "inboundCorrespondenceToDate": "2018-04-05",
           |      "inboundCorrespondenceDueDate": "2018-05-06",
           |      "periodKey": "#004"
           |    }
           |    ]
           |  }
           |  ]
           |}
    """.stripMargin)

    def desResponseWithoutObligationDetails(vrn: Vrn): JsValue = Json.parse(
        s"""
           |{
           |  "obligations": [
           |    {
           |      "identification": {
           |          "incomeSourceType": "A",
           |          "referenceNumber": "$vrn",
           |          "referenceType": "VRN"
           |      },
           |      "obligationDetails": []
           |    }
           |  ]
           |}
    """.stripMargin)
  }

  object FinancialData {


    lazy val vatHybrid: JsValue = Json.toJson(
      Liabilities(
        Seq(
          Liability(
            taxPeriod = None,
            `type` = "Balance brought forward",
            originalAmount = 0.0,
            outstandingAmount = None,
            due = None
          ),
          Liability(
            taxPeriod = Some(TaxPeriod(from = LocalDate.parse("2017-01-01"), to  = LocalDate.parse("2017-03-31"))),
            `type` = "VAT RETURN DEBIT CHARGE",
            originalAmount = 1000,
            outstandingAmount = None,
            due = None
          )
        )
      )
    )

    lazy val oneLiability: JsValue =
      Json.toJson(
        Liabilities(
          Seq(Liability(
            Some(TaxPeriod(from = LocalDate.parse("2017-01-01"), to = LocalDate.parse("2017-03-31"))),
            `type` = "VAT",
            originalAmount = 463872,
            outstandingAmount = Some(463872),
            due = Some(LocalDate.parse("2018-04-02"))
          ))
        )
      )
    lazy val minLiability: JsValue =
      Json.toJson(
        Liabilities(
          Seq(Liability(
            `type` = "VAT",
            originalAmount = 463872
          ))
        )
      )
    lazy val multipleLiabilities: JsValue =
      Json.toJson(
        Liabilities(
          Seq(
            Liability(
              Some(TaxPeriod(from = LocalDate.parse("2017-01-01"), to = LocalDate.parse("2017-04-05"))),
              `type` = "VAT",
              originalAmount = 463872,
              outstandingAmount = Some(463872),
              due = Some(LocalDate.parse("2017-03-08"))
            ),
            Liability(
              Some(TaxPeriod(from = LocalDate.parse("2017-04-01"), to = LocalDate.parse("2017-04-30"))),
              `type` = "VAT Return Debit Charge",
              originalAmount = 15.00,
              outstandingAmount = Some(15.00),
              due = Some(LocalDate.parse("2017-06-09"))
            ),
            Liability(
              Some(TaxPeriod(from = LocalDate.parse("2017-08-01"), to = LocalDate.parse("2017-08-31"))),
              `type` = "VAT CA Charge",
              originalAmount = 8493.38,
              outstandingAmount = Some(7493.38),
              due = Some(LocalDate.parse("2017-10-07"))
            )
          )
        )
      )
    lazy val multipleLiabilitiesWithoutNoHybrids: JsValue =
      Json.toJson(
        Liabilities(
          Seq(
            Liability(
              Some(TaxPeriod(from = LocalDate.parse("2017-01-01"), to = LocalDate.parse("2017-04-05"))),
              `type` = "VAT",
              originalAmount = 463872,
              outstandingAmount = Some(463872),
              due = Some(LocalDate.parse("2017-03-08"))
            ),
            Liability(
              Some(TaxPeriod(from = LocalDate.parse("2017-08-01"), to = LocalDate.parse("2017-08-31"))),
              `type` = "VAT CA Charge",
              originalAmount = 8493.38,
              outstandingAmount = Some(7493.38),
              due = Some(LocalDate.parse("2017-10-07"))
            )
          )
        )
      )


    lazy val vatHybridPayment: JsValue = Json.toJson(
      Payments(Seq(
        Payment(
          amount = -10,
          received = Some(LocalDate.parse("2017-11-27"))
        )
      ))
    )

    lazy val onePayment: JsValue = Json.toJson(
      Payments(Seq(
        Payment(
          amount = 1534.65,
          received = Some(LocalDate.parse("2017-02-12"))
        )
      ))
    )
    lazy val minPayment: JsValue = Json.toJson(
      Payments(Seq(
        Payment(
          amount = 123456
        )
      ))
    )
    lazy val multiplePayments: JsValue = Json.toJson(
      Payments(Seq(
        Payment(
          amount = 5,
          received = Some(LocalDate.parse("2017-02-11"))
        ),
        Payment(
          amount = 50.00,
          received = Some(LocalDate.parse("2017-03-11"))
        ),
        Payment(
          amount = 1000,
          received = Some(LocalDate.parse("2017-03-12"))
        ),
        Payment(
          amount = 321.00,
          received = Some(LocalDate.parse("2017-08-05"))
        ),
        Payment(
          amount = 91.00
        ),
        Payment(
          amount = 5.00,
          received = Some(LocalDate.parse("2017-09-12"))
        )
      ))
    )

    val singlePaymentDesResponse: JsValue = Json.parse(
      """
        |{
        |	"idType": "VRN",
        |	"idNumber": "100062914",
        |	"regimeType": "VATC",
        |	"processingDate": "2017-05-13T09:30:00.000Z",
        |	"financialTransactions": [{
        |			"chargeType": "VAT Return Debit Charge",
        |			"mainType": "VAT Return Charge",
        |			"periodKey": "15AD",
        |			"periodKeyDescription": "January 2018",
        |			"taxPeriodFrom": "2017-01-01",
        |			"taxPeriodTo": "2017-02-01",
        |			"businessPartner": "0100062914",
        |			"contractAccountCategory": "33",
        |			"contractAccount": "000917000429",
        |			"contractObjectType": "ZVAT",
        |			"contractObject": "00000018000000000104",
        |			"sapDocumentNumber": "003390002284",
        |			"sapDocumentNumberItem": "0001",
        |			"chargeReference": "XQ002750002150",
        |			"mainTransaction": "4700",
        |			"subTransaction": "1174",
        |			"originalAmount": 1534.65,
        |			"outstandingAmount": 0,
        |			"clearedAmount": 1534.65,
        |			"accruedInterest": 0,
        |			"items": [{
        |				"subItem": "000",
        |				"dueDate": "2017-01-12",
        |				"amount": 1000.65,
        |				"clearingDate": "2017-02-12",
        |				"clearingReason": "01",
        |				"outgoingPaymentMethod": "A",
        |				"paymentLock": "a",
        |				"clearingLock": "A",
        |				"interestLock": "C",
        |				"dunningLock": "1",
        |				"returnFlag": true,
        |				"paymentReference": "a",
        |				"paymentAmount": 1534.65,
        |				"paymentMethod": "A",
        |				"paymentLot": "081203010024",
        |				"paymentLotItem": "000001",
        |				"clearingSAPDocument": "3350000253",
        |				"statisticalDocument": "A"
        |			}]
        |		}
        |	]
        |}
      """.stripMargin)

    val missingPaymentDesResponse: JsValue = Json.parse(
      """
        |{
        |	"idType": "VRN",
        |	"idNumber": "100062914",
        |	"regimeType": "VATC",
        |	"processingDate": "2017-05-13T09:30:00.000Z",
        |	"financialTransactions": []
        |}
      """.stripMargin)

    val singleLiabilityDesResponse: JsValue = Json.parse(
      """
        |{
        |	"idType": "VRN",
        |	"idNumber": "XQIT00000000001",
        |	"regimeType": "VATC",
        |	"processingDate": "2017-03-07T09:30:00.000Z",
        |	"financialTransactions": [{
        |			"chargeType": "VAT",
        |			"mainType": "2100",
        |			"periodKey": "13RL",
        |			"periodKeyDescription": "abcde",
        |			"taxPeriodFrom": "2017-01-01",
        |			"taxPeriodTo": "2017-03-31",
        |			"businessPartner": "6622334455",
        |			"contractAccountCategory": "02",
        |			"contractAccount": "D",
        |			"contractObjectType": "ABCD",
        |			"contractObject": "00000003000000002757",
        |			"sapDocumentNumber": "1040000872",
        |			"sapDocumentNumberItem": "XM00",
        |			"chargeReference": "XM002610011594",
        |			"mainTransaction": "1234",
        |			"subTransaction": "5678",
        |			"originalAmount": 463872,
        |			"outstandingAmount": 463872,
        |			"accruedInterest": 10000,
        |			"items": [{
        |				"subItem": "001",
        |				"dueDate": "2018-04-02",
        |				"amount": 463872
        |			}]
        |		}
        |	]
        |}
        |
    """.stripMargin)

    val missingLiabilityDesResponse: JsValue = Json.parse(
      """
        |{
        |	"idType": "VRN",
        |	"idNumber": "XQIT00000000001",
        |	"regimeType": "VATC",
        |	"processingDate": "2017-03-07T09:30:00.000Z",
        |	"financialTransactions": []
        |}
        |
    """.stripMargin)

  }

}
