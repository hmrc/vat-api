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

package uk.gov.hmrc.vatapi.models

import org.joda.time.LocalDate
import play.api.Logger
import play.api.libs.json._

import scala.util.{Failure, Success, Try}

sealed trait FinancialData

case class Liabilities(liabilities: Seq[Liability]) extends FinancialData

object Liabilities {
  implicit val format: OFormat[Liabilities] = Json.format[Liabilities]

  implicit val from = new DesTransformValidator[des.FinancialData, Liabilities] {
    def from(desFinancialData: des.FinancialData) = {
      Try {
        Liabilities(
          desFinancialData.financialTransactions.map { l =>
            val period =
              if (l.taxPeriodFrom.nonEmpty && l.taxPeriodTo.nonEmpty)
                Some(TaxPeriod(LocalDate.parse(l.taxPeriodFrom.get), LocalDate.parse(l.taxPeriodTo.get)))
              else None
            val dueDate = if (l.items.exists(_.dueDate.nonEmpty)) Some(LocalDate.parse(l.items.head.dueDate.get)) else None
            Liability(
              taxPeriod = period,
              `type` = l.chargeType,
              originalAmount = l.originalAmount,
              outstandingAmount = l.outstandingAmount,
              due = dueDate
            )
          }
        )
      } match {
        case Success(obj) =>
          Right(obj)
        case Failure(ex) =>
          Left(new DesTransformError {
            override val msg: String = s"[Liabilities] Unable to parse the Json from DES model"
          })
      }
    }
  }

}

case class Liability(
                      taxPeriod: Option[TaxPeriod] = None,
                      `type`: String,
                      originalAmount: Amount,
                      outstandingAmount: Option[Amount] = None,
                      due: Option[LocalDate] = None
                    )

object Liability {
  implicit val format: OFormat[Liability] = Json.format[Liability]
}

case class TaxPeriod(from: LocalDate, to: LocalDate)

object TaxPeriod {
  implicit val format: OFormat[TaxPeriod] = Json.format[TaxPeriod]
}

case class Payments(payments: Seq[Payment]) extends FinancialData

object Payments {
  implicit val format: OFormat[Payments] = Json.format[Payments]

  implicit val from = new DesTransformValidator[des.FinancialData, Payments] {
    def from(desFinancialData: des.FinancialData) = {
      Try { Payments(
        desFinancialData.financialTransactions.filter(ft =>
          ft.items.map(_.paymentAmount.isDefined).reduce(_&&_)
        ).flatMap { liability =>
          val period =
            if (liability.taxPeriodFrom.nonEmpty && liability.taxPeriodTo.nonEmpty)
              Some(TaxPeriod(LocalDate.parse(liability.taxPeriodFrom.get), LocalDate.parse(liability.taxPeriodTo.get)))
            else None
          liability.items.map { it =>
            val receivedDate = if (it.clearingDate.nonEmpty) Some(LocalDate.parse(it.clearingDate.get)) else None
            val payment = Payment(
              amount = it.paymentAmount.get,
              received = receivedDate
            )
            payment.taxPeriod = period
            payment
          }
        }
      )} } match {
      case Success(obj) =>
        Right(obj)
      case Failure(ex) =>
        Left(new DesTransformError {
          override val msg: String = s"[Payments] Unable to parse the Json from DES model"
        })
    }
  }
}

case class Payment(amount: Amount, received: Option[LocalDate] = None) {
  var taxPeriod: Option[TaxPeriod] = None
}

object Payment {
  implicit val format: OFormat[Payment] = Json.format[Payment]
}