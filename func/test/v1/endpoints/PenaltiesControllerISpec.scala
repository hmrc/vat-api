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
import play.api.libs.json.{JsValue, Json}
import play.api.libs.ws.{WSRequest, WSResponse}
import play.api.test.Helpers.AUTHORIZATION
import support.IntegrationBaseSpec
import v1.constants.PenaltiesConstants
import v1.models.errors.{MtdError, PenaltiesInvalidIdValue, VrnFormatError}
import v1.stubs.{AuditStub, AuthStub, PenaltiesStub}

class PenaltiesControllerISpec extends IntegrationBaseSpec {

  implicit val appConfig = app.injector.instanceOf[AppConfig]

  private trait Test {

    def uri: String = s"/${PenaltiesConstants.vrn}/penalties"

    def setupStubs(): StubMapping

    def request: WSRequest = {
      setupStubs()
      buildRequest(uri)
        .withHttpHeaders(
          (ACCEPT, "application/vnd.hmrc.1.0+json"),
          (AUTHORIZATION, "Bearer 123")
        )
    }
  }

  "PenaltiesController" when {

    "GET /[VRN]/penalties" when {

      "raw vrn cannot be parsed" must {

        "return BadRequest" in new Test {

          override def uri: String = s"/${PenaltiesConstants.invalidVrn}/penalties"

          override def setupStubs(): StubMapping = {
            AuditStub.audit()
            AuthStub.authorised()
          }

          val response: WSResponse = await(request.get())
          response.status shouldBe BAD_REQUEST
          response.json shouldBe Json.toJson(VrnFormatError)
          response.header("Content-Type") shouldBe Some("application/json")
        }
      }

      "vrn can be parsed" when {

        "a valid request is made" must {

          "return 200 and penalties data (full)" in new Test {

            override def setupStubs(): StubMapping = {
              AuditStub.audit()
              AuthStub.authorised()
              PenaltiesStub.onSuccess(PenaltiesStub.GET, PenaltiesConstants.penaltiesURl(), OK, PenaltiesConstants.downstreamTestPenaltiesResponseJsonMax)
            }

            val response: WSResponse = await(request.get())
            response.status shouldBe OK
            response.json shouldBe PenaltiesConstants.upstreamTestPenaltiesResponseJsonMax
            response.header("Content-Type") shouldBe Some("application/json")
          }
          "return 200 and penalties data (min with new optional fields missing)" in new Test {

            override def setupStubs(): StubMapping = {
              AuditStub.audit()
              AuthStub.authorised()
              PenaltiesStub.onSuccess(PenaltiesStub.GET, PenaltiesConstants.penaltiesURl(), OK, PenaltiesConstants.downstreamTestPenaltiesResponseJsonMissingFields)
            }

            val response: WSResponse = await(request.get())
            response.status shouldBe OK
            response.json shouldBe PenaltiesConstants.upstreamTestPenaltiesResponseJsonMissingField
            response.header("Content-Type") shouldBe Some("application/json")
          }
        }

        "an invalid request is made" must {

          "return 500" in new Test {
            val errorBody: JsValue = Json.parse(
              """
                |{
                |"failures": [{
                | "code":"DOWNSTREAM_ERROR",
                | "reason":"test exception"
                |}]
                |}
                |""".stripMargin)
            override def setupStubs(): StubMapping = {
              AuditStub.audit()
              AuthStub.authorised()
              PenaltiesStub.onError(PenaltiesStub.GET, PenaltiesConstants.penaltiesURl(), INTERNAL_SERVER_ERROR, errorBody)
            }

            val response: WSResponse = await(request.get())
            response.status shouldBe INTERNAL_SERVER_ERROR
            response.json shouldBe Json.toJson(PenaltiesConstants.errorWrapper(MtdError("DOWNSTREAM_ERROR", "test exception")))
            response.header("Content-Type") shouldBe Some("application/json")
          }
        }

        "VRN is invalid" must {
          "return 400 error" in new Test {

            val errorBody: JsValue = Json.parse(
              """
                |{
                |"failures": [{
                | "code":"INVALID_IDVALUE",
                | "reason":"Some Reason"
                |}
                |]
                |}
                |""".stripMargin)

            val expectedJson: JsValue = Json.toJson(PenaltiesInvalidIdValue)

            override def setupStubs(): StubMapping = {
              AuditStub.audit()
              AuthStub.authorised()
              PenaltiesStub.onError(PenaltiesStub.GET, PenaltiesConstants.penaltiesURl(), BAD_REQUEST, errorBody)
            }

            val response: WSResponse = await(request.get())
            response.status shouldBe BAD_REQUEST
            response.json shouldBe expectedJson
            response.header("Content-Type") shouldBe Some("application/json")
          }
        }

        "unexpected error" must {

          "return 500" in new Test {

            val errorBody: JsValue = Json.parse(
              """
                |{
                |"failures": [{
                | "code":"REASON",
                | "reason":"Some Reason"
                |}]
                |}
                |""".stripMargin)

            override def setupStubs(): StubMapping = {
              AuditStub.audit()
              AuthStub.authorised()
              PenaltiesStub.onError(PenaltiesStub.GET, PenaltiesConstants.penaltiesURl(), INTERNAL_SERVER_ERROR, errorBody)
            }

            val response: WSResponse = await(request.get())
            response.status shouldBe INTERNAL_SERVER_ERROR
            response.json shouldBe Json.toJson(MtdError("REASON", "Some Reason"))
            response.header("Content-Type") shouldBe Some("application/json")
          }

          "multiple errors return 500" in new Test {

            val errorBody: JsValue = Json.parse(
              """
                |{
                |"failures": [{
                | "code":"REASON",
                | "reason":"Some Reason"
                |},
                |{
                |"code":"INVALID_IDVALUE",
                | "reason":"Some Reason"
                |}
                |]
                |}
                |""".stripMargin)

            override def setupStubs(): StubMapping = {
              AuditStub.audit()
              AuthStub.authorised()
              PenaltiesStub.onError(PenaltiesStub.GET, PenaltiesConstants.penaltiesURl(), INTERNAL_SERVER_ERROR, errorBody)
            }

            val response: WSResponse = await(request.get())
            response.status shouldBe INTERNAL_SERVER_ERROR
            response.json shouldBe Json.toJson(MtdError("INVALID_REQUEST", "Invalid request penalties",
              Some(Json.toJson(Seq(
                MtdError("REASON", "Some Reason"),
                MtdError("VRN_INVALID", "The provided VRN is invalid.")
              )))))
            response.header("Content-Type") shouldBe Some("application/json")
          }
        }
      }
    }
  }
}
