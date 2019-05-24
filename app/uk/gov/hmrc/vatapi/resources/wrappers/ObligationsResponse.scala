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

package uk.gov.hmrc.vatapi.resources.wrappers

import play.api.http.Status
import play.api.libs.json.Json.toJson
import play.api.mvc.Result
import uk.gov.hmrc.domain.Vrn
import uk.gov.hmrc.http.HttpResponse
import uk.gov.hmrc.vatapi.models.des.DesErrorCode._
import uk.gov.hmrc.vatapi.models.des.ObligationDetail
import uk.gov.hmrc.vatapi.models.{DesTransformError, Obligations, _}
import uk.gov.hmrc.vatapi.resources.VatResult

case class ObligationsResponse(underlying: HttpResponse) extends Response {


  def obligations(vrn: Vrn): Either[DesTransformError, Option[Obligations]] = {

    def noneFound: Either[DesTransformError, Option[Obligations]] = {
      logger.warn(s"[ObligationsResponse][noneFound] No obligation details found. JSON: ${underlying.status}")
      Right(None)
    }

    def error(message: String): Either[DesTransformError, Option[Obligations]] = {
      logger.error(s"[ObligationsResponse][error] $message. status: ${underlying.status}")
      Left(new DesTransformError {
        val msg = message
      })
    }

    def oneFound(obligation: des.Obligations): Either[DesTransformError, Option[Obligations]] = {
      obligation.obligations.find(obj => obj.obligationDetails.nonEmpty).fold(noneFound) {
        desObligation =>
          val obligationsOrError: Seq[Either[DesTransformError, Obligation]] = for {
            details <- desObligation.obligationDetails
          } yield DesTransformValidator[ObligationDetail, Obligation].from(details)

          obligationsOrError.find(_.isLeft) match {
            case Some(ex) => Left(ex.left.get)
            case None => Right(Some(Obligations(obligationsOrError map (_.right.get))))
          }
      }
    }

    jsonOrError match {
      case Right(js) =>
        logger.debug(s"[ObligationsResponse][desObligations] Json response body from DES : $js")
        js.asOpt[des.Obligations] match {
          case Some(obligations) => oneFound(obligations)
          case _ => error("The response from DES does not match the expected format")
        }
      case _ =>
        logger.error(s"[ObligationsResponse][desObligations] Non json response from DES : ${underlying.status}")
        error("Non json response from DES")
    }
  }

  override def errorMappings: PartialFunction[Int, VatResult] = {

    case 400 if errorCodeIsOneOf(INVALID_IDNUMBER) => VatResult.Failure(Status.BAD_REQUEST, Errors.VrnInvalid)
    case 400 if errorCodeIsOneOf(INVALID_DATE_TO) => VatResult.Failure(Status.BAD_REQUEST, Errors.InvalidDateTo)
    case 400 if errorCodeIsOneOf(INVALID_DATE_FROM) => VatResult.Failure(Status.BAD_REQUEST, Errors.InvalidDateFrom)
    case 400 if errorCodeIsOneOf(INVALID_DATE_RANGE) => VatResult.Failure(Status.BAD_REQUEST, Errors.DateRangeTooLarge)
    case 400 if errorCodeIsOneOf(INVALID_STATUS) => VatResult.Failure(Status.BAD_REQUEST, Errors.InvalidStatus)
    case 400 if errorCodeIsOneOf(INVALID_IDTYPE, INVALID_REGIME, NOT_FOUND_BPKEY) =>
      VatResult.Failure(Status.INTERNAL_SERVER_ERROR, Errors.InternalServerError)
    case 404 if errorCodeIsOneOf(NOT_FOUND) => VatResult.Failure(Status.NOT_FOUND, Errors.NotFound)
  }

}
