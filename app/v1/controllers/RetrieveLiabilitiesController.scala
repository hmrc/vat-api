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
import utils.{EndpointLogContext, Logging}
import v1.controllers.requestParsers.LiabilitiesRequestParser
import v1.models.errors._
import v1.models.request.liability.LiabilityRawData
import v1.services.{EnrolmentsAuthService, RetrieveLiabilitiesService}

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class RetrieveLiabilitiesController @Inject()(val authService: EnrolmentsAuthService,
                                              requestParser: LiabilitiesRequestParser,
                                              service: RetrieveLiabilitiesService,
                                              cc: ControllerComponents)(implicit ec: ExecutionContext)
  extends AuthorisedController(cc) with BaseController with Logging {

  implicit val endpointLogContext: EndpointLogContext =
    EndpointLogContext(
      controllerName = "FinancialDataResource",
      endpointName = "retrieveLiabilities"
    )

  def retrieveLiabilities(vrn: String, from: Option[String], to: Option[String]): Action[AnyContent] =
    authorisedAction(vrn).async { implicit request =>
      logger.info(message = s"[FinancialDataResource][retrieveLiabilities] Retrieving Liabilities from DES")

      val rawRequest: LiabilityRawData = LiabilityRawData(vrn, from, to)

      val result = for {
        parsedRequest <- EitherT.fromEither[Future](requestParser.parseRequest(rawRequest))
        serviceResponse <- EitherT(service.retrieveLiabilities(parsedRequest))
      } yield {
        logger.info(message = s"[FinancialDataResource][retrieveLiabilities] Successfully retrieved Liabilities from DES")

        Ok(Json.toJson(serviceResponse.responseData))
          .withApiHeaders(serviceResponse.correlationId)
          .as(MimeTypes.JSON)
      }

      result.leftMap { errorWrapper =>
        val correlationId = getCorrelationId(errorWrapper)
        val result = errorResult(errorWrapper).withApiHeaders(correlationId)
        result
      }.merge

    }

  private def errorResult(errorWrapper: ErrorWrapper) = {
    (errorWrapper.error: @unchecked) match {
      case VrnFormatError | VrnFormatErrorDes | InvalidDateFromError | InvalidDateToError |
           RuleDateRangeInvalidError | BadRequestError | InvalidDataError => BadRequest(Json.toJson(errorWrapper))
      case LegacyUnauthorisedError => Forbidden(Json.toJson(errorWrapper))
      case LegacyNotFoundError => NotFound(Json.toJson(errorWrapper))
      case DownstreamError => InternalServerError(Json.toJson(errorWrapper))
    }
  }
}
