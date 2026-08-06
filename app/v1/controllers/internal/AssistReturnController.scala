/*
 * Copyright 2026 HM Revenue & Customs
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

package v1.controllers.internal

import play.api.libs.json.{ JsValue, Json }
import play.api.mvc.{ Action, AnyContent, ControllerComponents, Result }
import utils._
import v1.controllers.{ AuthorisedController, BaseController }
import v1.models.errors._
import v1.models.outcomes.ResponseWrapper
import v1.models.request.submit.SubmitRawData
import v1.services.{ AssistReturnService, EnrolmentsAuthService }

import javax.inject.{ Inject, Singleton }
import scala.concurrent.ExecutionContext

@Singleton
class AssistReturnController @Inject()(val authService: EnrolmentsAuthService, assistReturnService: AssistReturnService, cc: ControllerComponents)(
    implicit ec: ExecutionContext)
    extends AuthorisedController(cc)
    with BaseController
    with Logging {

  private implicit val endpointLogContext: EndpointLogContext =
    EndpointLogContext(controllerName = "AssistReturnController", endpointName = "validateReturnAndRetrieveObligation")

  def validateReturnAndRetrieveObligation(vrn: String): Action[JsValue] =
    authorisedAction(vrn).async(parse.json) { implicit request =>
      implicit val correlationId: String = request.headers.get("X-CorrelationId").getOrElse("no-correlation-id-found")

      val rawRequest = SubmitRawData(vrn, AnyContent(request.body))

      assistReturnService.validateAndRetrieveOpenObligation(rawRequest).map {

        case Right(ResponseWrapper(corrId, obligation)) =>
          Ok(Json.toJson(obligation)).withApiHeaders(corrId)

        case Left(errorWrapper) =>
          errorResult(errorWrapper)
      }
    }

  /** Only obligation-lookup failures produce 5xx. Everything else is a 400: parser validation errors, TAX_PERIOD_NOT_ENDED,
    * and an unmatched period key.
    */
  private def errorResult(errorWrapper: ErrorWrapper): Result =
    (errorWrapper.error match {
      case ServiceUnavailableError | DownstreamError => ServiceUnavailable(Json.toJson(errorWrapper))
      case _                                         => BadRequest(Json.toJson(errorWrapper))
    }).withApiHeaders(errorWrapper.correlationId)
}
