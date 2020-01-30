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

package uk.gov.hmrc.vatapi.connectors

import javax.inject.Inject
import play.api.Logger
import uk.gov.hmrc.domain.Vrn
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.http.DefaultHttpClient
import uk.gov.hmrc.vatapi.config.AppContext
import uk.gov.hmrc.vatapi.models.FinancialDataQueryParams
import uk.gov.hmrc.vatapi.resources.wrappers.FinancialDataResponse

import scala.concurrent.{ExecutionContext, Future}

class FinancialDataConnector @Inject()(
                                        override val http: DefaultHttpClient,
                                        override val appContext: AppContext
                                      ) extends BaseConnector {

  private lazy val baseUrl: String = s"${appContext.desUrl}/enterprise/financial-data"
  val logger: Logger = Logger(this.getClass)

  def getFinancialData(vrn: Vrn, params: FinancialDataQueryParams)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[FinancialDataResponse] = {

    logger.debug(s"[FinancialDataConnector][getFinancialData] Retrieve financial data for VRN $vrn.")

    val queryString = s"dateFrom=${params.from}&dateTo=${params.to}&"
    httpGet[FinancialDataResponse](
      s"$baseUrl/VRN/$vrn/VATC?${queryString}onlyOpenItems=false&includeLocks=false&calculateAccruedInterest=true&customerPaymentInformation=true", FinancialDataResponse)
  }
}
