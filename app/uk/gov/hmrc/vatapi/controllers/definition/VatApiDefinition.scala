/*
 * Copyright 2020 HM Revenue & Customs
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
import play.api.Logger
import uk.gov.hmrc.vatapi.config.{AppContext, FeatureSwitch}
import uk.gov.hmrc.vatapi.controllers.definition.APIStatus.APIStatus

@Singleton
class VatApiDefinition @Inject()(appContext: AppContext) {

  lazy val definition: Definition =
    Definition(
      scopes = Seq(
        Scope(
          key = readScope,
          name = "View your VAT information",
          description = "Allow read access to VAT data"
        ),
        Scope(
          key = writeScope,
          name = "Change your VAT information",
          description = "Allow write access to VAT data"
        )
      ),
      api = APIDefinition(
        name = "VAT (MTD)",
        description =
          "An API for providing VAT data",
        context = appContext.apiGatewayRegistrationContext,
        versions = Seq(
          APIVersion(
            version = "1.0",
            status = buildAPIStatus("1.0"),
            endpointsEnabled = true
          )
        ),
        requiresTrust = None
      )
    )
  val logger: Logger = Logger(this.getClass)

  private val readScope = "read:vat"
  private val writeScope = "write:vat"

  private def buildAPIStatus(version: String): APIStatus = {
    appContext.apiStatus(version) match {
      case "ALPHA" => APIStatus.ALPHA
      case "BETA" => APIStatus.BETA
      case "STABLE" => APIStatus.STABLE
      case "DEPRECATED" => APIStatus.DEPRECATED
      case "RETIRED" => APIStatus.RETIRED
      case _ => logger.error(s"[ApiDefinition][buildApiStatus] no API status found in config. Reverting to alpha")
        APIStatus.ALPHA
    }
  }

}
