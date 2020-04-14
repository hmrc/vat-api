package v1.models.request.submit

import play.api.mvc.AnyContentAsJson
import v1.models.request.RawData

case class SubmitRawData(vrn: String, body: AnyContentAsJson) extends RawData
