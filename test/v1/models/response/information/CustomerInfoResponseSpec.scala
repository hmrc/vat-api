/*
 * Copyright 2023 HM Revenue & Customs
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

package v1.models.response.information

import play.api.libs.json.Json
import support.UnitSpec
import v1.constants.CustomerInfoConstants._

class CustomerInfoResponseSpec extends UnitSpec {
  "CustomerRawData" must {
    "write data to json min" in {
      Json.toJson(testCustomerInfoResponseMin) shouldBe emptyjson
    }
    "read from json min" in {
      testCustomerInfoResponseMinJson.as[CustomerInfoResponse] shouldBe testCustomerInfoResponseMin
    }
    "read from json max" in {
      downstreamTestustomerInfoResponseJsonMax.as[CustomerInfoResponse] shouldBe testCustomerInfoResponseMin
    }
  }
}
