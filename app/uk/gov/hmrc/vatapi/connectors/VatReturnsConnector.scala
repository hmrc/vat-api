/*
 * Copyright 2017 HM Revenue & Customs
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
import uk.gov.hmrc.vatapi.models.{VatReturn, des}
import uk.gov.hmrc.vatapi.resources.wrappers.VatReturnsResponse

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

object VatReturnsConnector {
  private lazy val baseUrl: String = AppContext.desUrl

  def post(vrn: Vrn, vatReturn: VatReturn)(
      implicit hc: HeaderCarrier): Future[VatReturnsResponse] =
    httpPost[des.VatReturn, VatReturnsResponse](
      baseUrl + s"/enterprise/return/vat/$vrn",
      des.VatReturn.from(vatReturn),
      VatReturnsResponse)

  def get(vrn: Vrn)(implicit hc: HeaderCarrier): Future[VatReturnsResponse] =
    httpGet[VatReturnsResponse](baseUrl + s"/enterprise/return/vat/$vrn",
                                VatReturnsResponse)

}
