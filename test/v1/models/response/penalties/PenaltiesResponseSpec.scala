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

package v1.models.response.penalties

import play.api.libs.json.Json
import support.UnitSpec
import v1.constants.PenaltiesConstants._

class PenaltiesResponseSpec extends UnitSpec {

  "PenaltiesData" must {

    "write data to json min" in {

      Json.toJson(testPenaltiesResponseMin) shouldBe testPenaltiesResponseJsonMin
    }

    "read from json min" in {
      testPenaltiesResponseJsonMin.as[PenaltiesResponse] shouldBe testPenaltiesResponseMin
    }

    "write data to json max" in {

      Json.toJson(testPenaltiesResponseMax) shouldBe upstreamTestPenaltiesResponseJsonMax
    }

    "read from json max" in {
      downstreamTestPenaltiesResponseJsonMax.as[PenaltiesResponse] shouldBe testPenaltiesResponseMax
    }

    "write data to optional fields change json" in {
      Json.toJson(testPenaltiesResponseMissingFields) shouldBe upstreamTestPenaltiesResponseJsonMissingField
    }

    "read from optional fields change json" in {
      downstreamTestPenaltiesResponseJsonMissingFields.as[PenaltiesResponse] shouldBe testPenaltiesResponseMissingFields
    }
  }
}
