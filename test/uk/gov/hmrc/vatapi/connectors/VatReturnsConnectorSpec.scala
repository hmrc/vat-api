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

import java.net.URLEncoder

import org.joda.time.DateTime
import org.scalatestplus.play.OneAppPerSuite
import play.api.http.Status._
import play.api.libs.json.Json
import uk.gov.hmrc.domain.Vrn
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}
import uk.gov.hmrc.vatapi.UnitSpec
import uk.gov.hmrc.vatapi.assets.TestConstants.VatReturn._
import uk.gov.hmrc.vatapi.config.WSHttp
import uk.gov.hmrc.vatapi.mocks.MockHttp
import uk.gov.hmrc.vatapi.mocks.config.MockAppContext
import uk.gov.hmrc.vatapi.models.des.{DesError, DesErrorCode}
import uk.gov.hmrc.vatapi.resources.wrappers.VatReturnResponse

import scala.concurrent.ExecutionContext.Implicits.global

class VatReturnsConnectorSpec extends UnitSpec with OneAppPerSuite
  with MockHttp
  with MockAppContext {

  class Test {
    val connector = new VatReturnsConnector {
      override val http: WSHttp = mockHttp
      override val appContext = mockAppContext
    }
    val testDesUrl = "test"
    MockAppContext.desUrl.returns(testDesUrl)
  }

  implicit val hc: HeaderCarrier = HeaderCarrier()

  val testVrn: Vrn = Vrn("123456789")
  val testDateTime: DateTime = DateTime.now()

  val successResponse = HttpResponse(OK, responseJson = None)
  val invalidPayloadResponse =
    HttpResponse(
      BAD_REQUEST,
      responseJson = Some(Json.toJson(DesError(DesErrorCode.INVALID_PAYLOAD, "Submission has not passed validation. Invalid parameter Payload.")))
    )


  "VatReturnsConnector.post" should {

    "return a VatReturnsResponse model with correct body in case of success" when {

      "pointing to the vat hybrid" in new Test {
        MockAppContext.vatHybridFeatureEnabled.thenReturn(true)
        val testUrl: String = s"$testDesUrl/vat/traders/$testVrn/returns"
        setupMockHttpPostString(testUrl, desVatReturnDeclaration(testDateTime).toJsonString)(successResponse)
        await(connector.post(testVrn, desVatReturnDeclaration(testDateTime))) shouldBe VatReturnResponse(successResponse)
      }

      "pointing to the vat normal" in new Test {
        MockAppContext.vatHybridFeatureEnabled.thenReturn(false)
        val testUrl: String = s"$testDesUrl/enterprise/return/vat/$testVrn"
        setupMockHttpPostString(testUrl, desVatReturnDeclaration(testDateTime).toJsonString)(successResponse)
        await(connector.post(testVrn, desVatReturnDeclaration(testDateTime))) shouldBe VatReturnResponse(successResponse)
      }
    }

    "return a VatReturnsResponse with the correct error body when an error is retrieved from DES" when {

      "pointing to the vat hybrid" in new Test {
        MockAppContext.vatHybridFeatureEnabled.thenReturn(true)
        val testUrl: String = s"$testDesUrl/vat/traders/$testVrn/returns"
        setupMockHttpPostString(testUrl, desVatReturnDeclaration(testDateTime).toJsonString)(invalidPayloadResponse)
        await(connector.post(testVrn, desVatReturnDeclaration(testDateTime))) shouldBe VatReturnResponse(invalidPayloadResponse)
      }

      "pointing to the vat normal" in new Test {
        MockAppContext.vatHybridFeatureEnabled.thenReturn(false)
        val testUrl: String = s"$testDesUrl/enterprise/return/vat/$testVrn"
        setupMockHttpPostString(testUrl, desVatReturnDeclaration(testDateTime).toJsonString)(invalidPayloadResponse)
        await(connector.post(testVrn, desVatReturnDeclaration(testDateTime))) shouldBe VatReturnResponse(invalidPayloadResponse)
      }
    }

    "VatReturnsConnector.query" should {

      val periodKey = "test"
      "return a VatReturnsResponse model in case of success" when {

        "pointing to the vat hybrid" in new Test {
          MockAppContext.vatHybridFeatureEnabled.thenReturn(true)
          val testUrl: String = s"$testDesUrl/vat/traders/$testVrn/returns?period-key=${URLEncoder.encode(periodKey, "UTF-8")}"
          setupMockHttpGet(testUrl)(successResponse)
          await(connector.query(testVrn, periodKey)) shouldBe VatReturnResponse(successResponse)
        }

        "pointing to the vat normal" in new Test {
          MockAppContext.vatHybridFeatureEnabled.thenReturn(false)
          val testUrl: String = s"$testDesUrl/vat/returns/vrn/$testVrn?period-key=${URLEncoder.encode(periodKey, "UTF-8")}"
          setupMockHttpGet(testUrl)(successResponse)
          await(connector.query(testVrn, periodKey)) shouldBe VatReturnResponse(successResponse)
        }
      }

      "return a VatReturnsResponse with the correct error body when an error is retrieved from DES" when {

        "pointing to the vat hybrid" in new Test {
          MockAppContext.vatHybridFeatureEnabled.thenReturn(true)
          val testUrl: String = s"$testDesUrl/vat/traders/$testVrn/returns?period-key=${URLEncoder.encode(periodKey, "UTF-8")}"
          setupMockHttpGet(testUrl)(invalidPayloadResponse)
          await(connector.query(testVrn, periodKey)) shouldBe VatReturnResponse(invalidPayloadResponse)
        }

        "pointing to the vat normal" in new Test {
          MockAppContext.vatHybridFeatureEnabled.thenReturn(false)
          val testUrl: String = s"$testDesUrl/vat/returns/vrn/$testVrn?period-key=${URLEncoder.encode(periodKey, "UTF-8")}"
          setupMockHttpGet(testUrl)(invalidPayloadResponse)
          await(connector.query(testVrn, periodKey)) shouldBe VatReturnResponse(invalidPayloadResponse)
        }
      }
    }
  }
}

