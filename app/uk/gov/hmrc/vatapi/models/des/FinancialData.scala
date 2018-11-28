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

package uk.gov.hmrc.vatapi.models.des

import org.joda.time.DateTime
import play.api.libs.json.{Json, OFormat}
import uk.gov.hmrc.vatapi.models.Amount

case class FinancialData(
                          idType: Option[String] = None,
                          idNumber: Option[String] = None,
                          regimeType: Option[String] = None,
                          processingDate: String,
                          financialTransactions: Seq[FinancialTransaction]
                        ) {

}

object FinancialData {
  implicit val financialTransactionFormat: OFormat[FinancialTransaction] = FinancialTransaction.format
  implicit val format: OFormat[FinancialData] = Json.format[FinancialData]
}

case class FinancialTransaction(
                                  chargeType: String,
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
                                  originalAmount: Amount,
                                  outstandingAmount: Option[Amount] = None,
                                  clearedAmount: Option[Amount] = None,
                                  accruedInterest: Option[Amount] = None,
                                  items: Option[Seq[FinancialItem]] = None
                                )

object FinancialTransaction {
  implicit val itemFormat: OFormat[FinancialItem] = FinancialItem.format
  implicit val format: OFormat[FinancialTransaction] = Json.format[FinancialTransaction]
}

case class FinancialItem(
                          subItem: Option[String] = None,
                          dueDate: Option[String] = None,
                          amount: Option[Amount] = None,
                          clearingDate: Option[String] = None,
                          clearingReason: Option[String] = None,
                          outgoingPaymentMethod: Option[String] = None,
                          paymentLock: Option[String] = None,
                          clearingLock: Option[String] = None,
                          interestLock: Option[String] = None,
                          dunningLock: Option[String] = None,
                          returnFlag: Option[Boolean] = None,
                          paymentReference: Option[String] = None,
                          paymentAmount: Option[Amount] = None,
                          paymentMethod: Option[String] = None,
                          paymentLot: Option[String] = None,
                          paymentLotItem: Option[String] = None,
                          clearingSAPDocument: Option[String] = None,
                          statisticalDocument: Option[String] = None
                        )

object FinancialItem {
  implicit val format: OFormat[FinancialItem] = Json.format[FinancialItem]
}