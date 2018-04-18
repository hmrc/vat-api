/*
 * Copyright 2018 HM Revenue & Customs
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

import org.mockito.Matchers
import org.mockito.Mockito._
import org.mockito.stubbing.OngoingStubbing
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{BeforeAndAfterEach, Suite}
import play.api.libs.json.Writes
import uk.gov.hmrc.http.{HeaderCarrier, HttpReads, HttpResponse}
import uk.gov.hmrc.vatapi.config.WSHttp

import scala.concurrent.{ExecutionContext, Future}
trait MockHttp extends MockitoSugar with BeforeAndAfterEach { this: Suite =>

  val mockHttp: WSHttp = mock[WSHttp]

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockHttp)
  }

  def setupMockHttpGet(url: String)(response: HttpResponse): OngoingStubbing[Future[HttpResponse]] =
    when(mockHttp.GET[HttpResponse](Matchers.eq(url))
      (Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(Future.successful(response))


  def setupMockFailedHttpGet(url: String)(response: HttpResponse): OngoingStubbing[Future[HttpResponse]] =
    when(mockHttp.GET[HttpResponse](Matchers.eq(url))
      (Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(Future.failed(new Exception))

  def setupMockHttpPost[T, R](url: String, elem: T)(response: R): OngoingStubbing[Future[R]] ={
    when(
      mockHttp.POST[T, R](Matchers.eq(url), Matchers.eq[T](elem), Matchers.any[Seq[(String, String)]]())
      (Matchers.any[Writes[T]](), Matchers.any[HttpReads[R]](), Matchers.any[HeaderCarrier](), Matchers.any[ExecutionContext]())
    ).thenReturn(
      Future.successful(response))
  }

  def setupMockHttpPostString[R](url: String, elem: String)(response: R): OngoingStubbing[Future[R]] ={
    when(
      mockHttp.POSTString[R](Matchers.eq(url), Matchers.eq[String](elem), Matchers.any[Seq[(String, String)]]())
        (Matchers.any[HttpReads[R]](), Matchers.any[HeaderCarrier](), Matchers.any[ExecutionContext]())
    ).thenReturn(
      Future.successful(response))
  }
}
