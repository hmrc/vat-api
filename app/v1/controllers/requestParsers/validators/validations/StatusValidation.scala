package v1.controllers.requestParsers.validators.validations

import v1.models.errors.{MtdError, StatusFormatError}

object StatusValidation {
  private val statusRegex = "^(O|F)$"

  def validate(status: String): List[MtdError] = {
    if (status.matches(statusRegex)) NoValidationErrors else List(StatusFormatError)
  }
}
