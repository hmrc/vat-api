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


import org.joda.time.LocalDate
import play.api.mvc.Request

import scala.util.{Failure, Success, Try}

case class ObligationsQueryParams(fromDate: LocalDate, toDate: LocalDate, status: String)

object ObligationsQueryParams {

  def from(implicit request: Request[_]): Either[ErrorResult, ObligationsQueryParams] = {
    val params = for {
      fromDate <- getDateQueryParam("fromDate")
      toDate <- getDateQueryParam("toDate")
      status <- getStatus("status")
      _ <- validateDateRange(fromDate, toDate)
    } yield ObligationsQueryParams(fromDate, toDate, status)
    params match {
      case Success(x) => Right(x)
      case Failure(ex) => Left(GenericErrorResult(ex.getMessage))
    }
  }

  def getDateQueryParam(param: String)(implicit request: Request[_]) = {
    Try(LocalDate.parse(request.getQueryString(param).getOrElse("").trim))
  }

  def getStatus(param: String)(implicit request: Request[_]) = {
    val regex = "^[OCA]$"
    val value = request.getQueryString(param).getOrElse("").trim
    if (value.matches(regex))
      Try(value)
    else
      Failure(new IllegalArgumentException(s"Query parameter [$param] is missing or invalid. Valid inputs are [O or C or A]"))
  }

  def validateDateRange(fromDate: LocalDate, toDate: LocalDate) = {
    if (toDate.isAfter(fromDate) && toDate.isBefore(fromDate.plusDays(366)))
      Success(true)
    else
      Failure(new IllegalArgumentException(s"fromDate must be before toDate and date range should be less than or equal to 366 days"))
  }


}
