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

import nrs.models.IdentityData
import play.api.Logger
import play.api.libs.json.Json.toJson
import play.api.libs.json.{JsArray, JsResultException, Json}
import play.api.mvc.Results._
import play.api.mvc.{RequestHeader, Result}
import uk.gov.hmrc.auth.core.authorise.RawJsonPredicate
import uk.gov.hmrc.auth.core.retrieve.{OptionalRetrieval, Retrievals, ~}
import uk.gov.hmrc.auth.core.{Enrolment, Enrolments, _}
import uk.gov.hmrc.domain.Vrn
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.vatapi.auth.AffinityGroupToAuthContext._
import uk.gov.hmrc.vatapi.auth.{APIAuthorisedFunctions, AuthContext}
import uk.gov.hmrc.vatapi.config.AppContext
import uk.gov.hmrc.vatapi.models.Errors
import uk.gov.hmrc.vatapi.models.Errors.ClientOrAgentNotAuthorized

import scala.concurrent.{ExecutionContext, Future}

object AuthorisationService extends AuthorisationService {
  override val apiAuthorisedFunctions: APIAuthorisedFunctions.type = APIAuthorisedFunctions
}

trait AuthorisationService {
  type AuthResult = Either[Result, AuthContext]

  val apiAuthorisedFunctions: APIAuthorisedFunctions
  private lazy val vatAuthEnrolments = AppContext.vatAuthEnrolments

  private val logger = Logger(AuthorisationService.getClass)

  def authCheck(vrn: Vrn)(implicit hc: HeaderCarrier, reqHeader: RequestHeader, ec: ExecutionContext): Future[AuthResult] =
    authoriseAsClient(vrn)

  def authCheckWithNrsRequirement(vrn: Vrn)(implicit hc: HeaderCarrier, reqHeader: RequestHeader, ec: ExecutionContext): Future[AuthResult] =
    authoriseAsClientWithNrsRequirement(vrn)

  def getClientReference(enrolments: Enrolments): Option[String] =
    enrolments.enrolments
      .flatMap(_.identifiers)
      .find(_.key == vatAuthEnrolments.identifier)
      .map(_.value)

  private def authoriseAsClient(vrn: Vrn)(implicit hc: HeaderCarrier,
                                          requestHeader: RequestHeader,
                                          ec: ExecutionContext): Future[AuthResult] = {
    import Retrievals._

    logger.debug(s"[AuthorisationService] [authoriseAsClient] Check user authorisation for MTD VAT based on VRN $vrn.")
    apiAuthorisedFunctions.authorised(
      RawJsonPredicate(JsArray(Seq(Json.toJson(Enrolment(vatAuthEnrolments.enrolmentToken).withIdentifier(vatAuthEnrolments.identifier, vrn.vrn)
        .withDelegatedAuthRule(vatAuthEnrolments.authRule.getOrElse("mtd-vat-auth")))))))
      .retrieve(
        affinityGroup and authorisedEnrolments and agentInformation
      ) {
        case Some(userType) ~ enrolments ~ agentInfo
          if userType == AffinityGroup.Organisation || userType == AffinityGroup.Individual || userType == AffinityGroup.Agent =>
          logger.debug(s"[AuthorisationService] [authoriseAsClient] Authorisation succeeded as fully-authorised organisation " +
            s"for VRN ${getClientReference(enrolments).getOrElse("")}.")
          Future.successful(Right(authContext(userType, None, Some(agentInfo))))
        case _ => logger.error(s"[AuthorisationService] [authoriseAsClient] Authorisation failed due to unsupported affinity group.")
          Future.successful(Left(Forbidden(toJson(ClientOrAgentNotAuthorized))))
      } recoverWith unauthorisedError
  }

  private def authoriseAsClientWithNrsRequirement(vrn: Vrn)(implicit hc: HeaderCarrier,
                                                            requestHeader: RequestHeader,
                                                            ec: ExecutionContext): Future[AuthResult] = {
    import Retrievals._

    val individualName = OptionalRetrieval(itmpName.propertyNames.head, itmpName.reads)
    val individualAddress = OptionalRetrieval(itmpAddress.propertyNames.head, itmpAddress.reads)

    logger.debug(s"[AuthorisationService] [authoriseAsClientWithNrsRequirement] Check user authorisation for MTD VAT based on VRN $vrn.")
    apiAuthorisedFunctions.authorised(
      RawJsonPredicate(JsArray(Seq(Json.toJson(Enrolment(vatAuthEnrolments.enrolmentToken).withIdentifier(vatAuthEnrolments.identifier, vrn.vrn)
        .withDelegatedAuthRule(vatAuthEnrolments.authRule.getOrElse("mtd-vat-auth")))))))
      .retrieve(
        affinityGroup and authorisedEnrolments
          and internalId and externalId and agentCode and credentials
          and confidenceLevel and nino and saUtr and name and dateOfBirth
          and email and agentInformation and groupIdentifier and credentialRole
          and mdtpInformation and individualName and itmpDateOfBirth and individualAddress
          and credentialStrength and loginTimes
      ) {
        case affGroup ~ enrolments ~ inId ~ exId ~ agCode ~ creds
          ~ confLevel ~ ni ~ saRef ~ nme ~ dob
          ~ eml ~ agInfo ~ groupId ~ credRole
          ~ mdtpInfo ~ iname ~ idob ~ iaddress
          ~ credStrength ~ logins
          if affGroup.contains(AffinityGroup.Organisation) || affGroup.contains(AffinityGroup.Individual) || affGroup.contains(AffinityGroup.Agent) =>

          val identityData =
            IdentityData(
              inId, exId, agCode, creds,
              confLevel, ni, saRef, nme, dob,
              eml, agInfo, groupId,
              credRole, mdtpInfo, iname, idob,
              iaddress, affGroup, credStrength, logins)
          val afGroup = affGroup.get
          logger.debug(s"[AuthorisationService] [authoriseAsClientWithNrsRequirement] Authorisation succeeded as fully-authorised organisation " +
            s"for VRN ${getClientReference(enrolments).getOrElse("")}.")
          Future.successful(Right(authContext(afGroup, Some(identityData), Some(agInfo))))
        case _ => logger.error(s"[AuthorisationService] [authoriseAsClientWithNrsRequirement] Authorisation failed due to unsupported affinity group.")
          Future.successful(Left(Forbidden(toJson(ClientOrAgentNotAuthorized))))
      } recoverWith unauthorisedError
  }

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
}
