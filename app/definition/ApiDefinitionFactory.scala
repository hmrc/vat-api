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

package definition

import config.AppConfig

import javax.inject.{Inject, Singleton}
import utils.Logging

@Singleton
class ApiDefinitionFactory @Inject()(appConfig: AppConfig) extends Logging{

  private val readScope = "read:vat"
  private val writeScope = "write:vat"


  lazy val definition: Definition =
    Definition(
     api = APIDefinition(
        name = "VAT (MTD)",
        description =
          "An API for providing VAT data",
        context = appConfig.apiGatewayContext,
        categories = Seq("VAT_MTD"),
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

  private[definition] def buildAPIStatus(version: String): APIStatus = {
    APIStatus.parser.lift(appConfig.apiStatus(version))
      .getOrElse {
        logger.error(s"[ApiDefinition][buildApiStatus] no API Status found in config.  Reverting to Alpha")
        APIStatus.ALPHA
      }
  }
}