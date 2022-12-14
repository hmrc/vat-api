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
import v1.connectors.httpparsers.FinancialDataHttpParser.FinancialDataHttpReads
import v1.constants.{FinancialDataConstants, PenaltiesConstants}
import v1.models.errors._

class FinancialDataHttpParserSpec extends UnitSpec {

  "FinancialDataHttpParser" when {

    "FinancialDataHttpReads" when {

      "response is OK (200)" when {

        "json is valid" must {

          "return Right(FinancialDataResponse) min" in {

            val result = FinancialDataHttpReads.read("", "",
              HttpResponse(
                status = Status.OK,
                json = FinancialDataConstants.testDownstreamFinancialDetails,
                headers = Map(
                  "CorrelationId" -> Seq(FinancialDataConstants.correlationId)
                )
              )
            )

            result shouldBe Right(FinancialDataConstants.wrappedFinancialDataResponse())
          }

          "return Right(FinancialDataResponse) max" in {

            val result = FinancialDataHttpReads.read("", "",
              HttpResponse(
                status = Status.OK,
                json = FinancialDataConstants.testDownstreamFinancialDetails,
                headers = Map(
                  "CorrelationId" -> Seq(FinancialDataConstants.correlationId)
                )
              )
            )

            result shouldBe Right(FinancialDataConstants.wrappedFinancialDataResponse(FinancialDataConstants.testFinancialDataResponse))
          }

          "return Right(FinancialDataResponse) No Document Details" in {

            val result = FinancialDataHttpReads.read("", "",
              HttpResponse(
                status = Status.OK,
                json = FinancialDataConstants.testDownstreamFinancialDetailsNoDocumentDetails,
                headers = Map(
                  "CorrelationId" -> Seq(FinancialDataConstants.correlationId)
                )
              )
            )

            result shouldBe Right(FinancialDataConstants.wrappedFinancialDataResponse(FinancialDataConstants.testFinancialNoDocumentDetailsDataResponse))
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

            val result = FinancialDataHttpReads.read("", "",
              HttpResponse(
                status = Status.OK,
                json = jsonObject,
                headers = Map(
                  "CorrelationId" -> Seq(FinancialDataConstants.correlationId)
                )
              )
            )

            result shouldBe Left(FinancialDataConstants.errorWrapper(InvalidJson))
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

          val result = FinancialDataHttpReads.read("", "",
            HttpResponse(
              status = Status.BAD_REQUEST,
              json = error,
              headers = Map(
                "CorrelationId" -> Seq(FinancialDataConstants.correlationId)
              )
            )
          )
          result shouldBe Left(FinancialDataConstants.errorWrapper(MtdError("INVALID_IDTYPE", "Submission has not passed validation. Invalid parameter idType.")))
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
              |"code":"INVALID_IDNUMBER",
              |"reason":"Submission has not passed validation. Invalid parameter IDVALUE."
              |}]
              |}
              |""".stripMargin)

          val result = FinancialDataHttpReads.read("", "",
            HttpResponse(
              status = Status.BAD_REQUEST,
              json = error,
              headers = Map(
                "CorrelationId" -> Seq(FinancialDataConstants.correlationId)
              )
            )
          )

          result shouldBe Left(FinancialDataConstants.errorWrapperMulti(
            Seq(MtdError("INVALID_IDTYPE", "Submission has not passed validation. Invalid parameter idType."),
              FinancialInvalidIdNumber)))
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

          val result = FinancialDataHttpReads.read("", "",
            HttpResponse(
              status = Status.NOT_FOUND,
              json = error,
              headers = Map(
                "CorrelationId" -> Seq(FinancialDataConstants.correlationId)
              )
            )
          )
          result shouldBe Left(FinancialDataConstants.errorWrapper(FinancialNotDataFound))
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

          val result = FinancialDataHttpReads.read("", "",
            HttpResponse(
              status = status,
              json = error,
              headers = Map(
                "CorrelationId" -> Seq(PenaltiesConstants.correlationId)
              )
            )
          )
          result shouldBe Left(FinancialDataConstants.errorWrapper(MtdError("INTERNAL_SERVICE_ERROR", "Something went wrong")))
        }
      }

      "errorHelper" must {

        def jsonString(code: String): JsValue = {
          Json.parse(s"""
            |{
            | "failures": [{
            | "code":"${code}",
            | "reason":"Some reason"
            | }]
            | }
            |""".stripMargin)
        }

        def multiJsonString(): JsValue = {
          Json.parse(s"""
                        |{
                        | "failures": [{
                        | "code":"INVALID_CORRELATIONID",
                        | "reason":"Some reason"
                        | },
                        | {
                        | "code":"INVALID_IDTYPE",
                        | "reason":"Some reason"
                        | },
                        | {
                        | "code":"INVALID_IDNUMBER",
                        | "reason":"Some reason"
                        | },
                        | {
                        | "code":"INVALID_DOC_NUMBER",
                        | "reason":"Some reason"
                        | }]
                        | }
                        |""".stripMargin)
        }


        "return multi errors when passed multiple errors" in {
          val result = FinancialDataHttpReads.errorHelper(multiJsonString(), BAD_REQUEST)
          result shouldBe (MtdError("INVALID_CORRELATIONID", "Some reason"),
            Some(Seq(
              MtdError("INVALID_IDTYPE", "Some reason"),
            FinancialInvalidIdNumber,
              MtdError("INVALID_DOC_NUMBER", "Some reason")
          )))
        }

        "return FinancialServiceUnavailable mtd error when passed a Internal Server Error and INTERNAL_SERVER_ERROR json" in {
          val result = FinancialDataHttpReads.errorHelper(jsonString("INTERNAL_SERVER_ERROR"), INTERNAL_SERVER_ERROR)
          result shouldBe (MtdError("INTERNAL_SERVER_ERROR", "Some reason"), None)
        }

        "return FinancialServiceUnavailable mtd error when passed a Service Unavailable and SERVICE_UNAVAILABLE json" in {
          val result = FinancialDataHttpReads.errorHelper(jsonString("SERVICE_UNAVAILABLE"), SERVICE_UNAVAILABLE)
          result shouldBe (MtdError("SERVICE_UNAVAILABLE", "Some reason"), None)
        }

        "return FinancialInvalidRequestUnprocessEntity mtd error when passed a Unprocessable entity and REQUEST_NOT_PROCESSED json" in {
          val result = FinancialDataHttpReads.errorHelper(jsonString("REQUEST_NOT_PROCESSED"), UNPROCESSABLE_ENTITY)
          result shouldBe (MtdError("REQUEST_NOT_PROCESSED", "Some reason"), None)
        }

        "return FinancialInvalidDocNumberUnprocessEntity mtd error when passed a Unprocessable entity and INVALID_DOC_NUMBER json" in {
          val result = FinancialDataHttpReads.errorHelper(jsonString("INVALID_DOC_NUMBER"), UNPROCESSABLE_ENTITY)
          result shouldBe (MtdError("INVALID_DOC_NUMBER", "Some reason"), None)
        }

        "return FinancialInvalidRegimeUnprocessEntity mtd error when passed a Unprocessable entity and INVALID_REGIME_TYPE json" in {
          val result = FinancialDataHttpReads.errorHelper(jsonString("INVALID_REGIME_TYPE"), UNPROCESSABLE_ENTITY)
          result shouldBe (MtdError("INVALID_REGIME_TYPE", "Some reason"), None)
        }

        "return FinancialInvalidIdValueUnprocessEntity mtd error when passed a Unprocessable entity and INVALID_ID json" in {
          val result = FinancialDataHttpReads.errorHelper(jsonString("INVALID_ID"), UNPROCESSABLE_ENTITY)
          result shouldBe (MtdError("INVALID_ID", "Some reason"), None)
        }

        "return FinancialInvalidIdTypeUnprocessEntity mtd error when passed a Unprocessable entity and INVALID_IDTYPE json" in {
          val result = FinancialDataHttpReads.errorHelper(jsonString("INVALID_IDTYPE"), UNPROCESSABLE_ENTITY)
          result shouldBe (MtdError("INVALID_IDTYPE", "Some reason"), None)
        }

        "return FinancialDuplicateSubmission mtd error when passed a Conflict and DUPLICATE_SUBMISSION json" in {
          val result = FinancialDataHttpReads.errorHelper(jsonString("DUPLICATE_SUBMISSION"), CONFLICT)
          result shouldBe (MtdError("DUPLICATE_SUBMISSION", "Some reason"), None)
        }

        "return FinancialNotDataFound mtd error when passed a Not Found and NO_DATA_FOUND json" in {
          val result = FinancialDataHttpReads.errorHelper(jsonString("NO_DATA_FOUND"), NOT_FOUND)
          result shouldBe (FinancialNotDataFound, None)
        }

        "return FinancialInvalidCorrelationId mtd error when passed a Bad Request and INVALID_CORRELATIONID json" in {
          val result = FinancialDataHttpReads.errorHelper(jsonString("INVALID_CORRELATIONID"), BAD_REQUEST)
          result shouldBe (MtdError("INVALID_CORRELATIONID", "Some reason"), None)
        }

        "return FinancialInvalidIdType mtd error when passed a Bad Request and INVALID_IDTYPE json" in {
          val result = FinancialDataHttpReads.errorHelper(jsonString("INVALID_IDTYPE"), BAD_REQUEST)
          result shouldBe (MtdError("INVALID_IDTYPE", "Some reason"), None)
        }

        "return FinancialInvalidRegimeType mtd error when passed a Bad Request and INVALID_REGIME_TYPE json" in {
          val result = FinancialDataHttpReads.errorHelper(jsonString("INVALID_REGIME_TYPE"), BAD_REQUEST)
          result shouldBe (MtdError("INVALID_REGIME_TYPE", "Some reason"), None)
        }

        "return FinancialInvalidDocNumber mtd error when passed a Bad Request and INVALID_DOC_NUMBER json" in {
          val result = FinancialDataHttpReads.errorHelper(jsonString("INVALID_DOC_NUMBER"), BAD_REQUEST)
          result shouldBe (MtdError("INVALID_DOC_NUMBER", "Some reason"), None)
        }

        "return FinancialInvalidOnlyOpenItems mtd error when passed a Bad Request and INVALID_ONLY_OPEN_ITEMS json" in {
          val result = FinancialDataHttpReads.errorHelper(jsonString("INVALID_ONLY_OPEN_ITEMS"), BAD_REQUEST)
          result shouldBe (MtdError("INVALID_ONLY_OPEN_ITEMS", "Some reason"), None)
        }

        "return FinancialInvalidIncludeLocks mtd error when passed a Bad Request and INVALID_INCLUDE_LOCKS json" in {
          val result = FinancialDataHttpReads.errorHelper(jsonString("INVALID_INCLUDE_LOCKS"), BAD_REQUEST)
          result shouldBe (MtdError("INVALID_INCLUDE_LOCKS", "Some reason"), None)
        }

        "return FinancialInvalidIncludeAccruedInterest mtd error when passed a Bad Request and INVALID_CALCULATE_ACCRUED_INTEREST json" in {
          val result = FinancialDataHttpReads.errorHelper(jsonString("INVALID_CALCULATE_ACCRUED_INTEREST"), BAD_REQUEST)
          result shouldBe (MtdError("INVALID_CALCULATE_ACCRUED_INTEREST", "Some reason"), None)
        }

        "return FinancialInvalidIncludeCustomerPaymentInfo mtd error when passed a Bad Request and INVALID_CUSTOMER_PAYMENT_INFORMATION json" in {
          val result = FinancialDataHttpReads.errorHelper(jsonString("INVALID_CUSTOMER_PAYMENT_INFORMATION"), BAD_REQUEST)
          result shouldBe (MtdError("INVALID_CUSTOMER_PAYMENT_INFORMATION", "Some reason"), None)
        }

        "return FinancialInvalidDateFrom mtd error when passed a Bad Request and INVALID_DATE_FROM json" in {
          val result = FinancialDataHttpReads.errorHelper(jsonString("INVALID_DATE_FROM"), BAD_REQUEST)
          result shouldBe (MtdError("INVALID_DATE_FROM", "Some reason"), None)
        }

        "return FinancialInvalidDateTo mtd error when passed a Bad Request and INVALID_DATE_TO json" in {
          val result = FinancialDataHttpReads.errorHelper(jsonString("INVALID_DATE_TO"), BAD_REQUEST)
          result shouldBe (MtdError("INVALID_DATE_TO", "Some reason"), None)
        }

        "return FinancialInvalidDateRange mtd error when passed a Bad Request and INVALID_DATE_RANGE json" in {
          val result = FinancialDataHttpReads.errorHelper(jsonString("INVALID_DATE_RANGE"), BAD_REQUEST)
          result shouldBe (MtdError("INVALID_DATE_RANGE", "Some reason"), None)
        }

        "return FinancialInvalidRemovePaymentOnAccount mtd error when passed a Bad Request and INVALID_REMOVE_PAYMENT_ON_ACCOUNT json" in {
          val result = FinancialDataHttpReads.errorHelper(jsonString("INVALID_REMOVE_PAYMENT_ON_ACCOUNT"), BAD_REQUEST)
          result shouldBe (MtdError("INVALID_REMOVE_PAYMENT_ON_ACCOUNT", "Some reason"), None)
        }

        "return FinancialInvalidIncludeStats mtd error when passed a Bad Request and INVALID_INCLUDE_STATISTICAL json" in {
          val result = FinancialDataHttpReads.errorHelper(jsonString("INVALID_INCLUDE_STATISTICAL"), BAD_REQUEST)
          result shouldBe (MtdError("INVALID_INCLUDE_STATISTICAL", "Some reason"), None)
        }

        "return FinancialInvalidSearchItem mtd error when passed a Bad Request and INVALID_REQUEST json" in {
          val result = FinancialDataHttpReads.errorHelper(jsonString("INVALID_SEARCH_ITEM"), BAD_REQUEST)
          result shouldBe (FinancialInvalidSearchItem, None)
        }

        "return FinancialInvalidRequest mtd error when passed a Bad Request and INVALID_REQUEST json" in {
          val result = FinancialDataHttpReads.errorHelper(jsonString("INVALID_REQUEST"), BAD_REQUEST)
          result shouldBe (MtdError("INVALID_REQUEST", "Some reason"), None)
        }

      }
    }
  }
}
