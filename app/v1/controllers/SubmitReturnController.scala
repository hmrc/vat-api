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
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import play.mvc.Http.MimeTypes
import utils.{CurrentDateTime, DateUtils, EndpointLogContext, IdGenerator, Logging}
import v1.audit.AuditEvents
import v1.controllers.requestParsers.SubmitReturnRequestParser
import v1.models.audit.{AuditError, AuditResponse, NrsAuditDetail}
import v1.models.errors.ControllerError._
import v1.models.errors._
import v1.models.nrs.response.NrsResponse
import v1.models.request.submit.SubmitRawData
import v1.services.{AuditService, EnrolmentsAuthService, NrsService, SubmitReturnService}

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class SubmitReturnController @Inject()(val authService: EnrolmentsAuthService,
                                       requestParser: SubmitReturnRequestParser,
                                       service: SubmitReturnService,
                                       nrsService: NrsService,
                                       auditService: AuditService,
                                       cc: ControllerComponents,
                                       dateTime: CurrentDateTime,
                                       idGenerator: IdGenerator)
                                      (implicit ec: ExecutionContext)
  extends AuthorisedController(cc) with BaseController with Logging {

  implicit val endpointLogContext: EndpointLogContext =
    EndpointLogContext(
      controllerName = "SubmitReturnController",
      endpointName = "submitVatReturn"
    )

  def submitReturn(vrn: String): Action[JsValue] =
    authorisedAction(vrn, nrsRequired = true).async(parse.json) { implicit request =>

      implicit val correlationId: String = idGenerator.getCorrelationId
      logger.info(message = s"[${endpointLogContext.controllerName}][${endpointLogContext.endpointName}] " +
        s"Submitting Vat Return for VRN : $vrn with correlationId : $correlationId")

      val rawRequest: SubmitRawData = SubmitRawData(vrn, AnyContent(request.body))

      val nrsId = idGenerator.getUid
      val submissionTimestamp = dateTime.getDateTime

      val arn = request.userDetails.agentReferenceNumber

      val result = for {
        parsedRequest <- EitherT.fromEither[Future](requestParser.parseRequest(rawRequest))
        nrsResponse <- EitherT(nrsService.submitNrs(parsedRequest, nrsId, submissionTimestamp))
        serviceResponse <- EitherT(service.submitReturn(parsedRequest.copy(body =
          parsedRequest.body.copy(receivedAt =
            Some(submissionTimestamp.toString(DateUtils.dateTimePattern)), agentReference = arn))))
      } yield {
        logger.info(message = s"${endpointLogContext.controllerName}][${endpointLogContext.endpointName}] " +
          s" - Successfully created with correlationId : ${serviceResponse.correlationId}")

        auditService.auditEvent(AuditEvents.auditSubmit(serviceResponse.correlationId,
          request.userDetails, AuditResponse(CREATED, Right(Some(Json.toJson(serviceResponse.responseData))))))

        Created(Json.toJson(serviceResponse.responseData))
          .withApiHeaders(serviceResponse.correlationId,
            "Receipt-ID" -> nrsId,
            "Receipt-Timestamp" -> submissionTimestamp.toString(DateUtils.isoInstantDatePattern),
            "Receipt-Signature" -> nrsResponse.cadesTSignature)
          .as(MimeTypes.JSON)
      }

      result.leftMap { errorWrapper =>
        val resCorrelationId: String = errorWrapper.correlationId
        val leftResult = errorResult(errorWrapper).withApiHeaders(resCorrelationId)
        logger.warn(ControllerError(endpointLogContext ,vrn, request, leftResult.header.status, errorWrapper.error.message, resCorrelationId))

        auditService.auditEvent(AuditEvents.auditSubmit(resCorrelationId,
          request.userDetails, AuditResponse(leftResult.header.status, Left(retrieveAuditErrors(errorWrapper)))))

        leftResult
      }.merge
    }

  private def errorResult(errorWrapper: ErrorWrapper) = {
    (errorWrapper.error: @unchecked) match {
      case VrnFormatError | VrnFormatErrorDes | BadRequestError |
           PeriodKeyFormatError | PeriodKeyFormatErrorDes | BodyPeriodKeyFormatError |
           VATTotalValueRuleError | VATNetValueRuleError | NumericFormatRuleError |
           MandatoryFieldRuleError | StringFormatRuleError | UnMappedPlayRuleError => BadRequest(Json.toJson(errorWrapper))
      case TaxPeriodNotEnded | DuplicateVatSubmission | FinalisedValueRuleError => Forbidden(Json.toJson(errorWrapper))
      case DownstreamError => InternalServerError(Json.toJson(errorWrapper))
      case _: MtdError => BadRequest(Json.toJson(errorWrapper))
    }
  }

  private def retrieveAuditErrors(errorWrapper: ErrorWrapper): Seq[AuditError] = {

    val allErrors: Seq[MtdError] = errorWrapper.errors match {
      case Some(errors) if errors.nonEmpty => ErrorWrapper.allErrors(errors).map(_.as[MtdError])
      case _ => Seq(errorWrapper.error)
    }
    allErrors.map(error => AuditError(error.code))
  }
}
