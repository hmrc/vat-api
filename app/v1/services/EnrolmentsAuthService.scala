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

import javax.inject.{Inject, Singleton}
import play.api.Logger
import play.api.libs.json.JsResultException
import uk.gov.hmrc.auth.core.AffinityGroup.{Agent, Individual, Organisation}
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.auth.core.authorise.Predicate
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals._
import uk.gov.hmrc.auth.core.retrieve.~
import uk.gov.hmrc.http.HeaderCarrier
import v1.models.auth.UserDetails
import v1.models.errors.{DownstreamError, ForbiddenDownstreamError, LegacyUnauthorisedError}
import v1.models.outcomes.AuthOutcome

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class EnrolmentsAuthService @Inject()(val connector: AuthConnector) {

  private val authFunction: AuthorisedFunctions = new AuthorisedFunctions {
    override def authConnector: AuthConnector = connector
  }

  private def newUserDetails(affinityGroup: String): UserDetails =
    UserDetails(
      userType = affinityGroup,
      agentReferenceNumber = None,
      clientId = ""
    )

  def authorised(predicate: Predicate)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[AuthOutcome] = {
    authFunction.authorised(predicate).retrieve(affinityGroup and allEnrolments) {
      case Some(Individual) ~ _ =>
        val user: UserDetails = newUserDetails(affinityGroup = "Individual")
        successLogging(Individual)
        Future.successful(Right(user))
      case Some(Organisation) ~ _ =>
        val user: UserDetails = newUserDetails(affinityGroup = "Organisation")
        successLogging(Organisation)
        Future.successful(Right(user))
      case Some(Agent) ~ _ =>
        val user: UserDetails = newUserDetails(affinityGroup = "Agent")
        successLogging(Agent)
        retrieveAgentReference().map(arn => Right(user.copy(agentReferenceNumber = arn)))
      case _ =>
        Logger.error(s"[AuthorisationService] [authoriseAsClient] Authorisation failed due to unsupported affinity group.")
        Future.successful(Left(LegacyUnauthorisedError))
    } recoverWith unauthorisedError
  }

  private def successLogging(affinityGroup: AffinityGroup)(implicit hc: HeaderCarrier, ec: ExecutionContext): Unit = {

    val clientReference = retrieveClientReference(affinityGroup).map {
      case Some(clientReference) => clientReference
      case _ => ""
    }

    Logger.debug(s"[AuthorisationService] [authoriseAsClient] Authorisation succeeded as fully-authorised organisation " +
      s"for VRN $clientReference.")
  }

  private def retrieveClientReference(affinityGroup: AffinityGroup)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Option[String]] = {
    authFunction.authorised(affinityGroup and Enrolment("HMRC-MTD-VAT"))
      .retrieve(Retrievals.agentCode and Retrievals.allEnrolments) {
        case _ ~ enrolments => Future.successful(getClientReferenceFromEnrolments(enrolments))
        case _ => Future.successful(None)
      }
  }

  def getClientReferenceFromEnrolments(enrolments: Enrolments): Option[String] = enrolments
    .getEnrolment("HMRC-MTD-VAT")
    .flatMap(_.getIdentifier("VRN"))
    .map(_.value)

  private def retrieveAgentReference()(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Option[String]] = {
    authFunction.authorised(AffinityGroup.Agent and Enrolment("HMRC-AS-AGENT"))
      .retrieve(Retrievals.agentCode and Retrievals.allEnrolments) {
        case _ ~ enrolments => Future.successful(getAgentReferenceFromEnrolments(enrolments))
        case _ => Future.successful(None)
      }
  }

  def getAgentReferenceFromEnrolments(enrolments: Enrolments): Option[String] = enrolments
    .getEnrolment("HMRC-AS-AGENT")
    .flatMap(_.getIdentifier("AgentReferenceNumber"))
    .map(_.value)

  private def unauthorisedError: PartialFunction[Throwable, Future[AuthOutcome]] = {
    case _: InsufficientEnrolments =>
      Logger.warn(s"[AuthorisationService] [unauthorisedError] Client authorisation failed due to unsupported insufficient enrolments.")
      Future.successful(Left(LegacyUnauthorisedError))
    case _: InsufficientConfidenceLevel =>
      Logger.warn(s"[AuthorisationService] [unauthorisedError] Client authorisation failed due to unsupported insufficient confidenceLevels.")
      Future.successful(Left(LegacyUnauthorisedError))
    case _: JsResultException =>
      Logger.warn(s"[AuthorisationService] [unauthorisedError] - Did not receive minimum data from Auth required for NRS Submission")
      Future.successful(Left(ForbiddenDownstreamError))
    case exception@_ =>
      Logger.warn(s"[AuthorisationService] [unauthorisedError] Client authorisation failed due to internal server error. auth-client exception was ${exception.getClass.getSimpleName}")
      Future.successful(Left(DownstreamError))
  }
}
