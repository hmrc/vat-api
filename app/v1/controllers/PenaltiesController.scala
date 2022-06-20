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
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, ControllerComponents, Result}
import utils.{IdGenerator, Logging}
import v1.controllers.requestParsers.PenaltiesRequestParser
import v1.models.errors.{ErrorWrapper, VrnFormatError, VrnNotFound}
import v1.models.outcomes.ResponseWrapper
import v1.models.request.penalties.PenaltiesRawData
import v1.models.response.penalties.PenaltiesResponse
import v1.services.{AuditService, EnrolmentsAuthService, PenaltiesService}

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class PenaltiesController @Inject()(val authService: EnrolmentsAuthService,
                                    requestParser: PenaltiesRequestParser,
                                    service: PenaltiesService,
                                    auditService: AuditService,
                                    cc: ControllerComponents,
                                    val idGenerator: IdGenerator)
                                   (implicit ec: ExecutionContext)
  extends AuthorisedController(cc) with BaseController with Logging {

  //TODO feature switch

  def retrievePenalties(vrn: String): Action[AnyContent] = authorisedAction(vrn).async { implicit request =>

    implicit val correlationId: String = idGenerator.getUid

    logger.info(s"[PenaltiesController][retrievePenalties] correlationId: $correlationId: " +
      s"attempting to retrieve penalties for VRN: $vrn")


    val result: EitherT[Future, ErrorWrapper, Result] = {
      for {
        parsedRequest <- EitherT.fromEither[Future](requestParser.parseRequest(PenaltiesRawData(vrn)))
        serviceResponse <- EitherT(service.retrievePenalties(parsedRequest))
      } yield {
        logger.info(s"[PenaltiesController][retrievePenalties] correlationId : $correlationId: " +
          s"successfully received penalties from Penalties")

        //TODO auditing

        Ok(Json.toJson(serviceResponse.responseData))
          .withApiHeaders(serviceResponse.correlationId)
      }
    }
    result.leftMap { errorWrapper: ErrorWrapper =>
      val resCorrelationId: String = errorWrapper.correlationId
      val leftResult = errorResult(errorWrapper).withApiHeaders(resCorrelationId)

      //TODO auditing

      leftResult
    }.merge
  }

  private def errorResult(errorWrapper: ErrorWrapper): Result = {
    (errorWrapper.error: @unchecked) match {
      case VrnFormatError => BadRequest(Json.toJson(errorWrapper))
      case VrnNotFound => NotFound(Json.toJson(errorWrapper))
      case _ => InternalServerError(Json.toJson(errorWrapper))
    }
  }
}
