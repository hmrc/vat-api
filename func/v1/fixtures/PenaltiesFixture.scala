package v1.fixtures

import config.AppConfig
import play.api.libs.json.{JsObject, Json}
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import v1.controllers.UserRequest
import v1.models.auth.UserDetails
import v1.models.domain.Vrn
import v1.models.errors.{ErrorWrapper, MtdError}
import v1.models.outcomes.ResponseWrapper
import v1.models.request.penalties.{PenaltiesRawData, PenaltiesRequest}
import v1.models.response.penalties.{FinancialData, PenaltiesData, PenaltiesResponse}

trait PenaltiesFixture {
  val correlationId: String = "abc123-789xyz"
  val userRequest: UserRequest[AnyContentAsEmpty.type] = UserRequest(UserDetails("Individual",None,"id"),FakeRequest())

  val vrn: String = "123456789"
  val rawData: PenaltiesRawData = PenaltiesRawData(vrn)
  val penaltiesRequest: PenaltiesRequest = PenaltiesRequest(Vrn(vrn))
  val invalidVrn = "fakeVRN"
  val invalidRawData: PenaltiesRawData = PenaltiesRawData(invalidVrn)

  val testPenaltiesData: PenaltiesData = PenaltiesData(
    dummyPenaltyData1 = "testData1",
    dummyPenaltyData2 = "testData2",
    dummyPenaltyData3 = "testData3"
  )

  val testPenaltiesDataJson: JsObject = Json.obj(
    "dummyPenaltyData1" -> "testData1",
    "dummyPenaltyData2" -> "testData2",
    "dummyPenaltyData3" -> "testData3"
  )

  val testFinancialData: FinancialData = FinancialData(
    dummyFinancialData1 = "testData1",
    dummyFinancialData2 = "testData2",
    dummyFinancialData3 = "testData3"
  )

  val testFinancialDataJson: JsObject = Json.obj(
    "dummyFinancialData1" -> "testData1",
    "dummyFinancialData2" -> "testData2",
    "dummyFinancialData3" -> "testData3"
  )

  val testPenaltiesResponse: PenaltiesResponse = PenaltiesResponse(
    getPenaltiesData = testPenaltiesData,
    financialData = testFinancialData
  )

  val testPenaltiesResponseJson: JsObject = Json.obj(
    "getPenaltiesData" -> testPenaltiesDataJson,
    "financialData" -> testFinancialDataJson
  )

  def wrappedPenaltiesResponse(penaltiesResponse: PenaltiesResponse = testPenaltiesResponse): ResponseWrapper[PenaltiesResponse] = {
    ResponseWrapper(correlationId, penaltiesResponse)
  }

  def errorWrapper(error: MtdError): ErrorWrapper = ErrorWrapper(correlationId, error)

  def penaltiesURl(vrn: String = vrn)(implicit appConfig: AppConfig) = appConfig.penaltiesBaseUrl + s"/penalties/vat/penalties/full-data/$vrn"
}
