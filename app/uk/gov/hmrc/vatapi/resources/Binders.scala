/*
 * Copyright 2019 HM Revenue & Customs
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

import org.joda.time.LocalDate
import org.joda.time.format.DateTimeFormat
import play.api.mvc.{PathBindable, QueryStringBindable}
import uk.gov.hmrc.domain.Vrn
import uk.gov.hmrc.vatapi.models.{FinancialDataQueryParams, ObligationsQueryParams, OptEither}

import scala.util.{Failure, Success, Try}

object Binders {

  implicit def vrnBinder(implicit stringBinder: PathBindable[String]) = new PathBindable[Vrn] {
    val vrnRegex = """^\d{9}$"""

    def unbind(key: String, vrn: Vrn): String = stringBinder.unbind(key, vrn.value)

    def bind(key: String, value: String): Either[String, Vrn] = {
      if (value.matches(vrnRegex)) {
        Right(Vrn(value))
      } else {
        Left("ERROR_VRN_INVALID")
      }
    }
  }

  implicit def obligationsQueryParamsBinder(implicit stringBinder: QueryStringBindable[String]) = new QueryStringBindable[ObligationsQueryParams] {

    override def bind(key: String, params: Map[String, Seq[String]]): OptEither[ObligationsQueryParams] = {
      val from = stringBinder.bind("from", params)
      val to = stringBinder.bind("to", params)
      val status = stringBinder.bind("status", params)

      val query = ObligationsQueryParams.from(from, to, status)
      if (query.isRight)
        Some(Right(query.right.get))
      else
        Some(Left(query.left.get))
    }

    override def unbind(key: String, value: ObligationsQueryParams): String = stringBinder.unbind(key, value.map(key).toString)

  }

  implicit def financialDataQueryParamsBinder(implicit stringBinder: QueryStringBindable[String]) = new QueryStringBindable[FinancialDataQueryParams] {
    override def bind(key: String, params: Map[String, Seq[String]]): OptEither[FinancialDataQueryParams] = {
      val from = stringBinder.bind("from", params)
      val to = stringBinder.bind("to", params)

      val query = FinancialDataQueryParams.from(from, to)
      if (query.isRight)
        Some(Right(query.right.get))
      else
        Some(Left(query.left.get))
    }

    override def unbind(key: String, value: FinancialDataQueryParams): String = stringBinder.unbind(key, value.map(key).toString)
  }

  val format: String = "yyy-MM-dd"

  implicit val dateQueryParamsBinder = new QueryStringBindable[LocalDate] {

    override def unbind(key: String, date: LocalDate): String = date.toString

    override def bind(key: String, params: Map[String, Seq[String]]): Option[Either[String, LocalDate]] =
      for {
        dates <- params.get(key)
      } yield
        Try {
          DateTimeFormat.forPattern(format).parseLocalDate(dates(0))
        } match {
          case Success(v) => Right(v)
          case Failure(_) => Left("ERROR_INVALID_DATE")
        }

  }
}
