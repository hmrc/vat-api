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

package v1.controllers

import play.api.libs.json.Json
import play.api.libs.typedmap.TypedKey
import play.api.mvc._
import uk.gov.hmrc.auth.core.Enrolment
import uk.gov.hmrc.auth.core.authorise.Predicate
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController
import utils.IdGenerator
import v1.controllers.requestParsers.validators.validations.VrnValidation
import v1.models.auth.UserDetails
import v1.models.errors.{DownstreamError, ForbiddenDownstreamError, LegacyUnauthorisedError, VrnFormatError}
import v1.services.{AuditService, EnrolmentsAuthService}

import scala.concurrent.{ExecutionContext, Future}

case class UserRequest[A](userDetails: UserDetails, request: Request[A]) extends WrappedRequest[A](request)

abstract class AuthorisedController(cc: ControllerComponents)(implicit ec: ExecutionContext) extends BackendController(cc) {
object AuditFailureHandler { val AttrKey: TypedKey[(String, RequestHeader, Result) => Unit] = TypedKey("auditFailureHandler")}

  val authService: EnrolmentsAuthService
  val idGenerator: IdGenerator

  def authorisedAction(vrn: String, nrsRequired: Boolean = false, auditOnFailure: Boolean = false): ActionBuilder[UserRequest, AnyContent] = new ActionBuilder[UserRequest, AnyContent] {

    override def parser: BodyParser[AnyContent] = cc.parsers.defaultBodyParser

    override protected def executionContext: ExecutionContext = cc.executionContext

    def predicate(vrn: String): Predicate =
      Enrolment("HMRC-MTD-VAT")
        .withIdentifier("VRN", vrn)
        .withDelegatedAuthRule("mtd-vat-auth")

    override def invokeBlock[A](request: Request[A], block: UserRequest[A] => Future[Result]): Future[Result] = {

      implicit val headerCarrier: HeaderCarrier = hc(request)

      val clientId = request.headers.get("X-Client-Id").getOrElse("N/A")
      val correlationId: String = idGenerator.getUid

      def auditFailure(result: Result): Unit =
        if (auditOnFailure) {
          println("checking for audit failure")
          request.attrs
            .get(AuditFailureHandler.AttrKey)
            .foreach{handler =>
              println(s"checking for handler failure")
              handler(correlationId, request, result)}
        }

      if (VrnValidation.validate(vrn) == Nil) {
        authService.authorised(predicate(vrn), nrsRequired)(headerCarrier,executionContext, request).flatMap[Result] {
          case Right(userDetails) => block(UserRequest(userDetails.copy(clientId = clientId), request))
          case Left(LegacyUnauthorisedError) => val result = Forbidden(Json.toJson(LegacyUnauthorisedError))
            auditFailure(result)
            Future.successful(result)
          case Left(ForbiddenDownstreamError) => val result = Forbidden(Json.toJson(DownstreamError))
            auditFailure(result)
            Future.successful(result)
          case Left(_) => val result = InternalServerError(Json.toJson(DownstreamError))
            auditFailure(result)
            Future.successful(result)
        }
      } else {
        val result = BadRequest(Json.toJson(VrnFormatError))
        auditFailure(result)
        Future.successful(result)
      }
    }
  }
}
