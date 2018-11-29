/*
 * Copyright 2018 HM Revenue & Customs
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

package uk.gov.hmrc.vatapi.support

import uk.gov.hmrc.vatapi.models.des
import uk.gov.hmrc.vatapi.models.des.{FinancialItem, FinancialTransaction}


object FinancialData {

  import FinancialTransaction._
  import FinancialItem._

  val testFinancialData: des.FinancialData = des.FinancialData(
    idType = Some("MTDSA"),
    idNumber = Some("XQIT00000000001"),
    regimeType = Some("ITSA"),
    processingDate = "2017-03-07T09:30:00.000Z",
    financialTransactions = Seq(testLiabilityFinancialTransaction, testPaymentFinancialTransaction)
  )

  val noLiabilities: des.FinancialData = testFinancialData.copy(financialTransactions = Seq())
  val justPayments: des.FinancialData = testFinancialData.copy(financialTransactions = Seq(testPaymentFinancialTransaction))
  val minimumPaymentData: des.FinancialData = testFinancialData.copy(financialTransactions = Seq(testMinPaymentFinancialTransaction))
  val justLiabilities: des.FinancialData = testFinancialData.copy(financialTransactions = Seq(testLiabilityFinancialTransaction))
  val minimumLiabilityData: des.FinancialData = testFinancialData.copy(financialTransactions = Seq(testMinLiabilityFinancialTransaction))


  val badLiabilityModel: des.FinancialData = testFinancialData.copy(financialTransactions = Seq(testLiabilityFinancialTransaction.copy(taxPeriodFrom = Some(""))))
  val badPaymentModel: des.FinancialData = testFinancialData.copy(
    financialTransactions = Seq(testPaymentFinancialTransaction.copy(
      items = Some(Seq(testPaymentItem.copy(clearingDate = Some("")))))
    )
  )

  object FinancialTransaction {

    val testPaymentFinancialTransaction: FinancialTransaction = des.FinancialTransaction(
      chargeType = "VAT",
      mainType = Some("2100"),
      periodKey = Some("13RL"),
      periodKeyDescription = Some("abcde"),
      taxPeriodFrom = Some("1967-08-13"),
      taxPeriodTo = Some("1967-08-14"),
      businessPartner = Some("6622334455"),
      contractAccountCategory = Some("02"),
      contractAccount = Some("X"),
      contractObjectType = Some("ABCD"),
      contractObject = Some("00000003000000002757"),
      sapDocumentNumber = Some(""),
      sapDocumentNumberItem = Some(""),
      chargeReference = Some(""),
      mainTransaction = Some(""),
      subTransaction = Some(""),
      originalAmount = Some(10000),
      outstandingAmount = Some(10000),
      clearedAmount = Some(1.0),
      accruedInterest = Some(1.0),
      items = Some(Seq(testPaymentItem))
    )

    val testMinPaymentFinancialTransaction = des.FinancialTransaction(
      chargeType = "VAT",
      originalAmount = Some(10000),
      items = Some(Seq(testMinPaymentItem))
    )

    val testLiabilityFinancialTransaction: FinancialTransaction = testPaymentFinancialTransaction.copy(
      taxPeriodFrom = Some("1977-08-13"),
      taxPeriodTo = Some("1977-08-14"),
      items = Some(Seq(testLiabilityItem)))

    val testMinLiabilityFinancialTransaction: FinancialTransaction = des.FinancialTransaction(
      chargeType = "VAT",
      originalAmount = Some(10000),
      items = Some(Seq(testMinLiabilityItem))
    )
  }

  object FinancialItem {

    val testPaymentItem: FinancialItem = des.FinancialItem(
      subItem = Some("001"),
      dueDate = Some("1967-08-13"),
      amount= Some(10000.00),
      clearingDate = Some("1967-08-13"),
      clearingReason = Some("01"),
      outgoingPaymentMethod = None,
      paymentLock = None,
      clearingLock = None,
      interestLock = None,
      dunningLock = None,
      returnFlag = Some(true),
      paymentReference = None,
      paymentAmount = Some(10000),
      paymentMethod = Some("A"),
      paymentLot = Some("081203010024"),
      paymentLotItem = Some("000001"),
      clearingSAPDocument = Some("3350000253"),
      statisticalDocument = Some("A")
    )

    val testMinPaymentItem = des.FinancialItem(
      paymentAmount = Some(10000)
    )
    val testLiabilityItem: FinancialItem = des.FinancialItem(
      subItem = Some("001"),
      dueDate = Some("1967-08-13"),
      amount= Some(10000.00),
      clearingDate = None,
      clearingReason = None,
      outgoingPaymentMethod = None,
      paymentLock = None,
      clearingLock = None,
      interestLock = None,
      dunningLock = None,
      returnFlag = None,
      paymentReference = None,
      paymentAmount = None,
      paymentMethod = None,
      paymentLot = None,
      paymentLotItem = None,
      clearingSAPDocument = None,
      statisticalDocument = None
    )

    val testMinLiabilityItem = des.FinancialItem()

  }


}
