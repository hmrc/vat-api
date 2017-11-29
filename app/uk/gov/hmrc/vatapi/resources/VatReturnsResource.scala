/*
 * Copyright 2017 HM Revenue & Customs
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
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.{Action, AnyContent}
import uk.gov.hmrc.domain.Vrn
import uk.gov.hmrc.play.microservice.controller.BaseController
import uk.gov.hmrc.vatapi.connectors.VatReturnsConnector
import uk.gov.hmrc.vatapi.models.VatReturnDeclaration
import uk.gov.hmrc.vatapi.resources.wrappers.VatReturnsResponse

import scala.concurrent.ExecutionContext.Implicits.global
import uk.gov.hmrc.vatapi.models.Errors

object VatReturnsResource extends BaseController {

  val logger: Logger = Logger(this.getClass)

  private val connector = VatReturnsConnector

  def submitVatReturn(vrn: Vrn): Action[JsValue] = Action.async(parse.json) { implicit request =>

    def isFinalised(vatReturn: VatReturnDeclaration): Option[Errors.Error] =
      if(vatReturn.finalised) None
      else Some(Errors.NotFinalisedDeclaration)

    for {
      validDeclaration <- validate[VatReturnDeclaration](request.body)
      result <- authorise[VatReturnDeclaration, VatReturnsResponse](validDeclaration, isFinalised) { vatReturn =>
        connector.post(vrn, vatReturn.toReturn)
      }
    } yield result match {
      case Left(errorResult) => handleErrors(errorResult)
      case Right(response) =>
        response.filter {
          case 200 => Created(Json.toJson(response.vatReturn))
          case _   => InternalServerError
        }
    }

  }

  def retrieveVatReturn(vrn: Vrn): Action[AnyContent] =
    Action.async {
      implicit request =>
        connector.get(vrn) map {
          response =>
            response.filter {
              case 200 =>
                response.retrieve match {
                  case Right(vatReturn) => Ok(Json.toJson(vatReturn))
                  case Left(err) =>
                    logger.error(err.msg)
                    NotFound
                }
            }
        }
    }
}
