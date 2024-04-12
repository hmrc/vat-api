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

package config

import controllers.Assets
import definition.ApiDefinitionFactory
import javax.inject.{Inject, Singleton}
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

@Singleton
class DocumentationController @Inject()(vatApiDefinition: ApiDefinitionFactory,
                                        cc: ControllerComponents,
                                        assets: Assets,
                                        implicit val appConfig: AppConfig)
  extends BackendController(cc) with FeatureToggleSupport {

  def definition(): Action[AnyContent] = Action {
    Ok(Json.toJson(vatApiDefinition.definition))
  }

  def raml(version: String, file: String): Action[AnyContent] = {
      assets.at(s"/public/api/conf/$version", file)
  }
}
