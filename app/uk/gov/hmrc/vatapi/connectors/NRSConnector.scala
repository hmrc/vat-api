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

import java.util.concurrent.TimeoutException

import javax.inject.Inject
import play.api.Logger
import play.api.libs.json.{JsValue, Json, Writes}
import play.api.libs.ws.WSClient
import uk.gov.hmrc.domain.Vrn
import uk.gov.hmrc.http._
import uk.gov.hmrc.play.bootstrap.http.DefaultHttpClient
import uk.gov.hmrc.vatapi.BaseConnector
import uk.gov.hmrc.vatapi.config.AppContext
import uk.gov.hmrc.vatapi.httpparsers.EmptyNrsData
import uk.gov.hmrc.vatapi.httpparsers.NrsSubmissionHttpParser.{NrsSubmissionOutcome, NrsSubmissionOutcomeReads}
import uk.gov.hmrc.vatapi.models.NRSSubmission

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Success, Try}


class NRSConnector @Inject()(
                              override val http: DefaultHttpClient,
                              override val appContext: AppContext,
                              ws: WSClient
                            ) extends BaseConnector {

  val logger: Logger = Logger(this.getClass)
  val nrsSubmissionUrl: String => String = vrn => s"${appContext.nrsServiceUrl}/submission"
  val nrsMaxTimeout: Duration = appContext.nrsMaxTimeoutMillis.milliseconds

  private val xApiKeyHeader = "X-API-Key"

  def submit(vrn: Vrn, nrsSubmission: NRSSubmission)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[NrsSubmissionOutcome] = {

    logger.debug(s"[NRSConnector][submit] - Submission to NRS for 9 box vat return for VRN: $vrn")

    val nrsResponse = {
      val submitUrl = nrsSubmissionUrl(vrn.toString)
      val headers = hc.withExtraHeaders(xApiKeyHeader -> s"${appContext.xApiKey}", "User-Agent" -> appContext.appName).headers

      implicit val nrsWrites = implicitly[Writes[NRSSubmission]]

      ws.url(submitUrl)
        .withHeaders(headers: _*)
        .withRequestTimeout(nrsMaxTimeout)
        .post(Json.toJson(nrsSubmission))
    }

    nrsResponse.map { res =>

      val resJson = Try(res.json) match {
        case Success(json: JsValue) => Some(json)
        case _ => None
      }

      val httpResponse = HttpResponse(
        res.status,
        resJson,
        res.allHeaders,
        None
      )

      Logger.debug(s"[NRSConnector][submit] - NRS Call succeeded")

      NrsSubmissionOutcomeReads.read("", "", httpResponse)

    }.recover {
      case e: TimeoutException => {
        logger.warn(s"[NRSConnector][submit] - NRS Call timed out for VRN: $vrn - $e")
        Right(EmptyNrsData)
      }
    }
  }
}
