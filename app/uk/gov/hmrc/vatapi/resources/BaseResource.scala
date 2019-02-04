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

import org.joda.time.DateTime
import play.api.Logger
import play.api.libs.concurrent.Execution.Implicits._
import play.api.mvc.{ActionBuilder, _}
import uk.gov.hmrc.domain.Vrn
import uk.gov.hmrc.play.bootstrap.controller.BaseController
import uk.gov.hmrc.vatapi.auth.{Agent, AuthContext, Organisation}
import uk.gov.hmrc.vatapi.config.{AppContext, FeatureSwitch}
import uk.gov.hmrc.vatapi.services.AuthorisationService

import scala.concurrent.Future
import scala.util.Right

trait BaseResource extends BaseController {
  val authService: AuthorisationService
  val appContext: AppContext

  val logger: Logger = Logger(this.getClass)

  lazy val featureSwitch = FeatureSwitch(appContext.featureSwitch)

  def AuthAction(vrn: Vrn) = new ActionRefiner[Request, AuthRequest] {
    logger.debug(s"[BaseResource][AuthAction] Check MTD VAT authorisation for the VRN : $vrn")

    override protected def refine[A](request: Request[A]): Future[Either[Result, AuthRequest[A]]] =
      if (featureSwitch.isAuthEnabled) {
        implicit val ev: Request[A] = request
        authService.authCheck(vrn) map {
          case Right(authContext) => Right(new AuthRequest(authContext, request))
          case Left(authError) => Left(authError)
        }
      } else
        Future.successful(Right(new AuthRequest(Organisation(), request)))
  }

  def AuthActionWithNrsRequirement(vrn: Vrn): ActionRefiner[Request, AuthRequest] = new ActionRefiner[Request, AuthRequest] {
    logger.debug(s"[BaseResource][AuthAction] Check MTD VAT authorisation for the VRN : $vrn and retrieve NRS data")

    override protected def refine[A](request: Request[A]): Future[Either[Result, AuthRequest[A]]] =
      if (featureSwitch.isAuthEnabled) {
        implicit val ev: Request[A] = request
        authService.authCheckWithNrsRequirement(vrn) map {
          case Right(authContext) => Right(new AuthRequest(authContext, request))
          case Left(authError) => Left(authError)
        }
      } else
        Future.successful(Right(new AuthRequest(Organisation(), request)))
  }

  def APIAction(vrn: Vrn, nrsRequired: Boolean = false): ActionBuilder[AuthRequest] = if (nrsRequired) {
    new ActionBuilder[Request] with ActionFilter[Request] {
      override protected def filter[A](request: Request[A]): Future[Option[Result]] =
        Future {
          None
        }
    } andThen AuthActionWithNrsRequirement(vrn)
  } else {
    new ActionBuilder[Request] with ActionFilter[Request] {
      override protected def filter[A](request: Request[A]): Future[Option[Result]] =
        Future {
          None
        }
    } andThen AuthAction(vrn)
  }


  def getRequestDateTimestamp(implicit request: AuthRequest[_]): String = {
    val requestTimestampHeader = "X-Request-Timestamp"
    val requestTimestamp = request.headers.get(requestTimestampHeader) match {
      case Some(timestamp) if timestamp.trim.length > 0 => timestamp.trim
      case _ =>
        logger.warn(s"$requestTimestampHeader header is not passed or is empty in the request.")
        DateTime.now().toString()
    }
    requestTimestamp
  }

  def getArn(implicit request: AuthRequest[_]): Option[String] = {
    request.authContext match {
      case Agent(_, _, _, enrolments) => enrolments.getEnrolment("HMRC-AS-AGENT").flatMap(_.getIdentifier("AgentReferenceNumber")).map(_.value)
      case c: AuthContext => c.agentReference
    }
  }


}

class AuthRequest[A](val authContext: AuthContext, request: Request[A]) extends WrappedRequest[A](request)