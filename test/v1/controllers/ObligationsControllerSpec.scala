package v1.controllers

import play.api.libs.json.{JsValue, Json}
import uk.gov.hmrc.domain.Vrn
import uk.gov.hmrc.http.HeaderCarrier
import v1.mocks.requestParsers.MockObligationRequestParser
import v1.mocks.services.{MockEnrolmentsAuthService, MockObligationService}
import v1.models.request.obligations.{ObligationsRawData, ObligationsRequest}
import v1.models.response.obligations.{Obligation, ObligationsResponse}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class ObligationsControllerSpec extends ControllerBaseSpec
  with MockEnrolmentsAuthService
  with MockObligationService
  with MockObligationRequestParser{

  trait Test {
    val hc: HeaderCarrier = HeaderCarrier()

    val controller: ObligationsController = new ObligationsController (
      mockEnrolmentsAuthService,
      mockObligationRequestParser,
      mockObligationsService,
      cc
    )

    MockEnrolmentsAuthService.authoriseUser()
  }

  val vrn: String = "123456789"
  val from: String = "2017-01-01"
  val to: String = "2017-03-31"
  val status: String = "F"
  val correlationId: String = "X-ID"

  val retrieveObligationsRawData: ObligationsRawData =
    ObligationsRawData(
      vrn, Some(from), Some(to), Some(status)
    )

  val retrieveObligationsRequest: ObligationsRequest =
    ObligationsRequest(
      vrn = Vrn(vrn),  Some(from), Some(to), Some(status)
    )

  val desJson: JsValue = Json.parse(
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
       |               "inboundCorrespondenceDateReceived":"2017-05-06"
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
  val mtdJson: JsValue = Json.parse(
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

  "obligations" when {
    "a valid request is supplied" should {
      "return the expected data on a successful service call" in new Test {

      }
    }
  }
}
