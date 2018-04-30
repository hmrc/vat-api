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

import org.joda.time.DateTime
import org.scalatestplus.play.OneAppPerSuite
import play.api.http.Status._
import play.api.libs.json.Json
import uk.gov.hmrc.domain.Vrn
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}
import uk.gov.hmrc.vatapi.UnitSpec
import uk.gov.hmrc.vatapi.assets.TestConstants.VatReturn._
import uk.gov.hmrc.vatapi.config.{AppContext, WSHttp}
import uk.gov.hmrc.vatapi.mocks.MockHttp
import uk.gov.hmrc.vatapi.mocks.config.MockAppContext
import uk.gov.hmrc.vatapi.models.des
import uk.gov.hmrc.vatapi.models.des.{DesError, DesErrorCode}
import uk.gov.hmrc.vatapi.resources.wrappers.VatReturnResponse

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class VatReturnsConnectorSpec extends UnitSpec with OneAppPerSuite
  with MockHttp
  with MockAppContext {

  object TestVatReturnsConnector extends VatReturnsConnector {
    override val http: WSHttp = mockHttp
    override val appContext = mockAppContext
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

    lazy val testUrl: String = s"${AppContext.desUrl}/enterprise/return/vat/" + testVrn
    def result(requestBody: des.VatReturnDeclaration): Future[VatReturnResponse] = TestVatReturnsConnector.post(testVrn, requestBody)

    "return a VatReturnsResponse model with correct body in case of success" in {
      setupMockHttpPostString(testUrl, desVatReturnDeclaration(testDateTime).toJsonString)(successResponse)
      await(result(desVatReturnDeclaration(testDateTime))) shouldBe VatReturnResponse(successResponse)
    }

    "return a VatReturnsResponse with the correct error body when an error is retrieved from DES" in {
      setupMockHttpPostString(testUrl, desVatReturnDeclaration(testDateTime).toJsonString)(invalidPayloadResponse)
      await(result(desVatReturnDeclaration(testDateTime))) shouldBe VatReturnResponse(invalidPayloadResponse)
    }
  }
}

