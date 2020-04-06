/*
 * Copyright 2020 HM Revenue & Customs
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

package v1.models.response.payments

import java.time.LocalDate

import play.api.libs.json._
import v1.models.response.common.TaxPeriod

case class PaymentsResponse(payments: Seq[Payment])

object PaymentsResponse {

  implicit val writes: Writes[PaymentsResponse] = (paymentsResponse: PaymentsResponse) => Json.obj("payments" -> {
    for (
      payment <- paymentsResponse.payments
      if payment.paymentItem.isDefined;
      item <- payment.paymentItem.get
    ) yield Json.obj(
      "amount" -> item.amount,
      "received" -> item.received
    )
  }
  )

  //retrieve all transactions, filter out any particular payments, then return a model only if there's data
  implicit def reads(implicit to: String): Reads[PaymentsResponse] = {
    (JsPath \ "financialTransactions").read[Seq[Payment]].map { payments =>
      payments.filter { payment =>
        paymentCheck(payment) && dateCheck(payment.taxPeriod, to)
      }
    }
  }.filter(_.nonEmpty).map(PaymentsResponse(_))

  //filter particular payments
  private def paymentCheck(payment: Payment) = {
    val paymentType = payment.`type`.toLowerCase
    paymentType != "payment on account"
  }

  //filter the payments that have response to date beyond the request to date
  private def dateCheck(taxPeriod: Option[TaxPeriod], requestToDate: String) = {
    val toDate = taxPeriod.fold(None: Option[LocalDate]){l => Some(LocalDate.parse(l.to))}
    toDate.fold(true){ desTo => desTo.compareTo(LocalDate.parse(requestToDate)) <= 0
    }
  }



}
