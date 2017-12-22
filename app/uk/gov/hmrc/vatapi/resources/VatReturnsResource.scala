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
import org.joda.time.LocalDate
import uk.gov.hmrc.vatapi.models.{DateRange, Errors, QueryDateRange}

import scala.concurrent.ExecutionContext.Implicits.global
import cats.implicits._
import com.github.nscala_time.time.Imports._

object VatReturnsResource extends BaseController {

  val logger: Logger = Logger(this.getClass)

  private val connector = VatReturnsConnector

  def submitVatReturn(vrn: Vrn): Action[JsValue] = Action.async(parse.json) { implicit request =>

    fromDes {
      for {
        vatReturn <- validateJson[VatReturnDeclaration](request.body)
        _ <- authorise(vatReturn){ case _ if !vatReturn.finalised => Errors.NotFinalisedDeclaration }
        response <- execute{ _ => connector.post(vrn, vatReturn.toDes) }
      } yield response
    } onSuccess { response =>
      response.filter {
        case 200 => Created(Json.toJson(response.vatReturn))
        case _   => InternalServerError
      }
    }

  }

  def retrieveVatReturns(vrn: Vrn, queryRange: QueryDateRange): Action[AnyContent] = Action.async { implicit request =>

    val today = new LocalDate
    val fourYearsAgo = today.minusYears(4)

    val range =
      DateRange(
        queryRange.from.getOrElse(fourYearsAgo),
        queryRange.to.getOrElse(today)
      )

    fromDes {
      for {
        _ <- validate(queryRange) { case _ if(range.from > range.to) => Errors.InvalidDateRange }
        _ <- authorise(queryRange) { case _ if(range.from < fourYearsAgo) => Errors.DateRangeTooLarge }
        response <- execute { _ => connector.query(vrn, range) }
      } yield response
    } onSuccess {
      _.retrieve match {
        case Right(vatReturns) => Ok(Json.toJson(vatReturns))
        case Left(err)         => {
          logger.error(err.msg)
          InternalServerError
        }
      }
    }

  }

}
