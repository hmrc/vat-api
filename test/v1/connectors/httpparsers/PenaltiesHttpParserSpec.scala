/*
 * Copyright 2022 HM Revenue & Customs
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
import play.api.libs.json.Json
import support.UnitSpec
import uk.gov.hmrc.http.HttpResponse
import v1.connectors.httpparsers.PenaltiesHttpParser.PenaltiesHttpReads
import v1.constants.PenaltiesConstants
import v1.models.errors._

class PenaltiesHttpParserSpec extends UnitSpec {

  "PenaltiesHttpParser" when {

    "PenaltiesHttpReads" when {

      "response is OK (200)" when {

        "json is valid" must {

          "return Right(PenaltiesResponse) min" in {

            val result = PenaltiesHttpReads.read("", "",
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

            val result = PenaltiesHttpReads.read("", "",
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

            val result = PenaltiesHttpReads.read("", "",
              HttpResponse(
                status = Status.OK,
                json = jsonObject,
                headers = Map(
                  "CorrelationId" -> Seq(PenaltiesConstants.correlationId)
                )
              )
            )

            result shouldBe Left(PenaltiesConstants.errorWrapper(InvalidJson))
          }
        }
      }

      "response is BAD_REQUEST (400)" must {

        "return Left(InvalidVrn)" in {

          val error = Json.parse(
            """
              |{
              |"failures": [{
              |"code":"INVALID_IDVALUE",
              |"reason":"Submission has not passed validation. Invalid parameter idNumber."
              |}]
              |}
              |""".stripMargin)

          val result = PenaltiesHttpReads.read("", "",
            HttpResponse(
              status = Status.BAD_REQUEST,
              json = error,
              headers = Map(
                "CorrelationId" -> Seq(PenaltiesConstants.correlationId)
              )
            )
          )
          result shouldBe Left(PenaltiesConstants.errorWrapper(MtdError("VRN_INVALID", "The provided VRN is invalid.")))
        }
      }

      "response is NOT_FOUND (404)" must {

        "return Left(VrnNotFound)" in {

          val error = Json.parse(
            """
              |{
              |"failures": [{
              |"code":"NO_DATA_FOUND",
              |"reason":"Submission has not passed validation. Invalid parameter idType."
              |}]
              |}
              |""".stripMargin)

          val result = PenaltiesHttpReads.read("", "",
            HttpResponse(
              status = Status.NOT_FOUND,
              json = error,
              headers = Map(
                "CorrelationId" -> Seq(PenaltiesConstants.correlationId)
              )
            )
          )
          result shouldBe Left(PenaltiesConstants.errorWrapper(PenaltiesNotDataFound))
        }
      }

      "response is INTERNAL_SERVER_ERROR (500)" must {

        "return Left(UnexpectedFailure)" in {

          val status = Status.INTERNAL_SERVER_ERROR

          val error = Json.parse(
            """
              |{
              |"failures": [{
              |"code":"INTERNAL_SERVICE_ERROR",
              |"reason":"Something went wrong"
              |}]
              |}
              |""".stripMargin)

          val result = PenaltiesHttpReads.read("", "",
            HttpResponse(
              status = status,
              json = error,
              headers = Map(
                "CorrelationId" -> Seq(PenaltiesConstants.correlationId)
              )
            )
          )
          result shouldBe Left(PenaltiesConstants.errorWrapper(MtdError("INTERNAL_SERVICE_ERROR", "Something went wrong")))
        }
      }
      "errorHelper" must{
        "return 500 when multiple errors occur including any 500" in{

          val status = Status.BAD_REQUEST

          val error = Json.parse(
            """
              |{
              |"failures": [{
              |"code":"INVALID_REGIME",
              |"reason":"Something went wrong"
              |},
              |{
              |"code":"INVALID_IDVALUE",
              |"reason":"Submission has not passed validation. Invalid parameter idNumber."
              |}]
              |}
              |""".stripMargin)

          val result = PenaltiesHttpReads.read("", "",
            HttpResponse(
              status = status,
              json = error,
              headers = Map(
                "CorrelationId" -> Seq(PenaltiesConstants.correlationId)
              )
            )
          )
          result shouldBe Left(PenaltiesConstants.errorWrapper(MtdError("INTERNAL_SERVER_ERROR", "An internal server error occurred")))
        }

        "return 400 when multiple errors occur including a 400 only" in {

          val status = Status.BAD_REQUEST

          val error = Json.parse(
            """
              |{
              |"failures": [{
              |"code":"INVALID_IDVALUE",
              |"reason":"Something went wrong"
              |},
              |{
              |"code":"NO_DATA_FOUND",
              |"reason":"Submission has not passed validation. Invalid parameter idNumber."
              |}]
              |}
              |""".stripMargin)

          val result = PenaltiesHttpReads.read("", "",
            HttpResponse(
              status = status,
              json = error,
              headers = Map(
                "CorrelationId" -> Seq(PenaltiesConstants.correlationId)
              )
            )
          )

          result shouldBe Left(PenaltiesConstants.errorWrapper(
            MtdError("INVALID_REQUEST", "Invalid request penalties",
              Some(Json.toJson(Seq(
                MtdError("VRN_INVALID", "The provided VRN is invalid."),
                MtdError("MATCHING_RESOURCE_NOT_FOUND", "No penalties could be found in the last 24 months"))))
            ))
          )
        }
      }
    }
  }

}
