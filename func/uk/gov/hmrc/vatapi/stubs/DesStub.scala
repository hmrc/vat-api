/*
 * Copyright 2019 HM Revenue & Customs
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
import play.api.libs.json.JsValue
import uk.gov.hmrc.support.WireMockMethods

object DesStub extends WireMockMethods {

  def onSuccess(method: HTTPMethod, uri: String, status: Int, body: JsValue): StubMapping = {
    when(method = method, uri = uri)
      .thenReturn(status = status, body)
  }

  def onSuccess(method: HTTPMethod, uri: String, queryParams: Map[String, String], status: Int, body: JsValue): StubMapping = {
    when(method = method, uri = uri, queryParams = queryParams)
      .thenReturn(status = status, body)
  }

  def onError(method: HTTPMethod, uri: String, errorStatus: Int, errorBody: String): StubMapping = {
    when(method = method, uri = uri)
      .thenReturn(status = errorStatus, errorBody)
  }

  def onError(method: HTTPMethod, uri: String, queryParams: Map[String, String], errorStatus: Int, errorBody: String): StubMapping = {
    when(method = method, uri = uri, queryParams)
      .thenReturn(status = errorStatus, errorBody)
  }
}
