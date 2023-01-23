/*
 * Copyright 2023 HM Revenue & Customs
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

package v1.connectors.httpparsers

import play.api.http.Status._
import play.api.libs.json.Reads
import uk.gov.hmrc.http.{HttpReads, HttpResponse}
import utils.pagerDutyLogging.{PagerDutyLoggingEndpointName, PagerDutyLogging}
import v1.connectors.DesOutcome
import v1.controllers.UserRequest
import v1.models.errors.{ConnectorError, DownstreamError, OutboundError}
import v1.models.outcomes.ResponseWrapper

object StandardDesHttpParser extends HttpParser {

  case class SuccessCode(status: Int) extends AnyVal

  // Return Right[DesResponse[Unit]] as success response has no body - no need to assign it a value
  implicit def readsEmpty(implicit successCode: SuccessCode = SuccessCode(NO_CONTENT),
                          connectorError: ConnectorError,
                          userRequest: UserRequest[_], pagerDutyLoggingEndpointName: PagerDutyLoggingEndpointName.Value): HttpReads[DesOutcome[Unit]] =
    (_: String, url: String, response: HttpResponse) => doRead(url, response) { correlationId =>
      Right(ResponseWrapper(correlationId, ()))
    }

  implicit def reads[A: Reads](implicit successCode: SuccessCode = SuccessCode(OK),
                               connectorError: ConnectorError,
                               userRequest: UserRequest[_], pagerDutyLoggingEndpointName: PagerDutyLoggingEndpointName.Value): HttpReads[DesOutcome[A]] =
    (_: String, url: String, response: HttpResponse) => doRead(url, response) { correlationId =>
      response.validateJson[A] match {
        case Some(ref) => Right(ResponseWrapper(correlationId, ref))
        case None => Left(ResponseWrapper(correlationId, OutboundError(DownstreamError)))
      }
    }

  private def doRead[A](url: String, response: HttpResponse,
                       )(successOutcomeFactory: String => DesOutcome[A])(
                         implicit successCode: SuccessCode, connectorError: ConnectorError,
                         userRequest: UserRequest[_], pagerDutyLoggingEndpointName: PagerDutyLoggingEndpointName.Value): DesOutcome[A] = {

    val responseCorrelationId = retrieveCorrelationId(response)

    if (response.status != successCode.status) {
      infoLog(
        "[StandardDesHttpParser][read] - " +
          s"Error response received from DES with status: ${response.status} and body\n" +
          s"${response.body} and correlationId: $responseCorrelationId when calling $url - " +
          s"vrn: ${connectorError.vrn}, requestId: ${connectorError.requestId}")
    }
    response.status match {
      case successCode.status =>
        infoLog(
          "[StandardDesHttpParser][read] - " +
            s"Success response received from DES with correlationId: $responseCorrelationId when calling $url")
        successOutcomeFactory(responseCorrelationId)
      case BAD_REQUEST |
           NOT_FOUND |
           FORBIDDEN |
           CONFLICT |
           UNPROCESSABLE_ENTITY =>
        PagerDutyLogging.log(pagerDutyLoggingEndpointName, response.status, response.body, infoLog(_), userRequest.userDetails.userType)
        Left(ResponseWrapper(responseCorrelationId, parseErrors(response)))
      case _ =>
        PagerDutyLogging.log(pagerDutyLoggingEndpointName, response.status, response.body, errorLog(_), userRequest.userDetails.userType)
        Left(ResponseWrapper(responseCorrelationId, OutboundError(DownstreamError)))
    }
  }
}