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

package uk.gov.hmrc.vatapi.services

import java.nio.charset.StandardCharsets
import java.util.Base64

import nrs.models._
import org.joda.time.{DateTime, LocalDate}
import play.api.Logger
import play.api.libs.json.Json
import uk.gov.hmrc.domain.Vrn
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.vatapi.connectors.NRSConnector
import uk.gov.hmrc.vatapi.httpparsers.NrsSubmissionHttpParser.NrsSubmissionOutcome
import uk.gov.hmrc.vatapi.models.VatReturnDeclaration

import scala.concurrent.{ExecutionContext, Future}

object NRSService extends NRSService {
  override val nrsConnector: NRSConnector = NRSConnector
}

trait NRSService {
  val nrsConnector: NRSConnector

  def submit(vrn: Vrn, payload: VatReturnDeclaration)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[NrsSubmissionOutcome] = {
    Logger.debug(s"[NRSService][submit] - Submitting payload to NRS")
    nrsConnector.submit(vrn, convertToNrsSubmission(payload))
  }

  private def convertToNrsSubmission(payload: VatReturnDeclaration): NRSSubmission = {

    val encoder = Base64.getEncoder
    NRSSubmission(
      payload = encoder.encodeToString(Json.toJson(payload).toString.getBytes(StandardCharsets.UTF_8)),
      metadata = Metadata(
        businessId = "",
        notableEvent = "",
        payloadContentType = "",
        payloadSha256Checksum = Some(""),
        userSubmissionTimestamp = DateTime.parse("2018-06-30T01:20"),
        identityData = IdentityData(

        ),
        userAuthToken = "",
        headerData = HeaderData(

        ),
        searchKeys = SearchKeys(
          vrn = Vrn("123456789"),
          companyName = "",
          taxPeriodEndDate = LocalDate.parse("2018-06-30")
        )
      )
    )
  }
}



