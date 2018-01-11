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

import org.joda.time.{DateTime, DateTimeZone}
import play.api.libs.json.{Json, Format}
import uk.gov.hmrc.vatapi.models
import uk.gov.hmrc.vatapi.models.Amount
import uk.gov.hmrc.vatapi.models.dateTimeFormat

case class VatReturn(periodKey: String,
                     inboundCorrespondenceFromDate: String,
                     inboundCorrespondenceToDate: String,
                     vatDueSales: Amount,
                     vatDueAcquisitions: Amount,
                     vatDueTotal: Amount,
                     vatReclaimedCurrPeriod: Amount,
                     vatDueNet: Amount,
                     totalValueSalesExVAT: Amount,
                     totalValuePurchasesExVAT: Amount,
                     totalValueGoodsSuppliedExVAT: Amount,
                     totalAcquisitionsExVAT: Amount,
                     agentReferenceNumber: Option[String] = None,
                     receivedAt: DateTime)

object VatReturn {
  implicit val reads: Format[VatReturn] = Json.format[VatReturn]
}

case class VatReturns(vatReturns: Seq[VatReturn])

object VatReturns {
  implicit val format: Format[VatReturns] = Json.format[VatReturns]
}
