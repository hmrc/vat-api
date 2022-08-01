package v1.stubs

import com.github.tomakehurst.wiremock.stubbing.StubMapping
import play.api.libs.json.JsValue
import support.WireMockMethods

object PenaltiesStub extends WireMockMethods {

  def onSuccess(method: HTTPMethod, uri: String, status: Int, body: JsValue): StubMapping = {
    when(method = method, uri = uri)
      .thenReturn(status = status, body)
  }

  def onError(method: HTTPMethod, uri: String, errorStatus: Int, errorBody: String): StubMapping = {
    when(method = method, uri = uri)
      .thenReturn(status = errorStatus, errorBody)
  }

  def onError(method: HTTPMethod, uri: String, errorStatus: Int, errorBody: JsValue): StubMapping = {
    when(method = method, uri = uri)
      .thenReturn(status = errorStatus, errorBody)
  }
}
