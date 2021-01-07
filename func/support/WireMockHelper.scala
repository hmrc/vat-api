/*
 * Copyright 2021 HM Revenue & Customs
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

package support

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import com.github.tomakehurst.wiremock.core.WireMockConfiguration._
import org.scalatest.concurrent.{Eventually, IntegrationPatience}
import org.scalatestplus.play.guice.GuiceOneServerPerSuite

object WireMockHelper extends Eventually with IntegrationPatience {

  val wireMockPort: Int = 11111
  val host: String = "localhost"
}

trait WireMockHelper {

  self: GuiceOneServerPerSuite =>

  import WireMockHelper._

  lazy val wireMockConf: WireMockConfiguration = wireMockConfig.port(wireMockPort)
  lazy val wireMockServer: WireMockServer = new WireMockServer(wireMockConf)

  def startWireMock(): Unit = {
    wireMockServer.start()
    WireMock.configureFor(host, wireMockPort)
  }

  def stopWireMock(): Unit = wireMockServer.stop()

  def resetWireMock(): Unit = WireMock.reset()
}
