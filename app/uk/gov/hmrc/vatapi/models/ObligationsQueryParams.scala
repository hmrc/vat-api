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

package uk.gov.hmrc.vatapi.models


import org.joda.time.LocalDate

import scala.util.Try

case class ObligationsQueryParams(from: LocalDate, to: LocalDate, status: String) {
  val map = Map("from" -> from, "to" -> to, "status" -> status)
}

object ObligationsQueryParams {
  val dateRegex = """^\d{4}-\d{2}-\d{2}$"""
  val statusRegex = "^[OFA]$"

  def from(fromOpt: OptEither[String], toOpt: OptEither[String], statusOpt: OptEither[String]): Either[String, ObligationsQueryParams] = {
    val from = dateQueryParam(fromOpt, "INVALID_DATE_FROM")
    val to = dateQueryParam(toOpt, "INVALID_DATE_TO")
    val status = statusQueryParam(statusOpt,  "INVALID_STATUS")

    val errors = for {
      paramOpt <- Seq(from, to, status, validDateRange(from, to))
      param <- paramOpt
      if param.isLeft
    } yield param.left.get

    if (errors.isEmpty) {
      Right(ObligationsQueryParams(from.map(_.right.get).get, to.map(_.right.get).get, status.map(_.right.get).get))
    } else {
      Left(errors.head)
    }
  }


  private def dateQueryParam(dateOpt: OptEither[String], errorCode: String): OptEither[LocalDate] = {
    val paramValue = dateOpt match {
      case Some(value) =>
        val dateString = value.right.get
        if (dateString.matches(dateRegex))
          Try(Right(LocalDate.parse(dateString))).getOrElse(Left(errorCode))
        else
          Left(errorCode)
      case None => Left(errorCode)
    }
    Some(paramValue)
  }


  private def statusQueryParam(statusOpt: OptEither[String], errorCode: String): OptEither[String] = {
    val paramValue = statusOpt match {
      case Some(value) =>
        val status = value.right.get
        if (status.matches(statusRegex))
          Right(status)
        else
          Left(errorCode)
      case None => Left(errorCode)
    }
    Some(paramValue)
  }


  def validDateRange(fromOpt: OptEither[LocalDate], toOpt: OptEither[LocalDate]) = {
    for {
      fromVal <- fromOpt
      if fromVal.isRight
      toVal <- toOpt
      if toVal.isRight
    } yield
      (fromVal.right.get, toVal.right.get) match {
        case (from, to) if !from.isBefore(to) || from.plusDays(365).isBefore(to) =>
          Left("INVALID_DATE_RANGE")
        case _ => Right(()) // object wrapped in Right irrelevant
      }
  }

}
