/*
 * Copyright 2022 HM Revenue & Customs
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
import v1.constants.PenaltiesConstants

class PenaltiesResponseSpec extends UnitSpec {

  "PenaltiesResponse" must {

    "read data from json" in {

      Json.toJson(PenaltiesConstants.testPenaltiesResponse) shouldBe PenaltiesConstants.testPenaltiesResponseJson
    }

    "write to json" in {

      PenaltiesConstants.testPenaltiesResponseJson.as[PenaltiesResponse] shouldBe PenaltiesConstants.testPenaltiesResponse
    }
  }
}
