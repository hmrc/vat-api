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

import play.api.libs.functional.syntax._
import play.api.libs.json._
import utils.FinancialDataReadsUtils
import v1.models.response.common.TaxPeriod
import v1.models.response.payments.PaymentsResponse.Payment

case class PaymentsResponse(payments: Seq[Payment])

object PaymentsResponse extends FinancialDataReadsUtils {

  private val unsupportedChargeTypes: Seq[String] = Seq("payment on account")

  implicit val writes: Writes[PaymentsResponse] = (paymentsResponse: PaymentsResponse) =>
    Json.obj("payments" -> {
      for {
        payment <- paymentsResponse.payments
        item <- payment.paymentItems.getOrElse(Seq.empty[PaymentItem])
      } yield Json.toJson(item)
    })

  implicit def reads(implicit to: String): Reads[PaymentsResponse] =
    (JsPath \ "financialTransactions")
      .read(filterNotArrayReads[Payment]("chargeType", unsupportedChargeTypes))
      .map(_.filter(payment => dateCheck(payment.taxPeriod, to) && itemsCheck(payment.paymentItems)))
      .map(PaymentsResponse(_))

  //intermediary case class for reading in data before filtering
  case class Payment(taxPeriod: Option[TaxPeriod],
                     `type`: String,
                     paymentItems: Option[Seq[PaymentItem]])

  object Payment {

    implicit val reads: Reads[Payment] = (
      TaxPeriod.reads and
        (JsPath \ "chargeType").read[String] and
        (JsPath \ "items").readNullable[Seq[PaymentItem]].map(filterNotEmpty)
      ) (Payment.apply _)

    def filterNotEmpty(items: Option[Seq[PaymentItem]]): Option[Seq[PaymentItem]] = {
      items.map(_.filterNot(_ == PaymentItem.empty)) match {
        case None => None
        case Some(Nil) => None
        case nonEmpty => nonEmpty
      }
    }
  }
}
