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
import play.api.http.MimeTypes
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, ControllerComponents, Result}
import utils.{EndpointLogContext, Logging}
import v1.controllers.requestParsers.PaymentsRequestParser
import v1.models.errors._
import v1.models.request.payments.PaymentsRawData
import v1.services.{EnrolmentsAuthService, PaymentsService}

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class PaymentsController @Inject()(val authService: EnrolmentsAuthService,
                                   requestParser: PaymentsRequestParser,
                                   service: PaymentsService,
                                   cc: ControllerComponents)(implicit ec: ExecutionContext)
extends AuthorisedController(cc) with BaseController with Logging {

  implicit val endpointLogContext: EndpointLogContext =
    EndpointLogContext(
      controllerName = "PaymentsController",
      endpointName = "retrievePayments"
    )

  def retrievePayments(vrn: String, from: Option[String], to: Option[String]): Action[AnyContent] =
    authorisedAction(vrn).async{ implicit request =>
      logger.info(s"[${endpointLogContext.controllerName}][${endpointLogContext.endpointName}] " +
        s"Successfully retrieved Payments from DES")

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
          logger.info(s"[${endpointLogContext.controllerName}][${endpointLogContext.endpointName}] " +
            s"Successfully retrieved Payments from DES")

          Ok(Json.toJson(serviceResponse.responseData))
            .withApiHeaders(serviceResponse.correlationId)
            .as(MimeTypes.JSON)
        }

      result.leftMap{errorWrapper =>
        val correlationId = getCorrelationId(errorWrapper)
        val result = errorResult(errorWrapper).withApiHeaders(correlationId)
        result
      }.merge

    }

  private def errorResult(errorWrapper: ErrorWrapper): Result = {
    (errorWrapper.error: @unchecked) match {
      case VrnFormatError | VrnFormatErrorDes |
           FinancialDataInvalidDateFromError | InvalidDateFromErrorDes |
           FinancialDataInvalidDateToError | InvalidDateToErrorDes |
           FinancialDataInvalidDateRangeError | InvalidDataError
      => BadRequest(Json.toJson(errorWrapper))
      case LegacyNotFoundError => NotFound(Json.toJson(errorWrapper))
      case DownstreamError => InternalServerError(Json.toJson(errorWrapper))
    }
  }
}
