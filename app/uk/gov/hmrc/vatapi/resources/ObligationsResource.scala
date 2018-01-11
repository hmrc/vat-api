/*
 * Copyright 2018 HM Revenue & Customs
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

package uk.gov.hmrc.vatapi.resources

import play.api.Logger
import play.api.libs.json.Json
import play.api.mvc.Action
import uk.gov.hmrc.domain.Vrn
import uk.gov.hmrc.play.microservice.controller.BaseController
import uk.gov.hmrc.vatapi.connectors.ObligationsConnector
import uk.gov.hmrc.vatapi.models.{Errors, ObligationsQueryParams}

import scala.concurrent.ExecutionContext.Implicits.global

object ObligationsResource extends BaseController {
  val logger: Logger = Logger(this.getClass)

  private val connector = ObligationsConnector

  def retrieveObligations(vrn: Vrn, params: ObligationsQueryParams) = Action.async { implicit request =>
    connector.get(vrn, params) map { response =>
      response.filter {
        case 200 =>
          response.obligations match {
            case Right(obj) => obj.map(x => Ok(Json.toJson(x))).getOrElse(NotFound)
            case Left(ex) =>
              logger.error(ex.msg)
              InternalServerError(Json.toJson(Errors.InternalServerError))
          }
      }
    }
  }

}
