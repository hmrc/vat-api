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

package uk.gov.hmrc.vatapi.models


import org.joda.time.LocalDate

import scala.util.Try

case class ObligationsQueryParams(from: Option[LocalDate], to: Option[LocalDate], status: Option[String] = None) {
  val map = Map("from" -> from, "to" -> to, "status" -> status)

  def queryString: String = {

    (from, to, status) match {
      case (Some(fromDate), Some(toDate), Some(statusValue)) => s"from=$fromDate&to=$toDate&status=$statusValue"
      case (Some(fromDate), Some(toDate), None) => s"from=$fromDate&to=$toDate"
      case (None, None, Some("O")) => "status=O"
    }

  }
}

object ObligationsQueryParams {
  val dateRegex = """^\d{4}-\d{2}-\d{2}$"""
  val statusRegex = "^[OF]$"

  def from(fromOpt: OptEither[String], toOpt: OptEither[String], statusOpt: OptEither[String])(): Either[String, ObligationsQueryParams] = {
    val from = dateQueryParam(fromOpt, "INVALID_DATE_FROM")
    val to = dateQueryParam(toOpt, "INVALID_DATE_TO")
    val status = statusQueryParam(statusOpt, "INVALID_STATUS")

    (from, to, status) match {
      case (None, None, Some(statusE)) => {
        statusE match {
          case Right(actualStatus) => {
            if (actualStatus == "O") {
              Right(ObligationsQueryParams(from.map(_.right.get), to.map(_.right.get), status.map(_.right.get)))
            } else {
              Left("MISSING_DATE_RANGE")
            }
          }
          case _ => Left("INVALID_STATUS")
        }
      }
      case (Some(_), Some(_), _) => {
        val errors = for {
          paramOpt <- Seq(from, to, status, validDateRange(from, to))
          param <- paramOpt
          if param.isLeft
        } yield param.left.get
        if (errors.isEmpty) {
          Right(ObligationsQueryParams(from.map(_.right.get), to.map(_.right.get), status.map(_.right.get)))
        } else {
          Left(errors.head)
        }
      }
      case (None, Some(_), _) => Left("INVALID_DATE_FROM")
      case (Some(_), None, _) => Left("INVALID_DATE_TO")
      case _ => Left("INVALID_DATE_FROM")
    }

  }


  private def dateQueryParam(dateOpt: OptEither[String], errorCode: String): OptEither[LocalDate] = {
    dateOpt match {
      case Some(value) =>
        val dateString = value.right.get
        if (dateString.matches(dateRegex))
          Some(Try(Right(LocalDate.parse(dateString))).getOrElse(Left(errorCode)))
        else
          Some(Left(errorCode))
      case None => None
    }
  }


  private def statusQueryParam(statusOpt: OptEither[String], errorCode: String): OptEither[String] = {
    statusOpt match {
      case Some(value) =>
        val status = value.right.get
        if (status.matches(statusRegex))
          Some(Right(status))
        else
          Some(Left(errorCode))
      case None => None
    }
  }


  def validDateRange(fromOpt: OptEither[LocalDate], toOpt: OptEither[LocalDate]) = {
    for {
      fromVal <- fromOpt if fromVal.isRight
      toVal <- toOpt if toVal.isRight
    } yield
      (fromVal.right.get, toVal.right.get) match {
        case (from, to) if !from.isBefore(to) || from.plusDays(365).isBefore(to) =>
          Left("INVALID_DATE_RANGE")
        case _ => Right(()) // object wrapped in Right irrelevant
      }
  }

}
