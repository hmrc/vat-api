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

package v1.connectors.httpparsers

import play.api.http.Status
import play.api.libs.json.{JsValue, Json}
import support.UnitSpec
import uk.gov.hmrc.http.HttpResponse
import v1.connectors.httpparsers.FinancialDataHIPHttpParser.FinancialDataHIPReads
import v1.connectors.httpparsers.FinancialDataHttpParser.FinancialDataHttpReads
import v1.constants.{FinancialDataConstants, FinancialDataHIPConstants, PenaltiesConstants}
import v1.models.errors._

class FinancialDataHIPHttpParserSpec extends UnitSpec {

  "FinancialDataHIPHttpParser" when {

    "FinancialDataHIPReads" when {

      "response is OK (200)" when {

        "json is valid" must {

          "return Right(FinancialDataHIPResponse) min" in {

            val result = FinancialDataHIPReads.read("", "",
              HttpResponse(
                status = Status.OK,
                json = FinancialDataHIPConstants.testDownstreamFinancialDetails,
                headers = Map(
                  "CorrelationId" -> Seq(FinancialDataHIPConstants.correlationId)
                )
              )
            )

            result shouldBe Right(FinancialDataHIPConstants.wrappedFinancialDataResponse())
          }

          "return Right(FinancialDataHIPResponse) max" in {

            val result = FinancialDataHIPReads.read("", "",
              HttpResponse(
                status = Status.OK,
                json = FinancialDataHIPConstants.testDownstreamFinancialDetails,
                headers = Map(
                  "CorrelationId" -> Seq(FinancialDataHIPConstants.correlationId)
                )
              )
            )

            result shouldBe Right(FinancialDataHIPConstants.wrappedFinancialDataResponse(FinancialDataHIPConstants.testFinancialDataResponse))
          }

          "return Right(FinancialDataHIPResponse) No Document Details" in {

            val result = FinancialDataHIPReads.read("", "",
              HttpResponse(
                status = Status.OK,
                json = FinancialDataHIPConstants.testDownstreamFinancialDetailsNoDocumentDetails,
                headers = Map(
                  "CorrelationId" -> Seq(FinancialDataHIPConstants.correlationId)
                )
              )
            )

            result shouldBe Right(FinancialDataHIPConstants.wrappedFinancialDataResponse(FinancialDataHIPConstants.testFinancialNoDocumentDetailsDataResponse))
          }
        }

        "json is invalid" must {

          "return Left(InvalidJson)" in {

            val jsonObject =
              Json.parse("""{
                           |  "success": {
                           |  "processingDate": "2025-05-06",
                           |    "financialData": {
                           |      "documentDetails": {
                           |        "test": "test"
                           |      }
                           |    }
                           |  }
                           |}
                           |""".stripMargin)

            val result = FinancialDataHIPReads.read("", "",
              HttpResponse(
                status = Status.OK,
                json = jsonObject,
                headers = Map(
                  "CorrelationId" -> Seq(FinancialDataHIPConstants.correlationId)
                )
              )
            )

            result shouldBe Left(FinancialDataHIPConstants.errorWrapper(InvalidJson))
          }
        }
      }

      "response is BAD_REQUEST (400) with Technical Error" must {
        "return Left(TechnicalError)" in {
          val errorJson = Json.parse(
            """
              |{
              |  "error": {
              |    "code": "TECH_ERR_CODE",
              |    "message": "Something technical failed",
              |    "logId": "ABC123XYZ"
              |  }
              |}
              |""".stripMargin)

          val response = HttpResponse(
            status = Status.BAD_REQUEST,
            json = errorJson,
            headers = Map("CorrelationId" -> Seq("abc123-789xyz"))
          )

          val result = FinancialDataHIPReads.read("POST", "/url", response)

          result shouldBe Left(
            ErrorWrapper(
              correlationId = "abc123-789xyz",
              error = MtdError("TECH_ERR_CODE", "Something technical failed")
            )
          )
        }
      }

      "response is BAD_REQUEST (400) with Business Errors" must {
        "return Left(BusinessErrors)" in {
          val errorJson = Json.parse(
            """
              |{
              |  "errors": [
              |    {
              |      "processingDate": "2024-05-01",
              |      "code": "BUS_ERR_001",
              |      "text": "Invalid request format"
              |    },
              |    {
              |      "processingDate": "2024-05-01",
              |      "code": "BUS_ERR_002",
              |      "text": "Missing taxpayer ID"
              |    }
              |  ]
              |}
              |""".stripMargin)

          val response = HttpResponse(
            status = Status.BAD_REQUEST,
            json = errorJson,
            headers = Map("CorrelationId" -> Seq("abc123-789xyz"))
          )

          val result = FinancialDataHIPReads.read("POST", "/url", response)

          result shouldBe Left(
            FinancialDataHIPConstants.errorWrapperMulti(
              Seq(
                MtdError("BUS_ERR_001", "Invalid request format"),
                MtdError("BUS_ERR_002", "Missing taxpayer ID")
              )
            )
          )
        }
      }
    }
  }
}
