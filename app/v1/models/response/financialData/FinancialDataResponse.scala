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

package v1.models.response.financialData

import play.api.libs.json._


case class TaxPayerDetails(
                            idType: String,
                            idNumber: String,
                            regimeType: String
                          )

object TaxPayerDetails {
  implicit val format: OFormat[TaxPayerDetails] = Json.format[TaxPayerDetails]
}


case class BalanceDetails(
                           balanceDueWithin30Days: BigDecimal,
                           nextPaymentDateForChargesDueIn30Days: Option[String],
                           balanceNotDueIn30Days: BigDecimal,
                           nextPaymentDateBalanceNotDue: Option[String],
                           overDueAmount: BigDecimal,
                           earliestPaymentDateOverDue: Option[String],
                           totalBalance: BigDecimal,
                           amountCodedOut: BigDecimal
                         )

object BalanceDetails {
  implicit val format: OFormat[BalanceDetails] = Json.format[BalanceDetails]
}


case class CodingDetails(
                          taxYearReturn: String,
                          totalReturnAmount: BigDecimal,
                          amountNotCoded: BigDecimal,
                          amountNotCodedDueDate: String,
                          amountCodedOut: Option[BigDecimal],
                          taxYearCoding: Option[String]
                        )

object CodingDetails {
  implicit val format: OFormat[CodingDetails] = Json.format[CodingDetails]
}

case class DocumentDetails(
                            taxYear: String,
                            documentId: String,
                            documentDate: String,
                            documentText: String,
                            documentDueDate: String,
                            documentDescription: Option[String],
                            formBundleNumber: Option[String],
                            totalAmount: BigDecimal,
                            documentOutstandingAmount: BigDecimal,
                            lastClearingDate: Option[String],
                            lastClearingReason: Option[String],
                            lastClearedAmount: Option[BigDecimal],
                            statisticalFlag: Boolean,
                            informationCode: Option[String],
                            paymentLot: Option[String],
                            paymentLotItem: Option[String],
                            accruingInterestAmount: Option[BigDecimal],
                            interestRate: Option[BigDecimal],
                            interestFromDate: Option[String],
                            interestEndDate: Option[String],
                            latePaymentInterestID: Option[String],
                            latePaymentInterestAmount: Option[BigDecimal],
                            lpiWithDunningBlock: Option[BigDecimal],
                            interestOutstandingAmount: Option[BigDecimal],
                            accruingPenaltyLPP1: Option[String],
                            lpp1Amount: Option[BigDecimal],
                            lpp1ID: Option[String],
                            accruingPenaltyLPP2: Option[String],
                            lpp2Amount: Option[BigDecimal],
                            lpp2ID: Option[String]
                          )

object DocumentDetails {

  implicit val reads: Reads[DocumentDetails] = for {
    taxYear                     <- (JsPath \ "taxYear").read[String]
    documentId                  <- (JsPath \ "documentId").read[String]
    documentDate                <- (JsPath \ "documentDate").read[String]
    documentText                <- (JsPath \ "documentText").read[String]
    documentDueDate             <- (JsPath \ "documentDueDate").read[String]
    documentDescription         <- (JsPath \ "documentDescription").readNullable[String]
    formBundleNumber            <- (JsPath \ "formBundleNumber").readNullable[String]
    totalAmount                 <- (JsPath \ "totalAmount").read[BigDecimal]
    documentOutstandingAmount   <- (JsPath \ "documentOutstandingAmount").read[BigDecimal]
    lastClearingDate            <- (JsPath \ "lastClearingDate").readNullable[String]
    lastClearingReason          <- (JsPath \ "lastClearingReason").readNullable[String]
    lastClearedAmount           <- (JsPath \ "lastClearedAmount").readNullable[BigDecimal]
    statisticalFlag             <- (JsPath \ "statisticalFlag").read[Boolean]
    informationCode             <- (JsPath \ "informationCode").readNullable[String]
    paymentLot                  <- (JsPath \ "paymentLot").readNullable[String]
    paymentLotItem              <- (JsPath \ "paymentLotItem").readNullable[String]
    accruingInterestAmount      <- (JsPath \ "accruingInterestAmount").readNullable[BigDecimal]
    interestRate                <- (JsPath \ "interestRate").readNullable[BigDecimal]
    interestFromDate            <- (JsPath \ "interestFromDate").readNullable[String]
    interestEndDate             <- (JsPath \ "interestEndDate").readNullable[String]
    latePaymentInterestID       <- (JsPath \ "latePaymentInterestID").readNullable[String]
    latePaymentInterestAmount   <- (JsPath \ "latePaymentInterestAmount").readNullable[BigDecimal]
    lpiWithDunningBlock         <- (JsPath \ "lpiWithDunningBlock").readNullable[BigDecimal]
    interestOutstandingAmount   <- (JsPath \ "interestOutstandingAmount").readNullable[BigDecimal]
    accruingPenaltyLPP1         <- (JsPath \ "accruingPenaltyLPP1").readNullable[String]
    lpp1Amount                  <- (JsPath \ "lpp1Amount").readNullable[BigDecimal]
    lpp1ID                      <- (JsPath \ "lpp1ID").readNullable[String]
    accruingPenaltyLPP2         <- (JsPath \ "accruingPenaltyLPP2").readNullable[String]
    lpp2Amount                  <- (JsPath \ "lpp2Amount").readNullable[BigDecimal]
    lpp2ID                      <- (JsPath \ "lpp2ID").readNullable[String]
  } yield {
    DocumentDetails(
      taxYear = taxYear,
      documentId = documentId,
      documentDate = documentDate,
      documentText = documentText,
      documentDueDate = documentDueDate,
      documentDescription = documentDescription,
      formBundleNumber = formBundleNumber,
      totalAmount = totalAmount,
      documentOutstandingAmount = documentOutstandingAmount,
      lastClearingDate = lastClearingDate,
      lastClearingReason = lastClearingReason,
      lastClearedAmount = lastClearedAmount,
      statisticalFlag = statisticalFlag,
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

  implicit val writes: Writes[DocumentDetails] = new Writes[DocumentDetails] {
    override def writes(o: DocumentDetails): JsValue = {
      JsObject(
        Map(
          "taxYear" -> Json.toJson(o.taxYear),
          "documentId" -> Json.toJson(o.documentId),
          "documentDate" -> Json.toJson(o.documentDate),
          "documentText" -> Json.toJson(o.documentText),
          "documentDueDate" -> Json.toJson(o.documentDueDate),
          "documentDescription" -> Json.toJson(o.documentDescription),
          "formBundleNumber" -> Json.toJson(o.formBundleNumber),
          "totalAmount" -> Json.toJson(o.totalAmount),
          "documentOutstandingAmount" -> Json.toJson(o.documentOutstandingAmount),
          "lastClearingDate" -> Json.toJson(o.lastClearingDate),
          "lastClearingReason" -> Json.toJson(o.lastClearingReason),
          "lastClearedAmount" -> Json.toJson(o.lastClearedAmount),
          "statisticalFlag" -> Json.toJson(o.statisticalFlag),
          "informationCode" -> Json.toJson(o.informationCode),
          "paymentLot" -> Json.toJson(o.paymentLot),
          "paymentLotItem" -> Json.toJson(o.paymentLotItem),
          "accruingInterestAmount" -> Json.toJson(o.accruingInterestAmount),
          "interestRate" -> Json.toJson(o.interestRate),
          "interestFromDate" -> Json.toJson(o.interestFromDate),
          "interestEndDate" -> Json.toJson(o.interestEndDate),
          "latePaymentInterestID" -> Json.toJson(o.latePaymentInterestID),
          "latePaymentInterestAmount" -> Json.toJson(o.latePaymentInterestAmount),
          "lpiWithDunningBlock" -> Json.toJson(o.lpiWithDunningBlock),
          "interestOutstandingAmount" -> Json.toJson(o.interestOutstandingAmount),
          "accruingPenaltyLPP1" -> Json.toJson(o.accruingPenaltyLPP1),
          "lpp1Amount" -> Json.toJson(o.lpp1Amount),
          "lpp1ID" -> Json.toJson(o.lpp1ID),
          "accruingPenaltyLPP2" -> Json.toJson(o.accruingPenaltyLPP2),
          "lpp2Amount" -> Json.toJson(o.lpp2Amount),
          "lpp2ID" -> Json.toJson(o.lpp2ID),
        ).filterNot(_._2 == JsNull)
      )
    }
  }
}

case class FinancialDetails(
                             taxYear: String,
                             documentId: String,
                             chargeType: Option[String],
                             mainType: Option[String],
                             periodKey: Option[String],
                             periodKeyDescription: Option[String],
                             taxPeriodFrom: Option[String],
                             taxPeriodTo: Option[String],
                             businessPartner: Option[String],
                             contractAccountCategory: Option[String],
                             contractAccount: Option[String],
                             contractObjectType: Option[String],
                             contractObject: Option[String],
                             sapDocumentNumber: Option[String],
                             sapDocumentNumberItem: Option[String],
                             chargeReference: Option[String],
                             mainTransaction: Option[String],
                             subTransaction: Option[String],
                             originalAmount: Option[BigDecimal],
                             outstandingAmount: Option[BigDecimal],
                             clearedAmount: Option[BigDecimal],
                             accruedInterest: Option[BigDecimal],
                             items: Seq[Items]
                           )

object FinancialDetails {
  implicit val reads: Reads[FinancialDetails] = for {
    taxYear                   <- (JsPath \ "taxYear").read[String]
    documentId                <- (JsPath \ "documentId").read[String]
    chargeType                <- (JsPath \ "chargeType").readNullable[String]
    mainType                  <- (JsPath \ "mainType").readNullable[String]
    periodKey                 <- (JsPath \ "periodKey").readNullable[String]
    periodKeyDescription      <- (JsPath \ "periodKeyDescription").readNullable[String]
    taxPeriodFrom             <- (JsPath \ "taxPeriodFrom").readNullable[String]
    taxPeriodTo               <- (JsPath \ "taxPeriodTo").readNullable[String]
    businessPartner           <- (JsPath \ "businessPartner").readNullable[String]
    contractAccountCategory   <- (JsPath \ "contractAccountCategory").readNullable[String]
    contractAccount           <- (JsPath \ "contractAccount").readNullable[String]
    contractObjectType        <- (JsPath \ "contractObjectType").readNullable[String]
    contractObject            <- (JsPath \ "contractObject").readNullable[String]
    sapDocumentNumber         <- (JsPath \ "sapDocumentNumber").readNullable[String]
    sapDocumentNumberItem     <- (JsPath \ "sapDocumentNumberItem").readNullable[String]
    chargeReference           <- (JsPath \ "chargeReference").readNullable[String]
    mainTransaction           <- (JsPath \ "mainTransaction").readNullable[String]
    subTransaction            <- (JsPath \ "subTransaction").readNullable[String]
    originalAmount            <- (JsPath \ "originalAmount").readNullable[BigDecimal]
    outstandingAmount         <- (JsPath \ "outstandingAmount").readNullable[BigDecimal]
    clearedAmount             <- (JsPath \ "clearedAmount").readNullable[BigDecimal]
    accruedInterest           <- (JsPath \ "accruedInterest").readNullable[BigDecimal]
    items                     <- (JsPath \ "items").read[Seq[Items]]
  } yield {
    FinancialDetails(
      taxYear = taxYear,
      documentId = documentId,
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

  implicit val writes: Writes[FinancialDetails] = new Writes[FinancialDetails] {
    override def writes(o: FinancialDetails): JsValue = {
      JsObject(
        Map(
          "taxYear" -> Json.toJson(o.taxYear),
          "documentId" -> Json.toJson(o.documentId),
          "chargeType" -> Json.toJson(o.chargeType),
          "mainType" -> Json.toJson(o.mainType),
          "periodKey" -> Json.toJson(o.periodKey),
          "periodKeyDescription" -> Json.toJson(o.periodKeyDescription),
          "taxPeriodFrom" -> Json.toJson(o.taxPeriodFrom),
          "taxPeriodTo" -> Json.toJson(o.taxPeriodTo),
          "businessPartner" -> Json.toJson(o.businessPartner),
          "contractAccountCategory" -> Json.toJson(o.contractAccountCategory),
          "contractAccount" -> Json.toJson(o.contractAccount),
          "contractObjectType" -> Json.toJson(o.contractObjectType),
          "contractObject" -> Json.toJson(o.contractObject),
          "sapDocumentNumber" -> Json.toJson(o.sapDocumentNumber),
          "sapDocumentNumberItem" -> Json.toJson(o.sapDocumentNumberItem),
          "chargeReference" -> Json.toJson(o.chargeReference),
          "mainTransaction" -> Json.toJson(o.mainTransaction),
          "subTransaction" -> Json.toJson(o.subTransaction),
          "originalAmount" -> Json.toJson(o.originalAmount),
          "outstandingAmount" -> Json.toJson(o.outstandingAmount),
          "clearedAmount" -> Json.toJson(o.clearedAmount),
          "accruedInterest" -> Json.toJson(o.accruedInterest),
          "items" -> Json.toJson(o.items)
        ).filterNot(_._2 == JsNull)
      )
    }
  }

}

case class Items(
                  subItem: Option[String],
                  dueDate: Option[String],
                  amount: Option[BigDecimal],
                  clearingDate: Option[String],
                  clearingReason: Option[String],
                  outgoingPaymentMethod: Option[String],
                  paymentLock: Option[String],
                  clearingLock: Option[String],
                  interestLock: Option[String],
                  dunningLock: Option[String],
                  returnFlag: Option[Boolean],
                  paymentReference: Option[String],
                  paymentAmount: Option[BigDecimal],
                  paymentMethod: Option[String],
                  paymentLot: Option[String],
                  paymentLotItem: Option[String],
                  clearingSAPDocument: Option[String],
                  codingInitiationDate: Option[String],
                  statisticalDocument: Option[String],
                  DDCollectionInProgress: Option[Boolean],
                  returnReason: Option[String],
                  promisetoPay: Option[String]
                )

object Items {
  implicit val format: OFormat[Items] = Json.format[Items]
}


case class FinancialDataResponse(taxPayerDetails: TaxPayerDetails,
                                 balanceDetails: BalanceDetails,
                                 codingDetails: Option[Seq[CodingDetails]],
                                 documentDetails: Seq[DocumentDetails],
                                 financialDetails: Seq[FinancialDetails])

object FinancialDataResponse {
  implicit val format: OFormat[FinancialDataResponse] = Json.format[FinancialDataResponse]
}

case class FinancialDataErrors(
                            failures: List[FinancialDataError]
                          )

object FinancialDataErrors {
  implicit val format: OFormat[FinancialDataErrors] = Json.format[FinancialDataErrors]
}

case class FinancialDataError(
                         code: String,
                         reason: String
                       )

object FinancialDataError {
  implicit val format: OFormat[FinancialDataError] = Json.format[FinancialDataError]
}