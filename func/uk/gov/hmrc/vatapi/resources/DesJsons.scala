package uk.gov.hmrc.vatapi.resources

import play.api.libs.json._

object DesJsons {

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


  object Obligations {
    def apply(id: String = "abc"): String = {
      s"""
         |{
         |  "obligations": [
         |        {
         |        "identification": {
         |          "incomeSourceType": "A",
         |          "referenceNumber": "$id",
         |          "referenceType": "VRN"
         |        },
         |      "obligationDetails": [
         |        {
         |          "status": "F",
         |          "inboundCorrespondenceFromDate": "2017-04-06",
         |          "inboundCorrespondenceToDate": "2017-07-05",
         |          "inboundCorrespondenceDateReceived": "2017-08-01",
         |          "inboundCorrespondenceDueDate": "2017-08-05",
         |          "periodKey": "#001"
         |        },
         |        {
         |          "status": "O",
         |          "inboundCorrespondenceFromDate": "2017-07-06",
         |          "inboundCorrespondenceToDate": "2017-10-05",
         |          "inboundCorrespondenceDueDate": "2017-11-05",
         |          "periodKey": "#002"
         |        },
         |        {
         |          "status": "O",
         |          "inboundCorrespondenceFromDate": "2017-10-06",
         |          "inboundCorrespondenceToDate": "2018-01-05",
         |          "inboundCorrespondenceDueDate": "2018-02-05",
         |          "periodKey": "#003"
         |        },
         |        {
         |          "status": "O",
         |          "inboundCorrespondenceFromDate": "2018-01-06",
         |          "inboundCorrespondenceToDate": "2018-04-05",
         |          "inboundCorrespondenceDueDate": "2018-05-06",
         |          "periodKey": "#004"
         |        }
         |      ]
         |    }
         |  ]
         |}
         """.stripMargin
    }
  }
}
