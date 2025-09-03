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
import play.api.http.Status.{ BAD_REQUEST, CREATED, OK }
import play.api.libs.json.{ JsValue, Json }
import support.UnitSpec
import uk.gov.hmrc.http.HttpResponse
import v1.connectors.httpparsers.FinancialDataHttpParser.FinancialDataHttpReads
import v1.constants.FinancialDataConstants._
import v1.constants.{ FinancialDataConstants, PenaltiesConstants }
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
    "API response is from IF" when {
      Seq(OK, CREATED).foreach { successResponse =>
        s"response is a $successResponse" when {
          "json is valid" must {
            "return Right(FinancialDataResponse) min" in {

              val result = FinancialDataHttpReads.read(
                "",
                "",
                HttpResponse(
                  status = successResponse,
                  json = FinancialDataConstants.testDownstreamFinancialDetails,
                  headers = Map(
                    "CorrelationId" -> Seq(FinancialDataConstants.correlationId)
                  )
                )
              )

              result shouldBe Right(FinancialDataConstants.wrappedFinancialDataResponse())
            }

            "return Right(FinancialDataResponse) max" in {

              val result = FinancialDataHttpReads.read(
                "",
                "",
                HttpResponse(
                  status = successResponse,
                  json = FinancialDataConstants.testDownstreamFinancialDetails,
                  headers = Map(
                    "CorrelationId" -> Seq(FinancialDataConstants.correlationId)
                  )
                )
              )

              result shouldBe Right(FinancialDataConstants.wrappedFinancialDataResponse(FinancialDataConstants.testFinancialDataResponse))
            }

            "return Right(FinancialDataResponse) No Document Details" in {

              val result = FinancialDataHttpReads.read(
                "",
                "",
                HttpResponse(
                  status = successResponse,
                  json = FinancialDataConstants.testDownstreamFinancialDetailsNoDocumentDetails,
                  headers = Map(
                    "CorrelationId" -> Seq(FinancialDataConstants.correlationId)
                  )
                )
              )

              result shouldBe Right(
                FinancialDataConstants.wrappedFinancialDataResponse(FinancialDataConstants.testFinancialNoDocumentDetailsDataResponse))
            }
          }

          "json is invalid" must {
            "return Left(InvalidJson)" in {

              val jsonObject =
                Json.parse("""{
                    |  "getFinancialData": {
                    |    "financialDetails": {
                    |      "documentDetails": {
                    |        "test": "test"
                    |      }
                    |    }
                    |  }
                    |}
                    |""".stripMargin)

              val result = FinancialDataHttpReads.read("",
                                                       "",
                                                       HttpResponse(
                                                         status = successResponse,
                                                         json = jsonObject,
                                                         headers = Map(
                                                           "CorrelationId" -> Seq(FinancialDataConstants.correlationId)
                                                         )
                                                       ))

              result shouldBe Left(FinancialDataConstants.errorWrapper(InvalidJson))
            }
          }
        }
      }

      "response is BAD_REQUEST (400)" must {

        "return Left(InvalidVrn)" in {

          val error = Json.parse("""
              |{
              |"failures": [{
              |"code":"INVALID_IDNUMBER",
              |"reason":"Submission has not passed validation. Invalid parameter idNumber."
              |}]
              |}
              |""".stripMargin)

          val result = FinancialDataHttpReads.read("",
                                                   "",
                                                   HttpResponse(
                                                     status = Status.BAD_REQUEST,
                                                     json = error,
                                                     headers = Map(
                                                       "CorrelationId" -> Seq(FinancialDataConstants.correlationId)
                                                     )
                                                   ))
          result shouldBe Left(FinancialDataConstants.errorWrapper(MtdError("VRN_INVALID", "The provided VRN is invalid")))
        }
      }

      "response is NOT_FOUND (404)" must {

        "return Left(VrnNotFound)" in {

          val error = Json.parse("""
              |{
              |"failures": [{
              |"code":"NO_DATA_FOUND",
              |"reason":"Submission has not passed validation. Invalid parameter idType."
              |}]
              |}
              |""".stripMargin)

          val result = FinancialDataHttpReads.read("",
                                                   "",
                                                   HttpResponse(
                                                     status = Status.NOT_FOUND,
                                                     json = error,
                                                     headers = Map(
                                                       "CorrelationId" -> Seq(FinancialDataConstants.correlationId)
                                                     )
                                                   ))
          result shouldBe Left(FinancialDataConstants.errorWrapper(FinancialNotDataFound))
        }
      }

      "response is INTERNAL_SERVER_ERROR (500)" must {

        "return Left(UnexpectedFailure)" in {

          val status = Status.INTERNAL_SERVER_ERROR

          val error = Json.parse("""
              |{
              |"failures": [{
              |"code":"INTERNAL_SERVICE_ERROR",
              |"reason":"Something went wrong"
              |}]
              |}
              |""".stripMargin)

          val result = FinancialDataHttpReads.read("",
                                                   "",
                                                   HttpResponse(
                                                     status = status,
                                                     json = error,
                                                     headers = Map(
                                                       "CorrelationId" -> Seq(PenaltiesConstants.correlationId)
                                                     )
                                                   ))
          result shouldBe Left(FinancialDataConstants.errorWrapper(MtdError("INTERNAL_SERVICE_ERROR", "Something went wrong")))
        }
      }
    }

    "API response is from HIP" when {
      Seq(OK, CREATED).foreach { successResponse =>
        s"response is a $successResponse" when {
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

      "response is an error (any non 200/201 status)" must {
        "return an error in an ErrorWrapper (appropriate error handled by .errorHelper)" in {
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
  }

  "FinancialDataHttpParser .errorHelper" when {
    "the error response matches the FinancialDataErrorsIF format" must {

      def jsonString(code: String): JsValue = {
        Json.parse(s"""
             |{
             | "failures": [{
             | "code":"$code",
             | "reason":"Some reason"
             | }]
             | }
             |""".stripMargin)
      }

      def multiJsonString(): JsValue = {
        Json.parse(s"""
             |{
             |  "failures": [
             |    {
             |      "code":"INVALID_IDNUMBER",
             |      "reason":"Some reason"
             |    },
             |    {
             |      "code":"INVALID_DOC_NUMBER_OR_CHARGE_REFERENCE_NUMBER",
             |      "reason":"Some reason"
             |    }
             |  ]
             |}
             |""".stripMargin)
      }

      def multiJsonStringServerError(): JsValue = {
        Json.parse(s"""
             |{
             |  "failures": [
             |    {
             |      "code":"INVALID_IDNUMBER",
             |      "reason":"Some reason"
             |    },
             |    {
             |      "code":"SERVER_ERROR",
             |      "reason":"Some reason"
             |    }
             |  ]
             |}
             |""".stripMargin)
      }

      "return multi errors when passed multiple errors bad " in {
        val result = FinancialDataHttpReads.errorHelper(multiJsonString())
        result shouldBe MtdError("INVALID_REQUEST",
                                 "Invalid request financial details",
                                 Some(
                                   Json.toJson(
                                     Seq(
                                       FinancialInvalidIdNumber,
                                       FinancialInvalidSearchItem
                                     ))))
      }

      "return INTERNAL_SERVER_ERROR when passed multiple errors including one which maps to INTERNAL_SERVER_ERROR" in {
        val result = FinancialDataHttpReads.errorHelper(multiJsonStringServerError())
        result shouldBe DownstreamError
      }

      "return INTERNAL_SERVER_ERROR when passed SERVER_ERROR json" in {
        val result = FinancialDataHttpReads.errorHelper(jsonString("SERVER_ERROR"))
        result shouldBe DownstreamError
      }

      "return INTERNAL_SERVER_ERROR when passed SERVICE_UNAVAILABLE json" in {
        val result = FinancialDataHttpReads.errorHelper(jsonString("SERVICE_UNAVAILABLE"))
        result shouldBe DownstreamError
      }

      "return INTERNAL_SERVER_ERROR when passed REQUEST_NOT_PROCESSED json" in {
        val result = FinancialDataHttpReads.errorHelper(jsonString("REQUEST_NOT_PROCESSED"))
        result shouldBe DownstreamError
      }

      "return CHARGE_REFERENCE_INVALID when passed INVALID_DOC_NUMBER_OR_CHARGE_REFERENCE_NUMBER json" in {
        val result = FinancialDataHttpReads.errorHelper(jsonString("INVALID_DOC_NUMBER_OR_CHARGE_REFERENCE_NUMBER"))
        result shouldBe FinancialInvalidSearchItem
      }

      "return INTERNAL_SERVER_ERROR when passed INVALID_REGIME_TYPE json" in {
        val result = FinancialDataHttpReads.errorHelper(jsonString("INVALID_REGIME_TYPE"))
        result shouldBe DownstreamError
      }

      "return INTERNAL_SERVER_ERROR when passed INVALID_ID json" in {
        val result = FinancialDataHttpReads.errorHelper(jsonString("INVALID_ID"))
        result shouldBe DownstreamError
      }

      "return INTERNAL_SERVER_ERROR when passed INVALID_IDTYPE json" in {
        val result = FinancialDataHttpReads.errorHelper(jsonString("INVALID_IDTYPE"))
        result shouldBe DownstreamError
      }

      "return INTERNAL_SERVER_ERROR when passed DUPLICATE_SUBMISSION json" in {
        val result = FinancialDataHttpReads.errorHelper(jsonString("DUPLICATE_SUBMISSION"))
        result shouldBe DownstreamError
      }

      "return FinancialNotDataFound when passed NO_DATA_FOUND json" in {
        val result = FinancialDataHttpReads.errorHelper(jsonString("NO_DATA_FOUND"))
        result shouldBe FinancialNotDataFound
      }

      "return INTERNAL_SERVER_ERROR when passed INVALID_CORRELATIONID json" in {
        val result = FinancialDataHttpReads.errorHelper(jsonString("INVALID_CORRELATIONID"))
        result shouldBe DownstreamError
      }

      "return INTERNAL_SERVER_ERROR when passed INVALID_ADD_LOCK_INFORMATION json" in {
        val result = FinancialDataHttpReads.errorHelper(jsonString("INVALID_ADD_LOCK_INFORMATION"))
        result shouldBe DownstreamError
      }

      "return INTERNAL_SERVER_ERROR when passed INVALID_ADD_ACCRUING_INTEREST_DETAILS json" in {
        val result = FinancialDataHttpReads.errorHelper(jsonString("INVALID_ADD_ACCRUING_INTEREST_DETAILS"))
        result shouldBe DownstreamError
      }

      "return INTERNAL_SERVER_ERROR when passed INVALID_DATE_FROM json" in {
        val result = FinancialDataHttpReads.errorHelper(jsonString("INVALID_DATE_FROM"))
        result shouldBe DownstreamError
      }

      "return INTERNAL_SERVER_ERROR when passed INVALID_DATE_TO json" in {
        val result = FinancialDataHttpReads.errorHelper(jsonString("INVALID_DATE_TO"))
        result shouldBe DownstreamError
      }

      "return INTERNAL_SERVER_ERROR when passed INVALID_DATE_RANGE json" in {
        val result = FinancialDataHttpReads.errorHelper(jsonString("INVALID_DATE_RANGE"))
        result shouldBe DownstreamError
      }

      "return INTERNAL_SERVER_ERROR when passed INVALID_INCLUDE_PAYMENT_ON_ACCOUNT json" in {
        val result = FinancialDataHttpReads.errorHelper(jsonString("INVALID_INCLUDE_PAYMENT_ON_ACCOUNT"))
        result shouldBe DownstreamError
      }

      "return INTERNAL_SERVER_ERROR when passed INVALID_INCLUDE_STATISTICAL_ITEMS json" in {
        val result = FinancialDataHttpReads.errorHelper(jsonString("INVALID_INCLUDE_STATISTICAL_ITEMS"))
        result shouldBe DownstreamError
      }

      "return CHARGE_REFERENCE_INVALID when passed INVALID_SEARCH_ITEM json" in {
        val result = FinancialDataHttpReads.errorHelper(jsonString("INVALID_SEARCH_ITEM"))
        result shouldBe FinancialInvalidSearchItem
      }

      "return INTERNAL_SERVER_ERROR when passed INVALID_REQUEST json" in {
        val result = FinancialDataHttpReads.errorHelper(jsonString("INVALID_REQUEST"))
        result shouldBe DownstreamError
      }
    }

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

    "the error response matches neither FinancialDataErrorsIF nor FinancialDataErrorsHIP formats" must {
      "return a MtdError explaining service was unable to validate json error response" in {
        val errorWithInvalidJsonFormat = Json.parse(s"""
             |{
             |  "processingDate":"2017-01-01",
             |  "code":"ErrorCode",
             |  "text":"ErrorText"
             |}
             |""".stripMargin)

        val result = FinancialDataHttpReads.errorHelper(errorWithInvalidJsonFormat)

        result shouldBe MtdError("SERVER_ERROR", "Unable to validate json error response", Some(errorWithInvalidJsonFormat))
      }
    }
  }

}
