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

import javax.inject.Inject
import nrs.models.NRSSubmission
import play.api.Logger
import play.api.libs.json.Writes
import uk.gov.hmrc.domain.Vrn
import uk.gov.hmrc.http._
import uk.gov.hmrc.play.bootstrap.http.DefaultHttpClient
import uk.gov.hmrc.vatapi.BaseConnector
import uk.gov.hmrc.vatapi.config.AppContext
import uk.gov.hmrc.vatapi.httpparsers.NrsSubmissionHttpParser.{NrsSubmissionOutcome, NrsSubmissionOutcomeReads}

import scala.concurrent.{ExecutionContext, Future}


class NRSConnector @Inject()(
                              override val http: DefaultHttpClient,
                              override val appContext: AppContext
                            ) extends BaseConnector {

  val logger: Logger = Logger(this.getClass)
  val nrsSubmissionUrl: String => String = vrn => s"${appContext.nrsServiceUrl}/submission"
  private val xApiKeyHeader = "X-API-Key"

  def submit(vrn: Vrn, nrsSubmission: NRSSubmission)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[NrsSubmissionOutcome] = {

    logger.debug(s"[NRSConnector][submit] - Submission to NRS for 9 box vat return for VRN: $vrn")

    val submitUrl = nrsSubmissionUrl(vrn.toString)

    http.POST[NRSSubmission, NrsSubmissionOutcome](submitUrl, nrsSubmission)(
      implicitly[Writes[NRSSubmission]],
      NrsSubmissionOutcomeReads,
      withTestHeader(hc.withExtraHeaders(xApiKeyHeader -> s"${appContext.xApiKey}")),
      implicitly)
  }
}