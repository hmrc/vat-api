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

package uk.gov.hmrc.vatapi

import org.joda.time.{DateTime, LocalDate}
import play.api.libs.json._

package object models {

  type Amount = BigDecimal
  type SourceId = String
  type PropertyId = String
  type PeriodId = String
  type SummaryId = String
  type JsonValidationErrors = Seq[(JsPath, Seq[JsonValidationError])]

  type OptEither[T] = Option[Either[String, T]]


  private val MAX_AMOUNT = BigDecimal("99999999999999.98")
  private val VAT_MAX_AMOUNT_13_DIGITS = BigDecimal("9999999999999.99")
  private val VAT_MAX_AMOUNT_11_DIGITS = BigDecimal("99999999999.99")

  /**
    * Asserts that amounts must have a maximum of two decimal places
    */
  val amountValidator: Reads[Amount] = Reads
    .of[Amount]
    .filter(
      JsonValidationError("amount should be a number less than 99999999999999.98 with up to 2 decimal places", ErrorCode.INVALID_MONETARY_AMOUNT))(
      amount => amount.scale < 3 && amount <= MAX_AMOUNT)

  /**
    * Asserts that amounts must be non-negative and have a maximum of two decimal places
    */
  val nonNegativeAmountValidator: Reads[Amount] = Reads
    .of[Amount]
    .filter(JsonValidationError("amounts should be a non-negative number less than 99999999999999.98 with up to 2 decimal places",
      ErrorCode.INVALID_MONETARY_AMOUNT))(
      amount => amount >= 0 && amount.scale < 3 && amount <= MAX_AMOUNT)

  val vatAmountValidator: Reads[Amount] = Reads
    .of[Amount]
    .filter(
      JsonValidationError(
        "amount should be a monetary value (to 2 decimal places), between -9,999,999,999,999.99 and 9,999,999,999,999.99",
        ErrorCode.INVALID_MONETARY_AMOUNT))(amount =>
      amount.scale < 3 && amount >= -VAT_MAX_AMOUNT_13_DIGITS && amount <= VAT_MAX_AMOUNT_13_DIGITS)

  val vatNonNegativeAmountValidator: Reads[Amount] = Reads
    .of[Amount]
    .filter(
      JsonValidationError(
        "amount should be a monetary value (to 2 decimal places), between 0 and 99,999,999,999.99",
        ErrorCode.INVALID_MONETARY_AMOUNT))(amount =>
      amount.scale < 3 && amount >= 0 && amount <= VAT_MAX_AMOUNT_11_DIGITS)

  val vatWholeAmountValidator: Reads[Amount] = Reads
    .of[Amount]
    .filter(
      JsonValidationError(
        "amount should be a whole monetary value between 0 and 9,999,999,999,999",
        ErrorCode.INVALID_MONETARY_AMOUNT))(
      amount =>
        (amount.scale <= 0 || amount
          .remainder(1) == 0) && amount.scale < 3 && amount >= 0 && amount
          .toBigInt() <= VAT_MAX_AMOUNT_13_DIGITS.toBigInt)

  val vatAmountValidatorWithZeroDecimals: Reads[Amount] = Reads
    .of[Amount]
    .filter(
      JsonValidationError(
        "amount should be a monetary value (to 2 decimal places), between -9,999,999,999,999.00 and 9,999,999,999,999.00",
        ErrorCode.INVALID_MONETARY_AMOUNT))(
      amount =>
        (amount.scale <= 0 || amount
          .remainder(1) == 0) && amount.scale < 3 && amount
          .toBigInt() >= -VAT_MAX_AMOUNT_13_DIGITS.toBigInt && amount
          .toBigInt() <= VAT_MAX_AMOUNT_13_DIGITS.toBigInt)


  implicit class Trimmer(reads: Reads[String]) {
    def trim: Reads[String] = reads.map(_.trim)
  }

  implicit class NullableTrimmer(reads: Reads[Option[String]]) {
    def trimNullable: Reads[Option[String]] = reads.map(_.map(_.trim))
  }

  val dateTimePattern = "yyyy-MM-dd'T'HH:mm:ss.SSSZ"

  implicit val dateTimeFormat: Format[DateTime] = Format[DateTime](
    JodaReads.jodaDateReads(dateTimePattern),
    JodaWrites.jodaDateWrites(dateTimePattern)
  )

  val datePattern = "yyyy-MM-dd"

  implicit val dateFormat: Format[LocalDate] = Format[LocalDate](
    JodaReads.jodaLocalDateReads(datePattern),
    JodaWrites.jodaLocalDateWrites(datePattern)
  )

}
