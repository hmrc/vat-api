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

import play.api.http.Status._
import play.api.libs.json.{ JsValue, Json }
import support.UnitSpec
import uk.gov.hmrc.http.HttpResponse
import v1.connectors.httpparsers.PenaltiesHttpParser.PenaltiesHttpReads
import v1.constants.PenaltiesConstants._
import v1.models.errors._

class PenaltiesHttpParserSpec extends UnitSpec {

  private def buildHttpResponse(status: Int, jsonBody: JsValue) =
    HttpResponse(
      status = status,
      json = jsonBody,
      headers = Map("CorrelationId" -> Seq(correlationId))
    )

  "PenaltiesHttpParser .read" when {
    "response is OK (200)" when {
      "json is valid" must {
        "return a parsed PenaltiesResponse where the ID has no active penalties" in {
          val response = buildHttpResponse(OK, testPenaltiesResponseJsonMin)
          val result   = PenaltiesHttpReads.read("", "", response)

          result shouldBe Right(wrappedPenaltiesResponse(testPenaltiesResponseMin))
        }

        "return a parsed PenaltiesResponse where the ID has full penalties data" in {
          val response = buildHttpResponse(OK, downstreamTestPenaltiesResponseJsonMax)
          val result   = PenaltiesHttpReads.read("", "", response)

          result shouldBe Right(wrappedPenaltiesResponse(testPenaltiesResponseMax))
        }
      }

      "json is invalid" must {
        "return as InvalidJson error" in {
          val invalidJsonObject = Json.parse("""
                                                | "totalisations" {
                                                |   "test": "test"
                                                | }
                                                |""".stripMargin)
          val invalidOkResponse = buildHttpResponse(OK, invalidJsonObject)
          val result            = PenaltiesHttpReads.read("", "", invalidOkResponse)

          result shouldBe Left(errorWrapper(InvalidJson))
        }
      }
    }

    "response is an error (any non 200 status)" when {
      "return an error in an ErrorWrapper (appropriate error handled by .errorHelper)" in {
        val errorJson    = buildErrorHip("002", Some("Invalid Tax Regime"))
        val httpResponse = buildHttpResponse(BAD_REQUEST, errorJson)

        val result = PenaltiesHttpReads.read("", "", httpResponse)

        result shouldBe Left(errorWrapper(MtdError("INTERNAL_SERVER_ERROR", "An internal server error occurred")))
      }
    }
  }

  "PenaltiesHttpParser .errorHelper" when {
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

    "the error response does not match the PenaltiesErrorsHIP format" must {
      "return a MtdError explaining service was unable to validate json error response" in {
        val errorWithInvalidJsonFormat = Json.parse(s"""
                                                       |{
                                                       |  "processingDate":"2017-01-01",
                                                       |  "code":"ErrorCode",
                                                       |  "text":"ErrorText"
                                                       |}
                                                       |""".stripMargin)

        val result = PenaltiesHttpReads.errorHelper(errorWithInvalidJsonFormat)

        result shouldBe MtdError(
          "SERVER_ERROR",
          "Unable to validate json error response with errors: List((/errors,List(JsonValidationError(List(error.path.missing),List()))))",
          Some(errorWithInvalidJsonFormat)
        )
      }
    }
  }

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
