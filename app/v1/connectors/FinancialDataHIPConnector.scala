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

package v1.connectors

import config.AppConfig
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient}
import v1.models.request.penalties.FinancialRequestHIP
import v1.models.response.financialData.FinancialDataHIPResponse
import v1.connectors.httpparsers.FinancialDataHIPHttpParser._
import v1.models.errors.{ErrorWrapper, MtdError}


import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class FinancialDataHIPConnector @Inject()(val http: HttpClient, val appConfig: AppConfig)
                                         (implicit ec: ExecutionContext) {


  def retrieveFinancialDataHIP(request: FinancialRequestHIP
                              )(implicit hc: HeaderCarrier,
                                correlationId: String): Future[Outcome[FinancialDataHIPResponse]] = {

    val url = s"${appConfig.hipBaseUrl}/RESTAdapter/cross-regime/taxpayer/financial-data/query/"
    val headers = buildHIPHeaders(correlationId)

    http.POST[FinancialRequestHIP, Outcome[FinancialDataHIPResponse]](
      url = url,
      body = request,
      headers = headers
    ).recover {
      case e: Exception =>
        logger.error(s"[FinancialDataHIPConnector][retrieveFinancialDataHIP] Request failed: ${e.getMessage}")
        Left(ErrorWrapper(correlationId, MtdError("DOWNSTREAM_ERROR", e.getMessage)))
    }
  }

    private def buildHIPHeaders(correlationId: String): Seq[(String, String)] = {
      Seq(
        "Authorization" -> s"Basic ${appConfig.hipAuthorisationToken}",
        appConfig.hipEnvironmentHeader,
        appConfig.hipServiceOriginatorIdKeyV1 -> appConfig.hipServiceOriginatorIdV1,
        "CorrelationId" -> correlationId,
        "Originating-System" -> "MDTP",
        "X-Request-Date" -> java.time.Instant.now().toString,
        "X-Transmitting-System" -> "HIP"
      )
    }
}