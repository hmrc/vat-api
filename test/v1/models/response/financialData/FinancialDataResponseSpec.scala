/*
 * Copyright 2023 HM Revenue & Customs
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

import play.api.libs.json.Json
import support.UnitSpec
import v1.constants.FinancialDataConstants

class FinancialDataResponseSpec extends UnitSpec {

  "FinancialDataResponse" must {

    "write FinancialDataResponse model to json" in {
      Json.toJson(FinancialDataConstants.testFinancialDataResponse) shouldBe FinancialDataConstants.testUpstreamFinancialDetails
    }

    "read from IF json response" in {
      FinancialDataConstants.testDownstreamFinancialDetails.as[FinancialDataResponse] shouldBe FinancialDataConstants.testFinancialDataResponse
    }

    "read from HIP json response" in {
      FinancialDataConstants.hipFinancialDetails.as[FinancialDataResponse] shouldBe FinancialDataConstants.testFinancialDataResponse
    }
  }

  private val errorHipJson   = Json.parse("""
                                  |{
                                  |  "processingDate":"2017-01-01",
                                  |  "code":"002",
                                  |  "text":"Invalid Tax Regime"
                                  |}
                                  |""".stripMargin)
  private val errorsHipJson  = Json.parse(s"""
                                    |{
                                    |  "errors": $errorHipJson
                                    |}
                                    |""".stripMargin)
  private val errorHipModel  = FinancialDataErrorHIP("2017-01-01", "002", "Invalid Tax Regime")
  private val errorsHipModel = FinancialDataErrorsHIP(errorHipModel)

  "FinancialDataErrorHIP" must {
    "write FinancialDataErrorHIP model to json" in {
      Json.toJson(errorHipModel) shouldBe errorHipJson
    }

    "read from json response" in {
      errorHipJson.as[FinancialDataErrorHIP] shouldBe errorHipModel
    }
  }
  "FinancialDataErrorsHIP" must {
    "write FinancialDataErrorsHIP model to json" in {
      Json.toJson(errorsHipModel) shouldBe errorsHipJson
    }

    "read from json response" in {
      errorsHipJson.as[FinancialDataErrorsHIP] shouldBe errorsHipModel
    }
  }

}
