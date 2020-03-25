package v1.controllers.requestParsers.validators.validations

import play.api.libs.json.{JsSuccess, JsValue, Reads}
import v1.models.errors.MtdError

object JsonFormatValidation {
  def validate[A](data: JsValue, error: MtdError)(implicit reads: Reads[A]): List[MtdError] = {

    data.validate[A] match {
      case JsSuccess(_, _) => NoValidationErrors
      case _               => List(error)
    }

  }
}
