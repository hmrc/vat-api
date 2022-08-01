package v1.fixtures

import config.AppConfig
import play.api.libs.json.{JsObject, Json}
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import v1.controllers.UserRequest
import v1.models.auth.UserDetails
import v1.models.domain.Vrn
import v1.models.request.penalties.{PenaltiesRawData, PenaltiesRequest}

trait PenaltiesFixture {
  val correlationId: String = "abc123-789xyz"
  val userRequest: UserRequest[AnyContentAsEmpty.type] = UserRequest(UserDetails("Individual",None,"id"),FakeRequest())

  val vrn: String = "123456789"
  val rawData: PenaltiesRawData = PenaltiesRawData(vrn)
  val penaltiesRequest: PenaltiesRequest = PenaltiesRequest(Vrn(vrn))
  val invalidVrn = "fakeVRN"
  val invalidRawData: PenaltiesRawData = PenaltiesRawData(invalidVrn)



  val testPenaltiesDataJson: JsObject = Json.obj(
    "dummyPenaltyData1" -> "testData1",
    "dummyPenaltyData2" -> "testData2",
    "dummyPenaltyData3" -> "testData3"
  )


  val testFinancialDataJson: JsObject = Json.obj(
    "dummyFinancialData1" -> "testData1",
    "dummyFinancialData2" -> "testData2",
    "dummyFinancialData3" -> "testData3"
  )


  val testPenaltiesResponseJson: JsObject = Json.obj(
    "getPenaltiesData" -> testPenaltiesDataJson,
    "financialData" -> testFinancialDataJson
  )



  def penaltiesURl(vrn: String = vrn)(implicit appConfig: AppConfig) = s"/penalties/vat/penalties/full-data/$vrn"

  val invalidJson: JsObject = Json.obj(
    "json" -> "invalid"
  )
}
