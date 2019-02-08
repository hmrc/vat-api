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

package uk.gov.hmrc.vatapi.controllers.definition

import javax.inject.{Inject, Singleton}
import play.api.http.LazyHttpErrorHandler
import play.api.libs.json.Json
import play.api.mvc.Action
import uk.gov.hmrc.vatapi.controllers.definition.JsonFormatters._

@Singleton
class DocumentationController @Inject()(vatApiDefinition: VatApiDefinition) extends uk.gov.hmrc.api.controllers.DocumentationController(LazyHttpErrorHandler) {

  override def definition() = Action {
    Ok(Json.toJson(vatApiDefinition.definition))
  }

  override def conf(version: String, file: String) = {
    super.at(s"/public/api/conf/$version", file)
  }
}