package v1.controllers.requestParsers.validators.validations

import v1.models.errors.MtdError

object VrnValidation {
  private val vrnRegex = """^\d{9}$"""

  def validate(vrn: String): List[MtdError] = {
    if (vrn.matches(vrnRegex)) NoValidationErrors else List(VrnFormatError)
  }
}
