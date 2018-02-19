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

import com.typesafe.config.Config
import net.ceedubs.ficus.Ficus._
import net.ceedubs.ficus.readers.{StringReader, ValueReader}
import play.api.http.HttpEntity
import play.api.libs.json.Json
import play.api.mvc.Results._
import play.api.mvc._
import play.api.{Application, Configuration, Logger, Play}
import play.routing.Router.Tags
import uk.gov.hmrc.api.config.{ServiceLocatorConfig, ServiceLocatorRegistration}
import uk.gov.hmrc.api.connector.ServiceLocatorConnector
import uk.gov.hmrc.api.controllers.{ErrorAcceptHeaderInvalid, HeaderValidator}
import uk.gov.hmrc.http.{HeaderCarrier, NotImplementedException}
import uk.gov.hmrc.play.auth.controllers.AuthParamsControllerConfig
import uk.gov.hmrc.play.auth.microservice.filters.AuthorisationFilter
import uk.gov.hmrc.play.config.{AppName, ControllerConfig}
import uk.gov.hmrc.play.microservice.bootstrap.DefaultMicroserviceGlobal
import uk.gov.hmrc.play.microservice.filters.{AuditFilter, LoggingFilter, MicroserviceFilterSupport}
import uk.gov.hmrc.vatapi.models._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.matching.Regex

case class ControllerConfigParams(needsHeaderValidation: Boolean = true,
                                  needsLogging: Boolean = true,
                                  needsAuditing: Boolean = true,
                                  needsTaxYear: Boolean = true)

object ControllerConfiguration extends ControllerConfig {
  private implicit val regexValueReader: ValueReader[Regex] =
    StringReader.stringValueReader.map(_.r)
  private implicit val controllerParamsReader: ValueReader[ControllerConfigParams] =
    ValueReader.relative[ControllerConfigParams] { config =>
      ControllerConfigParams(
        needsHeaderValidation =
          config.getAs[Boolean]("needsHeaderValidation").getOrElse(true),
        needsLogging = config.getAs[Boolean]("needsLogging").getOrElse(true),
        needsAuditing = config.getAs[Boolean]("needsAuditing").getOrElse(true),
        needsTaxYear = config.getAs[Boolean]("needsTaxYear").getOrElse(true)
      )
    }

  lazy val controllerConfigs: Config =
    Play.current.configuration.underlying.as[Config]("controllers")

  def controllerParamsConfig(controllerName: String): ControllerConfigParams = {
    controllerConfigs
      .as[Option[ControllerConfigParams]](controllerName)
      .getOrElse(ControllerConfigParams())
  }
}

object MicroserviceAuditFilter
  extends AuditFilter
    with AppName
    with MicroserviceFilterSupport {
  override val auditConnector: MicroserviceAuditConnector.type = MicroserviceAuditConnector

  override def controllerNeedsAuditing(controllerName: String) =
    AppContext.auditEnabled && ControllerConfiguration
      .controllerParamsConfig(controllerName)
      .needsAuditing

}

object MicroserviceLoggingFilter
  extends LoggingFilter
    with MicroserviceFilterSupport {
  override def controllerNeedsLogging(controllerName: String) =
    ControllerConfiguration.controllerParamsConfig(controllerName).needsLogging
}

object EmptyResponseFilter extends Filter with MicroserviceFilterSupport {
  val emptyHeader = "Gov-Empty-Response"

  override def apply(f: (RequestHeader) => Future[Result])(
    rh: RequestHeader): Future[Result] =
    f(rh) map { res =>
      if ((res.header.status == 201 || res.header.status == 409) && res.body.isKnownEmpty) {
        val headers = res.header.headers
          .updated("Content-Type", "application/json")
          .updated(emptyHeader, "true")
        res.copy(res.header.copy(headers = headers), HttpEntity.NoEntity)
      } else res
    }
}

// this filter is a workaround for the issue reported here https://jira.tools.tax.service.gov.uk/browse/APSR-87
object SetContentTypeFilter extends Filter with MicroserviceFilterSupport {
  override def apply(f: (RequestHeader) => Future[Result])(
    rh: RequestHeader): Future[Result] =
    f(rh).map(_.as("application/json"))
}


object SetXContentTypeOptionsFilter extends Filter with MicroserviceFilterSupport {
  val xContentTypeOptionsHeader = "X-Content-Type-Options"
  override def apply(f: (RequestHeader) => Future[Result])(rh: RequestHeader): Future[Result] = {
    f(rh).map(_.withHeaders((xContentTypeOptionsHeader, "nosniff")))
  }
}

object AuthParamsControllerConfiguration extends AuthParamsControllerConfig {
  lazy val controllerConfigs = ControllerConfiguration.controllerConfigs
}

object MicroserviceAuthFilter
  extends AuthorisationFilter
    with MicroserviceFilterSupport {

  override def authConnector = MicroserviceAuthConnector


  override def authParamsConfig = AuthParamsControllerConfiguration

  override def controllerNeedsAuth(controllerName: String): Boolean = ControllerConfiguration.paramsForController(controllerName).needsAuth
}

object HeaderValidatorFilter
  extends Filter
    with HeaderValidator
    with MicroserviceFilterSupport {
  def apply(next: (RequestHeader) => Future[Result])(
    rh: RequestHeader): Future[Result] = {
    val controller = rh.tags.get(Tags.ROUTE_CONTROLLER)
    val needsHeaderValidation =
      controller.forall(
        name =>
          ControllerConfiguration
            .controllerParamsConfig(name)
            .needsHeaderValidation)

    if (!needsHeaderValidation || acceptHeaderValidationRules(
      rh.headers.get("Accept"))) next(rh)
    else
      Future.successful(
        Status(ErrorAcceptHeaderInvalid.httpStatusCode)(
          Json.toJson(ErrorAcceptHeaderInvalid)))
  }
}

trait MicroserviceRegistration
  extends ServiceLocatorRegistration
    with ServiceLocatorConfig {
  override lazy val registrationEnabled: Boolean =
    AppContext.registrationEnabled
  override val slConnector: ServiceLocatorConnector = ServiceLocatorConnector(
    WSHttp)
  override implicit val hc: HeaderCarrier = HeaderCarrier()
}

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

  private def enabledFilters: Seq[EssentialFilter] = Seq.empty

  override def microserviceFilters: Seq[EssentialFilter] =
    Seq(SetXContentTypeOptionsFilter, HeaderValidatorFilter, EmptyResponseFilter, SetContentTypeFilter) ++ enabledFilters ++
      defaultMicroserviceFilters

  override def onStart(app: Application): Unit = {
    super.onStart(app)
    application = app
  }

  override def onError(request: RequestHeader, ex: Throwable): Future[Result] = {
    super.onError(request, ex).map { result =>
      ex match {
        case _ =>
          ex.getCause match {
            case ex: NotImplementedException =>
              NotImplemented(Json.toJson(ErrorNotImplemented))
            case _ => result
          }
      }
    }
  }


  override def onBadRequest(request: RequestHeader, error: String) = {
    super.onBadRequest(request, error).map { result =>
      error match {
        case "ERROR_VRN_INVALID"       => BadRequest(Json.toJson(ErrorBadRequest(ErrorCode.VRN_INVALID, "The provided Vrn is invalid")))
        case "ERROR_INVALID_DATE"      => BadRequest(Json.toJson(ErrorBadRequest(ErrorCode.INVALID_DATE, "The provided date is invalid")))
        case "ERROR_INVALID_FROM_DATE" => BadRequest(Json.toJson(ErrorBadRequest(ErrorCode.INVALID_FROM_DATE, "The provided from date is invalid")))
        case "ERROR_INVALID_TO_DATE"   => BadRequest(Json.toJson(ErrorBadRequest(ErrorCode.INVALID_TO_DATE, "The provided to date is invalid")))
        case _                         => result
      }
    }
  }
}
