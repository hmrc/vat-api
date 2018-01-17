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
    val ninoNotFound: String = error("NOT_FOUND_NINO", "The remote endpoint has indicated that no data can be found.")
    val notFoundProperty: String = error(
      "NOT_FOUND_PROPERTY",
      "The remote endpoint has indicated that no data can be found for the given property type.")
    val notFound: String = error("NOT_FOUND", "The remote endpoint has indicated that no data can be found.")
    val tradingNameConflict: String = error("CONFLICT", "Duplicated trading name.")
    val serverError: String =
      error("SERVER_ERROR", "DES is currently experiencing problems that require live service intervention.")
    val serviceUnavailable: String = error("SERVICE_UNAVAILABLE", "Dependent systems are currently not responding.")
    val tooManySources: String =
      error("TOO_MANY_SOURCES", "You may only have a maximum of one self-employment source.")
    val invalidPeriod: String =
      error("INVALID_PERIOD", "The remote endpoint has indicated that a overlapping period was submitted.")
    val overlappingPeriod: String =
      error("OVERLAPS_IN_PERIOD",
        "The remote endpoint has indicated that the submission period overlaps another period submitted.")
    val nonContiguousPeriod: String =
      error(
        "NOT_CONTIGUOUS_PERIOD",
        "The remote endpoint has indicated that the submission period is not contiguous with another period submission.")
    val misalignedPeriod: String =
      error("NOT_ALIGN_PERIOD",
        "The remote endpoint has indicated that the submission period is outside the Accounting Period.")
    val invalidObligation: String = error("INVALID_REQUEST", "Accounting period should be greater than 6 months.")
    val invalidBusinessId: String = error("INVALID_BUSINESSID", "Submission has not passed validation. Invalid parameter businessId.")
    val invalidOriginatorId: String =
      error("INVALID_ORIGINATOR_ID", "Submission has not passed validation. Invalid header Originator-Id.")
    val invalidCalcId: String = error("INVALID_CALCID", "Submission has not passed validation")
    val propertyConflict: String = error("CONFLICT", "Property already exists.")
    val invalidIncomeSource: String = error(
      "INVALID_INCOME_SOURCE",
      "The remote endpoint has indicated that the taxpayer does not have an associated property.")
    val notFoundIncomeSource: String = error("NOT_FOUND_INCOME_SOURCE", "The remote endpoint has indicated that no data can be found for the given income source id.")
    val invalidDateFrom: String =
      error("INVALID_DATE_FROM", "Submission has not passed validation. Invalid parameter from.")
    val invalidDateTo: String = error("INVALID_DATE_TO", "Submission has not passed validation. Invalid parameter to.")
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
