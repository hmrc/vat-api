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

package v1.nrs

import com.kenshoo.play.metrics.Metrics
import org.joda.time.DateTime
import play.api.libs.json.Json
import play.api.test.FakeRequest
import uk.gov.hmrc.domain.Vrn
import utils.{MockHashUtil, MockMetrics}
import v1.audit.AuditEvents
import v1.controllers.UserRequest
import v1.mocks.nrs.MockNrsConnector
import v1.mocks.services.MockAuditService
import v1.models.audit.NrsAuditDetail
import v1.models.auth.UserDetails
import v1.models.request.submit.{SubmitRequest, SubmitRequestBody}
import v1.nrs.models.NrsTestData.IdentityDataTestData
import v1.nrs.models.request.{Metadata, NrsSubmission, SearchKeys}
import v1.nrs.models.response.{NrsFailure, NrsResponse}
import v1.services.ServiceSpec

import scala.concurrent.Future

class NrsServiceSpec extends ServiceSpec {

  val metrics: Metrics = new MockMetrics

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

  private val submitRequestBodyString = Json.toJson(submitRequestBody).toString

  private val submitRequest: SubmitRequest =
    SubmitRequest(
      vrn = vrn,
      body = submitRequestBody
    )

  private val encodedString: String = "encodedString"
  private val checksum: String = "checksum"

  private val nrsId = "a5894863-9cd7-4d0d-9eee-301ae79cbae6"

  private val nrsSubmission: NrsSubmission =
    NrsSubmission(
      payload = encodedString,
      metadata = Metadata(
        businessId = "vat",
        notableEvent = "vat-return",
        payloadContentType = "application/json",
        payloadSha256Checksum = checksum,
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

  trait Test extends MockNrsConnector with MockAuditService with MockHashUtil {

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
      mockAuditService,
      mockNrsConnector,
      mockHashUtil,
      metrics
    )
  }

  "service" when {
    "service call successful" must {
      "return the expected result" in new Test {

        MockedAuditService.mockAuditEvent(
          AuditEvents.auditNrsSubmit(
            "submitToNonRepudiationStore",
            NrsAuditDetail(
              vrn = vrn.toString,
              authorization = "Bearer aaaa",
              nrSubmissionID = Some(nrsId),
              request = None,
              correlationId = correlationId)
          )
        )

        MockNrsConnector.submitNrs(nrsSubmission)
          .returns(Future.successful(Right(NrsResponse(nrsId, "", ""))))

        MockedHashUtil.encode(submitRequestBodyString).returns(encodedString)
        MockedHashUtil.getHash(submitRequestBodyString).returns(checksum)

        await(service.submit(submitRequest, nrsId, timestamp)) shouldBe Some(NrsResponse("a5894863-9cd7-4d0d-9eee-301ae79cbae6","",""))
      }
    }

    "service call unsuccessful" must {
      "map 4xx errors correctly" in new Test {

        MockedHashUtil.encode(submitRequestBodyString).returns(encodedString)
        MockedHashUtil.getHash(submitRequestBodyString).returns(checksum)

        MockNrsConnector.submitNrs(nrsSubmission)
          .returns(Future.successful(Left(NrsFailure.ExceptionThrown)))

        MockedAuditService.mockAuditEvent(
          AuditEvents.auditNrsSubmit(
            "submitToNonRepudiationStoreFailure",
            NrsAuditDetail(
              vrn = vrn.toString,
              authorization = "Bearer aaaa",
              nrSubmissionID = Some(nrsId),
              request = Some(Json.toJson(nrsSubmission)),
              correlationId = correlationId)
          )
        )

        await(service.submit(submitRequest, nrsId, timestamp)) shouldBe None
      }
    }
  }
}
