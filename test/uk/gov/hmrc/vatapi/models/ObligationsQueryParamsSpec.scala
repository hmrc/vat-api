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

package uk.gov.hmrc.vatapi.models

import org.joda.time.LocalDate
import play.api.test.{FakeRequest, Helpers}
import uk.gov.hmrc.vatapi.UnitSpec

class ObligationsQueryParamsSpec extends UnitSpec {

  "ObligationsQueryParams" should {
    "return error when the fromDate query parameter is missing" in {
      val request = FakeRequest(Helpers.GET, "/obligations?toDate=2017-03-31&status=A")
      ObligationsQueryParams.from(request).isLeft shouldBe true
    }

    "return error when the fromDate query parameter is not a valid date" in {
      val request = FakeRequest(Helpers.GET, "/obligations?fromDate=ABC&toDate=2017-03-31&status=A")
      ObligationsQueryParams.from(request).isLeft  shouldBe true
    }

    "return error when the toDate query parameter is missing" in {
      val request = FakeRequest(Helpers.GET, "/obligations?fromDate=2017-03-31&status=A")
      ObligationsQueryParams.from(request).isLeft shouldBe true
    }

    "return error when the toDate query parameter is not a valid date" in {
      val request = FakeRequest(Helpers.GET, "/obligations?toDate=ABC&status=A")
      ObligationsQueryParams.from(request).isLeft  shouldBe true
    }

    "return error when the status query parameter is missing" in {
      val request = FakeRequest(Helpers.GET, "/obligations?fromDate=2017-01-01&toDate=2017-03-31")
      ObligationsQueryParams.from(request).isLeft shouldBe true
    }

    "return error when the status query parameter is not a valid status" in {
      val request = FakeRequest(Helpers.GET, "/obligations?fromDate=2017-01-01&toDate=2017-03-31&status=X")
      ObligationsQueryParams.from(request).isLeft  shouldBe true
    }

    "return error when fromDate is after toDate" in {
      val request = FakeRequest(Helpers.GET, "/obligations?fromDate=2017-01-02&toDate=2017-01-01&status=A")
      ObligationsQueryParams.from(request).isLeft  shouldBe true
    }


    "return error when the date range is not more than 365 days" in {
      val request = FakeRequest(Helpers.GET, "/obligations?fromDate=2017-01-01&toDate=2018-01-02&status=A")
      ObligationsQueryParams.from(request).isLeft  shouldBe true
    }

    "return an object when all the query params are valid" in {
      val request = FakeRequest(Helpers.GET, "/obligations?fromDate=2017-01-01&toDate=2017-03-31&status=A")
      ObligationsQueryParams.from(request).isRight  shouldBe true
      ObligationsQueryParams.from(request).right.get.fromDate  shouldEqual  LocalDate.parse("2017-01-01")
    }
  }

}
