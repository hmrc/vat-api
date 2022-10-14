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
import play.api.http.Status._
import play.api.libs.json.{JsValue, Json}
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
              |"code":"INVALID_IDTYPE",
              |"reason":"Submission has not passed validation. Invalid parameter idType."
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
          result shouldBe Left(PenaltiesConstants.errorWrapper(MtdError("INVALID_IDTYPE", "Invalid Id Type")))
        }

        "return Left(MultiErrors)" in {

          val error = Json.parse(
            """
              |{
              |"failures": [{
              |"code":"INVALID_IDTYPE",
              |"reason":"Submission has not passed validation. Invalid parameter idType."
              |},
              |{
              |"code":"INVALID_IDVALUE",
              |"reason":"Submission has not passed validation. Invalid parameter IDVALUE."
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
          result shouldBe Left(PenaltiesConstants.errorWrapper(
            MtdError("INVALID_REQUEST", "Invalid request penalties", customJson = Some(Json.toJson(Seq(PenaltiesInvalidIdType, PenaltiesInvalidIdValue))))))
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

      "errorHelper" must {

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
                        | "failures": [{
                        | "code":"INVALID_IDTYPE",
                        | "reason":"Some reason"
                        | },
                        | {
                        | "code":"INVALID_IDVALUE",
                        | "reason":"Some reason"
                        | },
                        | {
                        | "code":"INVALID_DATELIMIT",
                        | "reason":"Some reason"
                        | },
                        | {
                        | "code":"INVALID_CORRELATIONID",
                        | "reason":"Some reason"
                        | }]
                        | }
                        |""".stripMargin)
        }

        "return multi errors when passed multiple errors" in {
          val expected: MtdError = MtdError("INVALID_REQUEST", "Invalid request penalties",
            Some(Json.toJson(PenaltiesInvalidIdType,
            PenaltiesInvalidIdValue,
            PenaltiesInvalidDataLimit,
            PenaltiesInvalidCorrelationId
          )))
          val result = PenaltiesHttpReads.errorHelper(multiJsonString(), BAD_REQUEST)
          result shouldBe expected
        }

          "return mtd error when passed a service unavailable and INTERNAL_SERVER_ERROR json" in {
            val expected: MtdError= MtdError("INTERNAL_SERVER_ERROR","Some reason")
            val result = PenaltiesHttpReads.errorHelper(jsonString("INTERNAL_SERVER_ERROR"), INTERNAL_SERVER_ERROR)
            result shouldBe expected
          }

          "return PenaltiesServiceUnavailable mtd error when passed a service unavailable and SERVICE_UNAVAILABLE json" in {
            val expected: MtdError = PenaltiesServiceUnavailable
            val result = PenaltiesHttpReads.errorHelper(jsonString("SERVICE_UNAVAILABLE"), SERVICE_UNAVAILABLE)
            result shouldBe expected
          }

          "return PenaltiesRequestNotProcessedUnprocessEntity mtd error when passed a Unprocess Entity and REQUEST_NOT_PROCESSED json" in {
            val expected: MtdError = PenaltiesRequestNotProcessedUnprocessEntity
            val result = PenaltiesHttpReads.errorHelper(jsonString("REQUEST_NOT_PROCESSED"), UNPROCESSABLE_ENTITY)
            result shouldBe expected
          }

          "return PenaltiesInvalidIdValueUnprocessEntity mtd error when passed a Unprocess Entity and INVALID_ID json" in {
            val expected: MtdError = PenaltiesInvalidIdValueUnprocessEntity
            val result = PenaltiesHttpReads.errorHelper(jsonString("INVALID_ID"), UNPROCESSABLE_ENTITY)
            result shouldBe expected
          }

          "return PenaltiesInvalidIdTypeUnprocessEntity mtd error when passed a Unprocess Entity and INVALID_IDTYPE json" in {
            val expected: MtdError = PenaltiesInvalidIdTypeUnprocessEntity
            val result = PenaltiesHttpReads.errorHelper(jsonString("INVALID_IDTYPE"), UNPROCESSABLE_ENTITY)
            result shouldBe expected
          }

          "return PenaltiesDuplicateSubmission mtd error when passed a Conflict and DUPLICATE_SUBMISSION json" in {
            val expected: MtdError = PenaltiesDuplicateSubmission
            val result = PenaltiesHttpReads.errorHelper(jsonString("DUPLICATE_SUBMISSION"), CONFLICT)
            result shouldBe expected
          }

          "return PenaltiesNotDataFound mtd error when passed a Not Found and NO_DATA_FOUND json" in {
            val expected: MtdError = PenaltiesNotDataFound
            val result = PenaltiesHttpReads.errorHelper(jsonString("NO_DATA_FOUND"), NOT_FOUND)
            result shouldBe expected
          }

          "return PenaltiesInvalidIdType mtd error when passed a Bad Request and INVALID_IDTYPE json" in {
            val expected: MtdError = PenaltiesInvalidIdType
            val result = PenaltiesHttpReads.errorHelper(jsonString("INVALID_IDTYPE"), BAD_REQUEST)
            result shouldBe expected
          }

          "return PenaltiesInvalidIdValue mtd error when passed a Bad Request and INVALID_IDVALUE json" in {
            val expected: MtdError = PenaltiesInvalidIdValue
            val result = PenaltiesHttpReads.errorHelper(jsonString("INVALID_IDVALUE"), BAD_REQUEST)
            result shouldBe expected
          }

          "return PenaltiesInvalidDataLimit mtd error when passed a Bad Request and INVALID_DATELIMIT json" in {
            val expected: MtdError = PenaltiesInvalidDataLimit
            val result = PenaltiesHttpReads.errorHelper(jsonString("INVALID_DATELIMIT"), BAD_REQUEST)
            result shouldBe expected
          }

          "return PenaltiesInvalidCorrelationId mtd error when passed a Bad Request and INVALID_CORRELATIONID json" in {
            val expected: MtdError = PenaltiesInvalidCorrelationId
            val result = PenaltiesHttpReads.errorHelper(jsonString("INVALID_CORRELATIONID"), BAD_REQUEST)
            result shouldBe expected
          }
        }
      }
    }

}
