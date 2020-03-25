package v1.controllers.requestParsers.validators.validations

import java.time.LocalDate

import v1.models.errors.MtdError

import scala.util.{Failure, Success, Try}

object DateFormatValidation {
  def validate(date: String, error: MtdError): List[MtdError] = Try {
    LocalDate.parse(date, dateFormat)
  } match {
    case Success(_) => NoValidationErrors
    case Failure(_) => List(error)
  }
}
