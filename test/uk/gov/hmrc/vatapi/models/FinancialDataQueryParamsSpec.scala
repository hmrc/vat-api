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
import org.scalatest.TestData
import org.scalatestplus.play.OneAppPerTest
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import uk.gov.hmrc.vatapi.UnitSpec

class FinancialDataQueryParamsSpec extends UnitSpec with OneAppPerTest {

  val testTime: LocalDate = LocalDate.now()

  implicit override def newAppForTest(testData: TestData): Application =
    new GuiceApplicationBuilder().configure(Map("Test.mtd-date" -> testTime.minusYears(10).toString)).build()

  "DateRangeQueryParams" should {

    "return an object when all the date range is within 1 year" in {
      val response = FinancialDataQueryParams.from(Some(Right(testTime.minusYears(1).toString)), Some(Right(testTime.minusDays(1).toString)))
      response.isRight shouldBe true
      response.right.get.from shouldEqual LocalDate.parse(testTime.minusYears(1).toString)
      response.right.get.to shouldEqual LocalDate.parse(testTime.minusDays(1).toString)
    }

    "return an error when the date range is greater than one year" in {
      val response = FinancialDataQueryParams.from(Some(Right(testTime.minusYears(1).toString)), Some(Right(testTime.toString)))
      response.isLeft shouldBe true
      response.left.get shouldBe "INVALID_DATE_RANGE"
    }

    "return error when the from date query parameter is missing" in {
      val response = FinancialDataQueryParams.from(None, Some(Right("2019-03-31")))
      response.isLeft shouldBe true
      response.left.get shouldBe "INVALID_DATE_FROM"
    }

    "return error when the from date query parameter is not a valid date" in {
      val response = FinancialDataQueryParams.from(Some(Right("ABC")), Some(Right("2019-03-31")))
      response.isLeft shouldBe true
      response.left.get shouldBe "INVALID_DATE_FROM"
    }

    "return error when the from date query parameter is before mtd-date in Config" in {

      val response = FinancialDataQueryParams.from(Some(Right(testTime.minusYears(11).toString)), Some(Right(testTime.minusYears(10).toString)))
      response.isLeft shouldBe true
      response.left.get shouldBe "INVALID_DATE_FROM"
    }

    "return error when the to date query parameter is missing" in {
      val response = FinancialDataQueryParams.from(Some(Right("2019-03-31")), None)
      response.isLeft shouldBe true
      response.left.get shouldBe "INVALID_DATE_TO"
    }

    "return error when the to date query parameter is not a valid date" in {
      val response = FinancialDataQueryParams.from(Some(Right("2019-03-31")), Some(Right("ABC")))
      response.isLeft shouldBe true
      response.left.get shouldBe "INVALID_DATE_TO"
    }

    "return error when the to date query parameter is a future date" in {
      val futureDate = LocalDate.now().plusDays(1)

      val response = FinancialDataQueryParams.from(Some(Right("2019-03-31")), Some(Right(futureDate.toString("yyyy-MM-dd"))))
      response.isLeft shouldBe true
      response.left.get shouldBe "INVALID_DATE_TO"
    }

    "return error when from date is after to date " in {
      val response = FinancialDataQueryParams.from(Some(Right(testTime.minusYears(3).toString)), Some(Right(testTime.minusYears(3).minusDays(1).toString)))
      response.isLeft shouldBe true
      response.left.get shouldBe "INVALID_DATE_RANGE"
    }
  }


}
