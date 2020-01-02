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

package uk.gov.hmrc.vatapi.mocks

import org.mockito.ArgumentCaptor
import org.mockito.stubbing.OngoingStubbing
import org.scalatest.Suite
import play.api.libs.json.Writes
import uk.gov.hmrc.http.{HeaderCarrier, HttpReads, HttpResponse}
import uk.gov.hmrc.play.bootstrap.http.DefaultHttpClient

import scala.concurrent.{ExecutionContext, Future}


trait MockHttp extends Mock { _: Suite =>

  val mockHttp: DefaultHttpClient = mock[DefaultHttpClient]

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockHttp)
  }

  private val headerCarrierCaptor = ArgumentCaptor.forClass(classOf[HeaderCarrier])

  object MockHttp {

    def GET[T](url: String): OngoingStubbing[Future[T]] = {
      when(mockHttp.GET[T](eqTo(url))(any(), headerCarrierCaptor.capture(), any()))
    }

    def fetchHeaderCarrier: HeaderCarrier = headerCarrierCaptor.getValue
  }

  def setupMockHttpGet(url: String)(response: HttpResponse): OngoingStubbing[Future[HttpResponse]] =
    when(mockHttp.GET[HttpResponse](eqTo(url))
      (any(), any(), any())).thenReturn(Future.successful(response))


  def setupMockFailedHttpGet(url: String)(response: HttpResponse): OngoingStubbing[Future[HttpResponse]] =
    when(mockHttp.GET[HttpResponse](eqTo(url))
      (any(), any(), any())).thenReturn(Future.failed(new Exception))

  def setupMockHttpPost[T, R](url: String, elem: T)(response: R): OngoingStubbing[Future[R]] ={
    when(
      mockHttp.POST[T, R](eqTo(url), eqTo[T](elem), any[Seq[(String, String)]]())
      (any[Writes[T]](), any[HttpReads[R]](), any[HeaderCarrier](), any[ExecutionContext]())
    ).thenReturn(
      Future.successful(response))
  }

  def setupMockHttpPostString[R](url: String, elem: String)(response: R): OngoingStubbing[Future[R]] ={
    when(
      mockHttp.POSTString[R](eqTo(url), eqTo[String](elem), any[Seq[(String, String)]]())
        (any[HttpReads[R]](), headerCarrierCaptor.capture(), any[ExecutionContext]())
    ).thenReturn(
      Future.successful(response))
  }
}
