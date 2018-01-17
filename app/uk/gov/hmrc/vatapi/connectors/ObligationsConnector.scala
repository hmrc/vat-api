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

import uk.gov.hmrc.domain.Vrn
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.vatapi.config.AppContext
import uk.gov.hmrc.vatapi.models.ObligationsQueryParams
import uk.gov.hmrc.vatapi.resources.wrappers.ObligationsResponse
import scala.concurrent.ExecutionContext.Implicits.global

import scala.concurrent.Future

object ObligationsConnector {
  private lazy val baseUrl: String = AppContext.desUrl

  def get(vrn: Vrn, queryParams: ObligationsQueryParams)(implicit hc: HeaderCarrier): Future[ObligationsResponse] = {
    val queryString = s"from=${queryParams.from}&to=${queryParams.to}&status=${queryParams.status}"
    httpGet[ObligationsResponse](baseUrl + s"/enterprise/obligation-data/vrn/$vrn/VATC?$queryString", ObligationsResponse)
  }


}
