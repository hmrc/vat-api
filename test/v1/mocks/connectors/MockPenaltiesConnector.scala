/*
 * Copyright 2023 HM Revenue & Customs
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

package v1.mocks.connectors

import org.scalamock.handlers.CallHandler5
import org.scalamock.scalatest.MockFactory
import uk.gov.hmrc.http.HeaderCarrier
import v1.connectors.{Outcome, PenaltiesConnector}
import v1.controllers.UserRequest
import v1.models.request.penalties.{FinancialRequest, PenaltiesRequest}
import v1.models.response.financialData.FinancialDataResponse
import v1.models.response.penalties.PenaltiesResponse

import scala.concurrent.{ExecutionContext, Future}

trait MockPenaltiesConnector extends MockFactory {

  val mockPenaltiesConnector: PenaltiesConnector = mock[PenaltiesConnector]

  def mockRetrievePenalties(penaltiesRequest: PenaltiesRequest,
                            penaltiesResponse: Outcome[PenaltiesResponse]
                           ): CallHandler5[PenaltiesRequest, HeaderCarrier, ExecutionContext, UserRequest[_], String, Future[Outcome[PenaltiesResponse]]] = {
    (mockPenaltiesConnector
      .retrievePenalties(_: PenaltiesRequest)(_: HeaderCarrier, _: ExecutionContext, _: UserRequest[_], _: String))
      .expects(penaltiesRequest, *, *, *, *)
      .returns(Future.successful(penaltiesResponse))
  }

  def mockRetrieveFinancialData(financialRequest: FinancialRequest,
                                financialDataResponse: Outcome[FinancialDataResponse]
                           ): CallHandler5[FinancialRequest, HeaderCarrier, ExecutionContext, UserRequest[_], String, Future[Outcome[FinancialDataResponse]]] = {
    (mockPenaltiesConnector
      .retrieveFinancialData(_: FinancialRequest)(_: HeaderCarrier, _: ExecutionContext, _: UserRequest[_], _: String))
      .expects(financialRequest, *, *, *, *)
      .returns(Future.successful(financialDataResponse))
  }
}