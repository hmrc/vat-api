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

package routing

import config.{AppConfig, FeatureSwitch, FeatureToggleSupport}
import definition.Versions
import javax.inject.{Inject, Singleton}
import play.api.Logging
import play.api.http.{DefaultHttpRequestHandler, HttpConfiguration, HttpErrorHandler, HttpFilters}
import play.api.libs.json.Json
import play.api.mvc.{DefaultActionBuilder, Handler, RequestHeader, Results}
import play.api.routing.Router
import play.core.DefaultWebCommands
import v1.controllers.requestParsers.validators.validations.VrnValidation
import v1.models.errors.{InvalidAcceptHeaderError, UnsupportedVersionError, VrnFormatError}

@Singleton
class VersionRoutingRequestHandler @Inject()(versionRoutingMap: VersionRoutingMap,
                                             errorHandler: HttpErrorHandler,
                                             httpConfiguration: HttpConfiguration,
                                             filters: HttpFilters,
                                             action: DefaultActionBuilder,
                                             implicit val config: AppConfig)
  extends DefaultHttpRequestHandler(
    webCommands = new DefaultWebCommands,
    optDevContext = None,
    router = versionRoutingMap.defaultRouter,
    errorHandler = errorHandler,
    configuration = httpConfiguration,
    filters = filters.filters
  ) with Logging with FeatureToggleSupport {

  private val unsupportedVersionAction = action(Results.NotFound(Json.toJson(UnsupportedVersionError)))
  private val invalidAcceptHeaderError = action(Results.NotAcceptable(Json.toJson(InvalidAcceptHeaderError)))

  override def routeRequest(request: RequestHeader): Option[Handler] = {

    def documentHandler: Option[Handler] = routeWith(versionRoutingMap.defaultRouter)(request)

    def apiHandler: Option[Handler] = Versions.getFromRequest(request) match {
      case Some(version) =>
        versionRoutingMap.versionRouter(version) match {
          case Some(versionRouter) if isVersionEnabled(version) =>
            routeWith(versionRouter)(request)
          case Some(_) => Some(unsupportedVersionAction)
          case None => Some(invalidAcceptHeaderError)
        }
      case None =>
        logger.warn(s"\n$request\n")
        Some(invalidAcceptHeaderError)
    }

    documentHandler orElse apiHandler
  }

  private def routeWith(router: Router)(request: RequestHeader): Option[Handler] =
    router
      .handlerFor(request)
      .orElse {
        if (validatePath(request.path)) {
          if (request.path.endsWith("/")) {
            val pathWithoutSlash = request.path.dropRight(1)
            val requestWithModifiedPath = request.withTarget(request.target.withPath(pathWithoutSlash))
            router.handlerFor(requestWithModifiedPath)
          }
          else None
        }
        else {
          Some(action(Results.BadRequest(Json.toJson(VrnFormatError))))
        }
      }

  private def validatePath(path: String): Boolean = {
    val vrn = path.split("/")(1)
    if(VrnValidation.validate(vrn) == Nil)
      true
    else false
  }
}