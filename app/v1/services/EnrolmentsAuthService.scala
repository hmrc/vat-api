/*
 * Copyright 2024 HM Revenue & Customs
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
import play.api.libs.json.JsResultException
import play.api.mvc.Request
import uk.gov.hmrc.auth.core.AffinityGroup.{Agent, Individual, Organisation}
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.auth.core.authorise.Predicate
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals.{affinityGroup, _}
import uk.gov.hmrc.auth.core.retrieve.{ItmpAddress, ItmpName, ~}
import uk.gov.hmrc.http.HeaderCarrier
import utils.Logging
import v1.models.auth.UserDetails
import v1.models.errors.{DownstreamError, ForbiddenDownstreamError, LegacyUnauthorisedError, MtdError}
import v1.models.outcomes.AuthOutcome
import v1.nrs.models.request.IdentityData

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class EnrolmentsAuthService @Inject()(val connector: AuthConnector) extends Logging {

  private val authFunction: AuthorisedFunctions = new AuthorisedFunctions {
    override def authConnector: AuthConnector = connector
  }

  def authorised(predicate: Predicate, nrsRequired: Boolean = false)
                (implicit hc: HeaderCarrier, ec: ExecutionContext, request: Request[_]): Future[AuthOutcome] = {
    if(!nrsRequired){
      authFunction.authorised(predicate).retrieve(affinityGroup and allEnrolments) {
        case Some(Individual) ~ enrolments => createUserDetailsWithLogging(affinityGroup = "Individual", enrolments)
        case Some(Organisation) ~ enrolments => createUserDetailsWithLogging(affinityGroup = "Organisation", enrolments)
        case Some(Agent) ~ enrolments => createUserDetailsWithLogging(affinityGroup = "Agent", enrolments)
        case affinityGroup =>
          warnLog(s"[AuthorisationService] [authoriseAsClient] Authorisation failed due to unsupported affinity group. $affinityGroup")
          Future.successful(Left(LegacyUnauthorisedError))
      }recoverWith unauthorisedError
    } else {
      authFunction.authorised(predicate).retrieve(affinityGroup and allEnrolments
        and internalId and externalId and agentCode and credentials
        and confidenceLevel and nino and saUtr and name and dateOfBirth
        and email and agentInformation and groupIdentifier and credentialRole
        and mdtpInformation and credentialStrength and loginTimes
        and itmpName and itmpDateOfBirth and itmpAddress
      ) {
        case affGroup ~ enrolments ~ inId ~ exId ~ agCode ~ creds
          ~ confLevel ~ ni ~ saRef ~ nme ~ dob
          ~ eml ~ agInfo ~ groupId ~ credRole
          ~ mdtpInfo ~ credStrength ~ logins
          ~ itmpName ~ itmpDateOfBirth ~ itmpAddress
          if affGroup.contains(AffinityGroup.Organisation) || affGroup.contains(AffinityGroup.Individual) || affGroup.contains(AffinityGroup.Agent) =>

          val emptyItmpName: ItmpName = ItmpName(None, None, None)
          val emptyItmpAddress: ItmpAddress = ItmpAddress(None, None, None, None, None, None, None, None)

          val identityData =
            IdentityData(
              inId, exId, agCode, creds,
              confLevel, ni, saRef, nme, dob,
              eml, agInfo, groupId,
              credRole, mdtpInfo, itmpName.getOrElse(emptyItmpName), itmpDateOfBirth,
              itmpAddress.getOrElse(emptyItmpAddress), affGroup, credStrength, logins
            )

          createUserDetailsWithLogging(affinityGroup = affGroup.get.toString, enrolments, Some(identityData))
        case affinityGroup =>
          warnLog(s"[EnrolmentsAuthService] [authorised with nrsRequired = true] Authorisation failed due to unsupported affinity group. $affinityGroup")
          Future.successful(Left(LegacyUnauthorisedError))

      }recoverWith unauthorisedError
    }

  }

  private def createUserDetailsWithLogging(affinityGroup: String,
                                           enrolments: Enrolments,
                                           identityData: Option[IdentityData] = None): Future[Right[MtdError, UserDetails]] = {

    val clientReference = getClientReferenceFromEnrolments(enrolments)
    logger.debug(s"[AuthorisationService] [authoriseAsClient] Authorisation succeeded as" +
      s"fully-authorised organisation for VRN $clientReference.")

    val userDetails = UserDetails(
      userType = affinityGroup,
      agentReferenceNumber = None,
      clientId = "",
      identityData
    )

    if (affinityGroup != "Agent") {
      Future.successful(Right(userDetails))
    } else {
      Future.successful(Right(userDetails.copy(agentReferenceNumber = getAgentReferenceFromEnrolments(enrolments))))
    }
  }

  def getClientReferenceFromEnrolments(enrolments: Enrolments): Option[String] = enrolments
    .getEnrolment("HMRC-MTD-VAT")
    .flatMap(_.getIdentifier("VRN"))
    .map(_.value)

  def getAgentReferenceFromEnrolments(enrolments: Enrolments): Option[String] = enrolments
    .getEnrolment("HMRC-AS-AGENT")
    .flatMap(_.getIdentifier("AgentReferenceNumber"))
    .map(_.value)

  private def unauthorisedError(implicit request: Request[_]): PartialFunction[Throwable, Future[AuthOutcome]] = {
    case _: InsufficientEnrolments =>
      warnLog(s"[AuthorisationService] [unauthorisedError] Client authorisation failed due to unsupported insufficient enrolments.")
      Future.successful(Left(LegacyUnauthorisedError))
    case _: InsufficientConfidenceLevel =>
      warnLog(s"[AuthorisationService] [unauthorisedError] Client authorisation failed due to unsupported insufficient confidenceLevels.")
      Future.successful(Left(LegacyUnauthorisedError))
    case _: JsResultException =>
      warnLog(s"[AuthorisationService] [unauthorisedError] - Did not receive minimum data from Auth required for NRS Submission")
      Future.successful(Left(ForbiddenDownstreamError))
    case exception@_ =>
      warnLog(s"[AuthorisationService] [unauthorisedError] Client authorisation failed due to internal server error. auth-client exception was ${exception.getClass.getSimpleName}")
      Future.successful(Left(DownstreamError))
  }
}
