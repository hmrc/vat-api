/*
 * Copyright 2023 HM Revenue & Customs
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

import config.AppConfig
import play.api.libs.json.{JsObject, JsValue, Json}
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import v1.controllers.UserRequest
import v1.models.auth.UserDetails
import v1.models.domain.Vrn
import v1.models.errors.{ErrorWrapper, MtdError}
import v1.models.outcomes.ResponseWrapper
import v1.models.request.information.{CustomerInfoRequest, CustomerRawData}
import v1.models.response.information.{CustomerDetails, CustomerInfoResponse, FlatRateScheme}

object CustomerInfoConstants {

  implicit val correlationId: String = "abc123-789xyz"
  val userDetails: UserDetails = UserDetails("Individual", None, "client-Id")
  implicit val userRequest: UserRequest[AnyContentAsEmpty.type] = UserRequest(userDetails,FakeRequest())

  val vrn: String = "123456789"
  val rawData: CustomerRawData = CustomerRawData(vrn)
  val customerInfoRequest: CustomerInfoRequest = CustomerInfoRequest(Vrn(vrn))
  val invalidVrn = "fakeVRN"
  val invalidRawData: CustomerRawData = CustomerRawData(invalidVrn)
  def customerInfoURl(vrn: String = vrn)(implicit appConfig: AppConfig) = s"/vat-subscription/$vrn/full-information"
  def customerInfoURLWithConfig(vrn: String = vrn)(implicit appConfig: AppConfig) = appConfig.vatSubscriptionUrl+  s"/vat-subscription/$vrn/full-information"

  val testCustomerDetailsMax: CustomerDetails = CustomerDetails(
    effectiveRegistrationDate = Some( "2024-10-11")
  )
  val testFlatRateSchemeMax: FlatRateScheme = FlatRateScheme(
    frsCategory = Some("001"),
    startDate =  Some("2024-10-11")
  )

  val testCustomerInfoResponseMin: CustomerInfoResponse = CustomerInfoResponse(
    customerDetails = None,
    flatRateScheme = None
  )
  val testCustomerInfoResponseMax: CustomerInfoResponse = CustomerInfoResponse(
    customerDetails = Some(testCustomerDetailsMax),
    flatRateScheme = Some(testFlatRateSchemeMax)
  )

  val testCustomerInfoResponseMinJson:  JsObject = Json.obj()

  val upstreamTestCustomerInfoResponseJsonMax: JsObject = Json.obj(
    "customerDetails" -> Some(upstreamTestCustomerDetailsDataJsonMax),
    "flatRateScheme" -> Some(upstreamTestflatRateSchemeJsonMax)
  )

  val upstreamTestCustomerDetailsDataJsonMax: JsObject = Json.obj(
    "effectiveRegistrationDate" -> "2024-10-11"
  )

  val upstreamTestflatRateSchemeJsonMax: JsObject = Json.obj(
    "frsCategory" -> "001",
    "startDate" -> "2024-10-11"
  )



  val downstreamTestustomerInfoResponseJsonMax: JsObject = Json.obj(
    "customerDetails" -> downstreamTestCustomerDetailsJsonMax,
    "flatRateScheme" -> downstreamTestflatRateSchemeJsonMax
  )

  val emptyjson: JsObject = Json.obj()



  val downstreamTestCustomerDetailsJsonMax: JsObject = Json.obj(
    "effectiveRegistrationDate" -> "2024-10-11"
  )

  val downstreamTestflatRateSchemeJsonMax: JsObject = Json.obj(
    "frsCategory" -> "001",
    "startDate" -> "2024-10-11"
  )

  def errorWrapper(error: MtdError): ErrorWrapper = ErrorWrapper(correlationId, error)
  def errorWrapperMulti(error: Seq[MtdError]): ErrorWrapper = ErrorWrapper(correlationId, error.head, Some(error.tail))

  def wrappedCustomerInfoResponse(customerInfoResponse: CustomerInfoResponse = testCustomerInfoResponseMin): ResponseWrapper[CustomerInfoResponse] = {
    ResponseWrapper(correlationId, customerInfoResponse)
  }

  def wrappedCustomerInfoMaxResponse(customerInfoResponse: CustomerInfoResponse = testCustomerInfoResponseMax): ResponseWrapper[CustomerInfoResponse] = {
    ResponseWrapper(correlationId, customerInfoResponse)
  }
}
