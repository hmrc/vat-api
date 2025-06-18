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

package v1.constants

import config.AppConfig
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import v1.controllers.UserRequest
import v1.models.auth.UserDetails
import v1.models.domain.Vrn
import v1.models.errors.{ErrorWrapper, MtdError}
import v1.models.outcomes.ResponseWrapper
import v1.models.request.penalties.{FinancialRawData, FinancialRequest}
import v1.models.response.financialData._

object FinancialDataConstants {

  implicit val correlationId: String = "abc123-789xyz"
  val userDetails: UserDetails = UserDetails("Individual", None, "client-Id")
  implicit val userRequest: UserRequest[AnyContentAsEmpty.type] = UserRequest(userDetails, FakeRequest())

  val vrn: String = "123456789"
  val searchItem = "XA002616060079"
  val rawData: FinancialRawData = FinancialRawData(vrn, searchItem)
  val financialRequest: FinancialRequest = FinancialRequest(Vrn(vrn), searchItem)
  val invalidVrn = "fakeVRN"
  val invalidRawData: FinancialRawData = FinancialRawData(invalidVrn, searchItem)

  def financialDataUrl(vrn: String = vrn)(implicit appConfig: AppConfig): String = s"/penalties/penalty/financial-data/VRN/$vrn/VATC"

  def financialDataUrlWithConfig(vrn: String = vrn)(implicit appConfig: AppConfig): String = appConfig.penaltiesBaseUrl + s"/penalties/VATC/penalty/financial-data/VRN/$vrn?searchType=CHGREF&searchItem=${searchItem}"

  
  val testDownstreamFinancialDetailsNoDocumentDetails: JsValue = {
    Json.parse(
      """{
        |"getFinancialData": {
        |"financialDetails": {
        |  "totalisation": {
        |    "regimeTotalisation": {
        |      "totalAccountOverdue": 1000.00,
        |      "totalAccountNotYetDue": 250.00,
        |      "totalAccountCredit": 40.00,
        |      "totalAccountBalance": 1210.00
        |    },
        |    "targetedSearch_SelectionCriteriaTotalisation": {
        |      "totalOverdue": 123.45,
        |      "totalNotYetDue": 12.34,
        |      "totalBalance": 12.45,
        |      "totalCredit": 13.46,
        |      "totalCleared": 12.35
        |    },
        |    "additionalReceivableTotalisations" :{
        |     "totalAccountPostedInterest": 100,
        |     "totalAccountAccruingInterest": 100
        |    }
        |  }
        |}}}
        |""".stripMargin
    )
  }

  val testDownstreamFinancialDetails: JsValue = {
    Json.parse(
      """{
        |"getFinancialData": {
        |"financialDetails": {
        |  "totalisation": {
        |    "regimeTotalisation": {
        |      "totalAccountOverdue": 1000.00,
        |      "totalAccountNotYetDue": 250.00,
        |      "totalAccountCredit": 40.00,
        |      "totalAccountBalance": 1210.00
        |    },
        |    "targetedSearch_SelectionCriteriaTotalisation": {
        |      "totalOverdue": 123.45,
        |      "totalNotYetDue": 12.34,
        |      "totalBalance": 12.45,
        |      "totalCredit": 13.46,
        |      "totalCleared": 12.35
        |    },
        |    "additionalReceivableTotalisations" :{
        |     "totalAccountPostedInterest": 100,
        |     "totalAccountAccruingInterest": 100
        |    }
        |  },
        |  "documentDetails": [
        |    {
        |      "documentNumber": "187346702498",
        |      "documentType": "P1",
        |      "chargeReferenceNumber": "XP001286394838",
        |      "businessPartnerNumber": "100893731",
        |      "contractAccountNumber": "900726630",
        |      "contractAccountCategory": "32",
        |      "contractObjectNumber": "104920928302302",
        |      "contractObjectType": "ZVAT",
        |      "postingDate": "2022-03-01",
        |      "issueDate": "2022-03-01",
        |      "documentTotalAmount": 123.45,
        |      "documentClearedAmount": 111.11,
        |      "documentOutstandingAmount": 12.34,
        |      "documentLockDetails": {
        |        "lockType": "",
        |        "lockStartDate": "2022-03-01",
        |        "lockEndDate": "2022-03-01"
        |      },
        |      "documentInterestTotals": {
        |        "interestPostedAmount": 13.12,
        |        "interestPostedChargeRef": "XB001286323438",
        |        "interestAccruingAmount": 12.10,
        |        "interestTotalAmount": 1.23
        |      },
        |      "documentPenaltyTotals": {
        |        "Penalty Type": "LPP1",
        |        "Posted Amount": 10.00,
        |        "Posted Charge Reference": "XR00123933492",
        |        "Accruing Amount": 0.00
        |      },
        |      "lineItemDetails": [
        |        {
        |          "chargeDescription": "IN1",
        |          "itemNumber": "0001",
        |          "subItemNumber": "003",
        |          "mainTransaction": "4576",
        |          "subTransaction": "1000",
        |          "periodFromDate": "2022-03-01",
        |          "periodToDate": "2022-03-01",
        |          "periodKey": "13RL",
        |          "netDueDate": "2022-03-01",
        |          "formBundleNumber": "125435934761",
        |          "statisticalKey": "1",
        |          "amount": 123.45,
        |          "clearingDate": "2022-03-01",
        |          "clearingReason": "01",
        |          "clearingDocument": "719283701921",
        |          "outgoingPaymentMethod": "B",
        |          "lineItemLockDetails": {
        |            "lockType": "",
        |            "lockStartDate": "",
        |            "lockEndDate": ""
        |          },
        |          "lineItemInterestDetails": {
        |            "interestKey": "01",
        |            "interestStartDate": "2022-03-01",
        |            "currentInterestRate": 2,
        |            "interestPostedAmount": 123,
        |            "interestAccruingAmount": 123
        |          }
        |        }
        |      ]
        |    }
        |  ]
        |}}}
        |""".stripMargin
    )
  }

  val testUpstreamFinancialDetails: JsValue = {
    Json.parse(
      """{
        |  "totalisations": {
        |    "totalOverdue": 123.45,
        |    "totalNotYetDue": 12.34,
        |    "totalBalance": 12.45,
        |    "totalCredit": 13.46,
        |    "totalCleared": 12.35,
        |    "additionalReceivableTotalisations":{
        |       "totalAccountPostedInterest": 100,
        |       "totalAccountAccruingInterest": 100
        |    }
        |  },
        |  "documentDetails": [
        |    {
        |      "postingDate": "2022-03-01",
        |      "issueDate": "2022-03-01",
        |      "documentTotalAmount": 123.45,
        |      "documentInterestTotals": {
        |       "interestPostedAmount": 13.12,
        |       "interestAccruingAmount": 12.10
        |      },
        |      "documentClearedAmount": 111.11,
        |      "documentOutstandingAmount": 12.34,
        |      "lineItemDetails": [
        |        {
        |          "chargeDescription": "IN1",
        |          "periodFromDate": "2022-03-01",
        |          "periodToDate": "2022-03-01",
        |          "netDueDate": "2022-03-01",
        |          "amount": 123.45,
        |          "lineItemInterestDetails": {
        |            "interestStartDate": "2022-03-01",
        |            "currentInterestRate": 2,
        |            "interestPostedAmount": 123,
        |            "interestAccruingAmount": 123
        |          }
        |        }
        |      ]
        |    }
        |  ]
        |}
        |""".stripMargin
    )
  }

  val testLineItemInterestDetails: LineItemInterestDetails = LineItemInterestDetails(
    currentInterestRate = Some(2),
    interestPostedAmount = Some(123),
    interestAccruingAmount = Some(123),
    interestStartDate = Some("2022-03-01")
  )

  val testLineItemDetails: LineItemDetail = LineItemDetail(
    chargeDescription = Some("IN1"),
    periodFromDate = Some("2022-03-01"),
    periodToDate = Some("2022-03-01"),
    netDueDate = Some("2022-03-01"),
    amount = Some(123.45),
    lineItemInterestDetails = Some(testLineItemInterestDetails)
  )

  val testDocumentInterestTotals: DocumentInterestTotals = DocumentInterestTotals(
    interestPostedAmount = Some(13.12),
    interestAccruingAmount = Some(12.10)
  )


  val testDocumentDetail: DocumentDetail = DocumentDetail(
    postingDate = Some("2022-03-01"),
    issueDate = Some("2022-03-01"),
    documentTotalAmount = Some(123.45),
    documentClearedAmount = Some(111.11),
    documentInterestTotals = Some(testDocumentInterestTotals),
    documentOutstandingAmount = Some(12.34),
    lineItemDetails = Some(Seq(testLineItemDetails))
  )

  val testAdditionalReceivableTotalisations: AdditionalReceivableTotalisations = AdditionalReceivableTotalisations(
    totalAccountPostedInterest = Some(100),
    totalAccountAccruingInterest = Some(100)
  )

  val testTotalisation: Totalisations = Totalisations(
    totalOverdue = Some(123.45),
    totalNotYetDue = Some(12.34),
    totalBalance = Some(12.45),
    totalCredit = Some(13.46),
    totalCleared = Some(12.35),
    additionalReceivableTotalisations = Some(testAdditionalReceivableTotalisations)
  )

  val testFinancialNoDocumentDetailsDataResponse: FinancialDataResponse = FinancialDataResponse(Some(testTotalisation), None)
  val testFinancialDataResponse: FinancialDataResponse = FinancialDataResponse(Some(testTotalisation), Some(Seq(testDocumentDetail)))

  def wrappedFinancialDataResponse(financialResponse: FinancialDataResponse = testFinancialDataResponse): ResponseWrapper[FinancialDataResponse] = {
    ResponseWrapper(correlationId, financialResponse)
  }

  def errorWrapper(error: MtdError): ErrorWrapper = ErrorWrapper(correlationId, error)
  def errorWrapperMulti(error: Seq[MtdError]): ErrorWrapper = ErrorWrapper(correlationId, error.head, Some(error.tail))
}