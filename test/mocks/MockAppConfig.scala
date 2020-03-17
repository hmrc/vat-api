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

package mocks

import config.AppConfig
import org.scalamock.handlers.CallHandler
import org.scalamock.scalatest.MockFactory
import play.api.Configuration

trait MockAppConfig extends MockFactory {

  val mockAppConfig: AppConfig = mock[AppConfig]

  object MockedAppConfig {
    def desBaseUrl: CallHandler[String] = (mockAppConfig.desBaseUrl _: () => String).expects()
    def desToken: CallHandler[String] = (mockAppConfig.desToken _).expects()
    def desEnvironment: CallHandler[String] = (mockAppConfig.desEnv _).expects()
    def mtdIdBaseUrl: CallHandler[String] = (mockAppConfig.mtdIdBaseUrl _: () => String).expects()
    def featureSwitch: CallHandler[Option[Configuration]] = (mockAppConfig.featureSwitch _: () => Option[Configuration]).expects()
    def apiGatewayContext: CallHandler[String]            = (mockAppConfig.apiGatewayContext _: () => String).expects()
    def apiStatus: CallHandler[String] = (mockAppConfig.apiStatus: String => String).expects("1.0")
    def endpointsEnabled: CallHandler[Boolean] = (mockAppConfig.endpointsEnabled: String => Boolean).expects("1.0")
  }
}
