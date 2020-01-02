/*
 * Copyright 2020 HM Revenue & Customs
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

package uk.gov.hmrc.vatapi.stubs

import com.github.tomakehurst.wiremock.stubbing.StubMapping
import play.api.http.Status._
import uk.gov.hmrc.support.WireMockMethods

object NrsStub extends WireMockMethods {

  private val uri: String = s".*/submission.*"

  def success(): StubMapping = {
    when(method = POST, uri = uri)
      .thenReturnInternal(status = ACCEPTED, headers, body = Some(successResponse))
  }

  def onError(errorStatus: Int, errorBody: String = "{}"): StubMapping = {
    when(method = POST, uri = uri)
      .thenReturn(status = errorStatus, errorBody)
  }

  private val successResponse: String =
    s"""
       |{
       |  "nrSubmissionId":"2dd537bc-4244-4ebf-bac9-96321be13cdc",
       |  "cadesTSignature":"30820b4f06092a864886f70111111111c0445c464",
       |  "timestamp":"2018-02-14T09:32:15Z"
       |}
         """.stripMargin

  private val headers = Map("Content-Type" -> "application/json", "Receipt-Id" -> "de1249ad-c242-4f22-9fe6-f357b1bfcccf",
    "Receipt-Signature" -> "757b1365-d89e-4dac-8317-ba87efca6c21",
    "Receipt-Timestamp" -> "2018-03-27T15:10:44.798Z")
}
