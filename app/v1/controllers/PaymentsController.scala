/*
 * Copyright 2023 HM Revenue & Customs
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
import v1.controllers.requestParsers.PaymentsRequestParser
import v1.models.audit.AuditResponse
import v1.models.errors._
import v1.models.request.payments.PaymentsRawData
import v1.services.{AuditService, EnrolmentsAuthService, PaymentsService}

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class PaymentsController @Inject()(val authService: EnrolmentsAuthService,
                                   requestParser: PaymentsRequestParser,
                                   service: PaymentsService,
                                   auditService: AuditService,
                                   cc: ControllerComponents,
                                   val idGenerator: IdGenerator)
                                  (implicit ec: ExecutionContext)
extends AuthorisedController(cc) with BaseController with Logging {

  implicit val endpointLogContext: EndpointLogContext =
    EndpointLogContext(
      controllerName = "PaymentsController",
      endpointName = "retrievePayments"
    )

  def retrievePayments(vrn: String, from: Option[String], to: Option[String]): Action[AnyContent] =
    authorisedAction(vrn).async { implicit request =>

      implicit val correlationId: String = idGenerator.getUid
      infoLog(s"[${endpointLogContext.controllerName}][${endpointLogContext.endpointName}] " +
        s"Retrieve Payments for VRN : $vrn with correlationId : $correlationId")

      val rawRequest: PaymentsRawData =
        PaymentsRawData(
          vrn = vrn,
          from = from,
          to = to
        )

      val result =
        for {
          parsedRequest <- EitherT.fromEither[Future](requestParser.parseRequest(rawRequest))
          serviceResponse <- EitherT(service.retrievePayments(parsedRequest))
        } yield {
          infoLog(s"[${endpointLogContext.controllerName}][${endpointLogContext.endpointName}] " +
            s"Successfully retrieved Payments from DES with correlationId : ${serviceResponse.correlationId}")

          auditService.auditEvent(AuditEvents.auditPayments(serviceResponse.correlationId,
            request.userDetails, AuditResponse(OK, Right(Some(Json.toJson(serviceResponse.responseData))))))

          Ok(Json.toJson(serviceResponse.responseData))
            .withApiHeaders(serviceResponse.correlationId)
        }

      result.leftMap { errorWrapper =>
        val resCorrelationId: String = errorWrapper.correlationId
        val leftResult = errorResult(errorWrapper).withApiHeaders(resCorrelationId)
        warnLog(ControllerError(endpointLogContext, vrn, request, leftResult.header.status, errorWrapper.error.message, resCorrelationId))
        auditService.auditEvent(AuditEvents.auditPayments(resCorrelationId,
          request.userDetails, AuditResponse(httpStatus = leftResult.header.status, Left(errorWrapper.auditErrors))))

        leftResult
      }.merge
    }

  private def errorResult(errorWrapper: ErrorWrapper): Result = {
    (errorWrapper.error: @unchecked) match {
      case VrnFormatError | VrnFormatErrorDes |
           FinancialDataInvalidDateFromError | InvalidDateFromErrorDes |
           FinancialDataInvalidDateToError | InvalidDateToErrorDes |
           FinancialDataInvalidDateRangeError | InvalidDataError | RuleIncorrectGovTestScenarioError
      => BadRequest(Json.toJson(errorWrapper))
      case RuleInsolventTraderError => Forbidden(Json.toJson(errorWrapper))
      case LegacyNotFoundError => NotFound(Json.toJson(errorWrapper))
      case DownstreamError => InternalServerError(Json.toJson(errorWrapper))
    }
  }
}
