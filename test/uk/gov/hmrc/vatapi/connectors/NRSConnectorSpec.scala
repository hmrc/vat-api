/*
 * Copyright 2019 HM Revenue & Customs
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

package uk.gov.hmrc.vatapi.connectors

import java.util.concurrent.TimeoutException

import nrs.models.NRSSubmission
import org.scalatestplus.play.OneAppPerSuite
import play.api.libs.json.{JsResultException, Json, Writes}
import play.api.libs.ws.{WSClient, WSRequest, WSResponse}
import uk.gov.hmrc.domain.Vrn
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.vatapi.UnitSpec
import uk.gov.hmrc.vatapi.assets.TestConstants.NRSResponse._
import uk.gov.hmrc.vatapi.httpparsers.NrsSubmissionHttpParser.NrsSubmissionOutcome
import uk.gov.hmrc.vatapi.httpparsers.{EmptyNrsData, NRSData}
import uk.gov.hmrc.vatapi.mocks.MockHttp
import uk.gov.hmrc.vatapi.mocks.config.MockAppContext

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class NRSConnectorSpec extends UnitSpec with OneAppPerSuite
  with MockHttp
  with MockAppContext {

  class Setup {
    val wsClient = mock[WSClient]
    val testNrsConnector = new NRSConnector(mockHttp, mockAppContext, wsClient)

    MockAppContext.nrsMaxTimeoutMilliseconds returns 5000

    val testUrl: String = testNrsConnector.nrsSubmissionUrl(testVrn.vrn)
    def result(requestBody: NRSSubmission): Future[NrsSubmissionOutcome] = testNrsConnector.submit(testVrn, requestBody)
  }

  implicit val hc: HeaderCarrier = HeaderCarrier(nsStamp = 2L)

  val testVrn = Vrn("123456789")

  "NRSConnector.submit" should {

    "successful responses are returned from the connector" should {
      "return the correctly formatted NRS Data model" in new Setup {

        val request = mock[WSRequest]
        val response = mock[WSResponse]
        val json = nrsResponseJson
        val expectedResponse = NRSData("2dd537bc-4244-4ebf-bac9-96321be13cdc","This has been deprecated - DO NOT USE","")


        implicit val nrsWrites = implicitly[Writes[NRSSubmission]]
        val meJson = Json.toJson(nrsSubmission)

        when(wsClient.url(testUrl)).thenReturn(request)
        when(request.withHeaders(any())).thenReturn(request)
        when(request.withRequestTimeout(testNrsConnector.nrsMaxTimeout)).thenReturn(request)
        when(request.post(eqTo(meJson))(any())).thenReturn(Future.successful(response))
        when(response.json).thenReturn(nrsResponseJson)
        when(response.status).thenReturn(202)

        await(result(nrsSubmission)) shouldBe Right(expectedResponse)

      }

    }

    "return EmptyNrsData" when {
      "the connection times out" in new Setup {

        val request = mock[WSRequest]

        implicit val nrsWrites = implicitly[Writes[NRSSubmission]]

        when(wsClient.url(testUrl)).thenReturn(request)
        when(request.withHeaders(any())).thenReturn(request)
        when(request.withRequestTimeout(testNrsConnector.nrsMaxTimeout)).thenReturn(request)
        when(request.post(eqTo(Json.toJson(nrsSubmission)))(any())).thenReturn(Future.failed(new TimeoutException("Expected Error")))

        await(result(nrsSubmission)) shouldBe Right(EmptyNrsData)

      }

      "the response JSON cannot be parsed" in new Setup {

        val request = mock[WSRequest]
        val response = mock[WSResponse]

        implicit val nrsWrites = implicitly[Writes[NRSSubmission]]

        when(wsClient.url(testUrl)).thenReturn(request)
        when(request.withHeaders(any())).thenReturn(request)
        when(request.withRequestTimeout(testNrsConnector.nrsMaxTimeout)).thenReturn(request)
        when(request.post(eqTo(Json.toJson(nrsSubmission)))(any())).thenReturn(Future.successful(response))
        when(response.json).thenThrow(JsResultException(Seq()))
        await(result(nrsSubmission)) shouldBe Right(EmptyNrsData)

      }
    }
  }
}