package v1.fixtures

import play.api.libs.json.{JsValue, Json}
import v1.models.response.obligations.{Obligation, ObligationsResponse}

trait ObligationsFixture {

  val obligationsDesJson: JsValue = Json.parse(
    s"""
       |{
       |   "obligations":[
       |      {
       |         "identification":{
       |            "referenceNumber":"123456789",
       |            "referenceType":"VRN"
       |         },
       |         "obligationDetails":[
       |            {
       |               "status":"F",
       |               "inboundCorrespondenceFromDate":"2017-01-01",
       |               "inboundCorrespondenceToDate":"2017-03-31",
       |               "inboundCorrespondenceDateReceived":"2017-05-06",
       |               "inboundCorrespondenceDueDate":"2017-05-07",
       |               "periodKey":"18A1"
       |            },
       |            {
       |               "status":"O",
       |               "inboundCorrespondenceFromDate":"2017-04-01",
       |               "inboundCorrespondenceToDate":"2017-06-30",
       |               "inboundCorrespondenceDueDate":"2017-08-07",
       |               "periodKey":"18A2"
       |            }
       |         ]
       |      }
       |   ]
       |}
       |""".stripMargin
  )
  val obligationsMtdJson: JsValue = Json.parse(
    s"""
       |{
       |  "obligations": [
       |    {
       |      "start": "2017-01-01",
       |      "end": "2017-03-31",
       |      "due": "2017-05-07",
       |      "status": "F",
       |      "periodKey": "18A1",
       |      "received": "2017-05-06"
       |    },
       |    {
       |      "start": "2017-04-01",
       |      "end": "2017-06-30",
       |      "due": "2017-08-07",
       |      "status": "O",
       |      "periodKey": "18A2"
       |    }
       |  ]
       |}
       |""".stripMargin
  )

  val obligationsResponse: ObligationsResponse =
    ObligationsResponse(Seq(
      Obligation(
        start = "2017-01-01",
        end = "2017-03-31",
        due = "2017-05-07",
        status = "F",
        periodKey = "18A1",
        received = Some("2017-05-06")
      ),
      Obligation(
        start = "2017-04-01",
        end =  "2017-06-30",
        due = "2017-08-07",
        status = "O",
        periodKey = "18A2",
        received = None
      )
    )
    )
}
