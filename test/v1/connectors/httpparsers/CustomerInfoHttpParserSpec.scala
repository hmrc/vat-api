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
import play.api.libs.json.Json
import support.UnitSpec
import uk.gov.hmrc.http.HttpResponse
import v1.connectors.httpparsers.CustomerInfoHttpParser.CustomerInfoHttpReads
import v1.constants
import v1.constants.CustomerInfoConstants
import v1.models.errors._

class CustomerInfoHttpParserSpec extends UnitSpec {

  "CustomerInfoHttpParserSpec" when {

    "CustomerInfoHttpReads" when {

      "response is OK (200)" when {

        "json is valid" must {

          "return Right(CustomerInfoResponse) max" in {

            val result = CustomerInfoHttpReads.read("", "",
              HttpResponse(
                status = Status.OK,
                json = CustomerInfoConstants.testCustomerInfoResponseMinJson,
                headers = Map(
                  "CorrelationId" -> Seq(CustomerInfoConstants.correlationId)
                )
              )
            )

            result shouldBe Right(CustomerInfoConstants.wrappedCustomerInfoResponse(CustomerInfoConstants.testCustomerInfoResponseMin))
          }
        }
      }

      "response is BAD_REQUEST (400)" must {

        "return Left(InvalidVrn)" in {

          val error = Json.parse(
            """
              |{
              |"failures": {
              |"code":"INVALID_VRN",
              |"reason":"The provided VRN is invalid."
              |}
              |}
              |""".stripMargin)

          val result = CustomerInfoHttpReads.read("", "",
            HttpResponse(
              status = Status.BAD_REQUEST,
              json = error,
              headers = Map(
                "CorrelationId" -> Seq(CustomerInfoConstants.correlationId)
              )
            )
          )
          result shouldBe Left(CustomerInfoConstants.errorWrapper(MtdError("INVALID_VRN", "The provided VRN is invalid.")))
        }
      }

      "response is INTERNAL_SERVER_ERROR (500)" must {

        "return Left(UnexpectedFailure)" in {

          val status = Status.INTERNAL_SERVER_ERROR

          val error = Json.parse(
            """
              |{
              |"failures": {
              |"code":"INTERNAL_SERVER_ERROR",
              |"reason":"An internal server error occurred"
              |}
              |}
              |""".stripMargin)

          val result = CustomerInfoHttpReads.read("", "",
            HttpResponse(
              status = status,
              json = error,
              headers = Map(
                "CorrelationId" -> Seq(CustomerInfoConstants.correlationId)
              )
            )
          )
          result shouldBe Left(CustomerInfoConstants.errorWrapper(MtdError("INTERNAL_SERVER_ERROR", "An internal server error occurred")))
        }
      }
      "response is NOT_FOUND (404)" must{
        "return 404 when not found" in{

          val status = Status.NOT_FOUND

          val error = Json.parse(
            """
              |{
              |"failures": {
              |"code":"NOT_FOUND",
              |"reason":"The backend indicated that no subscription found"
              |}
              |}
              |""".stripMargin)

          val result = CustomerInfoHttpReads.read("", "",
            HttpResponse(
              status = status,
              json = error,
              headers = Map(
                "CorrelationId" -> Seq(CustomerInfoConstants.correlationId)
              )
            )
          )
          result shouldBe Left(CustomerInfoConstants.errorWrapper(MtdError("NOT_FOUND", "The requested resource could not be found")))
        }
      }
    }
  }
}
