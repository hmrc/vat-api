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

import cats.implicits._
import play.api.Logger
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.{Action, AnyContent}
import uk.gov.hmrc.domain.Vrn
import uk.gov.hmrc.play.microservice.controller.BaseController
import uk.gov.hmrc.vatapi.connectors.VatReturnsConnector
import uk.gov.hmrc.vatapi.models.Errors.Error
import uk.gov.hmrc.vatapi.models.{ErrorCode, Errors, VatReturnDeclaration}

import scala.concurrent.ExecutionContext.Implicits.global

object VatReturnsResource extends BaseController {

  val logger: Logger = Logger(this.getClass)

  private val connector = VatReturnsConnector

  def submitVatReturn(vrn: Vrn): Action[JsValue] = Action.async(parse.json) {
    implicit request =>
      fromDes {
        for {
          vatReturn <- validateJson[VatReturnDeclaration](request.body)
          _ <- authorise(vatReturn) {
            case _ if !vatReturn.finalised => Errors.NotFinalisedDeclaration
          }
          response <- execute { _ =>
            connector.post(vrn, vatReturn.toDes)
          }
        } yield response
      } onSuccess { response =>
        response.filter {
          case 200 => Created(Json.toJson(response.vatReturn))
          case 400 =>
            Forbidden(
              Json.toJson(
                Errors.businessError(
                  Error(
                    ErrorCode.DUPLICATE_SUBMISSION.toString,
                    "The VAT return was already submitted for the given period",
                    Some("")))))
          case _ => InternalServerError
        }
      }

  }

  def retrieveVatReturns(vrn: Vrn, periodKey: String): Action[AnyContent] =
    Action.async { implicit request =>
      fromDes {
        for {
          _ <- validate[String](periodKey) {
            case _ if (periodKey.length != 4) => Errors.InvalidPeriodKey
          }
          response <- execute { _ =>
            connector.query(vrn, periodKey)
          }
        } yield response
      } onSuccess { response =>
        response.filter {
          case 200 => response.retrieve match {
            case Right(vatReturn) => Ok(Json.toJson(vatReturn))
            case Left(error) => logger.error(error.msg)
              InternalServerError
          }
        }
      }
    }
}
