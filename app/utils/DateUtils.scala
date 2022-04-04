/*
 * Copyright 2022 HM Revenue & Customs
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

package utils

import play.api.libs.json._

import java.time.format.DateTimeFormatter
import java.time.{LocalDate, OffsetDateTime}
import scala.util.{Failure, Success, Try}

object DateUtils {


  val isoInstantDatePattern = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'")
  val isoInstantDateRegex = """(\d){4}-(\d){2}-(\d){2}T(\d){2}:(\d){2}:(\d){2}Z"""
  val dateTimePattern = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
  val datePattern = DateTimeFormatter.ofPattern("yyyy-MM-dd")


  implicit def dateTimeWrites: Writes[OffsetDateTime] = new Writes[OffsetDateTime] {
    def writes(localDateTime: OffsetDateTime): JsValue = JsString(localDateTime.format(dateTimePattern))
  }

  implicit def dateTimeReads: Reads[OffsetDateTime] = new Reads[OffsetDateTime] {
    override def reads(json: JsValue): JsResult[OffsetDateTime] =
      Try(json.as[String]) match {
        case Success(value) => JsSuccess(OffsetDateTime.parse(value))
        case Failure(_) => JsError()
      }
  }

  val dateTimeFormat: Format[OffsetDateTime] = Format[OffsetDateTime](
    dateTimeReads,
    dateTimeWrites
  )


  implicit def isoInstantDateWrites: Writes[OffsetDateTime] = new Writes[OffsetDateTime] {
    def writes(localDateTime: OffsetDateTime): JsValue = JsString(localDateTime.format(isoInstantDatePattern))
  }

  implicit def isoInstantDateReads: Reads[OffsetDateTime] = new Reads[OffsetDateTime] {
    override def reads(json: JsValue): JsResult[OffsetDateTime] =
      Try(JsSuccess(OffsetDateTime.parse(json.as[String], isoInstantDatePattern), JsPath)).getOrElse(JsError())
  }


  implicit def defaultDateTimeReads: Reads[OffsetDateTime] = new Reads[OffsetDateTime] {
    override def reads(json: JsValue): JsResult[OffsetDateTime] =
      Try(JsSuccess(OffsetDateTime.parse(json.as[String], isoInstantDatePattern), JsPath)).getOrElse(JsError())
  }

  val defaultDateTimeFormat: Format[OffsetDateTime] = Format[OffsetDateTime](
    defaultDateTimeReads,
    dateTimeWrites
  )


implicit def dateWrites: Writes[LocalDate] = new Writes[LocalDate] {
  def writes(localDate: LocalDate): JsValue = JsString(localDate.format(datePattern))
}

  implicit def dateReads: Reads[LocalDate] = new Reads[LocalDate] {
    override def reads(json: JsValue): JsResult[LocalDate] =
      Try(JsSuccess(LocalDate.parse(json.as[String], datePattern), JsPath)).getOrElse(JsError())
  }


}
