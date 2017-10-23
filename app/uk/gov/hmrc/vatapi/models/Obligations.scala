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

package uk.gov.hmrc.vatapi.models

import com.github.nscala_time.time.OrderingImplicits
import org.joda.time.LocalDate
import play.api.libs.json._

import scala.util.{Failure, Success, Try}

case class Obligations(obligations: Seq[Obligation])

object Obligations {
  implicit val writes: Writes[Obligations] = Json.writes[Obligations]
}

case class Obligation(start: LocalDate, end: LocalDate, due: LocalDate, met: Boolean, periodKey : String, received : Option[LocalDate] = None)

object Obligation {
  implicit val jodaDateWrites: Writes[LocalDate] = new Writes[LocalDate] {
    def writes(d: LocalDate): JsValue = JsString(d.toString())
  }

  implicit val from =  new DesTransformValidator[des.ObligationDetail, Obligation] {
    def from(desObligation: des.ObligationDetail) = {
      Try(Obligation(
        start = LocalDate.parse(desObligation.inboundCorrespondenceFromDate),
        end = LocalDate.parse(desObligation.inboundCorrespondenceToDate),
        due = LocalDate.parse(desObligation.inboundCorrespondenceDueDate),
        met = desObligation.isFulfilled,
        periodKey = desObligation.periodKey,
        received = desObligation.inboundCorrespondenceDateReceived.map(LocalDate.parse))
      ) match {
        case Success(obj) => Right(obj)
        case Failure(ex) => Left(InvalidDateError(s"Unable to parse the date from des response $ex"))
      }
    }
  }

  implicit val localDateOrder: Ordering[LocalDate] = OrderingImplicits.LocalDateOrdering
  implicit val ordering: Ordering[Obligation] = Ordering.by(_.start)

  implicit val writes: Writes[Obligation] = Json.writes[Obligation]
}

case class InvalidDateError(msg: String) extends DesTransformError

