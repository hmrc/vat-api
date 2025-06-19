/*
 * Copyright 2025 HM Revenue & Customs
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
import play.api.libs.json._
import uk.gov.hmrc.http.{HttpReads, HttpResponse}
import utils.Logging
import v1.connectors.Outcome
import v1.models.errors._
import v1.models.outcomes.ResponseWrapper
import v1.models.response.financialData._

object FinancialDataHIPHttpParser extends Logging {

  implicit object FinancialDataHIPReads extends HttpReads[Outcome[FinancialDataHIPResponse]] with Logging {

    override def read(method: String, url: String, response: HttpResponse): Outcome[FinancialDataHIPResponse] = {
      val correlationId = retrieveCorrelationId(response)

      response.status match {
        case OK =>
          response.json.validate[FinancialDataHIPResponse] match {
            case JsSuccess(model, _) => Right(ResponseWrapper(correlationId, model))
            case JsError(errors) =>
              logger.error(s"[FinancialDataHIPReads][read] Failed to parse JSON: $errors")
              Left(ErrorWrapper(correlationId, InvalidJson))
          }

        case BAD_REQUEST | NOT_FOUND =>
          response.json.validate[FinancialDataHIPErrorResponse] match {
            case JsSuccess(errorModel, _) =>
              errorModel match {
                case TechnicalError(code, message, logId) =>
                  val err = MtdError(code, message)
                  logger.warn(s"[FinancialDataHIPReads][read] Technical error from HIP. Code: $code, Message: $message, LogId: $logId")
                  Left(ErrorWrapper(correlationId, err))

                case BusinessErrors(errors) =>
                  val mtdErrors = errors.map(e => MtdError(e.code, e.text))
                  val mainError = mtdErrors.head
                  val additionalErrors = if (mtdErrors.size > 1) Some(mtdErrors.tail) else None
                  logger.warn(s"[FinancialDataHIPReads][read] Business errors from HIP. Errors: $mtdErrors")
                  Left(ErrorWrapper(correlationId, mainError, additionalErrors))
              }

            case JsError(e) =>
              logger.error(s"[FinancialDataHIPReads][read] Failed to parse error JSON: $e")
              Left(ErrorWrapper(correlationId, DownstreamError))

            case _ =>
              logger.warn("[FinancialDataHIPReads] Unexpected error model type")
              Left(ErrorWrapper(correlationId, DownstreamError))
          }

        case SERVICE_UNAVAILABLE =>
          logger.warn(s"[FinancialDataHIPReads][read] Service unavailable")
          Left(ErrorWrapper(correlationId, MtdError(SERVICE_UNAVAILABLE.toString, "HIP service unavailable")))

        case INTERNAL_SERVER_ERROR =>
          logger.warn(s"[FinancialDataHIPReads][read] Internal server error")
          Left(ErrorWrapper(correlationId, MtdError(INTERNAL_SERVER_ERROR.toString, "HIP internal server error")))

        case status =>
          logger.warn(s"[FinancialDataHIPReads][read] Unexpected status $status")
          Left(ErrorWrapper(correlationId, MtdError(status.toString, "Unexpected HIP response")))
      }
    }

    private def retrieveCorrelationId(response: HttpResponse): String =
      response.header("CorrelationId").getOrElse("undefined")
  }
}