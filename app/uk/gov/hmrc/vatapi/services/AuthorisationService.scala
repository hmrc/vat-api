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

import play.api.Logger
import play.api.libs.json.Json.toJson
import play.api.mvc.Results._
import play.api.mvc.{RequestHeader, Result}
import uk.gov.hmrc.auth.core.retrieve.{Retrievals, ~}
import uk.gov.hmrc.auth.core.{Enrolment, Enrolments, _}
import uk.gov.hmrc.domain.Vrn
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.vatapi.config.{AppContext, MicroserviceAuthConnector}
import uk.gov.hmrc.vatapi.contexts.{AuthContext, Organisation}
import uk.gov.hmrc.vatapi.models.Errors
import uk.gov.hmrc.vatapi.models.Errors.ClientOrAgentNotAuthorized

import scala.concurrent.{ExecutionContext, Future}

object AuthorisationService extends AuthorisedFunctions {
  type AuthResult = Either[Result, AuthContext]

  override def authConnector: AuthConnector = MicroserviceAuthConnector
  private lazy val vatAuthEnrolments = AppContext.vatAuthEnrolments

  private val logger = Logger(AuthorisationService.getClass)

  def authCheck(vrn: Vrn)(implicit hc: HeaderCarrier, reqHeader: RequestHeader, ec: ExecutionContext): Future[AuthResult] =
    authoriseAsClient(vrn)

  def getClientReference(enrolments: Enrolments): Option[String] =
    enrolments.enrolments
      .flatMap(_.identifiers)
      .find(_.key == vatAuthEnrolments.identifier)
      .map(_.value)

  private def authoriseAsClient(vrn: Vrn)(implicit hc: HeaderCarrier,
                                              requestHeader: RequestHeader,
                                              ec: ExecutionContext): Future[AuthResult] = {
    logger.debug(s"[AuthorisationService] [authoriseAsClient] Check user authorisation for MTD VAT based on VRN ${vrn}.")
    authorised(
      Enrolment(vatAuthEnrolments.enrolmentToken)
        .withIdentifier(vatAuthEnrolments.identifier, vrn.vrn))
      .retrieve(Retrievals.affinityGroup and Retrievals.authorisedEnrolments) {
        case Some(AffinityGroup.Organisation) ~ enrolments =>
          logger.debug(s"[AuthorisationService] [authoriseAsClient] Authorisation succeeded as fully-authorised organisation " +
            s"for VRN ${getClientReference(enrolments).getOrElse("")}.")
          Future.successful(Right(Organisation))
        case _ => logger.error(s"[AuthorisationService] [authoriseAsClient] Authorisation failed due to unsupported affinity group.")
          Future.successful(Left(Forbidden(toJson(ClientOrAgentNotAuthorized))))
      } recoverWith unauthorisedError
  }

  private def unauthorisedError: PartialFunction[Throwable, Future[AuthResult]] = {
    case _: InsufficientEnrolments =>
      logger.error(s"[AuthorisationService] [unauthorisedError] Client authorisation failed due to unsupported insufficient enrolments.")
      Future.successful(Left(Forbidden(toJson(Errors.ClientOrAgentNotAuthorized))))
    case _ =>
      logger.error(s"[AuthorisationService] [unauthorisedError] Client authorisation failed due to internal server error.")
      Future.successful(Left(InternalServerError(toJson(
      Errors.InternalServerError("An internal server error occurred")))))
  }

}
