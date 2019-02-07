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

package uk.gov.hmrc.vatapi.models

import org.joda.time.LocalDate
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
          desFinancialData.financialTransactions.filter(_.chargeType != "Payment on account").filter(_.chargeType != "Hybrid Payments")
            .map { l =>
              val period =
                if (l.taxPeriodFrom.nonEmpty && l.taxPeriodTo.nonEmpty)
                  Some(TaxPeriod(LocalDate.parse(l.taxPeriodFrom.get), LocalDate.parse(l.taxPeriodTo.get)))
                else None

              val dueDate = l.items.map(_.exists(_.dueDate.nonEmpty)) match {
                case Some(result) if result => Some(LocalDate.parse(l.items.get.head.dueDate.get))
                case _ => None
              }

              Liability(
                taxPeriod = period,
                `type` = l.chargeType,
                originalAmount = l.originalAmount.get,
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
            override val msg: String = s"[Liabilities] Unable to parse the Json from DES model: $ex"
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
    def from(desFinancialData: des.FinancialData): Either[DesTransformError, Payments] = {
      Try {

        val payments = desFinancialData.financialTransactions.filter(_.chargeType != "Payment on account").collect {
          case ft if ft.items.nonEmpty =>
            ft.items.get.collect {
              case paymentItem if paymentItem.paymentAmount.nonEmpty =>
                val period =
                  if (ft.taxPeriodFrom.nonEmpty && ft.taxPeriodTo.nonEmpty)
                    Some(TaxPeriod(LocalDate.parse(ft.taxPeriodFrom.get), LocalDate.parse(ft.taxPeriodTo.get)))
                  else None

                val payment = Payment(
                  amount = paymentItem.paymentAmount.get,
                  received = paymentItem.clearingDate.map(LocalDate.parse)
                )
                payment.taxPeriod = period
                payment
            }
        }
        Payments(payments.flatten)
      }
    } match {
      case obj: Success[Payments] =>
        Right(obj.value)
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
