/*
 * Copyright 2024 HM Revenue & Customs
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

import uk.gov.hmrc.http.HeaderCarrier
import utils.Logging
import v1.connectors.FinancialDataHIPConnector
import v1.models.request.penalties.{FinancialRequest, FinancialRequestHIP}
import v1.models.response.financialData.FinancialDataHIPResponse
import v1.support.FinancialRequestMapper

import javax.inject.{Inject, Singleton}
import scala.concurrent.Future

@Singleton
class FinancialDataHIPService @Inject()(hipConnector: FinancialDataHIPConnector) extends Logging {

  def retrieveFinancialDataHIP(request: FinancialRequest)
                       (implicit hc: HeaderCarrier,
                        correlationId: String): Future[ServiceOutcome[FinancialDataHIPResponse]] = {

    val hipRequest: FinancialRequestHIP = FinancialRequestMapper.toHIP(request)
      hipConnector.retrieveFinancialDataHIP(hipRequest)

  }
}
