/*
 * Copyright 2019 HM Revenue & Customs
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

import java.net.URLEncoder

import play.api.Logger
import uk.gov.hmrc.domain.Vrn
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.vatapi.BaseConnector
import uk.gov.hmrc.vatapi.config.{AppContext, WSHttp}
import uk.gov.hmrc.vatapi.models.des
import uk.gov.hmrc.vatapi.resources.wrappers.VatReturnResponse

import scala.concurrent.{ExecutionContext, Future}

object VatReturnsConnector extends VatReturnsConnector {
  override val http: WSHttp = WSHttp
  override val appContext = AppContext
}

trait VatReturnsConnector extends BaseConnector {

  val logger: Logger = Logger(this.getClass)

  def post(vrn: Vrn, vatReturn: des.VatReturnDeclaration)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[VatReturnResponse] = {

    logger.debug(s"[VatReturnsConnector][post] - Submission for 9 box vat return for VRN: $vrn")

    val desUrl = s"${appContext.desUrl}/enterprise/return/vat/$vrn"
    val hybridUrl = s"${appContext.desUrl}/vat/traders/$vrn/returns"

    httpDesPostString[VatReturnResponse](
      url = if (appContext.vatHybridFeatureEnabled) hybridUrl else desUrl,
      elem = vatReturn.toJsonString,
      toResponse = VatReturnResponse
    )
  }

  def query(vrn: Vrn, periodKey: String)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[VatReturnResponse] = {

    logger.debug(s"[VatReturnsConnector][query] - Retrieve vat returns for VRN: $vrn and periodKey: $periodKey")

    val desUrl = s"${appContext.desUrl}/vat/returns/vrn/$vrn"
    val hybridUrl = s"${appContext.desUrl}/vat/traders/$vrn/returns"

    val getUrl: String = s"${if (appContext.vatHybridFeatureEnabled) hybridUrl else desUrl}?period-key=${URLEncoder.encode(periodKey, "UTF-8")}"

    httpGet[VatReturnResponse](getUrl, VatReturnResponse)
  }

}
