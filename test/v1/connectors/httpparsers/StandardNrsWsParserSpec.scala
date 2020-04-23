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
import play.api.libs.json.Json
import play.api.libs.ws.WSResponse
import play.api.libs.ws.ahc.AhcWSResponse
import play.api.libs.ws.ahc.cache.{CacheableHttpResponseBodyPart, CacheableHttpResponseStatus}
import play.shaded.ahc.org.asynchttpclient.Response
import play.shaded.ahc.org.asynchttpclient.uri.Uri
import support.UnitSpec
import v1.connectors.NrsOutcome
import v1.models.nrs.response.NrsError

class StandardNrsWsParserSpec extends UnitSpec {

  val method = "POST"
  val url = "test-url"

  import v1.connectors.httpparsers.StandardNrsWsParser._

  def wsReads: WsReads[NrsOutcome[SomeModel]] = implicitly

  val data = "someData"
  val nrsExpectedJson: Array[Byte] = Json.obj("data" -> data).toString().getBytes()

  val nrsResponse = SomeModel(data)

  "A generic WS parser" when {
    "no status code is specified" must {
      "return the expected success response for an ACCEPTED status" in {

        val wsResponse: WSResponse = new AhcWSResponse(new Response.ResponseBuilder()
          .accumulate(new CacheableHttpResponseStatus(Uri.create("http://uri"), ACCEPTED, "status text", "protocols!"))
          .accumulate(new CacheableHttpResponseBodyPart(nrsExpectedJson, true))
          .build());

        wsReads.wsRead(wsResponse, Right(SomeModel(""))) shouldBe Right(nrsResponse)
      }

      "return an error response for a BAD_REQUEST status" in {

        val wsResponse: WSResponse = new AhcWSResponse(new Response.ResponseBuilder()
          .accumulate(new CacheableHttpResponseStatus(Uri.create("http://uri"), BAD_REQUEST, "status text", "protocols!"))
          .accumulate(new CacheableHttpResponseBodyPart("".getBytes(), true))
          .build());

        wsReads.wsRead(wsResponse, Right(SomeModel(""))) shouldBe Left(NrsError)
      }

      "return the default response for an unexpected status" in {

        val wsResponse: WSResponse = new AhcWSResponse(new Response.ResponseBuilder()
          .accumulate(new CacheableHttpResponseStatus(Uri.create("http://uri"), IM_A_TEAPOT, "status text", "protocols!"))
          .accumulate(new CacheableHttpResponseBodyPart(nrsExpectedJson, true))
          .build());

        wsReads.wsRead(wsResponse, Right(SomeModel(""))) shouldBe Right(SomeModel(""))
      }
    }

    "a status code is specified" must {
      "return the expected success response for that status" in {

        implicit val successCode: SuccessCode = SuccessCode(OK)
        def wsReads: WsReads[NrsOutcome[SomeModel]] = implicitly

        val wsResponse: WSResponse = new AhcWSResponse(new Response.ResponseBuilder()
          .accumulate(new CacheableHttpResponseStatus(Uri.create("http://uri"), OK, "status text", "protocols!"))
          .accumulate(new CacheableHttpResponseBodyPart(nrsExpectedJson, true))
          .build());

        wsReads.wsRead(wsResponse, Right(SomeModel(""))) shouldBe Right(nrsResponse)
      }
    }

    "a response contains JSON which cannot be parsed as the expected response type" must {
      "return an error response" in {

        val wsResponse: WSResponse = new AhcWSResponse(new Response.ResponseBuilder()
          .accumulate(new CacheableHttpResponseStatus(Uri.create("http://uri"), ACCEPTED, "status text", "protocols!"))
          .accumulate(new CacheableHttpResponseBodyPart("".getBytes(), true))
          .build());

        wsReads.wsRead(wsResponse, Right(SomeModel(""))) shouldBe Left(NrsError)
      }
    }
  }
}
