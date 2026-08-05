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

package v1.controllers.internal

import cats.data.EitherT
import play.api.libs.json.{ JsValue, Json }
import play.api.mvc.{ Action, AnyContent, ControllerComponents, Result }
import utils._
import v1.controllers.requestParsers.AssistReturnRequestParser
import v1.controllers.{ AuthorisedController, BaseController }
import v1.models.errors._
import v1.models.request.submit.SubmitRawData
import v1.services.{ AssistObligationService, EnrolmentsAuthService }

import javax.inject.{ Inject, Singleton }
import scala.concurrent.{ ExecutionContext, Future }

@Singleton
class AssistReturnController @Inject()(val authService: EnrolmentsAuthService,
                                       requestParser: AssistReturnRequestParser,
                                       obligationsService: AssistObligationService,
                                       cc: ControllerComponents)(implicit ec: ExecutionContext)
    extends AuthorisedController(cc)
    with BaseController
    with Logging {

  implicit val endpointLogContext: EndpointLogContext =
    EndpointLogContext(controllerName = "AssistReturnController", endpointName = "validateReturnAndRetrieveObligation")

  def validateReturnAndRetrieveObligation(vrn: String): Action[JsValue] =
    authorisedAction(vrn).async(parse.json) { implicit request =>
      implicit val correlationId: String = request.headers.get("X-CorrelationId").getOrElse("no-correlation-id-found")

      val rawRequest = SubmitRawData(vrn, AnyContent(request.body))

      val result = for {
        parsedRequest <- EitherT.fromEither[Future](requestParser.parseRequest(rawRequest))
        obligation    <- EitherT(obligationsService.retrieveOpenObligation(parsedRequest))
      } yield {
        infoLog(
          s"$endpointLogContext VAT return validated and open obligation matched for VRN : $vrn, " +
            s"periodKey : ${obligation.responseData.periodKey}, correlationId : ${obligation.correlationId}")
        Ok(Json.toJson(obligation.responseData)).withApiHeaders(obligation.correlationId)
      }

      result.leftMap(errorResult).merge
    }

  private def errorResult(errorWrapper: ErrorWrapper): Result =
    (errorWrapper.error match {
      case ServiceUnavailableError | DownstreamError => ServiceUnavailable(Json.toJson(errorWrapper))
      case _                                         => BadRequest(Json.toJson(errorWrapper))
    }).withApiHeaders(errorWrapper.correlationId)
}
