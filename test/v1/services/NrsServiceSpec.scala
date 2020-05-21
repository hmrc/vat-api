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

package v1.services

import java.nio.charset.StandardCharsets
import java.util.Base64

import org.joda.time.DateTime
import play.api.libs.json.Json
import play.api.test.FakeRequest
import uk.gov.hmrc.domain.Vrn
import v1.controllers.UserRequest
import v1.mocks.connectors.MockNrsConnector
import v1.models.auth.UserDetails
import v1.models.errors.{DownstreamError, ErrorWrapper}
import v1.models.nrs.NrsTestData.IdentityDataTestData
import v1.models.nrs.request.{Metadata, NrsSubmission, SearchKeys}
import v1.models.nrs.response.{NrsError, NrsResponse}
import v1.models.request.submit.{SubmitRequest, SubmitRequestBody}

import scala.concurrent.Future

class NrsServiceSpec extends ServiceSpec {

  private val vrn: Vrn = Vrn("123456789")

  private val timestamp: DateTime = DateTime.parse("2018-04-07T12:13:25.156Z")

  private val submitRequestBody: SubmitRequestBody =
    SubmitRequestBody(
      periodKey = Some("F034"),
      vatDueSales = Some(4567.23),
      vatDueAcquisitions = Some(-456675.5),
      totalVatDue = Some(7756.65),
      vatReclaimedCurrPeriod = Some(-756822354.64),
      netVatDue = Some(8956743245.12),
      totalValueSalesExVAT = Some(43556767890.00),
      totalValuePurchasesExVAT = Some(34556790.00),
      totalValueGoodsSuppliedExVAT = Some(34556.00),
      totalAcquisitionsExVAT = Some(-68978.00),
      finalised = Some(true),
      receivedAt = None,
      agentReference = None
    )

  private val submitRequest: SubmitRequest =
    SubmitRequest(
      vrn = vrn,
      body = submitRequestBody
    )

  private val payloadString: String =
    Base64.getEncoder.encodeToString(
      Json.toJson(submitRequestBody)
        .toString()
        .getBytes(StandardCharsets.UTF_8)
    )

  private val nrsSubmission: NrsSubmission =
    NrsSubmission(
      payload = payloadString,
      metadata = Metadata(
        businessId = "vat",
        notableEvent = "vat-return",
        payloadContentType = "application/json",
        payloadSha256Checksum = None,
        userSubmissionTimestamp = timestamp,
        identityData = Some(IdentityDataTestData.correctModel),
        userAuthToken = "Bearer aaaa",
        headerData = Json.toJson(Map(
          "Host" -> "localhost",
          "Authorization" -> "Bearer aaaa",
          "dummyHeader1" -> "dummyValue1",
          "dummyHeader2" -> "dummyValue2"
        )),
        searchKeys =
          SearchKeys(
            vrn = Some(vrn.vrn),
            companyName = None,
            periodKey = submitRequestBody.periodKey,
            taxPeriodEndDate = None
          )
      )
    )

  private val nrsResponse: NrsResponse =
    NrsResponse(
      "id",
      "This has been deprecated - DO NOT USE",
      ""
    )

  trait Test extends MockNrsConnector {

    implicit val userRequest: UserRequest[_] =
      UserRequest(
        userDetails =
          UserDetails(
            userType = "Individual",
            agentReferenceNumber = None,
            clientId = "aClientId",
            identityData = Some(IdentityDataTestData.correctModel)
          ),
        request = FakeRequest().withHeaders(
          "Authorization" -> "Bearer aaaa",
          "dummyHeader1" -> "dummyValue1",
          "dummyHeader2" -> "dummyValue2"
        )
      )

    val service: NrsService = new NrsService(
        mockNrsConnector
      )
  }

  "service" when {
    "service call successful" must {
      "return the expected result" in new Test {

        MockNrsConnector.submitNrs(nrsSubmission)
          .returns(Future.successful(Right(nrsResponse)))

        await(service.submitNrs(submitRequest, timestamp)) shouldBe Right(nrsResponse)
      }
    }

    "service call successful (empty response)" must {
      "return the expected result" in new Test {

        MockNrsConnector.submitNrs(nrsSubmission)
          .returns(Future.successful(Right(NrsResponse.empty)))

        await(service.submitNrs(submitRequest, timestamp)) shouldBe Right(NrsResponse.empty)
      }
    }

    "service call unsuccessful" must {
      "map errors correctly" in new Test {

        MockNrsConnector.submitNrs(nrsSubmission)
          .returns(Future.successful(Left(NrsError)))

        await(service.submitNrs(submitRequest, timestamp)) shouldBe Left(ErrorWrapper(None, DownstreamError, None))
      }
    }
  }
}
