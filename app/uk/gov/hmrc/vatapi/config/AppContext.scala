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

package uk.gov.hmrc.vatapi.config

import play.api.Configuration
import play.api.Play._
import uk.gov.hmrc.play.config.ServicesConfig
import uk.gov.hmrc.vatapi.auth.VATAuthEnrolments

object AppContext extends AppContext with ServicesConfig {
  lazy val config: Configuration = current.configuration
}

trait AppContext extends ServicesConfig {
  val config: Configuration

  lazy val desEnv: String = config.getString(s"$env.microservice.services.des.env").getOrElse(throw new RuntimeException("desEnv is not configured"))
  lazy val desToken: String = config.getString(s"$env.microservice.services.des.token").getOrElse(throw new RuntimeException("desEnv is not configured"))
  lazy val appName: String = config.getString("appName").getOrElse(throw new RuntimeException("appName is not configured"))
  lazy val appUrl: String = config.getString("appUrl").getOrElse(throw new RuntimeException("appUrl is not configured"))
  lazy val apiGatewayContext: Option[String] = config.getString("api.gateway.context")
  lazy val apiGatewayRegistrationContext: String = apiGatewayContext.getOrElse(throw new RuntimeException("api.gateway.context is not configured"))
  lazy val apiGatewayLinkContext: String = apiGatewayContext.map(x => if(x.isEmpty) x else s"/$x").getOrElse("")
  lazy val apiStatus: String = config.getString("api.status").getOrElse(throw new RuntimeException("api.status is not configured"))

  lazy val serviceLocatorUrl: String = baseUrl("service-locator")
  lazy val desUrl: String = baseUrl("des")
  lazy val nrsServiceUrl: String = baseUrl("non-repudiation")

  lazy val registrationEnabled: Boolean = current.configuration.getBoolean(s"$env.microservice.services.service-locator.enabled").getOrElse(true)
  lazy val featureSwitch: Option[Configuration] = config.getConfig(s"$env.feature-switch")
  lazy val auditEnabled: Boolean = config.getBoolean(s"auditing.enabled").getOrElse(true)
  lazy val authEnabled: Boolean = config.getBoolean(s"$env.microservice.services.auth.enabled").getOrElse(true)
  lazy val mtdDate: String = config.getString(s"$env.mtd-date").getOrElse(throw new RuntimeException("mtd-date is not configured"))
  lazy val xApiKey: String = config.getString(s"$env.access-keys.xApiKey").getOrElse(throw new RuntimeException("X-API-Key is not configured"))
  lazy val vatAuthEnrolments: VATAuthEnrolments = VATAuthEnrolments(config.getString(s"$env.enrolments.key").getOrElse(throw new RuntimeException("enrolments.key is not configured")),
    config.getString(s"$env.enrolments.identifier").getOrElse(throw new RuntimeException("identifier is not configured")),
    config.getString(s"$env.enrolments.auth-rule"))

  lazy val vatHybridFeature = config.getBoolean("feature-switch.des.hybrid").getOrElse(false)
}
