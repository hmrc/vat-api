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

import akka.actor.ActorSystem
import javax.inject.{Inject, Singleton}
import play.api.{Configuration, Environment}
import uk.gov.hmrc.auth.core.{AuthConnector, PlayAuthConnector}
import uk.gov.hmrc.http.hooks.HttpHooks
import uk.gov.hmrc.http.{HttpDelete, HttpGet, HttpPost, HttpPut}
import uk.gov.hmrc.play.audit.http.HttpAuditing
import uk.gov.hmrc.play.audit.http.config.AuditingConfig
import uk.gov.hmrc.play.audit.http.connector.AuditConnector
import uk.gov.hmrc.play.bootstrap.config.LoadAuditingConfig
import uk.gov.hmrc.play.bootstrap.http.DefaultHttpClient
import uk.gov.hmrc.play.config.{AppName, ServicesConfig}
import uk.gov.hmrc.play.http.ws._
//import uk.gov.hmrc.play.microservice.config.LoadAuditingConfig


@Singleton
class MicroserviceAuditConnector @Inject()(configuration: Configuration, env: Environment) extends AuditConnector {
  val mode = env.mode
  lazy val auditingConfig: AuditingConfig = LoadAuditingConfig(configuration, mode, s"auditing")
}

@Singleton
class Hooks @Inject()(override val auditConnector: AuditConnector, appConfig: AppConfig) extends HttpHooks with HttpAuditing {
  override val hooks = Seq(AuditingHook)
  override val appName = "vat-api"
  //  override val auditConnector: AuditConnector // = MicroserviceAuditConnector
}

//trait WSHttp
//  extends HttpGet
//    with WSGet
//    with HttpPut
//    with WSPut
//    with HttpPost
//    with WSPost
//    with HttpDelete
//    with WSDelete
//    //    with Hooks
//    with AppName

class WSHttp @Inject()(hooksI: Hooks, config: Configuration, override val actorSystem: ActorSystem)  extends HttpGet
  with WSGet
  with HttpPut
  with WSPut
  with HttpPost
  with WSPost
  with HttpDelete
  with WSDelete
  //    with Hooks
  with AppName{
  override val configuration = Some(config.underlying)
  // TODO Check below
  override val appNameConfiguration = config
  // TODO What should this actually be?
  override val hooks = Seq()

}

class MicroserviceAuthConnector @Inject()(
                                           env: Environment,
                                           config: Configuration,
                                           override val http: WSHttp,
                                           override val runModeConfiguration: Configuration,
                                           actorSystem: ActorSystem
                                         ) extends AuthConnector with PlayAuthConnector with ServicesConfig {// with DefaultHttpClient {
  val authBaseUrl: String = baseUrl("auth")
  override val serviceUrl: String = baseUrl("auth")
  override val mode = env.mode
//  override val configuration = Some(config.underlying)
  // TODO Check below
//  override val appNameConfiguration = config
  // TODO What should this actually be?
//  override val hooks = Seq()

}
