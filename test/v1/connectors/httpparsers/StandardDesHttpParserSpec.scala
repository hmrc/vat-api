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

package v1.connectors.httpparsers

import play.api.http.Status._
import play.api.libs.json.{JsValue, Json, Reads}
import support.UnitSpec
import uk.gov.hmrc.http.{HttpReads, HttpResponse}
import v1.connectors.DesOutcome
import v1.models.errors._
import v1.models.outcomes.ResponseWrapper

// WLOG if Reads tested elsewhere
case class SomeModel(data: String)

object SomeModel {
  implicit val reads: Reads[SomeModel] = Json.reads
}

class StandardDesHttpParserSpec extends UnitSpec {

  val method = "POST"
  val url = "test-url"

  val correlationId = "a1e8057e-fbbc-47a8-a8b4-78d9f015c253"

  import v1.connectors.httpparsers.StandardDesHttpParser._

  val httpReads: HttpReads[DesOutcome[Unit]] = implicitly

  val data = "someData"
  val desExpectedJson: JsValue = Json.obj("data" -> data)

  val desModel = SomeModel(data)
  val desResponse = ResponseWrapper(correlationId, desModel)

  "The generic HTTP parser" when {
    "no status code is specified" must {
      val httpReads: HttpReads[DesOutcome[SomeModel]] = implicitly

      "return a Right DES response containing the model object if the response json corresponds to a model object" in {
        val httpResponse = HttpResponse(OK, Some(desExpectedJson), Map("CorrelationId" -> Seq(correlationId)))

        httpReads.read(method, url, httpResponse) shouldBe Right(desResponse)
      }

      "return an outbound error if a model object cannot be read from the response json" in {
        val badFieldTypeJson: JsValue = Json.obj("incomeSourceId" -> 1234, "incomeSourceName" -> 1234)
        val httpResponse = HttpResponse(OK, Some(badFieldTypeJson), Map("CorrelationId" -> Seq(correlationId)))
        val expected = ResponseWrapper(correlationId, OutboundError(DownstreamError))

        httpReads.read(method, url, httpResponse) shouldBe Left(expected)
      }

      handleErrorsCorrectly(httpReads)
      handleInternalErrorsCorrectly(httpReads)
      handleUnexpectedResponse(httpReads)
      handleBvrsCorrectly(httpReads)
    }

    "a success code is specified" must {
      "use that status code for success" in {
        implicit val successCode: SuccessCode = SuccessCode(PARTIAL_CONTENT)
        val httpReads: HttpReads[DesOutcome[SomeModel]] = implicitly

        val httpResponse = HttpResponse(PARTIAL_CONTENT, Some(desExpectedJson), Map("CorrelationId" -> Seq(correlationId)))

        httpReads.read(method, url, httpResponse) shouldBe Right(desResponse)
      }
    }
  }

  "The generic HTTP parser for empty response" when {
    "no status code is specified" must {
      val httpReads: HttpReads[DesOutcome[Unit]] = implicitly

      "receiving a 204 response" should {
        "return a Right DesResponse with the correct correlationId and no responseData" in {
          val httpResponse = HttpResponse(NO_CONTENT, responseHeaders = Map("CorrelationId" -> Seq(correlationId)))

          httpReads.read(method, url, httpResponse) shouldBe Right(ResponseWrapper(correlationId, ()))
        }
      }

      handleErrorsCorrectly(httpReads)
      handleInternalErrorsCorrectly(httpReads)
      handleUnexpectedResponse(httpReads)
      handleBvrsCorrectly(httpReads)
    }

    "a success code is specified" must {
      implicit val successCode: SuccessCode = SuccessCode(PARTIAL_CONTENT)
      val httpReads: HttpReads[DesOutcome[Unit]] = implicitly

      "use that status code for success" in {
        val httpResponse = HttpResponse(PARTIAL_CONTENT, responseHeaders = Map("CorrelationId" -> Seq(correlationId)))

        httpReads.read(method, url, httpResponse) shouldBe Right(ResponseWrapper(correlationId, ()))
      }
    }
  }

  val singleErrorJson: JsValue = Json.parse(
    """
      |{
      |   "code": "CODE",
      |   "reason": "MESSAGE"
      |}
    """.stripMargin
  )

  val multipleErrorsJson: JsValue = Json.parse(
    """
      |{
      |   "failures": [
      |       {
      |           "code": "CODE 1",
      |           "reason": "MESSAGE 1"
      |       },
      |       {
      |           "code": "CODE 2",
      |           "reason": "MESSAGE 2"
      |       }
      |   ]
      |}
    """.stripMargin
  )

  val malformedErrorJson: JsValue = Json.parse(
    """
      |{
      |   "coed": "CODE",
      |   "resaon": "MESSAGE"
      |}
    """.stripMargin
  )

  private def handleErrorsCorrectly[A](httpReads: HttpReads[DesOutcome[A]]): Unit =
    Seq(BAD_REQUEST, NOT_FOUND, FORBIDDEN, CONFLICT).foreach(
      responseCode =>
        s"receiving a $responseCode response" should {
          "be able to parse a single error" in {
            val httpResponse = HttpResponse(responseCode, Some(singleErrorJson), Map("CorrelationId" -> Seq(correlationId)))

            httpReads.read(method, url, httpResponse) shouldBe Left(ResponseWrapper(correlationId, DesErrors.single(DesErrorCode("CODE"))))
          }

          "be able to parse multiple errors" in {
            val httpResponse = HttpResponse(responseCode, Some(multipleErrorsJson), Map("CorrelationId" -> Seq(correlationId)))

            httpReads.read(method, url, httpResponse) shouldBe {
              Left(ResponseWrapper(correlationId, DesErrors(List(DesErrorCode("CODE 1"), DesErrorCode("CODE 2")))))
            }
          }

          "return an outbound error when the error returned doesn't match the Error model" in {
            val httpResponse = HttpResponse(responseCode, Some(malformedErrorJson), Map("CorrelationId" -> Seq(correlationId)))

            httpReads.read(method, url, httpResponse) shouldBe Left(ResponseWrapper(correlationId, OutboundError(DownstreamError)))
          }
        }
    )

  private def handleInternalErrorsCorrectly[A](httpReads: HttpReads[DesOutcome[A]]): Unit =
    Seq(INTERNAL_SERVER_ERROR, SERVICE_UNAVAILABLE).foreach(responseCode =>
      s"receiving a $responseCode response" should {
        "return an outbound error when the error returned matches the Error model" in {
          val httpResponse = HttpResponse(responseCode, Some(singleErrorJson), Map("CorrelationId" -> Seq(correlationId)))

          httpReads.read(method, url, httpResponse) shouldBe Left(ResponseWrapper(correlationId, OutboundError(DownstreamError)))
        }

        "return an outbound error when the error returned doesn't match the Error model" in {
          val httpResponse = HttpResponse(responseCode, Some(malformedErrorJson), Map("CorrelationId" -> Seq(correlationId)))

          httpReads.read(method, url, httpResponse) shouldBe Left(ResponseWrapper(correlationId, OutboundError(DownstreamError)))
        }
      })

  private def handleUnexpectedResponse[A](httpReads: HttpReads[DesOutcome[A]]): Unit =
    "receiving an unexpected response" should {
      val responseCode = 499
      "return an outbound error when the error returned matches the Error model" in {
        val httpResponse = HttpResponse(responseCode, Some(singleErrorJson), Map("CorrelationId" -> Seq(correlationId)))

        httpReads.read(method, url, httpResponse) shouldBe Left(ResponseWrapper(correlationId, OutboundError(DownstreamError)))
      }

      "return an outbound error when the error returned doesn't match the Error model" in {
        val httpResponse = HttpResponse(responseCode, Some(malformedErrorJson), Map("CorrelationId" -> Seq(correlationId)))

        httpReads.read(method, url, httpResponse) shouldBe Left(ResponseWrapper(correlationId, OutboundError(DownstreamError)))
      }
    }


  private def handleBvrsCorrectly[A](httpReads: HttpReads[DesOutcome[A]]): Unit = {


    val singleBvrJson = Json.parse(
      """
        |{
        |   "bvrfailureResponseElement": {
        |     "validationRuleFailures": [
        |       {
        |         "id": "BVR1"
        |       },
        |       {
        |         "id": "BVR2"
        |       }
        |     ]
        |   }
        |}
      """.stripMargin)

    s"receiving a response with a bvr errors" should {
      "return an outbound BUSINESS_ERROR error containing the BVR ids" in {
        val httpResponse = HttpResponse(BAD_REQUEST, Some(singleBvrJson), Map("CorrelationId" -> Seq(correlationId)))

        httpReads.read(method, url, httpResponse) shouldBe
          Left(ResponseWrapper(correlationId,
            OutboundError(BVRError, Some(Seq(MtdError("BVR1", ""), MtdError("BVR2", ""))))))
      }
    }
  }
}
