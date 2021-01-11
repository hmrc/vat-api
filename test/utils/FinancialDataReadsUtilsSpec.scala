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

package utils

import java.time.LocalDate

import play.api.libs.json.{JsError, JsPath, Json, Reads}
import support.UnitSpec
import v1.models.response.common.TaxPeriod

class FinancialDataReadsUtilsSpec extends UnitSpec with FinancialDataReadsUtils {

  case class TestItem(aField: String)

  object TestItem {
    implicit val reads: Reads[TestItem] = Json.reads[TestItem]
  }

  case class TestWrapper(items: Seq[TestItem])

  object TestWrapper{
    implicit val reads: Reads[TestWrapper] =
      (JsPath \ "array")
        .read(filterNotArrayReads[TestItem]("aField", Seq("notavalue")))
        .map(TestWrapper(_))
  }

  "dateCheck" when {
    val toDate: LocalDate = LocalDate.parse("2019-01-01")

    "a valid date is supplied" should {
      "return 'true'" in {
        dateCheck(Some(TaxPeriod("", "2019-01-01")), toDate) shouldBe true
      }
    }

    "an invalid date is supplied" should {
      "return 'false'" in {
        dateCheck(Some(TaxPeriod("", "2019-01-02")), toDate) shouldBe false
      }
    }

    "no date is supplied" should {
      "return 'true'" in {
        dateCheck(None, toDate) shouldBe true
      }
    }
  }

  "filterNotArrayReads" when {
    "read from valid JSON" should {
      "produce the expected object" in {

        val json = Json.parse(
          """
            |{
            |   "array": [
            |      {
            |      "aField": "aValue"
            |      },
            |      {
            |      "aField": "notAValue"
            |      }
            |   ]
            |}
          """.stripMargin
        )

        json.as[TestWrapper] shouldBe TestWrapper(Seq(TestItem("aValue")))
      }
    }

    "read from JSON with an empty array of items" should {
      "produce an empty sequence of objects" in {

        val json = Json.parse(
          """
            |{
            |   "array": [
            |   ]
            |}
          """.stripMargin
        )

        json.as[TestWrapper] shouldBe TestWrapper(Seq.empty[TestItem])
      }
    }

    "read from invalid JSON" should {
      "produce a JsError when mandatory fields are missing" in {

        val json = Json.parse(
          """
            |{
            |   "array": [
            |      {
            |      "notAField": "aValue"
            |      },
            |      {
            |      "aField": "notAValue"
            |      }
            |   ]
            |}
          """.stripMargin
        )

        json.validate[TestWrapper] shouldBe a[JsError]
      }

      "produce a JsError when array is missing" in {

        val json = Json.parse(
          """
            |{
            |   "notArray": []
            |}
          """.stripMargin
        )

        json.validate[TestWrapper] shouldBe a[JsError]
      }
    }
  }
}
