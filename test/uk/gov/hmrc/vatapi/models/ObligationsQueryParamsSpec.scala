/*
 * Copyright 2019 HM Revenue & Customs
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
      response.isRight shouldBe true
      response.right.get.from shouldEqual Some(LocalDate.parse("2017-01-01"))
    }

    "return error when the status query parameter is not a valid status" in {
      val response = ObligationsQueryParams.from(Some(Right("2017-01-01")), Some(Right("2017-03-31")), Some(Right("C")))
      response.isLeft shouldBe true
      response.left.get shouldBe "INVALID_STATUS"
    }

    "return error when the status query parameter is A" in {
      val response = ObligationsQueryParams.from(Some(Right("2017-01-01")), Some(Right("2017-03-31")), Some(Right("A")))
      response.isLeft shouldBe true
      response.left.get shouldBe "INVALID_STATUS"
    }

    "return error when from date is after to date " in {
      val response = ObligationsQueryParams.from(Some(Right("2017-01-02")), Some(Right("2017-01-01")), Some(Right("F")))
      response.isLeft shouldBe true
      response.left.get shouldBe "INVALID_DATE_RANGE"
    }

    "return success when the date range is single day" in {
      val response = ObligationsQueryParams.from(Some(Right("2017-01-01")), Some(Right("2017-01-01")), Some(Right("F")))
      response.isRight shouldBe true
      response.right.get shouldBe ObligationsQueryParams(Some(LocalDate.parse("2017-01-01")), Some(LocalDate.parse("2017-01-01")), Some("F"))
    }

    "return error when the date range is more than 365 days" in {
      val response = ObligationsQueryParams.from(Some(Right("2017-01-01")), Some(Right("2018-01-01")), Some(Right("F")))
      response.isLeft shouldBe true
      response.left.get shouldBe "INVALID_DATE_RANGE"
    }

    "return success when the date range is equal to 365 days" in {
      val response = ObligationsQueryParams.from(Some(Right("2017-01-01")), Some(Right("2017-12-31")), Some(Right("F")))
      response.isRight shouldBe true
      response.right.get shouldBe ObligationsQueryParams(Some(LocalDate.parse("2017-01-01")), Some(LocalDate.parse("2017-12-31")), Some("F"))
    }

    "return error when the date range is more than 366 days and it is a leap year" in {
      val response = ObligationsQueryParams.from(Some(Right("2020-01-01")), Some(Right("2021-01-01")), Some(Right("F")))
      response.isLeft shouldBe true
      response.left.get shouldBe "INVALID_DATE_RANGE"
    }

    "return success when the date range is equal to 366 days and it is a leap year" in {
      val response = ObligationsQueryParams.from(Some(Right("2020-01-01")), Some(Right("2020-12-31")), Some(Right("F")))
      response.isRight shouldBe true
      response.right.get shouldBe ObligationsQueryParams(Some(LocalDate.parse("2020-01-01")), Some(LocalDate.parse("2020-12-31")), Some("F"))
    }

    "return an object when all the query params are valid" in {
      val response = ObligationsQueryParams.from(Some(Right("2017-01-01")), Some(Right("2017-03-31")), Some(Right("F")))
      response.isRight shouldBe true
      response.right.get.from shouldEqual Some(LocalDate.parse("2017-01-01"))
    }
  }

}
