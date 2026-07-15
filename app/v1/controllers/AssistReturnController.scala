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

package v1.controllers

import play.api.libs.json.{JsValue, Json}
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import utils._
import v1.controllers.requestParsers.AssistReturnRequestParser
import v1.models.errors._
import v1.models.request.submit.SubmitRawData
import v1.services.EnrolmentsAuthService

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class AssistReturnController @Inject()(val authService: EnrolmentsAuthService,
                                       requestParser: AssistReturnRequestParser,
                                       cc: ControllerComponents,
                                       idGenerator: IdGenerator)
                                      (implicit ec: ExecutionContext)
  extends AuthorisedController(cc) with BaseController with Logging {

  implicit val endpointLogContext: EndpointLogContext =
    EndpointLogContext(controllerName = "AssistReturnController", endpointName = "validateVatReturn")

  def validateReturn(vrn: String): Action[JsValue] =
    authorisedAction(vrn).async(parse.json) { implicit request =>

      implicit val correlationId: String = idGenerator.getUid
      infoLog(s"[${endpointLogContext.controllerName}][${endpointLogContext.endpointName}] " +
        s"Validating Assist VAT Return for VRN : $vrn with correlationId : $correlationId")

      val rawRequest = SubmitRawData(vrn, AnyContent(request.body))

      val result = requestParser.parseRequest(rawRequest) match {
        case Right(_) =>
          infoLog(s"[${endpointLogContext.controllerName}][${endpointLogContext.endpointName}] " +
            s"validation passed for VRN : $vrn, correlationId : $correlationId")
          NoContent.withApiHeaders(correlationId)

        case Left(errorWrapper) =>
          val res = errorResult(errorWrapper).withApiHeaders(errorWrapper.correlationId)
          warnLog(s"[${endpointLogContext.controllerName}][${endpointLogContext.endpointName}] " +
            s"validation failed for VRN : $vrn, correlationId : ${errorWrapper.correlationId}, error : ${errorWrapper.error.message}")
          res
      }

      Future.successful(result)
    }

  private def errorResult(errorWrapper: ErrorWrapper) =
    (errorWrapper.error: @unchecked) match {
      case VrnFormatError | BadRequestError |
           PeriodKeyFormatError | BodyPeriodKeyFormatError |
           VATTotalValueRuleError | VATNetValueRuleError | NumericFormatRuleError |
           MandatoryFieldRuleError | StringFormatRuleError | UnMappedPlayRuleError => BadRequest(Json.toJson(errorWrapper))
      case _: MtdError => BadRequest(Json.toJson(errorWrapper))
    }
}