/*
 * Copyright 2020 HM Revenue & Customs
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

package v1.models.response.submit

import org.joda.time.DateTime
import play.api.libs.json.Json
import support.UnitSpec

class SubmitResponseSpec extends UnitSpec {

  val minmiumDesJson = Json.parse(
    """
      |{
      | "processingDate": "2018-01-16T08:20:27.895Z",
      | "formBundleNumber": "256660290587"
      |}
      |""".stripMargin)

  val maximiumDesJson = Json.parse(
    """
      |{
      | "processingDate": "2018-01-16T08:20:27.895Z",
      | "paymentIndicator": "BANK",
      | "formBundleNumber": "256660290587",
      | "chargeRefNumber": "aCxFaNx0FZsCvyWF"
      |}
      |""".stripMargin)

  val noMilliDesJson = Json.parse(
    """
      |{
      | "processingDate": "2018-01-16T08:20:27.89Z",
      | "formBundleNumber": "256660290587"
      |}
      |""".stripMargin)

  val minimiumMtdJson = Json.parse(
    """
      |{
      | "processingDate": "2018-01-16T08:20:27.895Z",
      | "formBundleNumber": "256660290587"
      |}
      |""".stripMargin)

  val maximiumMtdJson = Json.parse(
    """
      |{
      |  "processingDate": "2018-01-16T08:20:27.895Z",
      |  "paymentIndicator": "BANK",
      |  "formBundleNumber": "256660290587",
      |  "chargeRefNumber": "aCxFaNx0FZsCvyWF"
      |}
      |""".stripMargin)

  val noMilliMtdJson = Json.parse(
    """
      |{
      | "processingDate": "2018-01-16T08:20:27.890Z",
      | "formBundleNumber": "256660290587"
      |}
      |""".stripMargin)

  val maxSubmitResponseModel: SubmitResponse = SubmitResponse(processingDate = new DateTime("2018-01-16T08:20:27.895+0000"),
    paymentIndicator = Some("BANK"),
    formBundleNumber = "256660290587",
    chargeRefNumber = Some("aCxFaNx0FZsCvyWF"))

  val minSubmitResponseModel: SubmitResponse = SubmitResponse(processingDate = new DateTime("2018-01-16T08:20:27.895+0000"),
    formBundleNumber = "256660290587", None , None)

  val noMilliSubmitResponseModel: SubmitResponse = SubmitResponse(processingDate = new DateTime("2018-01-16T08:20:27.89Z"),
    formBundleNumber = "256660290587", None , None)


  "Submit Response" should {
    "return a SubmitResponse model" when {
      "minimal data fields are provided" in {

        minmiumDesJson.as[SubmitResponse] shouldBe minSubmitResponseModel
      }

      "all data fields are provided" in {

        maximiumDesJson.as[SubmitResponse] shouldBe maxSubmitResponseModel
      }

      "the processing date is provided without milliseconds" in {

        noMilliDesJson.as[SubmitResponse] shouldBe noMilliSubmitResponseModel
      }
    }

    "write to json" when {
      "a full model is provided" in {

        Json.toJson(maxSubmitResponseModel) shouldBe maximiumMtdJson
      }

      "a partial model is provided" in {

        Json.toJson(minSubmitResponseModel) shouldBe minimiumMtdJson
      }

      "the processing date has been provided without milliseconds" in {
        Json.toJson(noMilliSubmitResponseModel) shouldBe noMilliMtdJson

      }
    }
  }
}
