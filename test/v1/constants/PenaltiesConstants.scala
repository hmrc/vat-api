/*
 * Copyright 2022 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package v1.constants

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

object PenaltiesConstants {

  implicit val correlationId: String = "abc123-789xyz"
val userDetails = UserDetails("Individual", None, "client-Id")
  implicit val userRequest: UserRequest[AnyContentAsEmpty.type] = UserRequest(userDetails,FakeRequest())

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
}
