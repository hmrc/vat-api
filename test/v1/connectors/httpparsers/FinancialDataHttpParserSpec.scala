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

import play.api.http.Status.{ BAD_REQUEST, CREATED, FORBIDDEN, OK }
import play.api.libs.json.{ JsValue, Json }
import support.UnitSpec
import uk.gov.hmrc.http.HttpResponse
import v1.connectors.httpparsers.FinancialDataHttpParser.FinancialDataHttpReads
import v1.constants.FinancialDataConstants
import v1.constants.FinancialDataConstants._
import v1.constants.PenaltiesConstants.errorWrapper
import v1.models.errors._
import v1.models.outcomes.ResponseWrapper
import v1.models.response.financialData.FinancialDataResponse

class FinancialDataHttpParserSpec extends UnitSpec {

  private def buildHttpResponse(status: Int, jsonBody: JsValue) =
    HttpResponse(
      status = status,
      json = jsonBody,
      headers = Map("CorrelationId" -> Seq(FinancialDataConstants.correlationId))
    )

  "FinancialDataHttpParser .read" when {
    Seq(OK, CREATED).foreach { successResponse =>
      s"API response is a $successResponse" when {
        "json is valid" must {
          "return a partial FinancialDataResponse model when json has no Totalisations" in {
            val response = buildHttpResponse(successResponse, hipFinancialDetailsNoTotalisations)
            val result   = FinancialDataHttpReads.read("", "", response)

            val expectedModel  = FinancialDataResponse(totalisations = None, Some(Seq(testDocumentDetail)))
            val expectedResult = ResponseWrapper(correlationId, expectedModel)

            result shouldBe Right(expectedResult)
          }

          "return a partial FinancialDataResponse model when json has no DocumentDetails" in {
            val response = buildHttpResponse(successResponse, hipFinancialDetailsNoDocumentDetails)
            val result   = FinancialDataHttpReads.read("", "", response)

            val expectedModel  = FinancialDataResponse(Some(testTotalisation), documentDetails = None)
            val expectedResult = ResponseWrapper(correlationId, expectedModel)

            result shouldBe Right(expectedResult)
          }

          "return a full FinancialDataResponse model" in {
            val response = buildHttpResponse(successResponse, testDownstreamFinancialDetails)
            val result   = FinancialDataHttpReads.read("", "", response)

            val expectedModel  = FinancialDataResponse(Some(testTotalisation), Some(Seq(testDocumentDetail)))
            val expectedResult = ResponseWrapper(correlationId, expectedModel)

            result shouldBe Right(expectedResult)
          }
        }

        "json is invalid" must {
          "return Left(InvalidJson)" in {
            val invalidJsonObject =
              Json.parse("""{
                  |  "success": {
                  |    "financialData": {
                  |      "documentDetails": {
                  |        "test": "test"
                  |      }
                  |    }
                  |  }
                  |}
                  |""".stripMargin)
            val response = buildHttpResponse(successResponse, invalidJsonObject)

            val result = FinancialDataHttpReads.read("", "", response)

            result shouldBe Left(errorWrapper(InvalidJson))
          }
        }
      }
    }

    "response is FORBIDDEN (403)" must {
      "return an UnauthorisedError" in {
        val response =
          HttpResponse(status = FORBIDDEN, body = "403 - Forbidden", headers = Map("CorrelationId" -> Seq(correlationId)))
        val result = FinancialDataHttpReads.read("", "", response)

        result shouldBe Left(errorWrapper(MtdError("CLIENT_OR_AGENT_NOT_AUTHORISED", "The client and/or agent is not authorised")))
      }
    }

    "API response is an error (any non 200/201/403 status)" must {
      "return the error in an ErrorWrapper (appropriate error handled by .errorHelper)" in {
        val error = Json.parse("""
            |{
            |  "errors": {
            |   "processingDate":"2017-01-01",
            |   "code":"002",
            |   "text":"Invalid Tax Regime"
            |  }
            |}
            |""".stripMargin)

        val errorResponse = buildHttpResponse(BAD_REQUEST, error)
        val result        = FinancialDataHttpReads.read("", "", errorResponse)

        result shouldBe Left(ErrorWrapper(correlationId, DownstreamError))
      }
    }
  }

  "FinancialDataHttpParser .errorHelper" when {
    "the error response matches the FinancialDataErrorsHIP format" must {
      def buildErrorHip(code: String, text: Option[String] = None): JsValue = {
        val errorText = text.getOrElse("This is the error message")
        Json.parse(s"""
             |{
             |  "errors": {
             |    "processingDate":"2017-01-01",
             |    "code":"$code",
             |    "text":"$errorText"
             |  }
             |}
             |""".stripMargin)
      }

      "return a DownstreamError" when {
        Seq("002", "003", "015", "019", "020").foreach { errorCode =>
          s"error code is $errorCode" in {
            val result = FinancialDataHttpReads.errorHelper(buildErrorHip(errorCode))

            result shouldBe DownstreamError
          }
        }
        "error code is '017' and error text is 'Invalid Search Type'" in {
          val result = FinancialDataHttpReads.errorHelper(buildErrorHip("017", Some("Invalid Search Type")))

          result shouldBe DownstreamError
        }
      }

      "return a FinancialInvalidIdNumber" when {
        "error code is '016'" in {
          val result = FinancialDataHttpReads.errorHelper(buildErrorHip("016"))

          result shouldBe FinancialInvalidIdNumber
        }
      }

      "return a FinancialInvalidSearchItem" when {
        "error code is '017' and error text is NOT 'Invalid Search Type'" in {
          val result = FinancialDataHttpReads.errorHelper(buildErrorHip("017", Some("Invalid Charge Reference")))

          result shouldBe FinancialInvalidSearchItem
        }
      }

      "return a FinancialNotDataFound" when {
        "error code is '018'" in {
          val result = FinancialDataHttpReads.errorHelper(buildErrorHip("018"))

          result shouldBe FinancialNotDataFound
        }
      }

      "return a MtdError with error code and text" when {
        "error code is not recognised" in {
          val result = FinancialDataHttpReads.errorHelper(buildErrorHip("111", Some("Unknown error message")))

          result shouldBe MtdError("111", "Unknown error message")
        }
      }
    }

    "the error response does not match the FinancialDataErrorsHIP format" must {
      "return a MtdError explaining service was unable to validate json error response" in {
        val errorWithInvalidJsonFormat = Json.parse(s"""
             |{
             |  "processingDate":"2017-01-01",
             |  "code":"ErrorCode",
             |  "text":"ErrorText"
             |}
             |""".stripMargin)

        val result = FinancialDataHttpReads.errorHelper(errorWithInvalidJsonFormat)

        result shouldBe MtdError(
          "SERVER_ERROR",
          "Unable to validate json error response with errors: List((/errors,List(JsonValidationError(List(error.path.missing),List()))))",
          Some(errorWithInvalidJsonFormat)
        )
      }
    }
  }

}
