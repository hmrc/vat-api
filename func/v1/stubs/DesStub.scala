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

package v1.stubs

import com.github.tomakehurst.wiremock.stubbing.StubMapping
import play.api.http.Status.OK
import play.api.libs.json.JsValue
import support.WireMockMethods
import v1.fixtures.ViewReturnFixture

object DesStub extends WireMockMethods with ViewReturnFixture {

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

  private def url(vrn: String): String =
    s"/vat/returns/vrn/$vrn"

  def serviceSuccess(vrn: String, periodKey: String): StubMapping = {
    when(method = GET, uri = url(vrn), queryParams = Map("period-key" -> periodKey))
      .thenReturn(status = OK, viewReturnDesJson)
  }
}
