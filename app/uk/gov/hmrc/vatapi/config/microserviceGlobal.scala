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
//import uk.gov.hmrc.play.auth.controllers.AuthParamsControllerConfig
//import uk.gov.hmrc.play.auth.microservice.filters.AuthorisationFilter
//import uk.gov.hmrc.play.microservice.bootstrap.DefaultMicroserviceGlobal
//import uk.gov.hmrc.play.bootstrap.filters.{AuditFilter, LoggingFilter, MicroserviceFilterSupport}


// TODO WHERE IS THIS USED AND WHAT FOR? IS IT SUPERSEDED BY BOOTSTRAP?
//object MicroserviceAuditFilter
//  extends AuditFilter
//    with AppName
//    with MicroserviceFilterSupport {
//  override val auditConnector: MicroserviceAuditConnector.type = MicroserviceAuditConnector
//
//  override def controllerNeedsAuditing(controllerName: String) =
//    AppContext.auditEnabled && ControllerConfiguration
//      .controllerParamsConfig(controllerName)
//      .needsAuditing
//
//}


// TODO WHERE IS THIS USED AND WHAT FOR? IS IT SUPERSEDED BY BOOTSTRAP?
//object MicroserviceLoggingFilter
//  extends LoggingFilter
//    with MicroserviceFilterSupport {
//  override def controllerNeedsLogging(controllerName: String) =
//    ControllerConfiguration.controllerParamsConfig(controllerName).needsLogging
//}

// TODO WHAT NEEDS TO HAPPEN WITH THIS
//object AuthParamsControllerConfiguration extends AuthParamsControllerConfig {
//  lazy val controllerConfigs = ControllerConfiguration.controllerConfigs
//}

// TODO WHERE IS THIS USED AND WHAT FOR? IS IT SUPERSEDED BY BOOTSTRAP?
//object MicroserviceAuthFilter
//  extends AuthorisationFilter
//    with MicroserviceFilterSupport {
//
//  override def authConnector = MicroserviceAuthConnector
//
//
//  override def authParamsConfig = AuthParamsControllerConfiguration
//
//  override def controllerNeedsAuth(controllerName: String): Boolean = ControllerConfiguration.paramsForController(controllerName).needsAuth
//}


/* TODO What needs to happen with MicroserviceRegistration?
trait MicroserviceRegistration
  extends ServiceLocatorRegistration
    with ServiceLocatorConfig {
  override lazy val registrationEnabled: Boolean =
    AppContext.registrationEnabled
  override val slConnector: ServiceLocatorConnector = ServiceLocatorConnector(
    WSHttp)
  override implicit val hc: HeaderCarrier = HeaderCarrier()
}
*/

/* TODO IS THIS TO BE DELETED?
object MicroserviceGlobal
  extends DefaultMicroserviceGlobal
    with MicroserviceRegistration {

  private var application: Application = _

  override val auditConnector: MicroserviceAuditConnector.type = MicroserviceAuditConnector

  override def microserviceMetricsConfig(
                                          implicit app: Application): Option[Configuration] =
    app.configuration.getConfig(s"$env.microservice.metrics")

  override val loggingFilter: MicroserviceLoggingFilter.type = MicroserviceLoggingFilter

  override val microserviceAuditFilter: MicroserviceAuditFilter.type = MicroserviceAuditFilter

  override val authFilter = Some(MicroserviceAuthFilter)

  private def enabledFilters: Seq[EssentialFilter] = {
    val featureSwitch = FeatureSwitch(AppContext.featureSwitch)
    if (featureSwitch.isAgentSimulationFilterEnabled) Seq(AgentSimulationFilter)
    else Seq.empty
  }

  override def microserviceFilters: Seq[EssentialFilter] =
    Seq(SetXContentTypeOptionsFilter, HeaderValidatorFilter, EmptyResponseFilter, SetContentTypeFilter) ++ enabledFilters ++
      defaultMicroserviceFilters

}
*/

