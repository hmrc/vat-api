/*
 * Copyright 2022 HM Revenue & Customs
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

package config

import javax.inject.{Inject, Singleton}
import play.api.Configuration
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig
import utils.Retrying

import scala.concurrent.duration._

trait AppConfig {

  val servicesConfig: ServicesConfig

  // DES Config
  def desBaseUrl: String
  def desEnv: String
  def desToken: String
  def desEnvironmentHeaders: Option[Seq[String]]

  // API Config
  def apiGatewayContext: String
  def apiStatus(version: String): String
  def featureSwitch: Option[Configuration]
  def endpointsEnabled(version: String): Boolean

  // NRS config items
  def nrsApiKey: String
  def nrsRetries: List[FiniteDuration]
  def appName: String
  def nrsBaseUrl: String

  //Penalties config items
  def penaltiesBaseUrl: String
}

@Singleton
class AppConfigImpl @Inject()(config: ServicesConfig, configuration: Configuration) extends AppConfig {

  override val servicesConfig: ServicesConfig = config

  // DES Config
  val desBaseUrl: String = config.baseUrl("des")
  val desEnv: String = config.getString("microservice.services.des.env")
  val desToken: String = config.getString("microservice.services.des.token")
  val desEnvironmentHeaders: Option[Seq[String]] = configuration.getOptional[Seq[String]]("microservice.services.des.environmentHeaders")

  // API Config
  val apiGatewayContext: String = config.getString("api.gateway.context")
  def apiStatus(version: String): String = config.getString(s"api.$version.status")
  def featureSwitch: Option[Configuration] = configuration.getOptional[Configuration](s"feature-switch")
  def endpointsEnabled(version: String): Boolean = config.getBoolean(s"api.$version.endpoints.enabled")

  // NRS config items
  val nrsApiKey: String = config.getString("access-keys.xApiKey")
  val appName: String = config.getString("appName")
  private val nrsConfig = configuration.get[Configuration]("microservice.services.non-repudiation")
  val nrsBaseUrl: String = config.baseUrl("non-repudiation")
  lazy val nrsRetries: List[FiniteDuration] =
    Retrying.fibonacciDelays(getFiniteDuration(nrsConfig, "initialDelay"), nrsConfig.get[Int]("numberOfRetries"))

  //Penalties Config
  val penaltiesBaseUrl: String = config.baseUrl("penalties")

  private final def getFiniteDuration(config: Configuration, path: String): FiniteDuration = {
    val string = config.get[String](path)

    Duration.create(string) match {
      case f: FiniteDuration => f
      case _                 => throw new RuntimeException(s"Not a finite duration '$string' for $path")
    }
  }
}

trait FixedConfig {
  // Minimum tax year for MTD
  val minimumTaxYear = 2018
}