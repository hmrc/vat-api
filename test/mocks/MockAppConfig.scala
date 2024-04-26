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

package mocks

import config.AppConfig
import org.scalamock.handlers.CallHandler
import org.scalamock.scalatest.MockFactory
import play.api.Configuration

import scala.concurrent.duration.FiniteDuration

trait MockAppConfig extends MockFactory {

  val mockAppConfig: AppConfig = mock[AppConfig]

  object MockedAppConfig {

    //DES Config
    def desBaseUrl: CallHandler[String] = (() => mockAppConfig.desBaseUrl).expects()
    def desToken: CallHandler[String] = (() => mockAppConfig.desToken).expects()
    def desEnvironment: CallHandler[String] = (() => mockAppConfig.desEnv).expects()
    def desEnvironmentHeaders: CallHandler[Option[Seq[String]]] = (() => mockAppConfig.desEnvironmentHeaders).expects()

    //API Config
    def featureSwitch: CallHandler[Option[Configuration]] = (() => mockAppConfig.featureSwitch).expects()
    def apiGatewayContext: CallHandler[String] = (() => mockAppConfig.apiGatewayContext).expects()
    def apiStatus: CallHandler[String] = (mockAppConfig.apiStatus: String => String).expects("1.0")
    def endpointsEnabled: CallHandler[Boolean] = (mockAppConfig.endpointsEnabled: String => Boolean).expects("1.0")

    // NRS config items
    def nrsApiKey: CallHandler[String] = (() => mockAppConfig.nrsApiKey).expects()
    def appName: CallHandler[String] = (() => mockAppConfig.appName).expects()
    def nrsBaseUrl: CallHandler[String] = (() => mockAppConfig.nrsBaseUrl).expects()
    def nrsRetries: CallHandler[List[FiniteDuration]] = (mockAppConfig.nrsRetries _).expects()
  }
}