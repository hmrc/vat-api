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

import org.scalamock.handlers.CallHandler3
import org.scalamock.scalatest.MockFactory
import uk.gov.hmrc.http.HeaderCarrier
import v1.connectors.FinancialDataHIPConnector
import v1.models.request.penalties.{FinancialRequest, FinancialRequestHIP}
import v1.models.response.financialData.FinancialDataHIPResponse
import v1.services.ServiceOutcome
import v1.support.FinancialRequestMapper

import scala.concurrent.Future

trait MockFinancialDataHIPConnector extends MockFactory {

  val mockFinancialDataHIPConnector: FinancialDataHIPConnector = mock[FinancialDataHIPConnector]


    def mockRetrieveFinancialDataHIP(request: FinancialRequest)(response: ServiceOutcome[FinancialDataHIPResponse]): CallHandler3[FinancialRequestHIP, HeaderCarrier, String, Future[ServiceOutcome[FinancialDataHIPResponse]]] = {
      (mockFinancialDataHIPConnector.retrieveFinancialDataHIP(_: FinancialRequestHIP)(_: HeaderCarrier, _: String))
        .expects(FinancialRequestMapper.toHIP(request), *, * )
        .returns(Future.successful(response))
    }
}
