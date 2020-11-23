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

package v1.connectors

import java.util.concurrent.{TimeUnit, TimeoutException}

import mocks.MockAppConfig
import play.api.libs.json.JsValue
import play.api.libs.ws.WSResponse
import play.api.libs.ws.ahc.AhcWSResponse
import play.api.libs.ws.ahc.cache.{CacheableHttpResponseBodyPart, CacheableHttpResponseStatus}
import play.shaded.ahc.org.asynchttpclient.Response
import play.shaded.ahc.org.asynchttpclient.uri.Uri
import v1.mocks.MockWsClient
import v1.models.nrs.NrsTestData.{FullRequestTestData, NrsResponseTestData}
import v1.models.nrs.request.NrsSubmission
import v1.models.nrs.response.{NrsError, NrsResponse}

import scala.concurrent.Future
import scala.concurrent.duration.Duration

class NrsConnectorSpec extends ConnectorSpec {

  private val nrsSubmissionModel: NrsSubmission = FullRequestTestData.correctModel
  private val nrsSubmissionJson: JsValue = FullRequestTestData.correctJson
  private val nrsResponseJson: JsValue = NrsResponseTestData.correctJson

  class Test extends MockWsClient with MockAppConfig {

    val connector: NrsConnector =
      new NrsConnector(
        ws = mockWsClient,
        appConfig = mockAppConfig
      )

    val nrsRequestHeaders: Seq[(String, String)] =
      Seq(
        "X-API-Key" -> "dummyKey",
        "User-Agent" -> "vat-api"
      )

    MockWsClient.url(s"$baseUrl/submission")
      .returns(mockWsRequest)

    MockWsRequest.withHttpHeaders(nrsRequestHeaders)
      .returns(mockWsRequest)

    MockWsRequest.withRequestTimeout(Duration(100, TimeUnit.MILLISECONDS))
      .returns(mockWsRequest)

    MockedAppConfig.nrsBaseUrl returns baseUrl
    MockedAppConfig.nrsApiKey returns "dummyKey"
    MockedAppConfig.appName returns "vat-api"
    MockedAppConfig.nrsMaxTimeout returns Duration(100, TimeUnit.MILLISECONDS)
  }

  "NrsConnector" when {
    "a valid request is supplied" should {
      "return the expected NrsResponse for a successful response with valid body" in new Test {

        val wsResponse: WSResponse = new AhcWSResponse(new Response.ResponseBuilder()
          .accumulate(new CacheableHttpResponseStatus(Uri.create("http://uri"), ACCEPTED, "status text", "protocols!"))
          .accumulate(new CacheableHttpResponseBodyPart(nrsResponseJson.toString().getBytes, true))
          .build())

        MockWsRequest.post(nrsSubmissionJson)
          .returns(Future.successful(wsResponse))

        await(connector.submitNrs(nrsSubmissionModel, "anId")) shouldBe Right(NrsResponse.empty.copy(nrSubmissionId = "anId"))
      }

      "return an NrsError for an unsuccessful response with error code: 400" in new Test {

        val wsResponse: WSResponse = new AhcWSResponse(new Response.ResponseBuilder()
          .accumulate(new CacheableHttpResponseStatus(Uri.create("http://uri"), BAD_REQUEST, "status text", "protocols!"))
          .accumulate(new CacheableHttpResponseBodyPart("".getBytes, true))
          .build())

        MockWsRequest.post(nrsSubmissionJson)
          .returns(Future.successful(wsResponse))

        await(connector.submitNrs(nrsSubmissionModel, "anId")) shouldBe Left(NrsError)
      }

      "return the default result for an unsuccessful response with unexpected error code" in new Test {

        val wsResponse: WSResponse = new AhcWSResponse(new Response.ResponseBuilder()
          .accumulate(new CacheableHttpResponseStatus(Uri.create("http://uri"), INTERNAL_SERVER_ERROR, "status text", "protocols!"))
          .accumulate(new CacheableHttpResponseBodyPart("".getBytes, true))
          .build())

        MockWsRequest.post(nrsSubmissionJson)
          .returns(Future.successful(wsResponse))

        await(connector.submitNrs(nrsSubmissionModel, "anId")) shouldBe Right(NrsResponse.empty.copy(nrSubmissionId = "anId"))
      }
    }

    "the response time from NRS exceeds the specified request timeout" should {
      "return an error" in new Test {

        MockWsRequest.post(nrsSubmissionJson)
          .returns(Future.failed(new TimeoutException))

        await(connector.submitNrs(nrsSubmissionModel, "anId")) shouldBe Left(NrsError)
      }
    }
  }
}
