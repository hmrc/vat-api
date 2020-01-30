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

package uk.gov.hmrc.vatapi.models.des

import org.joda.time.DateTime
import play.api.libs.json._
import play.api.libs.functional.syntax._
import uk.gov.hmrc.vatapi.models.des.PaymentIndicator.PaymentIndicator

case class VatReturnsDES(processingDate: DateTime,
                         paymentIndicator: Option[PaymentIndicator],
                         formBundleNumber: String,
                         chargeRefNumber: Option[String])

object VatReturnsDES {
  implicit val reads: Reads[VatReturnsDES] =
    (
      ((__ \ "processingDate").read[DateTime](uk.gov.hmrc.vatapi.models.dateTimeFormat) or
        (__ \ "processingDate").read[DateTime](uk.gov.hmrc.vatapi.models.defaultDateTimeFormat)) and
        (__ \ "paymentIndicator").readNullable[PaymentIndicator] and
        (__ \ "formBundleNumber").read[String] and
        (__ \ "chargeRefNumber").readNullable[String]

      )(VatReturnsDES.apply _)

  implicit val dateFormats: Format[DateTime] = uk.gov.hmrc.vatapi.models.dateTimeFormat
  implicit val writes: Writes[VatReturnsDES] = Json.writes[VatReturnsDES]
}
