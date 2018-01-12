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

package uk.gov.hmrc.vatapi.models

import org.joda.time.LocalDate
import uk.gov.hmrc.vatapi.UnitSpec

class ObligationsQueryParamsSpec extends UnitSpec {

  "ObligationsQueryParams" should {
    "return error when the from date query parameter is missing" in {
      val response = ObligationsQueryParams.from(None, Some(Right("2017-03-31")), Some(Right("A")))
      response.isLeft shouldBe true
      response.left.get shouldBe "INVALID_DATE_FROM"
    }

    "return error when the from date query parameter is not a valid date" in {
      val response = ObligationsQueryParams.from(Some(Right("ABC")), Some(Right("2017-03-31")), Some(Right("O")))
      response.isLeft shouldBe true
      response.left.get shouldBe "INVALID_DATE_FROM"
    }

    "return error when the to date query parameter is missing" in {
      val response = ObligationsQueryParams.from(Some(Right("2017-03-31")), None, Some(Right("O")))
      response.isLeft shouldBe true
      response.left.get shouldBe "INVALID_DATE_TO"
    }

    "return error when the to date query parameter is not a valid date" in {
      val response = ObligationsQueryParams.from(Some(Right("2017-03-31")), Some(Right("ABC")), Some(Right("C")))
      response.isLeft shouldBe true
      response.left.get shouldBe "INVALID_DATE_TO"
    }

    "return error when the status query parameter is missing" in {
      val response = ObligationsQueryParams.from(Some(Right("2017-01-01")), Some(Right("2017-03-31")), None)
      response.isLeft shouldBe true
      response.left.get shouldBe "INVALID_STATUS"
    }

    "return error when the status query parameter is not a valid status" in {
      val response = ObligationsQueryParams.from(Some(Right("2017-01-01")), Some(Right("2017-03-31")), Some(Right("C")))
      response.isLeft shouldBe true
      response.left.get shouldBe "INVALID_STATUS"
    }

    "return error when from date is after to date " in {
      val response = ObligationsQueryParams.from(Some(Right("2017-01-02")), Some(Right("2017-01-01")), Some(Right("F")))
      response.isLeft shouldBe true
      response.left.get shouldBe "INVALID_DATE_RANGE"
    }


    "return error when the date range is more than 365 days" in {
      val response = ObligationsQueryParams.from(Some(Right("2017-01-01")), Some(Right("2018-01-02")), Some(Right("F")))
      response.isLeft shouldBe true
      response.left.get shouldBe "INVALID_DATE_RANGE"
    }

    "return an object when all the query params are valid" in {
      val response = ObligationsQueryParams.from(Some(Right("2017-01-01")), Some(Right("2017-03-31")), Some(Right("F")))
      response.isRight shouldBe true
      response.right.get.from shouldEqual LocalDate.parse("2017-01-01")
    }
  }

}
