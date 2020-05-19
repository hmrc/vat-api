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
import utils.{CurrentDateTime, DateTimeUtil, DateUtils, EndpointLogContext, Logging}
import v1.audit.AuditEvents
import v1.controllers.requestParsers.SubmitReturnRequestParser
import v1.models.audit.{AuditError, AuditResponse, NrsAuditDetail}
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
                                       val dateTime: CurrentDateTime)(implicit ec: ExecutionContext)
  extends AuthorisedController(cc) with BaseController with Logging {

  implicit val endpointLogContext: EndpointLogContext =
    EndpointLogContext(
      controllerName = "SubmitReturnController",
      endpointName = "submitVatReturn"
    )

  def submitReturn(vrn: String): Action[JsValue] = {
    authorisedAction(vrn, nrsRequired = true).async(parse.json) { implicit request =>
      logger.info(message = s"[${endpointLogContext.controllerName}][${endpointLogContext.endpointName}] " +
        s"Submitting Vat Return")

      val rawRequest: SubmitRawData = SubmitRawData(vrn, AnyContent(request.body))

      val submissionTimestamp = dateTime.getDateTime

      val arn = request.userDetails.agentReferenceNumber

      val result = for {
        parsedRequest <- EitherT.fromEither[Future](requestParser.parseRequest(rawRequest))
        nrsResponse <- EitherT(nrsService.submitNrs(parsedRequest, submissionTimestamp.toString(DateTimeUtil.datePattern)))
        serviceResponse <- EitherT(service.submitReturn(parsedRequest.copy(body =
          parsedRequest.body.copy(receivedAt =
            Some(submissionTimestamp.toString(DateTimeUtil.isoInstantDatePattern)), agentReference = arn))))
      } yield {
        logger.info(message = s"${endpointLogContext.controllerName}][${endpointLogContext.endpointName}] " +
          s" - Successfully created")

        nrsResponse match {
          case NrsResponse.empty => auditService.auditEvent(AuditEvents.auditNrsSubmit("submitToNonRepudiationStoreFailure",
            NrsAuditDetail(vrn, request.headers.get("Authorization").getOrElse(""),
              None, Some(Json.toJson(nrsService.buildNrsSubmission(parsedRequest,
                submissionTimestamp.toString(DateTimeUtil.datePattern), request))), "")))
          case _ => auditService.auditEvent(AuditEvents.auditNrsSubmit("submitToNonRepudiationStore",
            NrsAuditDetail(vrn, request.headers.get("Authorization").getOrElse(""),
              Some(nrsResponse.nrSubmissionId), None, "")))
        }

        auditService.auditEvent(AuditEvents.auditSubmit(serviceResponse.correlationId,
          request.userDetails, AuditResponse(CREATED, Right(Some(Json.toJson(serviceResponse.responseData))))))

        Created(Json.toJson(serviceResponse.responseData))
          .withApiHeaders(serviceResponse.correlationId,
            "Receipt-ID" -> nrsResponse.nrSubmissionId,
            "Receipt-Timestamp" -> submissionTimestamp.toString(DateUtils.dateTimePattern),
            "Receipt-Signature" -> nrsResponse.cadesTSignature)
          .as(MimeTypes.JSON)
      }

      result.leftMap { errorWrapper =>
        val correlationId = getCorrelationId(errorWrapper)
        val result = errorResult(errorWrapper).withApiHeaders(correlationId)

        auditService.auditEvent(AuditEvents.auditSubmit(correlationId,
          request.userDetails, AuditResponse(result.header.status, Left(retrieveAuditErrors(errorWrapper)))))

        result
      }.merge
    }
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
