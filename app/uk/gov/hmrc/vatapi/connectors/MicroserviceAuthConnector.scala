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

package uk.gov.hmrc.vatapi.connectors

import javax.inject.Inject
import play.api.{Configuration, Environment}
import uk.gov.hmrc.auth.core.{AuthConnector, PlayAuthConnector}
import uk.gov.hmrc.play.bootstrap.http.DefaultHttpClient
import uk.gov.hmrc.play.config.ServicesConfig

class MicroserviceAuthConnector @Inject()(
                                           configuration: Configuration,
                                           env: Environment,
                                           override val http: DefaultHttpClient
                                         ) extends AuthConnector with PlayAuthConnector with ServicesConfig {
  override val mode = env.mode
  override val runModeConfiguration: Configuration = configuration
  override val serviceUrl: String = baseUrl("auth")
  val authBaseUrl: String = baseUrl("auth")
}