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

package v1.services

import cats.data.EitherT
import cats.implicits._
import javax.inject.Inject
import org.joda.time.DateTime
import play.api.Logger
import play.api.libs.json.Json
import uk.gov.hmrc.http.HeaderCarrier
import utils.HashUtil
import v1.connectors.NrsConnector
import v1.controllers.UserRequest
import v1.models.errors.{DownstreamError, ErrorWrapper}
import v1.models.nrs.request.{Metadata, NrsSubmission, SearchKeys}
import v1.models.nrs.response.NrsResponse
import v1.models.request.submit.SubmitRequest

import scala.concurrent.{ExecutionContext, Future}

class NrsService @Inject()(connector: NrsConnector) {

  def submitNrs(vatSubmission: SubmitRequest, submissionTimestamp: DateTime)(
    implicit request: UserRequest[_],
    hc: HeaderCarrier,
    ec: ExecutionContext,
    correlationId: String): Future[Either[ErrorWrapper, NrsResponse]] = {

    val result = for {
      nrsResponse <- EitherT(connector.submitNrs(buildNrsSubmission(vatSubmission, submissionTimestamp, request)))
        .leftMap(_ => ErrorWrapper(correlationId, DownstreamError, None))
    } yield nrsResponse

    result.value
  }

  def buildNrsSubmission(vatSubmission: SubmitRequest, submissionTimestamp: DateTime, request: UserRequest[_]): NrsSubmission = {

    import vatSubmission._

    val payloadString = Json.toJson(body).toString()
    val htmlPayload = HashUtil.encode(payloadString)
    val sha256Checksum = HashUtil.getHash(payloadString)

    Logger.warn(s"[NrsService][buildNrsSubmission] - VRN: ${vatSubmission.vrn}payload:$htmlPayload\nchecksum:$sha256Checksum")

    NrsSubmission(
      payload = htmlPayload,
      Metadata(
        businessId = "vat",
        notableEvent = "vat-return",
        payloadContentType = "application/json",
        payloadSha256Checksum = sha256Checksum,
        userSubmissionTimestamp = submissionTimestamp,
        identityData = request.userDetails.identityData,
        userAuthToken = request.headers.get("Authorization").get,
        headerData = Json.toJson(request.headers.toMap.map { h => h._1 -> h._2.head }),
        searchKeys =
          SearchKeys(
            vrn = Some(vrn.vrn),
            companyName = None,
            periodKey = body.periodKey,
            taxPeriodEndDate = None
          )
      )
    )
  }
}
