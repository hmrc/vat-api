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

package uk.gov.hmrc.vatapi.utils

import uk.gov.hmrc.vatapi.UnitSpec
import ImplicitCurrencyFormatter._

class ImplicitCurrencyFormatterSpec extends UnitSpec {

  "ImplicitCurrencyFormatter" should {

    "correctly convert BigDecimal 10 to String '10.00'" in {
      val amountOne: BigDecimal = 10
      amountOne.toDesCurrency shouldBe "10.00"
    }

    "correctly convert BigDecimal 20.0 to String '20.00'" in {
      val amountTwo: BigDecimal = 20.0
      amountTwo.toDesCurrency shouldBe "20.00"
    }

    "correctly convert BigDecimal 30.00 to String '30.00'" in {
      val amountThree: BigDecimal = 30.00
      amountThree.toDesCurrency shouldBe "30.00"
    }

  }
}
