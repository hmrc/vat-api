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

import cats.data.EitherT
import cats.implicits._
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, ControllerComponents, Result}
import utils.{EndpointLogContext, IdGenerator, Logging}
import v1.audit.AuditEvents
import v1.controllers.requestParsers.CustomerInfoRequestParser
import v1.models.audit.AuditResponse
import v1.models.errors._
import v1.models.request.information.CustomerRawData
import v1.models.response.information.{CustomerDetails, CustomerInfoResponse, FlatRateScheme}
import v1.services.{AuditService, CustomerInfoService, EnrolmentsAuthService}

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class CustomerInfoController @Inject()(val authService: EnrolmentsAuthService,
                                       requestParser: CustomerInfoRequestParser,
                                       service: CustomerInfoService,
                                       auditService: AuditService,
                                       cc: ControllerComponents,
                                       val idGenerator: IdGenerator)
                                      (implicit ec: ExecutionContext)
extends AuthorisedController(cc) with BaseController with Logging {

  implicit val endpointLogContext: EndpointLogContext =
    EndpointLogContext(
      controllerName = "CustomerInfoController",
      endpointName = "retrieveCustomerInfo"
    )

  def retrieveCustomerInfo(vrn: String): Action[AnyContent] =
    authorisedAction(vrn).async { implicit request =>

      implicit val correlationId: String = idGenerator.getUid
      infoLog(s"[${endpointLogContext.controllerName}][${endpointLogContext.endpointName}] " +
        s"Retrieve Customer Info for VRN : $vrn with correlationId : $correlationId")

      val rawRequest: CustomerRawData =
        CustomerRawData(
          vrn = vrn
        )

      val result =
        for {
          parsedRequest <- EitherT.fromEither[Future](requestParser.parseRequest(rawRequest))
          serviceResponse <- EitherT(service.retrieveCustomerInfo(parsedRequest))
        } yield {
          infoLog(s"[${endpointLogContext.controllerName}][${endpointLogContext.endpointName}] " +
            s"Successfully retrieved CustomerInfo from vat-subscription with correlationId : ${serviceResponse.correlationId}")

          auditService.auditEvent(AuditEvents.auditCustomerInfo(serviceResponse.correlationId,
            request.userDetails, AuditResponse(OK, Right(Some(Json.toJson(serviceResponse.responseData))))))

          if (serviceResponse.responseData == CustomerInfoResponse(Some(CustomerDetails(None)),Some(FlatRateScheme(None,None)))) {
            warnLog(s"[${endpointLogContext.controllerName}][${endpointLogContext.endpointName}] " +
              s"Empty response data for VRN : $vrn with correlationId : ${serviceResponse.correlationId}")
            InternalServerError(Json.toJson(ErrorWrapper(serviceResponse.correlationId, DownstreamError)))
          } else {
            Ok(Json.toJson(serviceResponse.responseData))
              .withApiHeaders(serviceResponse.correlationId)
          }
        }

      result.leftMap { errorWrapper =>
        val resCorrelationId: String = errorWrapper.correlationId
        val leftResult = errorResult(errorWrapper).withApiHeaders(resCorrelationId)
        warnLog(ControllerError(endpointLogContext, vrn, request, leftResult.header.status, errorWrapper.error.message, resCorrelationId))
        auditService.auditEvent(AuditEvents.auditCustomerInfo(resCorrelationId,
          request.userDetails, AuditResponse(httpStatus = leftResult.header.status, Left(errorWrapper.auditErrors))))

        leftResult
      }.merge
    }

  private def errorResult(errorWrapper: ErrorWrapper): Result = {
    (errorWrapper.error: @unchecked) match {
      case CustomerInfoInvalidIdValue => BadRequest(Json.toJson(errorWrapper))
      case CustomerInfoNotDataFound => NotFound(Json.toJson(errorWrapper))
      case _ => InternalServerError(Json.toJson(errorWrapper))
    }
  }
}


