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
import play.api.libs.json.{ JsValue, Json }
import support.UnitSpec
import uk.gov.hmrc.http.HttpResponse
import v1.connectors.httpparsers.PenaltiesHttpParser.PenaltiesHttpReads
import v1.constants.PenaltiesConstants
import v1.models.errors._

class PenaltiesHttpParserSpec extends UnitSpec {

  "PenaltiesHttpParser .read" when {
    "response is OK (200)" when {
      "json is valid" must {
        "return Right(PenaltiesResponse) min" in {
          val result = PenaltiesHttpReads.read(
            "",
            "",
            HttpResponse(
              status = Status.OK,
              json = PenaltiesConstants.testPenaltiesResponseJsonMin,
              headers = Map(
                "CorrelationId" -> Seq(PenaltiesConstants.correlationId)
              )
            )
          )

          result shouldBe Right(PenaltiesConstants.wrappedPenaltiesResponse())
        }

        "return Right(PenaltiesResponse) max" in {

          val result = PenaltiesHttpReads.read(
            "",
            "",
            HttpResponse(
              status = Status.OK,
              json = PenaltiesConstants.downstreamTestPenaltiesResponseJsonMax,
              headers = Map(
                "CorrelationId" -> Seq(PenaltiesConstants.correlationId)
              )
            )
          )

          result shouldBe Right(PenaltiesConstants.wrappedPenaltiesResponse(PenaltiesConstants.testPenaltiesResponseMax))
        }
      }

      "json is invalid" must {

        "return Left(InvalidJson)" in {

          val jsonObject =
            Json.parse("""
                | "totalisations" {
                |   "test": "test"
                | }
                |""".stripMargin)

          val result = PenaltiesHttpReads.read("",
                                               "",
                                               HttpResponse(
                                                 status = Status.OK,
                                                 json = jsonObject,
                                                 headers = Map(
                                                   "CorrelationId" -> Seq(PenaltiesConstants.correlationId)
                                                 )
                                               ))

          result shouldBe Left(PenaltiesConstants.errorWrapper(InvalidJson))
        }
      }
    }

    "response is an error (any non 200 status)" when {
      "API response is from IF" when {
        "return an error in an ErrorWrapper (appropriate error handled by .errorHelper)" in {
          val error = Json.parse("""
                |{
                |"failures": [{
                |"code":"INVALID_IDVALUE",
                |"reason":"Submission has not passed validation. Invalid parameter idNumber."
                |}]
                |}
                |""".stripMargin)

          val result = PenaltiesHttpReads.read("",
                                               "",
                                               HttpResponse(
                                                 status = Status.BAD_REQUEST,
                                                 json = error,
                                                 headers = Map(
                                                   "CorrelationId" -> Seq(PenaltiesConstants.correlationId)
                                                 )
                                               ))
          result shouldBe Left(PenaltiesConstants.errorWrapper(MtdError("VRN_INVALID", "The provided VRN is invalid.")))
        }
      }

      "API response is from HIP" when {
        "return an error in an ErrorWrapper (appropriate error handled by .errorHelper)" in {
          val error = buildErrorHip("002", Some("Invalid Tax Regime"))
          val httpResponse = HttpResponse(
            status = Status.BAD_REQUEST,
            json = error,
            headers = Map("CorrelationId" -> Seq(PenaltiesConstants.correlationId))
          )
          val result = PenaltiesHttpReads.read("", "", httpResponse)

          result shouldBe Left(PenaltiesConstants.errorWrapper(MtdError("INTERNAL_SERVER_ERROR", "An internal server error occurred")))
        }
      }
    }
  }

  "PenaltiesHttpParser .errorHelper" when {
    "the error response matches the PenaltiesErrorsIF format" when {
      "there is a single error" must {
        val validDownstreamErrorCodes = Seq(
          "INVALID_REGIME",
          "INVALID_IDTYPE",
          "INVALID_DATELIMIT",
          "INVALID_CORRELATIONID",
          "DUPLICATE_SUBMISSION",
          "INVALID_ID",
          "REQUEST_NOT_PROCESSED",
          "SERVER_ERROR",
          "SERVICE_UNAVAILABLE"
        )

        "return a DownstreamError" when {
          validDownstreamErrorCodes.foreach { errorCode =>
            s"error code is '$errorCode'" in {
              val result = PenaltiesHttpReads.errorHelper(buildErrorIf(errorCode))

              result shouldBe DownstreamError
            }
          }
        }

        "return a PenaltiesInvalidIdValue" when {
          s"error code is 'INVALID_IDVALUE'" in {
            val result = PenaltiesHttpReads.errorHelper(buildErrorIf("INVALID_IDVALUE"))

            result shouldBe PenaltiesInvalidIdValue
          }
        }

        "return a MtdError with error code and reason" when {
          "error code is not recognised" in {
            val result = PenaltiesHttpReads.errorHelper(buildErrorIf("111", Some("Unknown error message")))

            result shouldBe MtdError("111", "Unknown error message")
          }
        }
      }

      "there are multiple errors" when {
        "at least one of the errors is a DownstreamError" must {
          "return a single DownstreamError" in {
            val result = PenaltiesHttpReads.errorHelper(
              buildMultipleErrorsIf(
                codeOne = "INVALID_IDVALUE", // PenaltiesInvalidIdValue
                codeTwo = "INVALID_REGIME" // DownstreamError
              ))

            result shouldBe DownstreamError
          }
        }
        "none of the errors are a DownstreamError" must {
          "return a single DownstreamError" in {
            val multipleJsonErrors = buildMultipleErrorsIf(
              codeOne = "INVALID_IDVALUE", // PenaltiesInvalidIdValue
              codeTwo = "UNKNOWN_CODE" // MtdError
            )
            val result = PenaltiesHttpReads.errorHelper(multipleJsonErrors)

            val expectedConvertedJson = Json.parse(s"""
                                                      |[
                                                      |  {"code":"VRN_INVALID","message":"The provided VRN is invalid."},
                                                      |  {"code":"UNKNOWN_CODE","message":"reason two"}
                                                      |]
                                                      |""".stripMargin)
            val expectedError         = MtdError("INVALID_REQUEST", "Invalid request penalty details", Some(Json.toJson(expectedConvertedJson)))

            result shouldBe expectedError
          }
        }
      }
    }

    "the error response matches the PenaltiesErrorsHIP format" must {

      "return a DownstreamError" when {
        Seq("002", "003", "015").foreach { errorCode =>
          s"error code is $errorCode" in {
            val result = PenaltiesHttpReads.errorHelper(buildErrorHip(errorCode))

            result shouldBe DownstreamError
          }
        }
      }

      "return a PenaltiesInvalidIdValue" when {
        "error code is '016'" in {
          val result = PenaltiesHttpReads.errorHelper(buildErrorHip("016"))

          result shouldBe PenaltiesInvalidIdValue
        }
      }

      "return a MtdError with error code and text" when {
        "error code is not recognised" in {
          val result = PenaltiesHttpReads.errorHelper(buildErrorHip("111", Some("Unknown error message")))

          result shouldBe MtdError("111", "Unknown error message")
        }
      }
    }

    "the error response matches neither PenaltiesErrorsIF nor PenaltiesErrorsHIP formats" must {
      "return a MtdError explaining service was unable to validate json error response" in {
        val errorWithInvalidJsonFormat = Json.parse(s"""
                                                       |{
                                                       |  "processingDate":"2017-01-01",
                                                       |  "code":"ErrorCode",
                                                       |  "text":"ErrorText"
                                                       |}
                                                       |""".stripMargin)

        val result = PenaltiesHttpReads.errorHelper(errorWithInvalidJsonFormat)

        result shouldBe MtdError("SERVER_ERROR", "Unable to validate json error response", Some(errorWithInvalidJsonFormat))
      }
    }
  }

  def buildErrorIf(code: String, reason: Option[String] = None): JsValue = {
    val errorReason = reason.getOrElse("This is the error message")
    Json.parse(s"""
                  |{
                  |  "failures": [{
                  |    "code":"$code",
                  |    "reason":"$errorReason"
                  |  }]
                  |}
                  |""".stripMargin)
  }

  def buildMultipleErrorsIf(codeOne: String, codeTwo: String): JsValue =
    Json.parse(s"""
                  |{
                  |  "failures": [
                  |     {
                  |       "code":"$codeOne",
                  |       "reason":"reason one"
                  |     },
                  |     {
                  |       "code":"$codeTwo",
                  |       "reason":"reason two"
                  |     }
                  |   ]
                  |}
                  |""".stripMargin)

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

}
