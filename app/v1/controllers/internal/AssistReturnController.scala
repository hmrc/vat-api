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
import play.api.mvc.{ Action, AnyContent, ControllerComponents }
import utils._
import v1.controllers.requestParsers.AssistReturnRequestParser
import v1.controllers.{ AuthorisedController, BaseController }
import v1.models.request.submit.SubmitRawData
import v1.services.EnrolmentsAuthService

import javax.inject.{ Inject, Singleton }
import scala.concurrent.{ ExecutionContext, Future }

@Singleton
class AssistReturnController @Inject()(val authService: EnrolmentsAuthService,
                                       requestParser: AssistReturnRequestParser,
                                       cc: ControllerComponents,
                                       idGenerator: IdGenerator)(implicit ec: ExecutionContext)
    extends AuthorisedController(cc)
    with BaseController
    with Logging {

  private val endpointLogContext: EndpointLogContext =
    EndpointLogContext(controllerName = "AssistReturnController", endpointName = "validateVatReturn")

  def validateReturn(vrn: String): Action[JsValue] =
    authorisedAction(vrn).async(parse.json) { implicit request =>
      implicit val correlationId: String = request.headers.get("X-CorrelationId").getOrElse("no-correlation-id-found")

      val rawRequest = SubmitRawData(vrn, AnyContent(request.body))

      val result = requestParser.parseRequest(rawRequest) match {
        case Right(_) =>
          infoLog(
            s"[${endpointLogContext.controllerName}][${endpointLogContext.endpointName}] " +
              s"VAT return validation PASSED for VRN : $vrn with correlationId : $correlationId")
          NoContent.withApiHeaders(correlationId)

        case Left(errorWrapper) =>
          infoLog(
            s"[${endpointLogContext.controllerName}][${endpointLogContext.endpointName}] " +
              s"VAT return validation FAILED for VRN : $vrn with correlationId : ${errorWrapper.correlationId}, error : ${errorWrapper.error.message}")
          BadRequest(Json.toJson(errorWrapper)).withApiHeaders(errorWrapper.correlationId)
      }

      Future.successful(result)
    }
}
