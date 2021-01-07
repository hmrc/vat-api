/*
 * Copyright 2021 HM Revenue & Customs
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

package definition

import play.api.http.HeaderNames.ACCEPT
import play.api.test.FakeRequest
import support.UnitSpec
import uk.gov.hmrc.http.HeaderCarrier

class VersionSpec extends UnitSpec {

  "Versions" when {
    "retrieved from header carrier" must {
      "return None when 'Accept' header missing" in {
        Versions.getFromRequest(HeaderCarrier()) shouldBe None
      }
      "return None when 'Accept' header does not contain a version" in {
        Versions.getFromRequest(HeaderCarrier().withExtraHeaders((ACCEPT, "application/json"))) shouldBe None
      }
      "return the version when 'Accept' header contains the version" in {
        Versions.getFromRequest(HeaderCarrier().withExtraHeaders((ACCEPT, "application/vnd.hmrc.1.0+json"))) shouldBe Some("1.0")
      }
    }

    "retrieved from a request header" must {
      "work" in {
        Versions.getFromRequest(FakeRequest().withHeaders((ACCEPT, "application/vnd.hmrc.1.0+json"))) shouldBe Some("1.0")
      }
    }
  }

}
