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

package uk.gov.hmrc.vatapi

import org.joda.time.LocalDate
import play.api.Logger
import play.api.data.validation.ValidationError
import play.api.libs.json.{JsPath, Reads}

import scala.io.{Codec, Source}
import scala.util.Try

package object models {

  type Amount = BigDecimal
  type SourceId = String
  type PropertyId = String
  type PeriodId = String
  type SummaryId = String
  type ValidationErrors = Seq[(JsPath, Seq[ValidationError])]

  private val MAX_AMOUNT = BigDecimal("99999999999999.98")

  /**
    * Asserts that amounts must have a maximum of two decimal places
    */
  val amountValidator: Reads[Amount] = Reads
    .of[Amount]
    .filter(
      ValidationError("amount should be a number less than 99999999999999.98 with up to 2 decimal places", ErrorCode.INVALID_MONETARY_AMOUNT))(
      amount => amount.scale < 3 && amount <= MAX_AMOUNT)

  /**
    * Asserts that amounts must be non-negative and have a maximum of two decimal places
    */
  val nonNegativeAmountValidator: Reads[Amount] = Reads
    .of[Amount]
    .filter(ValidationError("amounts should be a non-negative number less than 99999999999999.98 with up to 2 decimal places",
      ErrorCode.INVALID_MONETARY_AMOUNT))(
      amount => amount >= 0 && amount.scale < 3 && amount <= MAX_AMOUNT)

  val sicClassifications: Try[Seq[String]] =
    for {
      lines <- {
        Try(Source.fromInputStream(getClass.getClassLoader.getResourceAsStream("SICs.txt"))(Codec.UTF8))
          .recover {
            case ex =>
              Logger.error(s"Error loading SIC classifications file SICs.txt: ${ex.getMessage}")
              throw ex
          }
      }
    } yield lines.getLines().toIndexedSeq


  val postcodeValidator: Reads[String] = Reads
    .of[String]
    .filter(ValidationError("postalCode must match \"^[A-Z]{1,2}[0-9][0-9A-Z]?\\s?[0-9][A-Z]{2}|BFPO\\s?[0-9]{1,10}$\"",
      ErrorCode.INVALID_POSTCODE))(postcode =>
      postcode.matches("^[A-Z]{1,2}[0-9][0-9A-Z]?\\s?[0-9][A-Z]{2}|BFPO\\s?[0-9]{1,10}$"))

  def regexValidator(fieldName: String, regex: String): Reads[String] = Reads
    .of[String]
    .map(_.trim)
    .filter(ValidationError(s"$fieldName cannot be blank spaces and must match $regex",
      ErrorCode.INVALID_FIELD_FORMAT))(field => field.matches(regex))

  def stringRegex(maxLength: Int) = s"^[A-Za-z0-9 \\-,.&'\\/]{1,$maxLength}$$"

  def lengthIs(length: Int): Reads[String] =
    Reads.of[String].filter(ValidationError(s"field length must be $length characters", ErrorCode.INVALID_FIELD_LENGTH)
    )(name => name.length == length)

  val commencementDateValidator: Reads[LocalDate] = Reads
    .of[LocalDate]
    .filter(
      ValidationError("commencement date should be today or in the past", ErrorCode.DATE_NOT_IN_THE_PAST)
    )(date => date.isBefore(LocalDate.now()) || date.isEqual(LocalDate.now()))

  implicit class Trimmer(reads: Reads[String]) {
    def trim: Reads[String] = reads.map(_.trim)
  }

  implicit class NullableTrimmer(reads: Reads[Option[String]]) {
    def trimNullable: Reads[Option[String]] = reads.map(_.map(_.trim))
  }

}
