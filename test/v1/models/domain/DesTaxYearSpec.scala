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

package v1.models.domain

import support.UnitSpec

class DesTaxYearSpec extends UnitSpec {

  "DesTaxYear" when {
    "toString" should {
      "produce the correct String" in {
        DesTaxYear("2019").toString shouldBe "2019"
      }
    }

    "fromMtd" should {
      "produce the correct String" in {
        DesTaxYear.fromMtd("2019-20") shouldBe DesTaxYear("2020")
      }
    }

    "fromDesIntToString" should {
      "produce the correct String" in {
        DesTaxYear.fromDesIntToString(2019) shouldBe "2018-19"
      }
    }
  }

}
