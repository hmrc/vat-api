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

package v1.controllers

import cats.data.EitherT
import cats.implicits._
import javax.inject.{Inject, Singleton}
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import play.mvc.Http.MimeTypes
import utils.{EndpointLogContext, IdGenerator, Logging}
import v1.audit.AuditEvents
import v1.controllers.requestParsers.ViewReturnRequestParser
import v1.models.audit.AuditResponse
import v1.models.errors._
import v1.models.request.viewReturn.ViewRawData
import v1.services.{AuditService, EnrolmentsAuthService, ViewReturnService}

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ViewReturnController @Inject()(val authService: EnrolmentsAuthService,
                                    requestParser: ViewReturnRequestParser,
                                    service: ViewReturnService,
                                    auditService: AuditService,
                                    cc: ControllerComponents,
                                    val idGenerator: IdGenerator)(implicit ec: ExecutionContext)
  extends AuthorisedController(cc) with BaseController with Logging {

  implicit val endpointLogContext: EndpointLogContext =
    EndpointLogContext(
      controllerName = "ViewReturnController",
      endpointName = "viewReturn"
    )

  def viewReturn(vrn: String, periodKey: String): Action[AnyContent] =
    authorisedAction(vrn).async { implicit request =>

      implicit val correlationId: String = idGenerator.getUid
      logger.info(message = s"[${endpointLogContext.controllerName}][${endpointLogContext.endpointName}] " +
        s"Retrieve VAT returns for VRN : $vrn with correlationId : $correlationId")

      val rawRequest: ViewRawData =
        ViewRawData(
          vrn = vrn,
          periodKey = periodKey
        )

      val result =
        for {
          parsedRequest <- EitherT.fromEither[Future](requestParser.parseRequest(rawRequest))
          serviceResponse <- EitherT(service.viewReturn(parsedRequest))
        } yield {
          logger.info(message = s"[${endpointLogContext.controllerName}][${endpointLogContext.endpointName}] " +
            s"Successfully retrieved Vat Return from DES with correlationId : ${serviceResponse.correlationId}")

          auditService.auditEvent(AuditEvents.auditReturns(serviceResponse.correlationId,
            request.userDetails, AuditResponse(OK, Right(Some(Json.toJson(serviceResponse.responseData))))))

          Ok(Json.toJson(serviceResponse.responseData))
            .withApiHeaders(serviceResponse.correlationId)
            .as(MimeTypes.JSON)
        }

      result.leftMap { errorWrapper =>
        val resCorrelationId: String = errorWrapper.correlationId
        val leftResult = errorResult(errorWrapper).withApiHeaders(resCorrelationId)
        logger.warn(ControllerError(endpointLogContext, vrn, request, leftResult.header.status, errorWrapper.error.message, resCorrelationId))

        auditService.auditEvent(AuditEvents.auditReturns(resCorrelationId,
          request.userDetails, AuditResponse(httpStatus = leftResult.header.status, Left(errorWrapper.auditErrors))))

        leftResult
      }.merge

    }

  private def errorResult(errorWrapper: ErrorWrapper) = {
    (errorWrapper.error: @unchecked) match {
      case VrnFormatError | VrnFormatErrorDes |
           PeriodKeyFormatError | PeriodKeyFormatErrorDes |
           BadRequestError => BadRequest(Json.toJson(errorWrapper))
      case RuleDateRangeTooLargeError | InvalidInputDataError => Forbidden(Json.toJson(errorWrapper))
      case PeriodKeyFormatErrorDesNotFound => NotFound(Json.toJson(errorWrapper))
      case EmptyNotFoundError => NotFound
      case DownstreamError => InternalServerError(Json.toJson(errorWrapper))
    }
  }
}
