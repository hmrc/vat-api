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

package uk.gov.hmrc.vatapi.resources

import org.joda.time.LocalDate
import uk.gov.hmrc.vatapi.UnitSpec
import uk.gov.hmrc.vatapi.models.ObligationsQueryParams

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

  "obligationsQueryParamsBinder.bind" should {

    val validFromDate = LocalDate.parse("2018-04-01")
    val validToDate = LocalDate.parse("2019-03-31")

    "return Right when valid from/to supplied and status of O is provided" in {

      val queryParamsMap: Map[String, Seq[String]] = Map(
        "from" -> Seq("2018-04-01"),
        "to" -> Seq("2019-03-31"),
        "status" -> Seq("O")
      )

      val result = Binders.obligationsQueryParamsBinder.bind("", queryParamsMap)
      result shouldEqual Some(
        Right(
          ObligationsQueryParams(
            Some(validFromDate),
            Some(validToDate),
            status = Some("O")
          )
        )
      )
    }

    "return Right when valid from/to supplied and status of F is provided" in {

      val queryParamsMap: Map[String, Seq[String]] = Map(
        "from" -> Seq("2018-04-01"),
        "to" -> Seq("2019-03-31"),
        "status" -> Seq("F")
      )

      val result = Binders.obligationsQueryParamsBinder.bind("", queryParamsMap)
      result shouldEqual Some(
        Right(
          ObligationsQueryParams(
            Some(validFromDate),
            Some(validToDate),
            status = Some("F")
          )
        )
      )
    }

    "return Right when valid from/to supplied and no status is provided" in {

      val queryParamsMap: Map[String, Seq[String]] = Map(
        "from" -> Seq("2018-04-01"),
        "to" -> Seq("2019-03-31")
      )

      val result = Binders.obligationsQueryParamsBinder.bind("", queryParamsMap)
      result shouldEqual Some(
        Right(
          ObligationsQueryParams(
            Some(validFromDate),
            Some(validToDate),
            None
          )
        )
      )
    }

    "return Right when no from/to supplied and status of O is supplied" in {

      val queryParamsMap: Map[String, Seq[String]] = Map(
        "status" -> Seq("O")
      )

      val result = Binders.obligationsQueryParamsBinder.bind("", queryParamsMap)
      result shouldEqual Some(
        Right(
          ObligationsQueryParams(
            None,
            None,
            status = Some("O")
          )
        )
      )
    }

    "return Left when from is supplied but to is not supplied" in {

      val queryParamsMap: Map[String, Seq[String]] = Map(
        "from" -> Seq("2018-04-01"),
        "status" -> Seq("O")
      )

      val result = Binders.obligationsQueryParamsBinder.bind("", queryParamsMap)
      result shouldEqual Some(
        Left("INVALID_DATE_TO")
      )
    }

    "return Left when to is supplied but from is not supplied" in {

      val queryParamsMap: Map[String, Seq[String]] = Map(
        "to" -> Seq("2019-03-31"),
        "status" -> Seq("O")
      )

      val result = Binders.obligationsQueryParamsBinder.bind("", queryParamsMap)
      result shouldEqual Some(
        Left("INVALID_DATE_FROM")
      )
    }

    "return Left when no from/to date is supplied but status F is supplied" in {

      val queryParamsMap: Map[String, Seq[String]] = Map(
        "status" -> Seq("F")
      )

      val result = Binders.obligationsQueryParamsBinder.bind("", queryParamsMap)
      result shouldEqual Some(
        Left("MISSING_DATE_RANGE")
      )
    }

    "return Left when no from/to date is supplied with incorrect status of G" in {

      val queryParamsMap: Map[String, Seq[String]] = Map(
        "status" -> Seq("G")
      )

      val result = Binders.obligationsQueryParamsBinder.bind("", queryParamsMap)
      result shouldEqual Some(
        Left("INVALID_STATUS")
      )
    }

    "return Left when no from/to/status are supplied" in {

      val queryParamsMap: Map[String, Seq[String]] = Map(
      )

      val result = Binders.obligationsQueryParamsBinder.bind("", queryParamsMap)
      result shouldEqual Some(
        Left("INVALID_DATE_FROM")
      )
    }


  }

  "obligationsQueryParamsBinder.queryString" should {

    val fromDateString = "2018-04-01"
    val toDateString = "2019-03-31"
    val statusStringO = "O"
    val validFromDate = LocalDate.parse(fromDateString)
    val validToDate = LocalDate.parse(toDateString)

    "create a valid url when all parameters are supplied" in {
      val obligationsQueryParams = ObligationsQueryParams(Some(validFromDate), Some(validToDate), Some(statusStringO))
      val query = obligationsQueryParams.queryString

      query shouldBe s"from=$fromDateString&to=$toDateString&status=$statusStringO"
    }

    "create a valid url when from/to parameters are supplied but status is not" in {
      val obligationsQueryParams = ObligationsQueryParams(Some(validFromDate), Some(validToDate), None)
      val query = obligationsQueryParams.queryString

      query shouldBe s"from=$fromDateString&to=$toDateString"
    }

    "create a valid url when from/to parameters are not supplied but status is " in {
      val obligationsQueryParams = ObligationsQueryParams(None, None, Some("O"))
      val query = obligationsQueryParams.queryString

      query shouldBe s"status=O"
    }



  }

}
