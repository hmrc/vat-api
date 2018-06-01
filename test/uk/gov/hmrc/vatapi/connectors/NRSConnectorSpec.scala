/*
 * Copyright 2018 HM Revenue & Customs
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

import nrs.models.NRSSubmission
import org.scalatestplus.play.OneAppPerSuite
import play.api.http.Status._
import play.api.libs.json.Json
import uk.gov.hmrc.domain.Vrn
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}
import uk.gov.hmrc.vatapi.UnitSpec
import uk.gov.hmrc.vatapi.assets.TestConstants.NRSResponse._
import uk.gov.hmrc.vatapi.config.WSHttp
import uk.gov.hmrc.vatapi.httpparsers.NrsSubmissionHttpParser.NrsSubmissionOutcome
import uk.gov.hmrc.vatapi.mocks.MockHttp
import uk.gov.hmrc.vatapi.mocks.config.MockAppContext

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class NRSConnectorSpec extends UnitSpec with OneAppPerSuite
  with MockHttp
  with MockAppContext {

  object TestNRSConnector extends NRSConnector {
    override val http: WSHttp = mockHttp
    override val appContext = mockAppContext
  }

  implicit val hc: HeaderCarrier = HeaderCarrier()

  val testVrn = Vrn("123456789")

  val successResponse = HttpResponse(ACCEPTED, responseJson = Some(Json.toJson(nrsData)))
  val errorResponse = HttpResponse(BAD_REQUEST, responseString = Some("Error Message"))


  "NRSConnector.submit" should {

    lazy val testUrl: String = TestNRSConnector.nrsSubmissionUrl
    def result(requestBody: NRSSubmission): Future[NrsSubmissionOutcome] = TestNRSConnector.submit(testVrn, requestBody)

    "successful responses are returned from the connector" should {
      "return the correctly formatted NRS Data model" in {
        setupMockHttpPost(testUrl, nrsSubmission)(successResponse)
        await(result(nrsSubmission)) shouldBe successResponse
      }
    }

    "error responses are returned from the connector" should {
      "return an NRS Error model" in {
        setupMockHttpPost(testUrl, nrsSubmission)(errorResponse)
        await(result(nrsSubmission)) shouldBe errorResponse
      }
    }
  }
}