/*
 * Copyright 2017 HM Revenue & Customs
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

import play.api.libs.json.Format
import uk.gov.hmrc.vatapi.models.{EnumJson, des}

object PaymentIndicator extends Enumeration {
  type PaymentIndicator = Value

  val DirectDebit: des.PaymentIndicator.Value = Value("DD")
  val Bank: des.PaymentIndicator.Value = Value("BANK")

  implicit val format: Format[PaymentIndicator] =
    EnumJson.enumFormat(PaymentIndicator, Some("PaymentIndicator should be one of: DD, BANK"))

}

