/*
 * Copyright 2022 HM Revenue & Customs
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
import play.api.libs.json.{JsObject, JsValue, Json}
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import v1.constants.FinancialDataConstants.vrn
import v1.constants.PenaltiesConstants.correlationId
import v1.controllers.UserRequest
import v1.models.auth.UserDetails
import v1.models.domain.Vrn
import v1.models.errors.{ErrorWrapper, MtdError}
import v1.models.outcomes.ResponseWrapper
import v1.models.request.penalties.{FinancialRawData, FinancialRequest}
import v1.models.response.financialData
import v1.models.response.financialData._

object FinancialDataConstants {

  implicit val correlationId: String = "abc123-789xyz"
  val userDetails: UserDetails = UserDetails("Individual", None, "client-Id")
  implicit val userRequest: UserRequest[AnyContentAsEmpty.type] = UserRequest(userDetails, FakeRequest())

  val vrn: String = "123456789"
  val searchItem = "XC00178236592"
  val rawData: FinancialRawData = FinancialRawData(vrn)
  val financialRequest: FinancialRequest = FinancialRequest(Vrn(vrn))
  val invalidVrn = "fakeVRN"
  val invalidRawData: FinancialRawData = FinancialRawData(invalidVrn)

  def financialDataUrl(vrn: String = vrn)(implicit appConfig: AppConfig) = s"/penalties/penalty/financial-data/VRN/${vrn}/VATC"

  def financialDataUrlWithConfig(vrn: String = vrn)(implicit appConfig: AppConfig) = appConfig.penaltiesBaseUrl + s"/penalties/penalty/financial-data/VRN/${vrn}/VATC"

  val testDownstreamFinancialDetails: JsValue = {
    Json.parse(
      """{
        |  "totalisation": {
        |    "regimeTotalisation": {
        |      "totalAccountOverdue": 1000.00,
        |      "totalAccountNotYetDue": 250.00,
        |      "totalAccountCredit": 40.00,
        |      "totalAccountBalance": 1210.00
        |    },
        |    "targetedSearch": {
        |      "totalOverdue": 123.45,
        |      "totalNotYetDue": 12.34,
        |      "totalBalance": 12.45,
        |      "totalCredit": 13.46,
        |      "totalCleared": 12.35
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
        |            "interestStartDate": "2022-03-01"
        |          }
        |        }
        |      ]
        |    }
        |  ]
        |}
        |""".stripMargin
    )
  }

  val testUpstreamFinancialDetails: JsValue = {
    Json.parse(
      """{
        |  "totalisation": {
        |    "totalOverdue": 123.45,
        |    "totalNotYetDue": 12.34,
        |    "totalBalance": 12.45,
        |    "totalCredit": 13.46,
        |    "totalCleared": 12.35
        |  },
        |  "documentDetails": [
        |    {
        |      "postingDate": "2022-03-01",
        |      "issueDate": "2022-03-01",
        |      "documentTotalAmount": 123.45,
        |      "documentClearedAmount": 111.11,
        |      "documentOutstandingAmount": 12.34,
        |      "documentInterestTotal": 1.23,
        |      "lineItemDetails": [
        |        {
        |          "periodFromDate": "2022-03-01",
        |          "periodToDate": "2022-03-01",
        |          "periodKey": "13RL",
        |          "netDueDate": "2022-03-01",
        |          "amount": 123.45,
        |          "lineItemInterestDetails": {
        |            "interestKey": "01",
        |            "interestStartDate": "2022-03-01"
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
    interestKey = "01",
    interestStartDate = "2022-03-01"
  )

  val testLineItemDetails: LineItemDetail = LineItemDetail(
    periodFromDate = "2022-03-01",
    periodToDate = "2022-03-01",
    periodKey = "13RL",
    netDueDate = "2022-03-01",
    amount = 123.45,
    lineItemInterestDetails = testLineItemInterestDetails
  )

  val testDocumentDetail: DocumentDetail = DocumentDetail(
    postingDate = "2022-03-01",
    issueDate = "2022-03-01",
    documentTotalAmount = 123.45,
    documentClearedAmount = 111.11,
    documentOutstandingAmount = 12.34,
    documentInterestTotal = 1.23,
    lineItemDetails = Seq(testLineItemDetails)
  )

  val testTotalisation: Totalisation = Totalisation(
    totalOverdue = 123.45,
    totalNotYetDue = 12.34,
    totalBalance = 12.45,
    totalCredit = 13.46,
    totalCleared = 12.35)

  val testFinancialDataResponse: FinancialDataResponse = FinancialDataResponse(testTotalisation, Seq(testDocumentDetail))

  def wrappedFinancialDataResponse(financialResponse: FinancialDataResponse = testFinancialDataResponse): ResponseWrapper[FinancialDataResponse] = {
    ResponseWrapper(correlationId, financialResponse)
  }

  def errorWrapper(error: MtdError): ErrorWrapper = ErrorWrapper(correlationId, error)
  def errorWrapperMulti(error: Seq[MtdError]): ErrorWrapper = ErrorWrapper(correlationId, error.head, Some(error.tail))
}