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

import com.google.inject.ImplementedBy
import config.{AppConfig, FeatureSwitch, PenaltiesEndpointsFeature}
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

// Add routes corresponding to available versions...
case class VersionRoutingMapImpl @Inject()(appConfig: AppConfig,
                                           defaultRouter: Router,
                                           v1Router: v1.Routes,
                                           v1RoutesWithPenalties: v1WithPenalties.Routes
                                          ) extends VersionRoutingMap {

  val featureSwitch: FeatureSwitch = FeatureSwitch(appConfig.featureSwitch)

  println(Console.YELLOW)
  println("***********************************************")
  println(s"${}")
  println("***********************************************")
  println(Console.RESET)

  val map: Map[String, Router] = Map(
    VERSION_1 -> {
      if (featureSwitch.isEnabled(PenaltiesEndpointsFeature)) {
        logger.info("[VersionRoutingMap][map] using v1RoutesWithPenalties - pointing to new packages including penalties")
        v1RoutesWithPenalties
      } else {
        logger.info("[VersionRoutingMap][map] using v1Router - pointing to new packages")
        v1Router
      }
    }
  )
}