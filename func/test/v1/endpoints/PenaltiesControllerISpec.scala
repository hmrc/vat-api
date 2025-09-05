/*
 * Copyright 2024 HM Revenue & Customs
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

package v1.endpoints

import com.github.tomakehurst.wiremock.stubbing.StubMapping
import config.AppConfig
import play.api.http.HeaderNames.ACCEPT
import play.api.http.Status._
import play.api.libs.json.{ JsValue, Json }
import play.api.libs.ws.{ WSRequest, WSResponse }
import play.api.test.Helpers.AUTHORIZATION
import support.IntegrationBaseSpec
import v1.constants.PenaltiesConstants
import v1.constants.PenaltiesConstants.{
  downstreamTestPenaltiesResponseJsonMax,
  invalidVrn,
  testPenaltiesResponseJsonMin,
  upstreamTestLatePaymentPenaltyJson
}
import v1.models.errors.{ DownstreamError, InvalidJson, PenaltiesInvalidIdValue, VrnFormatError }
import v1.stubs.{ AuditStub, AuthStub, PenaltiesStub }

class PenaltiesControllerISpec extends IntegrationBaseSpec {

  implicit val appConfig: AppConfig = app.injector.instanceOf[AppConfig]

  private val uri = s"/${PenaltiesConstants.vrn}/penalties"

  private trait Test {
    AuditStub.audit()
    AuthStub.authorised()

    def stubSuccess(responseBody: JsValue): StubMapping =
      PenaltiesStub.onSuccess(PenaltiesStub.GET, PenaltiesConstants.penaltiesURl(), OK, responseBody)

    def stubError(errorStatus: Int, errorBody: String): StubMapping =
      PenaltiesStub.onError(PenaltiesStub.GET, PenaltiesConstants.penaltiesURl(), errorStatus, errorBody)

    def makeRequest(overrideUri: Option[String] = None): WSRequest = {
      buildRequest(overrideUri.getOrElse(uri))
        .withHttpHeaders(
          (ACCEPT, "application/vnd.hmrc.1.0+json"),
          (AUTHORIZATION, "Bearer 123")
        )
    }
  }

  "PenaltiesController" when {
    "GET /[VRN]/penalties" should {
      "return a 200 when valid request is made" which {
        "returns full penalties data when VRN matches active penalty data" in new Test {
          stubSuccess(downstreamTestPenaltiesResponseJsonMax)
          val response: WSResponse = await(makeRequest().get())

          response.status shouldBe OK
          response.json shouldBe PenaltiesConstants.upstreamTestPenaltiesResponseJsonMax
          response.header("Content-Type") shouldBe Some("application/json")
        }
        "returns empty penalties data when VRN data has no active penalties" in new Test {
          stubSuccess(testPenaltiesResponseJsonMin)
          val response: WSResponse = await(makeRequest().get())

          response.status shouldBe OK
          response.json shouldBe testPenaltiesResponseJsonMin
          response.header("Content-Type") shouldBe Some("application/json")
        }
      }

      "return a 400" when {
        "VRN value cannot be parsed" in new Test {
          private val overrideUri  = Some(s"/$invalidVrn/penalties")
          val response: WSResponse = await(makeRequest(overrideUri).get())

          response.status shouldBe BAD_REQUEST
          response.json shouldBe Json.toJson(VrnFormatError)
          response.header("Content-Type") shouldBe Some("application/json")
        }

        "VRN is invalid and a 404 is returned from upstream" in new Test {
          private val errorBody = s"""
                                       |{
                                       |  "errors": {
                                       |    "processingDate":"2017-01-01",
                                       |    "code":"016",
                                       |    "text":"Invalid ID Number"
                                       |  }
                                       |}
                                       |""".stripMargin
          stubError(NOT_FOUND, errorBody)
          val response: WSResponse = await(makeRequest().get())

          response.status shouldBe BAD_REQUEST
          response.json shouldBe Json.toJson(PenaltiesInvalidIdValue)
          response.header("Content-Type") shouldBe Some("application/json")
        }
      }

      "return a 500" when {
        "a 200 is returned but the response cannot be parsed" in new Test {
          private val invalidSuccessResponse = Json.obj(
            "totalisations"         -> Some(upstreamTestLatePaymentPenaltyJson),
            "lateSubmissionPenalty" -> Some(upstreamTestLatePaymentPenaltyJson),
            "latePaymentPenalty"    -> Some(upstreamTestLatePaymentPenaltyJson)
          )
          stubSuccess(invalidSuccessResponse)
          val response: WSResponse = await(makeRequest().get())

          response.status shouldBe INTERNAL_SERVER_ERROR
          response.json shouldBe Json.toJson(InvalidJson)
          response.header("Content-Type") shouldBe Some("application/json")
        }

        "there is an error from upstream" in new Test {
          private val errorBody = s"""
                                       |{
                                       |  "errors": {
                                       |    "processingDate":"2017-01-01",
                                       |    "code":"002",
                                       |    "text":"Invalid Tax Regime"
                                       |  }
                                       |}
                                       |""".stripMargin
          stubError(UNPROCESSABLE_ENTITY, errorBody)
          val response: WSResponse = await(makeRequest().get())

          response.status shouldBe INTERNAL_SERVER_ERROR
          response.json shouldBe Json.toJson(DownstreamError)
          response.header("Content-Type") shouldBe Some("application/json")
        }
      }
    }
  }
}
