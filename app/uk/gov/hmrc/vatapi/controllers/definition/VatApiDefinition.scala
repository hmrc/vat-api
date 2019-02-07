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
import play.api.Logger
import uk.gov.hmrc.vatapi.config.{AppContext, FeatureSwitch}
import uk.gov.hmrc.vatapi.controllers.definition.APIStatus.APIStatus
import uk.gov.hmrc.vatapi.controllers.definition.AuthType._
import uk.gov.hmrc.vatapi.controllers.definition.GroupName._
import uk.gov.hmrc.vatapi.controllers.definition.HttpMethod._
import uk.gov.hmrc.vatapi.controllers.definition.ResourceThrottlingTier._

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
            access = buildWhiteListingAccess(),
            status = buildAPIStatus("1.0"),
            endpoints = allEndpoints,
            endpointsEnabled = true
          )
        ),
        requiresTrust = None
      )
    )
  val logger: Logger = Logger(this.getClass)
  val vatEndpoints: Seq[Endpoint] = {
    Seq(
      Endpoint(
        uriPattern = "/{vrn}/obligations",
        endpointName = "Retrieve all VAT obligations",
        method = GET,
        authType = USER,
        throttlingTier = UNLIMITED,
        scope = Some(readScope),
        groupName = Vat)
      ,
      Endpoint(
        uriPattern = "/{vrn}/returns",
        endpointName = "Submit VAT return for period.",
        method = POST,
        authType = USER,
        throttlingTier = UNLIMITED,
        scope = Some(writeScope),
        groupName = Vat,
        queryParameters = Some(Seq(
          Parameter("from", true),
          Parameter("to", true),
          Parameter("status", true)
        )))
      ,
      Endpoint(
        uriPattern = "/{vrn}/returns",
        endpointName = "Retrieve submitted VAT returns",
        method = GET,
        authType = USER,
        throttlingTier = UNLIMITED,
        scope = Some(readScope),
        groupName = Vat),
      Endpoint(
        uriPattern = "/{vrn}/liabilities",
        endpointName = "Retrieve VAT liabilities",
        method = GET,
        authType = USER,
        throttlingTier = UNLIMITED,
        scope = Some(readScope),
        groupName = Vat,
        queryParameters = Some(Seq(
          Parameter("from", true),
          Parameter("to", true)
        ))
      )
    )
  }
  private val readScope = "read:vat"
  private val writeScope = "write:vat"
  private val allEndpoints = vatEndpoints

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

  private def buildWhiteListingAccess(): Option[Access] = {
    val featureSwitch = FeatureSwitch(appContext.featureSwitch, appContext.env)
    featureSwitch.isWhiteListingEnabled match {
      case true =>
        Some(Access("PRIVATE", featureSwitch.whiteListedApplicationIds))
      case false => None
    }
  }
}
