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

package uk.gov.hmrc.vatapi

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock._
import com.github.tomakehurst.wiremock.core.WireMockConfiguration._
import com.github.tomakehurst.wiremock.stubbing.StubMapping
import org.scalatest.concurrent.{Eventually, IntegrationPatience, ScalaFutures}
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach}
import org.scalatestplus.play.OneServerPerSuite
import play.api.http.Status._

import scala.concurrent.duration._

trait TestApplication
  extends UnitSpec
    with BeforeAndAfterEach
    with BeforeAndAfterAll
    with OneServerPerSuite
    with Eventually
    with ScalaFutures
    with IntegrationPatience
    with MockitoSugar {

  override implicit val timeout: FiniteDuration = 100 seconds

  private val WIREMOCK_PORT = 22222
  private val stubHost = "localhost"

  protected val wiremockBaseUrl: String = s"http://$stubHost:$WIREMOCK_PORT"
  private val wireMockServer = new WireMockServer(wireMockConfig().port(WIREMOCK_PORT))

  protected def baseBeforeAll(): StubMapping = {
    wireMockServer.stop()
    wireMockServer.start()
    WireMock.configureFor(stubHost, WIREMOCK_PORT)
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
