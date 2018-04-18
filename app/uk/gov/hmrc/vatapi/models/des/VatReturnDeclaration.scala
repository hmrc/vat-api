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
import org.joda.time.format.ISODateTimeFormat
import org.json4s.JsonAST._
import org.json4s.native.Serialization
import org.json4s.{CustomSerializer, DefaultFormats}
import uk.gov.hmrc.vatapi.models.Amount

object VatReturnDeclaration {
}

case class VatReturnDeclaration(
                                 periodKey: String,
                                 vatDueSales: Amount,
                                 vatDueAcquisitions: Amount,
                                 vatDueTotal: Amount,
                                 vatReclaimedCurrPeriod: Amount,
                                 vatDueNet: Amount,
                                 totalValueSalesExVAT: Amount,
                                 totalValuePurchasesExVAT: Amount,
                                 totalValueGoodsSuppliedExVAT: Amount,
                                 totalAllAcquisitionsExVAT: Amount,
                                 agentReferenceNumber: Option[String] = None,
                                 receivedAt: DateTime
                               ) {
  def toJsonString: String = {
    implicit val formats = DefaultFormats ++ Seq(BigDecimalSerializer) ++ Seq(JodaSerializer)
    Serialization.write(this)
  }
}



private object BigDecimalSerializer extends CustomSerializer[Amount](format => ({
    case jde: JDecimal => jde.num
  },
  {
    case bd : Amount => JDecimal(bd.setScale(2))
  }
))

private object JodaSerializer extends CustomSerializer[DateTime](format => ({
    case js: JString => DateTime.parse(js.s)
  },
  {
    case dt: DateTime => {
      val fmt = ISODateTimeFormat.dateTime()
      JString(dt.toString(fmt))
    }
  }
))
