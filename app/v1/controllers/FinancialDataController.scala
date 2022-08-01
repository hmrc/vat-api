/*
 * Copyright 2022 HM Revenue & Customs
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
import config.AppConfig
import javax.inject.{Inject, Singleton}
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, ControllerComponents, Result}
import utils.{EndpointLogContext, IdGenerator, Logging}
import v1.audit.AuditEvents
import v1.controllers.requestParsers.FinancialDataRequestParser
import v1.models.audit.AuditResponse
import v1.models.errors._
import v1.models.request.penalties.FinancialRawData
import v1.services.{AuditService, EnrolmentsAuthService, PenaltiesService}

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class FinancialDataController @Inject()(val authService: EnrolmentsAuthService,
                                        requestParser: FinancialDataRequestParser,
                                        service: PenaltiesService,
                                        auditService: AuditService,
                                        cc: ControllerComponents,
                                        val idGenerator: IdGenerator,
                                        appConfig: AppConfig)
                                       (implicit ec: ExecutionContext)
  extends AuthorisedController(cc) with BaseController with Logging {

  implicit val endpointLogContext: EndpointLogContext =
    EndpointLogContext(
      controllerName = "FinancialDataController",
      endpointName = "retrieveFinancialData"
    )


  def retrieveFinancialData(vrn: String): Action[AnyContent] = authorisedAction(vrn).async { implicit request =>

    implicit val correlationId: String = idGenerator.getUid

    logger.info(s"${endpointLogContext.toString} correlationId: $correlationId: " +
      s"attempting to retrieve financial data for VRN: $vrn")


    val result: EitherT[Future, ErrorWrapper, Result] = {
      for {
        parsedRequest <- EitherT.fromEither[Future](requestParser.parseRequest(FinancialRawData(vrn)))
        serviceResponse <- EitherT(service.retrieveFinancialData(parsedRequest))
      } yield {

        logger.info(s"${endpointLogContext.toString} " +
          s"Successfully retrieved Financial Data from DES with correlationId : ${serviceResponse.correlationId}")


        auditService.auditEvent(AuditEvents.auditFinancialData(serviceResponse.correlationId,
          request.userDetails, AuditResponse(OK, Right(Some(Json.toJson(serviceResponse.responseData))))
        ))

        Ok(Json.toJson(serviceResponse.responseData))
          .withApiHeaders(serviceResponse.correlationId)
      }
    }
    result.leftMap { errorWrapper: ErrorWrapper =>
      val resCorrelationId: String = errorWrapper.correlationId
      val leftResult = errorResult(errorWrapper).withApiHeaders(resCorrelationId)
      logger.warn(ControllerError(endpointLogContext, vrn, request, leftResult.header.status, errorWrapper.error.message, resCorrelationId))

      auditService.auditEvent(AuditEvents.auditFinancialData(resCorrelationId,
        request.userDetails, AuditResponse(httpStatus = leftResult.header.status, Left(errorWrapper.auditErrors))
      ))

      leftResult
    }.merge
  }

  private def errorResult(errorWrapper: ErrorWrapper): Result = {
    (errorWrapper.error: @unchecked) match {
      case VrnFormatError |
           FinancialInvalidCorrelationId |
           FinancialInvalidIdType |
           FinancialInvalidIdNumber |
           FinancialInvalidRegimeType |
           FinancialInvalidDocNumber |
           FinancialInvalidOnlyOpenItems |
           FinancialInvalidIncludeLocks |
           FinancialInvalidIncludeAccruedInterest |
           FinancialInvalidIncludeCustomerPaymentInfo |
           FinancialInvalidDateFrom |
           FinancialInvalidDateTo |
           FinancialInvalidDateRange |
           FinancialInvalidRemovePaymentOnAccount |
           FinancialInvalidIncludeStats |
           FinancialInvalidRequest => BadRequest(Json.toJson(errorWrapper))
      case FinancialNotDataFound => NotFound(Json.toJson(errorWrapper))
      case FinancialDuplicateSubmission => Conflict(Json.toJson(errorWrapper))
      case FinancialInvalidIdTypeUnprocessEntity |
           FinancialInvalidIdValueUnprocessEntity |
           FinancialInvalidRegimeUnprocessEntity |
           FinancialInvalidDocNumberUnprocessEntity |
           FinancialInvalidRequestUnprocessEntity => UnprocessableEntity(Json.toJson(errorWrapper))
      case FinancialServiceUnavailable => ServiceUnavailable(Json.toJson(errorWrapper))
      case _ => InternalServerError(Json.toJson(errorWrapper))
    }
  }
}
