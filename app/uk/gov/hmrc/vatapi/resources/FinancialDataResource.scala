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

package uk.gov.hmrc.vatapi.resources

import cats.implicits._
import play.api.Logger
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent}
import uk.gov.hmrc.domain.Vrn
import uk.gov.hmrc.vatapi.audit.{AuditEvents, AuditService}
import uk.gov.hmrc.vatapi.config.AppContext
import uk.gov.hmrc.vatapi.connectors.FinancialDataConnector
import uk.gov.hmrc.vatapi.models.{Errors, FinancialDataQueryParams, Liabilities, Payments}
import uk.gov.hmrc.vatapi.services.AuthorisationService

import scala.concurrent.ExecutionContext.Implicits.global

object FinancialDataResource extends FinancialDataResource {
  override val connector = FinancialDataConnector
  override val authService = AuthorisationService
  override val appContext = AppContext
  override val auditService = AuditService
}

trait FinancialDataResource extends BaseResource {

  val connector: FinancialDataConnector
  val authService: AuthorisationService
  val appContext: AppContext
  val auditService: AuditService

  def retrieveLiabilities(vrn: Vrn, params: FinancialDataQueryParams): Action[AnyContent] = APIAction(vrn).async { implicit request =>

    logger.debug(s"[FinancialDataResource][retrieveLiabilities] Retrieving Liabilities from DES")
    val result = fromDes {
      for {
        response <- execute { _ => connector.getFinancialData(vrn, params) }
      } yield response
    } onSuccess { response =>
      response.filter {
        case 200 =>
          response.getLiabilities(vrn) match {
            case Right(obj) =>

              val remainingLiabilities = obj.liabilities.filter(l =>
                l.`type` != "Payment on account" && (l.taxPeriod.isEmpty || l.taxPeriod.exists(l => !(l.to isAfter params.to)))
              )

              remainingLiabilities match {
                case Seq() =>
                  logger.error(s"[FinancialDataResource][retrieveLiabilities] Retrieved liabilities from DES but exceeded the 'dateTo' query parameter range")
                  NotFound(Json.toJson(Errors.NotFound))
                case _ =>
                  logger.debug(s"[FinancialDataResource][retrieveLiabilities] Successfully retrieved Liabilities from DES")
                  auditService.audit(AuditEvents.retrieveVatLiabilitiesAudit(response.getCorrelationId(), request.authContext.affinityGroup, getArn))
                  Ok(Json.toJson(Liabilities(remainingLiabilities)))
              }
            case Left(ex) =>
              logger.error(s"[FinancialDataResource][retrieveLiabilities] Error retrieving Liabilities from DES: ${ex.msg}")
              InternalServerError(Json.toJson(Errors.InternalServerError))
          }
      }
    }
    //Interim Error Handling
    result.recover {
      case ex =>
        Logger.warn(s"[FinancialDataResource][retrieveLiabilities] Unexpected dowstream error caught ${ex.getMessage}")
        InternalServerError(Json.toJson(Errors.InternalServerError))
    }
  }


  def retrievePayments(vrn: Vrn, params: FinancialDataQueryParams): Action[AnyContent] = APIAction(vrn).async { implicit request =>

    logger.debug(s"[FinancialDataResource][retrievePayments] Retrieving Payments from DES")

    val result = fromDes {
      for {
        response <- execute { _ => connector.getFinancialData(vrn, params) }
      } yield response
    } onSuccess { response =>
      response.filter {
        case 200 =>
          response.getPayments(vrn) match {
            case Right(obj) =>
              logger.debug(s"[FinancialDataResource][retrievePayments] Successfully retrieved Payments from DES")
              val payments = Payments(
                obj.payments.filter(_.taxPeriod.isEmpty) ++ obj.payments.filter(_.taxPeriod.isDefined).filterNot(_.taxPeriod.get.to isAfter params.to)
              )
              payments.payments match {
                case Seq() =>
                  logger.error(s"[FinancialDataResource][retrievePayments] Retrieved payments from DES but exceeded the 'dateTo' query parameter range")
                  NotFound(Json.toJson(Errors.NotFound))
                case _ =>
                  logger.debug(s"[FinancialDataResource][retrieveLiabilities] Successfully retrieved Liabilities from DES")
                  auditService.audit(AuditEvents.retrieveVatPaymentsAudit(response.getCorrelationId(), request.authContext.affinityGroup, getArn))
                  Ok(Json.toJson(payments))
              }
            case Left(ex) =>
              logger.error(s"[FinancialDataResource][retrievePayments] Error retrieving Payments from DES: ${ex.msg}")
              InternalServerError(Json.toJson(Errors.InternalServerError))
          }
      }
    }

    result.recover {
      case ex =>
        Logger.warn(s"[FinancialDataResource][retrievePayments] Unexpected dowstream error caught ${ex.getMessage}")
        InternalServerError(Json.toJson(Errors.InternalServerError))
    }
  }
}

