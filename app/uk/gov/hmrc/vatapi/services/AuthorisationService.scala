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
import uk.gov.hmrc.http.{HeaderCarrier, Upstream4xxResponse, Upstream5xxResponse}
import uk.gov.hmrc.vatapi.config.MicroserviceAuthConnector
import uk.gov.hmrc.vatapi.contexts.{AuthContext, Organisation}
import uk.gov.hmrc.vatapi.models.Errors
import uk.gov.hmrc.vatapi.models.Errors.ClientNotSubscribed

import scala.concurrent.{ExecutionContext, Future}
import scala.util.control.NonFatal
import scala.util.matching.Regex

object AuthorisationService extends AuthorisedFunctions {
  type AuthResult = Either[Result, AuthContext]

  override def authConnector: AuthConnector = MicroserviceAuthConnector

  private val logger = Logger(AuthorisationService.getClass)

  def authCheck(vrn: Vrn)(implicit hc: HeaderCarrier, reqHeader: RequestHeader, ec: ExecutionContext): Future[AuthResult] =
    authoriseAsClient(vrn)

  def getOrganisationReference(enrolments: Enrolments): Option[String] =
    enrolments.enrolments
      .flatMap(_.identifiers)
      .find(_.key == "VRN")
      .map(_.value)

  private def authoriseAsClient(vrn: Vrn)(implicit hc: HeaderCarrier,
                                              requestHeader: RequestHeader,
                                              ec: ExecutionContext): Future[AuthResult] = {
    logger.debug(s"[AuthorisationService] [authoriseAsClient] Check user authorisation for MTD VAT based on VRN ${vrn}.")
    authorised(
      Enrolment("HMRC-MTD-VAT")
        .withIdentifier("VRN", vrn.vrn)
        .withDelegatedAuthRule("mtd-vat-auth"))
      .retrieve(Retrievals.affinityGroup and Retrievals.authorisedEnrolments) {
        case Some(AffinityGroup.Organisation) ~ enrolments =>
          logger.debug(s"Client authorisation succeeded as fully-authorised organisation " +
            s"for VRN ${getOrganisationReference(enrolments).getOrElse("")}.")
          Future.successful(Right(Organisation))
      } recoverWith (unsubscribedAgentOrUnauthorisedClient orElse unhandledError)
  }

  private def unsubscribedAgentOrUnauthorisedClient: PartialFunction[Throwable, Future[AuthResult]] = {
    case _: InsufficientEnrolments =>
      logger.debug(s"Authorisation failed as filing-only agent.")
      Future.successful(Left(Forbidden(toJson(Errors.ClientNotSubscribed))))
    case _: UnsupportedAffinityGroup =>
      logger.debug(s"Authorisation failed as client.")
      Future.successful(Left(Forbidden(toJson(ClientNotSubscribed))))
  }

  private def unhandledError: PartialFunction[Throwable, Future[AuthResult]] = {
    val regex: Regex = """.*"Unable to decrypt value".*""".r
    lazy val internalServerError = Future.successful(
      Left(InternalServerError(toJson(Errors.InternalServerError("An internal server error occurred")))))

    locally { // http://www.scala-lang.org/old/node/3594
      case e @ (_: AuthorisationException | Upstream5xxResponse(regex(_*), _, _)) =>
        logger.error(s"Authorisation failed with unexpected exception. Exception: [$e]")
        Future.successful(Left(Forbidden(toJson(Errors.BadToken))))
      case e: Upstream4xxResponse =>
        logger.error(s"Unhandled 4xx response from play-auth: [$e]. Returning 500 to client.")
        internalServerError
      case e: Upstream5xxResponse =>
        logger.error(s"Unhandled 5xx response from play-auth: [$e]. Returning 500 to client.")
        internalServerError
      case NonFatal(e) =>
        logger.error(s"Unhandled non-fatal exception from play-auth: [$e]. Returning 500 to client.")
        internalServerError
    }
  }
}
