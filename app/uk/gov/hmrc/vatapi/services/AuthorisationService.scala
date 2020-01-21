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

package uk.gov.hmrc.vatapi.services

import javax.inject.{Inject, Singleton}
import org.joda.time.LocalDate
import play.api.Logger
import play.api.libs.json.JsResultException
import play.api.libs.json.Json.toJson
import play.api.mvc.Result
import play.api.mvc.Results._
import uk.gov.hmrc.auth.core.retrieve._
import uk.gov.hmrc.auth.core.{Enrolment, Enrolments, _}
import uk.gov.hmrc.domain.Vrn
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.vatapi.auth.AffinityGroupToAuthContext._
import uk.gov.hmrc.vatapi.auth.{APIAuthorisedFunctions, AuthContext}
import uk.gov.hmrc.vatapi.config.AppContext
import uk.gov.hmrc.vatapi.models.Errors.ClientOrAgentNotAuthorized
import uk.gov.hmrc.vatapi.models.{Errors, IdentityData}

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class AuthorisationService @Inject()(
                                      apiAuthorisedFunctions: APIAuthorisedFunctions,
                                      appContext: AppContext
                                    ) {

  type AuthResult = Either[Result, AuthContext]

  private lazy val vatAuthEnrolments = appContext.vatAuthEnrolments

  val logger = Logger(this.getClass)

  def authCheck(vrn: Vrn)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[AuthResult] =
    authoriseAsClient(vrn)

  private def authoriseAsClient(vrn: Vrn)(implicit hc: HeaderCarrier,
                                          ec: ExecutionContext): Future[AuthResult] = {
    import v2.Retrievals._

    logger.debug(s"[AuthorisationService] [authoriseAsClient] Check user authorisation for MTD VAT based on VRN $vrn.")
    apiAuthorisedFunctions.authorised(
      Enrolment(vatAuthEnrolments.enrolmentToken)
        .withIdentifier(vatAuthEnrolments.identifier, vrn.vrn)
        .withDelegatedAuthRule(vatAuthEnrolments.authRule))
      .retrieve(
        affinityGroup and allEnrolments and agentInformation
      ) {
        case Some(userType) ~ enrolments ~ agentInfo
          if userType == AffinityGroup.Organisation || userType == AffinityGroup.Individual || userType == AffinityGroup.Agent =>
          logger.debug(s"[AuthorisationService] [authoriseAsClient] Authorisation succeeded as fully-authorised organisation " +
            s"for VRN ${getClientReference(enrolments).getOrElse("")}.")
          Future.successful(Right(authContext(enrolments, userType, None, Some(agentInfo))))
        case _ => logger.error(s"[AuthorisationService] [authoriseAsClient] Authorisation failed due to unsupported affinity group.")
          Future.successful(Left(Forbidden(toJson(ClientOrAgentNotAuthorized))))
      } recoverWith unauthorisedError
  }

  def getClientReference(enrolments: Enrolments): Option[String] =
    enrolments.enrolments
      .flatMap(_.identifiers)
      .find(_.key == vatAuthEnrolments.identifier)
      .map(_.value)

  private def unauthorisedError: PartialFunction[Throwable, Future[AuthResult]] = {
    case _: InsufficientEnrolments =>
      logger.warn(s"[AuthorisationService] [unauthorisedError] Client authorisation failed due to unsupported insufficient enrolments.")
      Future.successful(Left(Forbidden(toJson(Errors.ClientOrAgentNotAuthorized))))
    case _: InsufficientConfidenceLevel =>
      logger.warn(s"[AuthorisationService] [unauthorisedError] Client authorisation failed due to unsupported insufficient confidenceLevels.")
      Future.successful(Left(Forbidden(toJson(Errors.ClientOrAgentNotAuthorized))))
    case _: JsResultException =>
      logger.warn(s"[AuthorisationService] [unauthorisedError] - Did not receive minimum data from Auth required for NRS Submission")
      Future.successful(Left(Forbidden(toJson(Errors.InternalServerError))))
    case exception@_ =>
      logger.warn(s"[AuthorisationService] [unauthorisedError] Client authorisation failed due to internal server error. auth-client exception was ${exception.getClass.getSimpleName}")
      Future.successful(Left(InternalServerError(toJson(
        Errors.InternalServerError("An internal server error occurred")))))
  }

  def authCheckWithNrsRequirement(vrn: Vrn)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[AuthResult] =
    authoriseAsClientWithNrsRequirement(vrn)

  private def authoriseAsClientWithNrsRequirement(vrn: Vrn)(implicit hc: HeaderCarrier,
                                                            ec: ExecutionContext): Future[AuthResult] = {
    import v2.Retrievals._

    logger.debug(s"[AuthorisationService] [authoriseAsClientWithNrsRequirement] Check user authorisation for MTD VAT based on VRN $vrn.")
    apiAuthorisedFunctions.authorised(
      Enrolment(vatAuthEnrolments.enrolmentToken)
        .withIdentifier(vatAuthEnrolments.identifier, vrn.vrn)
        .withDelegatedAuthRule(vatAuthEnrolments.authRule))
      .retrieve(
        affinityGroup and allEnrolments
          and internalId and externalId and agentCode and credentials
          and confidenceLevel and nino and saUtr and name and dateOfBirth
          and email and agentInformation and groupIdentifier and credentialRole
          and mdtpInformation and credentialStrength and loginTimes
      ) {
        case affGroup ~ enrolments ~ inId ~ exId ~ agCode ~ creds
          ~ confLevel ~ ni ~ saRef ~ nme ~ dob
          ~ eml ~ agInfo ~ groupId ~ credRole
          ~ mdtpInfo ~ credStrength ~ logins
          if affGroup.contains(AffinityGroup.Organisation) || affGroup.contains(AffinityGroup.Individual) || affGroup.contains(AffinityGroup.Agent) =>

          // setup dummy data for ITMP data
          val dummyItmpName: ItmpName = ItmpName(None, None, None)
          val dummyItmpDob: Option[LocalDate] = None
          val dummyItmpAddress: ItmpAddress = ItmpAddress(None, None, None, None, None, None, None, None)

          val identityData =
            IdentityData(
              inId, exId, agCode, creds,
              confLevel, ni, saRef, nme, dob,
              eml, agInfo, groupId,
              credRole, mdtpInfo, dummyItmpName, dummyItmpDob,
              dummyItmpAddress, affGroup, credStrength, logins)
          val afGroup = affGroup.get
          logger.debug(s"[AuthorisationService] [authoriseAsClientWithNrsRequirement] Authorisation succeeded as fully-authorised organisation " +
            s"for VRN ${getClientReference(enrolments).getOrElse("")}.")
          Future.successful(Right(authContext(enrolments, afGroup, Some(identityData), Some(agInfo))))
        case _ => logger.error(s"[AuthorisationService] [authoriseAsClientWithNrsRequirement] Authorisation failed due to unsupported affinity group.")
          Future.successful(Left(Forbidden(toJson(ClientOrAgentNotAuthorized))))
      } recoverWith unauthorisedError
  }
}
