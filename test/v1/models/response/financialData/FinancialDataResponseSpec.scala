/*
 * Copyright 2025 HM Revenue & Customs
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

package v1.models.response.financialData

import play.api.libs.json.{ JsResultException, JsValue, Json }
import support.UnitSpec
import v1.constants.FinancialDataConstants
import v1.constants.FinancialDataConstants.totalisationAndDocumentDetailsJson

class FinancialDataResponseSpec extends UnitSpec {

  "FinancialDataResponse" must {

    "write FinancialDataResponse model to json" in {
      Json.toJson(FinancialDataConstants.testFinancialDataResponse) shouldBe FinancialDataConstants.testUpstreamFinancialDetails
    }

    "read from json response" in {
      FinancialDataConstants.financialDetails.as[FinancialDataResponse] shouldBe FinancialDataConstants.testFinancialDataResponse
    }

    "return an exception" when {
      "read from json response that does not have wrapper 'success.financialData'" in {
        val invalidResponse: JsValue = Json.parse(s"""{"succes":{"financialata":$totalisationAndDocumentDetailsJson}}""")

        intercept[JsResultException] {
          invalidResponse.as[FinancialDataResponse]
        }
      }
    }
  }

  private val errorJson   = Json.parse("""
                                  |{
                                  |  "processingDate":"2017-01-01",
                                  |  "code":"002",
                                  |  "text":"Invalid Tax Regime"
                                  |}
                                  |""".stripMargin)
  private val errorsJson  = Json.parse(s"""
                                    |{
                                    |  "errors": $errorJson
                                    |}
                                    |""".stripMargin)
  private val errorModel  = FinancialDataError("2017-01-01", "002", "Invalid Tax Regime")
  private val errorsModel = FinancialDataErrors(errorModel)

  "FinancialDataError" must {
    "write FinancialDataError model to json" in {
      Json.toJson(errorModel) shouldBe errorJson
    }

    "read from json response" in {
      errorJson.as[FinancialDataError] shouldBe errorModel
    }
  }

  "FinancialDataErrors" must {
    "write FinancialDataErrors model to json" in {
      Json.toJson(errorsModel) shouldBe errorsJson
    }

    "read from json response" in {
      errorsJson.as[FinancialDataErrors] shouldBe errorsModel
    }
  }

}
