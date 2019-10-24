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

package uk.gov.hmrc.vatapi.config

import com.typesafe.config.ConfigFactory
import javax.inject.{Inject, Singleton}
import play.api.Play._
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig
import uk.gov.hmrc.vatapi.auth.VATAuthEnrolments

@Singleton
class AppContext @Inject()(config: ServicesConfig) extends FixedConfig {


  //API Platform Config
  lazy val appName: String = config.getString("appName")
  lazy val appUrl: String = config.getString("appUrl")
  lazy val apiGatewayContext: String = config.getString("api.gateway.context")
  lazy val apiGatewayRegistrationContext: String = apiGatewayContext
  lazy val apiGatewayLinkContext: String = apiGatewayContext
  lazy val registrationEnabled: Boolean = current.configuration.getBoolean(s"microservice.services.service-locator.enabled").getOrElse(true)
  lazy val serviceLocatorUrl: String = config.baseUrl("service-locator")

  //DES Config
  lazy val desEnv: String = config.getString(s"microservice.services.des.env")
  lazy val desToken: String = config.getString(s"microservice.services.des.token")
  lazy val desUrl: String = config.baseUrl("des")
  //NRS Config
  lazy val nrsServiceUrl: String = config.baseUrl("non-repudiation")
  lazy val xApiKey: String = config.getString(s"access-keys.xApiKey")
  lazy val nrsMaxTimeoutMillis: Int = config.getInt(s"microservice.services.non-repudiation.maxTimeout")

  lazy val featureSwitch: String = config.getString(s"feature-switch")

  lazy val vatAuthEnrolments: VATAuthEnrolments = VATAuthEnrolments(config.getString(s"enrolments.key"),
    config.getString(s"enrolments.identifier"),
    Some(config.getString(s"enrolments.authRule")))

  def apiStatus(version: String): String = config.getString(s"api.$version.status")
}

trait FixedConfig {
  val mtdDate = ConfigFactory.load().getString("mtd-date")
}
