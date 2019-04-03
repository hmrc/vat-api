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
import org.scalatest.TestData
import org.scalatestplus.play.guice.GuiceOneAppPerTest
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import uk.gov.hmrc.vatapi.UnitSpec

class FinancialDataQueryParamsSpec extends UnitSpec with GuiceOneAppPerTest {

  val testTime: LocalDate = LocalDate.now()

  implicit override def newAppForTest(testData: TestData): Application =
    new GuiceApplicationBuilder().configure(Map(s"Test.mtd-date" -> testTime.minusYears(10).toString)).build()

  "DateRangeQueryParams" should {

    "return an object when all the date range is within 1 year" in {
      val response = FinancialDataQueryParams.from(Some(Right(testTime.minusYears(1).toString)), Some(Right(testTime.minusDays(1).toString)))
      response.isRight shouldBe true
      response.right.get.from shouldEqual LocalDate.parse(testTime.minusYears(1).toString)
      response.right.get.to shouldEqual LocalDate.parse(testTime.minusDays(1).toString)
    }

    "return error when the from date query parameter is missing" in {
      val response = FinancialDataQueryParams.from(None, Some(Right("2019-03-31")))
      response.isLeft shouldBe true
      response.left.get shouldBe "DATE_FROM_INVALID"
    }

    "return error when the from date query parameter is not a valid date" in {
      val response = FinancialDataQueryParams.from(Some(Right("ABC")), Some(Right("2019-03-31")))
      response.isLeft shouldBe true
      response.left.get shouldBe "DATE_FROM_INVALID"
    }

    "return error when the from date query parameter is before mtd-date in Config" in {
      val from = FinancialDataQueryParams.minDate.minusDays(1)
      val to = FinancialDataQueryParams.minDate

      val response = FinancialDataQueryParams.from(Some(Right(from.toString)), Some(Right(to.toString)))
      response.isLeft shouldBe true
      response.left.get shouldBe "DATE_FROM_INVALID"
    }

    "return success when the from date query parameter is the mtd-date in Config" in {
      val from = FinancialDataQueryParams.minDate
      val to = FinancialDataQueryParams.minDate.plusDays(1)

      val response = FinancialDataQueryParams.from(Some(Right(from.toString)), Some(Right(to.toString)))
      response.isRight shouldBe true
      response.right.get.from shouldBe from
      response.right.get.to shouldBe to
    }

    "return error when the to date query parameter is missing" in {
      val response = FinancialDataQueryParams.from(Some(Right("2019-03-31")), None)
      response.isLeft shouldBe true
      response.left.get shouldBe "DATE_TO_INVALID"
    }

    "return error when the to date query parameter is not a valid date" in {
      val response = FinancialDataQueryParams.from(Some(Right("2019-03-31")), Some(Right("ABC")))
      response.isLeft shouldBe true
      response.left.get shouldBe "DATE_TO_INVALID"
    }

    "return success when the to date query parameter is the current date" in {
      val from = LocalDate.now().minusDays(1)
      val to = LocalDate.now()

      val response = FinancialDataQueryParams.from(Some(Right(from.toString)), Some(Right(to.toString)))
      response.isRight shouldBe true
      response.right.get.from shouldBe from
      response.right.get.to shouldBe to
    }

    "return error when the to date query parameter is a future date" in {
      val from = LocalDate.now()
      val to  = LocalDate.now().plusDays(1)

      val response = FinancialDataQueryParams.from(Some(Right(from.toString)), Some(Right(to.toString)))
      response.isLeft shouldBe true
      response.left.get shouldBe "DATE_TO_INVALID"
    }

    "return error when from date is after to date" in {
      val now = LocalDate.parse("2018-01-01")
      val response = FinancialDataQueryParams.from(now, Some(Right("2017-01-02")), Some(Right("2017-01-01")))
      response.isLeft shouldBe true
      response.left.get shouldBe "DATE_RANGE_INVALID"
    }

    "return success when the date range is single day" in {
      val now = LocalDate.parse("2018-01-01")
      val response = FinancialDataQueryParams.from(now, Some(Right("2017-01-01")), Some(Right("2017-01-01")))
      response.isRight shouldBe true
      response.right.get.from shouldBe LocalDate.parse("2017-01-01")
      response.right.get.to shouldBe LocalDate.parse("2017-01-01")
    }

    "return error when the date range is more than 365 days" in {
      val now = LocalDate.parse("2018-01-01")
      val response = FinancialDataQueryParams.from(now, Some(Right("2017-01-01")), Some(Right("2018-01-01")))
      response.isLeft shouldBe true
      response.left.get shouldBe "DATE_RANGE_INVALID"
    }

    "return success when the date range is equal to 365 days" in {
      val now = LocalDate.parse("2018-01-01")
      val response = FinancialDataQueryParams.from(now, Some(Right("2017-01-01")), Some(Right("2017-12-31")))
      response.isRight shouldBe true
      response.right.get.from shouldBe LocalDate.parse("2017-01-01")
      response.right.get.to shouldBe LocalDate.parse("2017-12-31")
    }

    "return error when the date range is more than 366 days and it is a leap year" in {
      val now = LocalDate.parse("2021-01-01")
      val response = FinancialDataQueryParams.from(now, Some(Right("2020-01-01")), Some(Right("2021-01-01")))
      response.isLeft shouldBe true
      response.left.get shouldBe "DATE_RANGE_INVALID"
    }

    "return success when the date range is equal to 366 days and it is a leap year" in {
      val now = LocalDate.parse("2021-01-01")
      val response = FinancialDataQueryParams.from(now, Some(Right("2020-01-01")), Some(Right("2020-12-31")))
      response.isRight shouldBe true
      response.right.get.from shouldBe LocalDate.parse("2020-01-01")
      response.right.get.to shouldBe LocalDate.parse("2020-12-31")
    }
  }


}
