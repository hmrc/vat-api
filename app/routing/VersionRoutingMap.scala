/*
 * Copyright 2024 HM Revenue & Customs
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

import com.google.inject.ImplementedBy
import config.{AppConfig, FeatureToggleSupport}
import definition.Versions.VERSION_1
import play.api.routing.Router
import utils.Logging

import javax.inject.Inject

// So that we can have API-independent implementations of
// VersionRoutingRequestHandler and VersionRoutingRequestHandlerSpec
// implement this for the specific API...
@ImplementedBy(classOf[VersionRoutingMapImpl])
trait VersionRoutingMap extends Logging {

  val defaultRouter: Router

  val map: Map[String, Router]

  final def versionRouter(version: String): Option[Router] = map.get(version)
}

// Add switches and multiple routes files to the map in order to coordinate the go-live of new 3rd party endpoints, or control multiple versions of exposed endpoints.
// After a new endpoint goes live, remove the switch and old route file to leave only long term version(s).
// Currently no switches are in use and only one route file exists.
case class VersionRoutingMapImpl @Inject()(defaultRouter: Router,
                                           v1Routes: v1.Routes,
                                           implicit val appConfig: AppConfig
                                          ) extends VersionRoutingMap with FeatureToggleSupport {
  

  val map: Map[String, Router] = Map(
    VERSION_1 -> {
        v1Routes
    }
  )
}
