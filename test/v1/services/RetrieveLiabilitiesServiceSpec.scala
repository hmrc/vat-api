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

import support.UnitSpec
import uk.gov.hmrc.domain.Vrn
import uk.gov.hmrc.http.HeaderCarrier
import utils.EndpointLogContext
import v1.mocks.connectors.MockRetrieveLiabilitiesConnector
import v1.models.errors._
import v1.models.outcomes.ResponseWrapper
import v1.models.request.liability.LiabilityRequest
import v1.models.response.liability.{Liability, LiabilityResponse}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class RetrieveLiabilitiesServiceSpec extends UnitSpec {

  private val vrn: String = "123456789"
  private val from: String = "2017-1-1"
  private val to: String = "2017-12-31"
  private val correlationId = "X-123"

  private val retrieveLiabilitiesRequest: LiabilityRequest =
    LiabilityRequest(
      vrn = Vrn(vrn),
      from = from,
      to = to
    )

  private val retrieveLiabilitiesResponse: LiabilityResponse =
    LiabilityResponse(Seq(Liability(None,"VAT",1.0,None,None)))

  trait Test extends MockRetrieveLiabilitiesConnector {

    implicit val hc: HeaderCarrier = HeaderCarrier()
    implicit val ec: EndpointLogContext = EndpointLogContext("c", "ep")

    val service = new RetrieveLiabilitiesService(
      connector = mockRetrieveLiabilitiesConnector
    )
  }

  "service" when {
    "service call successful" must {
      "return the mapped result" in new Test {

        MockRetrieveLiabilitiesConnector.retrieveLiabilities(retrieveLiabilitiesRequest)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, retrieveLiabilitiesResponse))))

        await(service.retrieveLiabilities(retrieveLiabilitiesRequest)) shouldBe Right(ResponseWrapper(correlationId, retrieveLiabilitiesResponse))
      }

      "return a 404 Not Found for an empty liabilities response" in new Test {

        MockRetrieveLiabilitiesConnector.retrieveLiabilities(retrieveLiabilitiesRequest)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, LiabilityResponse(Seq.empty[Liability])))))

        await(service.retrieveLiabilities(retrieveLiabilitiesRequest)) shouldBe Left(ErrorWrapper(Some(correlationId), NotFoundError))
      }
    }

    "service call unsuccessful" must {
      "map errors according to spec" when {

        def serviceError(desErrorCode: String, error: MtdError): Unit =
          s"a $desErrorCode error is returned from the service" in new Test {

            MockRetrieveLiabilitiesConnector.retrieveLiabilities(retrieveLiabilitiesRequest)
              .returns(Future.successful(Left(ResponseWrapper(correlationId, DesErrors.single(DesErrorCode(desErrorCode))))))

            await(service.retrieveLiabilities(retrieveLiabilitiesRequest)) shouldBe Left(ErrorWrapper(Some(correlationId), error))
          }

        val input: Seq[(String, MtdError)] = Seq(
          "INVALID_IDTYPE" -> DownstreamError,
          "INVALID_IDNUMBER" -> VrnFormatErrorDes,
          "INVALID_REGIMETYPE" -> DownstreamError,
          "INVALID_ONLYOPENITEMS" -> DownstreamError,
          "INVALID_INCLUDELOCKS" -> DownstreamError,
          "INVALID_CALCULATEACCRUEDINTEREST" -> DownstreamError,
          "INVALID_CUSTOMERPAYMENTINFORMATION" -> DownstreamError,
          "INVALID_DATEFROM" -> InvalidDateFromError,
          "INVALID_DATETO" -> InvalidDateToError,
          "NOT_FOUND" -> LegacyNotFoundError,
          "INVALID_DATA" -> InvalidDataError,
          "SERVER_ERROR" -> DownstreamError,
          "SERVICE_UNAVAILABLE" -> DownstreamError
        )

        input.foreach(args => (serviceError _).tupled(args))
      }
    }
  }
}
