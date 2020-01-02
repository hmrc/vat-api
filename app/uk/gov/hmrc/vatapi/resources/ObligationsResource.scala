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

package uk.gov.hmrc.vatapi.resources

import javax.inject.{Inject, Singleton}
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import uk.gov.hmrc.domain.Vrn
import uk.gov.hmrc.vatapi.audit.AuditEvents
import uk.gov.hmrc.vatapi.connectors.ObligationsConnector
import uk.gov.hmrc.vatapi.models.{Errors, ObligationsQueryParams}
import uk.gov.hmrc.vatapi.resources.wrappers.Response
import uk.gov.hmrc.vatapi.services.{AuditService, AuthorisationService}

import scala.concurrent.ExecutionContext

@Singleton
class ObligationsResource @Inject()(
                                     connector: ObligationsConnector,
                                     val authService: AuthorisationService,
                                     auditService: AuditService,
                                     cc: ControllerComponents
                                   )(implicit ec: ExecutionContext) extends BaseResource(cc) {

  def retrieveObligations(vrn: Vrn, params: ObligationsQueryParams): Action[AnyContent] = APIAction(vrn).async { implicit request =>
    logger.debug(s"[ObligationsResource][retrieveObligations] - Retrieve Obligations for VRN : $vrn")

    val arn = getArn

    def audit(vatResult: VatResult, correlationId: String) =
      auditService.audit(AuditEvents.retrieveVatObligationsAudit(correlationId,
        request.authContext.affinityGroup, arn, vatResult.auditResponse))

    val result =
      for {
        desResponse <- connector.get(vrn, params)
      } yield {
        val result = desResponse.filter {
          case OK =>
            desResponse.obligations(vrn) match {
              case Right(Some(obligations)) =>
                logger.debug(s"[ObligationsResource][retrieveObligations] Successfully retrieved Obligations from DES")
                VatResult.Success(OK, obligations)
              case Right(None) =>
                VatResult.Failure(NOT_FOUND, Errors.NotFound)
              case Left(ex) =>
                logger.error(s"[ObligationsResource][retrieveObligations] Json format from DES doesn't match the Obligations model: ${ex.msg}")
                VatResult.Failure(INTERNAL_SERVER_ERROR, Errors.InternalServerError)
            }
        }
        audit(result, desResponse.getCorrelationId)
        result
      }

    result.recover {
      case ex =>
        logger.warn(s"[ObligationsResource][retrieveObligations] Unexpected downstream error thrown ${ex.getMessage}")
        val result = VatResult.Failure(INTERNAL_SERVER_ERROR, Errors.InternalServerError)
        audit(result, Response.defaultCorrelationId)
        result
    }.map(_.result)
  }
}
