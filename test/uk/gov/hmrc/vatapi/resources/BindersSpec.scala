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

package uk.gov.hmrc.vatapi.resources

import uk.gov.hmrc.vatapi.UnitSpec

class BindersSpec extends UnitSpec {

  "vrnBinder.bind" should {

    "return Right when provided with a vrn passes both domain and DES validation" in {
      val vrn = generateVrn

      val result = Binders.vrnBinder.bind("vrn", vrn.vrn)
      result shouldEqual Right(vrn)
    }

    "return Left for a NINO that fails domain validation" in {
      val result = Binders.vrnBinder.bind("vrn", "12345678")
      result shouldEqual Left("ERROR_VRN_INVALID")
    }
  }

}
