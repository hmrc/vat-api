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
import org.joda.time.DateTime
import play.api.Logger
import play.api.libs.json.Json
import uk.gov.hmrc.domain.Vrn
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.vatapi.connectors.NRSConnector
import uk.gov.hmrc.vatapi.httpparsers.NrsSubmissionHttpParser.NrsSubmissionOutcome
import uk.gov.hmrc.vatapi.models.VatReturnDeclaration
import uk.gov.hmrc.vatapi.resources.AuthRequest

import scala.concurrent.{ExecutionContext, Future}

object NRSService extends NRSService {
  override val nrsConnector: NRSConnector = NRSConnector
}

trait NRSService {
  val logger: Logger = Logger(this.getClass)

  val nrsConnector: NRSConnector

  def submit(vrn: Vrn, payload: VatReturnDeclaration)(implicit hc: HeaderCarrier, ec: ExecutionContext, request: AuthRequest[_]): Future[NrsSubmissionOutcome] = {
    logger.debug(s"[NRSService][submit] - Submitting payload to NRS")
    nrsConnector.submit(vrn, convertToNrsSubmission(vrn, payload))
  }

  private def convertToNrsSubmission(vrn: Vrn, payload: VatReturnDeclaration)(implicit request: AuthRequest[_]): NRSSubmission = {

    // TODO - Add in validation to get rid of .get
    val encoder = Base64.getEncoder
    NRSSubmission(
      payload = encoder.encodeToString(Json.toJson(payload).toString.getBytes(StandardCharsets.UTF_8)),
      metadata = Metadata(
        businessId = "vat",
        notableEvent = "vat-return",
        payloadContentType = "application/json",
        payloadSha256Checksum = None,
        userSubmissionTimestamp = DateTime.now(),
        identityData = request.authContext.identityData,
        userAuthToken = request.headers.get("Authorization").get,
        headerData = Json.toJson(request.headers.toMap.map { h => h._1 -> h._2.head}),
        searchKeys = SearchKeys(
          vrn = Some(vrn),
          periodKey = Some(payload.periodKey)
        )
      )
    )
  }
}



