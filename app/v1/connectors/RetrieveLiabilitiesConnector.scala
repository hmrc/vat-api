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

package v1.connectors

import config.AppConfig
import javax.inject.{Inject, Singleton}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.http.HttpClient
import v1.models.request.liability.LiabilityRequest
import v1.models.response.liability.LiabilityResponse

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class RetrieveLiabilitiesConnector @Inject()(val http: HttpClient,
                                    val appConfig: AppConfig) extends BaseDesConnector {

  def retrieveLiabilities(request: LiabilityRequest)(implicit hc: HeaderCarrier,
                                                     ec: ExecutionContext): Future[DesOutcome[LiabilityResponse]] = {
    import v1.connectors.httpparsers.StandardDesHttpParser._
    implicit val requestFromDate: String = request.from
    val queryString = "&onlyOpenItems=false&includeLocks=false&calculateAccruedInterest=true&customerPaymentInformation=true"
    get(
      uri = DesUri[LiabilityResponse](s"VRN/${request.vrn.vrn}/VATC?dateFrom=${request.from}&dateTo=${request.to}$queryString")
    )
  }
}