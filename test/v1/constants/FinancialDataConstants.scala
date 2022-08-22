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
import v1.models.response.financialData.{BalanceDetails, CodingDetails, DocumentDetails, FinancialDataResponse, FinancialDetails, Items, TaxPayerDetails}
import v1.models.response.penalties._

object FinancialDataConstants {

  implicit val correlationId: String = "abc123-789xyz"
  val userDetails: UserDetails = UserDetails("Individual", None, "client-Id")
  implicit val userRequest: UserRequest[AnyContentAsEmpty.type] = UserRequest(userDetails, FakeRequest())

  val vrn: String = "123456789"
  val rawData: FinancialRawData = FinancialRawData(vrn)
  val financialRequest: FinancialRequest = FinancialRequest(Vrn(vrn))
  val invalidVrn = "fakeVRN"
  val invalidRawData: FinancialRawData = FinancialRawData(invalidVrn)

  def financialDataUrl(vrn: String = vrn)(implicit appConfig: AppConfig) = s"/penalties/penalty/financial-data/VRN/${vrn}/VATC"
  def financialDataUrlWithConfig(vrn: String = vrn)(implicit appConfig: AppConfig) = appConfig.penaltiesBaseUrl + s"/penalties/penalty/financial-data/VRN/${vrn}/VATC"

  val testTaxPayersJson: JsObject = Json.obj(
    "idType" -> "123",
    "idNumber" -> "123",
    "regimeType" -> "123"
  )

  val testBalanceDetailsJsonMin: JsObject = Json.obj(
    "balanceDueWithin30Days" -> 123,
    "balanceNotDueIn30Days" -> 123,
    "overDueAmount" -> 123,
    "totalBalance" -> 123,
    "amountCodedOut" -> 123
  )

  val testBalanceDetailsJsonMax: JsObject = Json.obj(
    "balanceDueWithin30Days" -> 123,
    "nextPaymentDateForChargesDueIn30Days" -> "123",
    "balanceNotDueIn30Days" -> 123,
    "nextPaymentDateBalanceNotDue" -> "123",
    "overDueAmount" -> 123,
    "earliestPaymentDateOverDue" -> "123",
    "totalBalance" -> 123,
    "amountCodedOut" -> 123
  )

  val testCodingDetails: JsValue = Json.parse(
    """
      |[{
      |     "taxYearReturn": "123",
      |      "totalReturnAmount": 123,
      |      "amountNotCoded": 123,
      |      "amountNotCodedDueDate": "2022-10-11",
      |      "amountCodedOut": 123,
      |      "taxYearCoding": "123"
      |},
      |{
      |      "taxYearReturn": "123",
      |      "totalReturnAmount": 123,
      |      "amountNotCoded": 123,
      |      "amountNotCodedDueDate": "2022-10-11",
      |      "amountCodedOut": 123,
      |      "taxYearCoding": "1234"
      |}]
      |""".stripMargin
  )


  val testDocumentDetailsJsonMin: JsValue = Json.parse(
    """[{
      |"taxYear": "123",
      |    "documentId": "123",
      |    "documentDate": "2022-10-11",
      |    "documentText": "123",
      |    "documentDueDate": "2022-10-11",
      |    "totalAmount": 123,
      |    "documentOutstandingAmount": 123,
      |    "statisticalFlag": false
      |}]
      |""".stripMargin

  )

  val testDocumentDetailsJsonMax: JsValue = Json.parse(
    """[{
      |"taxYear": "123",
      |    "documentId": "123",
      |    "documentDate": "2022-10-11",
      |    "documentText": "123",
      |    "documentDueDate": "2022-10-11",
      |    "totalAmount": 123,
      |    "documentOutstandingAmount": 123,
      |    "statisticalFlag": false,
      |    "documentDescription": "123",
      |      "formBundleNumber": "123",
      |      "lastClearingDate": "123",
      |      "lastClearingReason": "123",
      |      "lastClearedAmount": 123,
      |      "informationCode": "123",
      |      "paymentLot": "123",
      |      "paymentLotItem": "123",
      |      "accruingInterestAmount": 123,
      |      "interestRate": 123,
      |      "interestFromDate": "123",
      |      "interestEndDate": "123",
      |      "latePaymentInterestID": "123",
      |      "latePaymentInterestAmount": 123,
      |      "lpiWithDunningBlock": 123,
      |      "interestOutstandingAmount": 123,
      |      "accruingPenaltyLPP1": "123",
      |      "lpp1Amount": 123,
      |      "lpp1ID": "123",
      |      "accruingPenaltyLPP2": "123",
      |      "lpp2Amount": 123,
      |      "lpp2ID": "123"
      |},
      |{
      |    "taxYear": "123",
      |    "documentId": "123",
      |    "documentDate": "2022-10-11",
      |    "documentText": "123",
      |    "documentDueDate": "2022-10-11",
      |    "totalAmount": 123,
      |    "documentOutstandingAmount": 123,
      |    "statisticalFlag": false,
      |    "documentDescription": "1234",
      |      "formBundleNumber": "1234",
      |      "lastClearingDate": "1234",
      |      "lastClearingReason": "1234",
      |      "lastClearedAmount": 1234,
      |      "informationCode": "1234",
      |      "paymentLot": "1234",
      |      "paymentLotItem": "1234",
      |      "accruingInterestAmount": 1234,
      |      "interestRate": 1234,
      |      "interestFromDate": "1234",
      |      "interestEndDate": "1234",
      |      "latePaymentInterestID": "1234",
      |      "latePaymentInterestAmount": 1234,
      |      "lpiWithDunningBlock": 1234,
      |      "interestOutstandingAmount": 1234,
      |      "accruingPenaltyLPP1": "1234",
      |      "lpp1Amount": 1234,
      |      "lpp1ID": "1234",
      |      "accruingPenaltyLPP2": "1234",
      |      "lpp2Amount": 1234,
      |      "lpp2ID": "1234"
      |}]
      |""".stripMargin

  )

  val testItemsJsonMin: JsValue = Json.parse(
    """
      |[{}]
      |""".stripMargin
  )

  val testFinancialDetailsJsonMin: JsValue = Json.parse(
    """[{
      |"taxYear":  "123",
      |      "documentId":  "123",
      |      "items": [{}]
      |      }]
      |""".stripMargin
  )

  val testFinancialDetailsJsonMax: JsValue = Json.parse(
    """[{
      |      "taxYear":  "123",
      |      "documentId":  "123",
      |      "chargeType": "123",
      |      "mainType": "123",
      |      "periodKey": "123",
      |      "periodKeyDescription": "123",
      |    "taxPeriodFrom": "123",
      |    "taxPeriodTo": "123",
      |    "businessPartner": "123",
      |    "contractAccountCategory": "123",
      |    "contractAccount": "123",
      |    "contractObjectType": "123",
      |    "contractObject": "123",
      |    "sapDocumentNumber": "123",
      |    "sapDocumentNumberItem": "123",
      |    "chargeReference": "123",
      |    "mainTransaction": "123",
      |    "subTransaction": "123",
      |    "originalAmount": 123,
      |    "outstandingAmount": 123,
      |    "clearedAmount": 123,
      |    "accruedInterest": 123,
      |    "items": [{
      |         "subItem": "123",
      |         "dueDate": "123",
      |         "amount": 123,
      |         "clearingDate": "123",
      |         "clearingReason": "123",
      |         "outgoingPaymentMethod": "123",
      |         "paymentLock": "123",
      |         "clearingLock": "123",
      |         "interestLock": "123",
      |         "dunningLock": "123",
      |         "returnFlag": false,
      |         "paymentReference": "123",
      |         "paymentAmount": 123,
      |         "paymentMethod": "123",
      |         "paymentLot": "123",
      |         "paymentLotItem": "123",
      |         "clearingSAPDocument": "123",
      |         "codingInitiationDate": "123",
      |         "statisticalDocument": "123",
      |         "DDCollectionInProgress": false,
      |         "returnReason": "123",
      |         "promisetoPay": "123"
      |     },{
      |         "subItem": "1234",
      |         "dueDate": "1234",
      |         "amount": 1234,
      |         "clearingDate": "1234",
      |         "clearingReason": "1234",
      |         "outgoingPaymentMethod": "1234",
      |         "paymentLock": "1234",
      |         "clearingLock": "1234",
      |         "interestLock": "1234",
      |         "dunningLock": "1234",
      |         "returnFlag": true,
      |         "paymentReference": "1234",
      |         "paymentAmount": 1234,
      |         "paymentMethod": "1234",
      |         "paymentLot": "1234",
      |         "paymentLotItem": "1234",
      |         "clearingSAPDocument": "1234",
      |         "codingInitiationDate": "1234",
      |         "statisticalDocument": "1234",
      |         "DDCollectionInProgress": true,
      |         "returnReason": "1234",
      |         "promisetoPay": "1234"
      |     }]
      |      },
      |      {
      |      "taxYear":  "123",
      |      "documentId":  "123",
      |      "chargeType": "1234",
      |      "mainType": "1234",
      |      "periodKey": "1234",
      |      "periodKeyDescription": "1234",
      |    "taxPeriodFrom": "1234",
      |    "taxPeriodTo": "1234",
      |    "businessPartner": "1234",
      |    "contractAccountCategory": "1234",
      |    "contractAccount": "1234",
      |    "contractObjectType": "1234",
      |    "contractObject": "1234",
      |    "sapDocumentNumber": "1234",
      |    "sapDocumentNumberItem": "1234",
      |    "chargeReference": "1234",
      |    "mainTransaction": "1234",
      |    "subTransaction": "1234",
      |    "originalAmount": 1234,
      |    "outstandingAmount": 1234,
      |    "clearedAmount": 1234,
      |    "accruedInterest": 1234,
      |    "items": [{
      |         "subItem": "123",
      |         "dueDate": "123",
      |         "amount": 123,
      |         "clearingDate": "123",
      |         "clearingReason": "123",
      |         "outgoingPaymentMethod": "123",
      |         "paymentLock": "123",
      |         "clearingLock": "123",
      |         "interestLock": "123",
      |         "dunningLock": "123",
      |         "returnFlag": false,
      |         "paymentReference": "123",
      |         "paymentAmount": 123,
      |         "paymentMethod": "123",
      |         "paymentLot": "123",
      |         "paymentLotItem": "123",
      |         "clearingSAPDocument": "123",
      |         "codingInitiationDate": "123",
      |         "statisticalDocument": "123",
      |         "DDCollectionInProgress": false,
      |         "returnReason": "123",
      |         "promisetoPay": "123"
      |     },{
      |         "subItem": "1234",
      |         "dueDate": "1234",
      |         "amount": 1234,
      |         "clearingDate": "1234",
      |         "clearingReason": "1234",
      |         "outgoingPaymentMethod": "1234",
      |         "paymentLock": "1234",
      |         "clearingLock": "1234",
      |         "interestLock": "1234",
      |         "dunningLock": "1234",
      |         "returnFlag": true,
      |         "paymentReference": "1234",
      |         "paymentAmount": 1234,
      |         "paymentMethod": "1234",
      |         "paymentLot": "1234",
      |         "paymentLotItem": "1234",
      |         "clearingSAPDocument": "1234",
      |         "codingInitiationDate": "1234",
      |         "statisticalDocument": "1234",
      |         "DDCollectionInProgress": true,
      |         "returnReason": "1234",
      |         "promisetoPay": "1234"
      |     }]
      |      }
      |      ]
      |""".stripMargin
  )

  val testFinancialResponseJsonMin: JsObject = Json.obj(
    "taxPayerDetails" -> testTaxPayersJson,
    "balanceDetails" -> testBalanceDetailsJsonMin,
    "documentDetails" -> testDocumentDetailsJsonMin,
    "financialDetails" -> testFinancialDetailsJsonMin
  )

  val testFinancialResponseJsonMax: JsObject = Json.obj(
    "taxPayerDetails" -> testTaxPayersJson,
    "balanceDetails" -> testBalanceDetailsJsonMax,
    "codingDetails" -> testCodingDetails,
    "documentDetails" -> testDocumentDetailsJsonMax,
    "financialDetails" -> testFinancialDetailsJsonMax
  )

  val testTaxPayerDetails: TaxPayerDetails = TaxPayerDetails(
    idType = "123",
    idNumber = "123",
    regimeType = "123"
  )

  val testBalanceDetailsMin: BalanceDetails = BalanceDetails(
    balanceDueWithin30Days = 123,
    nextPaymentDateForChargesDueIn30Days = None,
    balanceNotDueIn30Days = 123,
    nextPaymentDateBalanceNotDue = None,
    overDueAmount = 123,
    earliestPaymentDateOverDue = None,
    totalBalance = 123,
    amountCodedOut = 123
  )

  val testBalanceDetailsMax: BalanceDetails = BalanceDetails(
    balanceDueWithin30Days = 123,
    nextPaymentDateForChargesDueIn30Days = Some("123"),
    balanceNotDueIn30Days = 123,
    nextPaymentDateBalanceNotDue = Some("123"),
    overDueAmount = 123,
    earliestPaymentDateOverDue = Some("123"),
    totalBalance = 123,
    amountCodedOut = 123
  )

  def testCodingDetails(taxYearCoding: String): CodingDetails = {
    CodingDetails(
      taxYearReturn = "123",
      totalReturnAmount = 123,
      amountNotCoded = 123,
      amountNotCodedDueDate = "2022-10-11",
      amountCodedOut = Some(123),
      taxYearCoding = Some(taxYearCoding)
    )
  }

  def testDocumentDetails(documentDescription: Option[String] = None,
                          formBundleNumber: Option[String] = None,
                          lastClearingDate: Option[String] = None,
                          lastClearingReason: Option[String] = None,
                          lastClearedAmount: Option[BigDecimal] = None,
                          informationCode: Option[String] = None,
                          paymentLot: Option[String] = None,
                          paymentLotItem: Option[String] = None,
                          accruingInterestAmount: Option[BigDecimal] = None,
                          interestRate: Option[BigDecimal] = None,
                          interestFromDate: Option[String] = None,
                          interestEndDate: Option[String] = None,
                          latePaymentInterestID: Option[String] = None,
                          latePaymentInterestAmount: Option[BigDecimal] = None,
                          lpiWithDunningBlock: Option[BigDecimal] = None,
                          interestOutstandingAmount: Option[BigDecimal] = None,
                          accruingPenaltyLPP1: Option[String] = None,
                          lpp1Amount: Option[BigDecimal] = None,
                          lpp1ID: Option[String] = None,
                          accruingPenaltyLPP2: Option[String] = None,
                          lpp2Amount: Option[BigDecimal] = None,
                          lpp2ID: Option[String] = None): DocumentDetails = {
    DocumentDetails(
      taxYear = "123",
      documentId = "123",
      documentDate = "2022-10-11",
      documentText = "123",
      documentDueDate = "2022-10-11",
      documentDescription = documentDescription,
      formBundleNumber = formBundleNumber,
      totalAmount = 123,
      documentOutstandingAmount = 123,
      lastClearingDate = lastClearingDate,
      lastClearingReason = lastClearingReason,
      lastClearedAmount = lastClearedAmount,
      statisticalFlag = false,
      informationCode = informationCode,
      paymentLot = paymentLot,
      paymentLotItem = paymentLotItem,
      accruingInterestAmount = accruingInterestAmount,
      interestRate = interestRate,
      interestFromDate = interestFromDate,
      interestEndDate = interestEndDate,
      latePaymentInterestID = latePaymentInterestID,
      latePaymentInterestAmount = latePaymentInterestAmount,
      lpiWithDunningBlock = lpiWithDunningBlock,
      interestOutstandingAmount = interestOutstandingAmount,
      accruingPenaltyLPP1 = accruingPenaltyLPP1,
      lpp1Amount = lpp1Amount,
      lpp1ID = lpp1ID,
      accruingPenaltyLPP2 = accruingPenaltyLPP2,
      lpp2Amount = lpp2Amount,
      lpp2ID = lpp2ID
    )
  }

  def testFinancialDetails(chargeType: Option[String] = None,
                           mainType: Option[String] = None,
                           periodKey: Option[String] = None,
                           periodKeyDescription: Option[String] = None,
                           taxPeriodFrom: Option[String] = None,
                           taxPeriodTo: Option[String] = None,
                           businessPartner: Option[String] = None,
                           contractAccountCategory: Option[String] = None,
                           contractAccount: Option[String] = None,
                           contractObjectType: Option[String] = None,
                           contractObject: Option[String] = None,
                           sapDocumentNumber: Option[String] = None,
                           sapDocumentNumberItem: Option[String] = None,
                           chargeReference: Option[String] = None,
                           mainTransaction: Option[String] = None,
                           subTransaction: Option[String] = None,
                           originalAmount: Option[BigDecimal] = None,
                           outstandingAmount: Option[BigDecimal] = None,
                           clearedAmount: Option[BigDecimal] = None,
                           accruedInterest: Option[BigDecimal] = None,
                           items: Seq[Items]): FinancialDetails = {
    FinancialDetails(
      taxYear = "123",
      documentId = "123",
      chargeType = chargeType,
      mainType = mainType,
      periodKey = periodKey,
      periodKeyDescription = periodKeyDescription,
      taxPeriodFrom = taxPeriodFrom,
      taxPeriodTo = taxPeriodTo,
      businessPartner = businessPartner,
      contractAccountCategory = contractAccountCategory,
      contractAccount = contractAccount,
      contractObjectType = contractObjectType,
      contractObject = contractObject,
      sapDocumentNumber = sapDocumentNumber,
      sapDocumentNumberItem = sapDocumentNumberItem,
      chargeReference = chargeReference,
      mainTransaction = mainTransaction,
      subTransaction = subTransaction,
      originalAmount = originalAmount,
      outstandingAmount = outstandingAmount,
      clearedAmount = clearedAmount,
      accruedInterest = accruedInterest,
      items = items
    )
  }

  def testItems(subItem: Option[String] = None,
                dueDate: Option[String] = None,
                amount: Option[BigDecimal] = None,
                clearingDate: Option[String] = None,
                clearingReason: Option[String] = None,
                outgoingPaymentMethod: Option[String] = None,
                paymentLock: Option[String] = None,
                clearingLock: Option[String] = None,
                interestLock: Option[String] = None,
                dunningLock: Option[String] = None,
                returnFlag: Option[Boolean] = None,
                paymentReference: Option[String] = None,
                paymentAmount: Option[BigDecimal] = None,
                paymentMethod: Option[String] = None,
                paymentLot: Option[String] = None,
                paymentLotItem: Option[String] = None,
                clearingSAPDocument: Option[String] = None,
                codingInitiationDate: Option[String] = None,
                statisticalDocument: Option[String] = None,
                DDCollectionInProgress: Option[Boolean] = None,
                returnReason: Option[String] = None,
                promisetoPay: Option[String] = None): Items = {
    Items(
      subItem = subItem,
      dueDate = dueDate,
      amount = amount,
      clearingDate = clearingDate,
      clearingReason = clearingReason,
      outgoingPaymentMethod = outgoingPaymentMethod,
      paymentLock = paymentLock,
      clearingLock = clearingLock,
      interestLock = interestLock,
      dunningLock = dunningLock,
      returnFlag = returnFlag,
      paymentReference = paymentReference,
      paymentAmount = paymentAmount,
      paymentMethod = paymentMethod,
      paymentLot = paymentLot,
      paymentLotItem = paymentLotItem,
      clearingSAPDocument = clearingSAPDocument,
      codingInitiationDate = codingInitiationDate,
      statisticalDocument = statisticalDocument,
      DDCollectionInProgress = DDCollectionInProgress,
      returnReason = returnReason,
      promisetoPay = promisetoPay
    )
  }

  val testFinancialResponseMin: FinancialDataResponse = FinancialDataResponse(
    taxPayerDetails = testTaxPayerDetails,
    balanceDetails = testBalanceDetailsMin,
    codingDetails = None,
    documentDetails = Seq(testDocumentDetails()),
    financialDetails = Seq(testFinancialDetails(items = Seq(testItems())))
  )

  val testDocumentDetails1: DocumentDetails = testDocumentDetails(
    documentDescription = Some("123"),
    formBundleNumber = Some("123"),
    lastClearingDate = Some("123"),
    lastClearingReason = Some("123"),
    lastClearedAmount = Some(123),
    informationCode = Some("123"),
    paymentLot = Some("123"),
    paymentLotItem = Some("123"),
    accruingInterestAmount = Some(123),
    interestRate = Some(123),
    interestFromDate = Some("123"),
    interestEndDate = Some("123"),
    latePaymentInterestID = Some("123"),
    latePaymentInterestAmount = Some(123),
    lpiWithDunningBlock = Some(123),
    interestOutstandingAmount = Some(123),
    accruingPenaltyLPP1 = Some("123"),
    lpp1Amount = Some(123),
    lpp1ID = Some("123"),
    accruingPenaltyLPP2 = Some("123"),
    lpp2Amount = Some(123),
    lpp2ID = Some("123")
  )

  val testDocumentDetails2: DocumentDetails = testDocumentDetails(
    documentDescription = Some("1234"),
    formBundleNumber = Some("1234"),
    lastClearingDate = Some("1234"),
    lastClearingReason = Some("1234"),
    lastClearedAmount = Some(1234),
    informationCode = Some("1234"),
    paymentLot = Some("1234"),
    paymentLotItem = Some("1234"),
    accruingInterestAmount = Some(1234),
    interestRate = Some(1234),
    interestFromDate = Some("1234"),
    interestEndDate = Some("1234"),
    latePaymentInterestID = Some("1234"),
    latePaymentInterestAmount = Some(1234),
    lpiWithDunningBlock = Some(1234),
    interestOutstandingAmount = Some(1234),
    accruingPenaltyLPP1 = Some("1234"),
    lpp1Amount = Some(1234),
    lpp1ID = Some("1234"),
    accruingPenaltyLPP2 = Some("1234"),
    lpp2Amount = Some(1234),
    lpp2ID = Some("1234")
  )

  val testItems1: Items = testItems(
    subItem = Some("123"),
    dueDate = Some("123"),
    amount = Some(123),
    clearingDate = Some("123"),
    clearingReason = Some("123"),
    outgoingPaymentMethod = Some("123"),
    paymentLock = Some("123"),
    clearingLock = Some("123"),
    interestLock = Some("123"),
    dunningLock = Some("123"),
    returnFlag = Some(false),
    paymentReference = Some("123"),
    paymentAmount = Some(123),
    paymentMethod = Some("123"),
    paymentLot = Some("123"),
    paymentLotItem = Some("123"),
    clearingSAPDocument = Some("123"),
    codingInitiationDate = Some("123"),
    statisticalDocument = Some("123"),
    DDCollectionInProgress = Some(false),
    returnReason = Some("123"),
    promisetoPay = Some("123")
  )

  val testItems2: Items = testItems(
    subItem = Some("1234"),
    dueDate = Some("1234"),
    amount = Some(1234),
    clearingDate = Some("1234"),
    clearingReason = Some("1234"),
    outgoingPaymentMethod = Some("1234"),
    paymentLock = Some("1234"),
    clearingLock = Some("1234"),
    interestLock = Some("1234"),
    dunningLock = Some("1234"),
    returnFlag = Some(true),
    paymentReference = Some("1234"),
    paymentAmount = Some(1234),
    paymentMethod = Some("1234"),
    paymentLot = Some("1234"),
    paymentLotItem = Some("1234"),
    clearingSAPDocument = Some("1234"),
    codingInitiationDate = Some("1234"),
    statisticalDocument = Some("1234"),
    DDCollectionInProgress = Some(true),
    returnReason = Some("1234"),
    promisetoPay = Some("1234")
  )


  val testFinancialDetails1: FinancialDetails = testFinancialDetails(
    chargeType = Some("123"),
    mainType = Some("123"),
    periodKey = Some("123"),
    periodKeyDescription = Some("123"),
    taxPeriodFrom = Some("123"),
    taxPeriodTo = Some("123"),
    businessPartner = Some("123"),
    contractAccountCategory = Some("123"),
    contractAccount = Some("123"),
    contractObjectType = Some("123"),
    contractObject = Some("123"),
    sapDocumentNumber = Some("123"),
    sapDocumentNumberItem = Some("123"),
    chargeReference = Some("123"),
    mainTransaction = Some("123"),
    subTransaction = Some("123"),
    originalAmount = Some(123),
    outstandingAmount = Some(123),
    clearedAmount = Some(123),
    accruedInterest = Some(123),
    items = Seq(testItems1, testItems2)
  )

  val testFinancialDetails2: FinancialDetails = testFinancialDetails(
    chargeType = Some("1234"),
    mainType = Some("1234"),
    periodKey = Some("1234"),
    periodKeyDescription = Some("1234"),
    taxPeriodFrom = Some("1234"),
    taxPeriodTo = Some("1234"),
    businessPartner = Some("1234"),
    contractAccountCategory = Some("1234"),
    contractAccount = Some("1234"),
    contractObjectType = Some("1234"),
    contractObject = Some("1234"),
    sapDocumentNumber = Some("1234"),
    sapDocumentNumberItem = Some("1234"),
    chargeReference = Some("1234"),
    mainTransaction = Some("1234"),
    subTransaction = Some("1234"),
    originalAmount = Some(1234),
    outstandingAmount = Some(1234),
    clearedAmount = Some(1234),
    accruedInterest = Some(1234),
    items = Seq(testItems1, testItems2)
  )


  val testFinancialResponseMax: FinancialDataResponse = financialData.FinancialDataResponse(
    taxPayerDetails = testTaxPayerDetails,
    balanceDetails = testBalanceDetailsMax,
    codingDetails = Some(Seq(testCodingDetails("123"), testCodingDetails("1234"))),
    documentDetails = Seq(testDocumentDetails1, testDocumentDetails2),
    financialDetails = Seq(testFinancialDetails1, testFinancialDetails2)
  )


  def wrappedFinancialDataResponse(financialResponse: FinancialDataResponse = testFinancialResponseMin): ResponseWrapper[FinancialDataResponse] = {
    ResponseWrapper(correlationId, financialResponse)
  }

  def errorWrapper(error: MtdError): ErrorWrapper = ErrorWrapper(correlationId, error)
  def errorWrapperMulti(error: Seq[MtdError]): ErrorWrapper = ErrorWrapper(correlationId, error.head, Some(error.tail))

}
