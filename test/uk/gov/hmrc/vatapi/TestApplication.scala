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

package uk.gov.hmrc.vatapi

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock._
import com.github.tomakehurst.wiremock.core.WireMockConfiguration._
import com.github.tomakehurst.wiremock.stubbing.StubMapping
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach}
import play.api.http.Status._

import scala.concurrent.duration._

trait TestApplication
  extends UnitSpec
    with BeforeAndAfterEach
    with BeforeAndAfterAll
    with MockitoSugar {

  override implicit val timeout: FiniteDuration = 100 seconds

  val mockPort = 22222
  val mockHost = "localhost"

  protected val wiremockBaseUrl: String = s"http://$mockHost:$mockHost"
  private val wireMockServer = new WireMockServer(wireMockConfig().port(mockPort))

  protected def baseBeforeAll(): StubMapping = {
    wireMockServer.stop()
    wireMockServer.start()
    WireMock.configureFor(mockHost, mockPort)
    // the below stub is here so that the application finds the registration endpoint which is called on startup
    stubFor(post(urlPathEqualTo("/registration")).willReturn(aResponse().withStatus(OK)))
  }

  override def beforeAll(): Unit = {
    super.beforeAll()
    baseBeforeAll()
  }

  override def afterAll(): Unit = {
    super.afterAll()
    wireMockServer.stop()
  }

  override def beforeEach(): Unit = {
    super.beforeEach()
    WireMock.reset()
  }

}
