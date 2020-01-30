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

package uk.gov.hmrc.vatapi.services

import org.joda.time.DateTime
import org.scalamock.scalatest.MockFactory
import org.scalatest.concurrent.ScalaFutures
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.http.Status.{BAD_REQUEST, OK}
import play.api.libs.json.Json
import uk.gov.hmrc.domain.Vrn
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}
import uk.gov.hmrc.vatapi.UnitSpec
import uk.gov.hmrc.vatapi.assets.TestConstants.NRSResponse._
import uk.gov.hmrc.vatapi.assets.TestConstants.VatReturn._
import uk.gov.hmrc.vatapi.mocks.connectors.MockVatReturnsConnector
import uk.gov.hmrc.vatapi.models.des
import uk.gov.hmrc.vatapi.models.des.{DesError, DesErrorCode}
import uk.gov.hmrc.vatapi.resources.wrappers.VatReturnResponse

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class VatReturnsServiceSpec extends UnitSpec with GuiceOneAppPerSuite with MockFactory with ScalaFutures with MockVatReturnsConnector {

  val testVatReturnsService = new VatReturnsService(mockVatReturnsConnector)

  implicit val hc: HeaderCarrier = HeaderCarrier()
  val testDateTime: DateTime = DateTime.now()

  val successResponse = VatReturnResponse(HttpResponse(OK, responseJson = Some(Json.toJson(vatReturnsDes))))
  val invalidPayloadResponse =
    VatReturnResponse(HttpResponse(
      BAD_REQUEST,
      responseJson = Some(Json.toJson(DesError(DesErrorCode.INVALID_PAYLOAD, "Submission has not passed validation. Invalid parameter Payload.")))
    ))


  "VatReturnsService.submit" when {

    lazy val testVrn: Vrn = Vrn("123456789")
    def result(submission: des.VatReturnDeclaration): Future[VatReturnResponse] = testVatReturnsService.submit(testVrn, submission)

    "successful responses are returned from the connector" should {
      "return the correctly formatted VatReturnsDes Data model" in {
        setupVatReturnSubmission(testVrn, desVatReturnDeclaration(timestamp))(successResponse)
        await(result(desVatReturnDeclaration(timestamp))) shouldBe successResponse
      }
    }

    "error responses are returned from the connector" should {
      "return a Des Error model" in {
        setupVatReturnSubmission(testVrn, desVatReturnDeclaration(timestamp))(invalidPayloadResponse)
        await(result(desVatReturnDeclaration(timestamp))) shouldBe invalidPayloadResponse
      }
    }
  }
}
