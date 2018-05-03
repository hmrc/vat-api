package uk.gov.hmrc.assets.des

import play.api.libs.json.{JsObject, JsString, Json}

object Errors {

  private def error(code: String, reason: String): String = {
    s"""
       |{
       |  "code": "$code",
       |  "reason": "$reason"
       |}
       """.stripMargin
  }

  private def multiError(codeReason: (String, String)*): String = {
    val errors = codeReason map {
      case (code, reason) =>
        JsObject(Seq("code" -> JsString(code), "reason" -> JsString(reason)))
    }
    Json
      .obj("failures" -> errors)
      .toString()
  }

  val invalidVrn: String = error("INVALID_VRN", "Submission has not passed validation. Invalid parameter VRN.")
  val invalidPayload: String = error("INVALID_PAYLOAD", "Submission has not passed validation. Invalid PAYLOAD.")
  val notFound: String = error("NOT_FOUND", "The remote endpoint has indicated that no data can be found.")
  val serverError: String = error("SERVER_ERROR", "DES is currently experiencing problems that require live service intervention.")
  val serviceUnavailable: String = error("SERVICE_UNAVAILABLE", "Dependent systems are currently not responding.")
}
