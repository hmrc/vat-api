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

package uk.gov.hmrc.vatapi.controllers.definition

import play.api.Logger
import uk.gov.hmrc.vatapi.config.{AppContext, FeatureSwitch}
import uk.gov.hmrc.vatapi.controllers.definition.APIStatus.APIStatus
import uk.gov.hmrc.vatapi.controllers.definition.AuthType._
import uk.gov.hmrc.vatapi.controllers.definition.GroupName._
import uk.gov.hmrc.vatapi.controllers.definition.HttpMethod._
import uk.gov.hmrc.vatapi.controllers.definition.ResourceThrottlingTier._

class VatApiDefinition {

  val logger: Logger = Logger(this.getClass)

  private val readScope = "read:vat"
  private val writeScope = "write:vat"

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


  private val allEndpoints = vatEndpoints

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
        context = AppContext.apiGatewayRegistrationContext,
        versions = Seq(
          APIVersion(
            version = "1.0",
            access = buildWhiteListingAccess(),
            status = buildAPIStatus(),
            endpoints = allEndpoints,
            endpointsEnabled = true
          )
        ),
        requiresTrust = None
      )
    )

  private def buildAPIStatus(): APIStatus = {
    AppContext.apiStatus match {
      case "ALPHA" => APIStatus.ALPHA
      case "BETA" => APIStatus.BETA
      case "STABLE" => APIStatus.STABLE
      case "DEPRECATED" => APIStatus.DEPRECATED
      case "RETIRED" => APIStatus.RETIRED
      case _ => logger.error(s"[ApiDefinition][buildApiStatus] no API Status found in config.  Reverting to Alpha")
        APIStatus.ALPHA
    }
  }

  private def buildWhiteListingAccess(): Option[Access] = {
    val featureSwitch = FeatureSwitch(AppContext.featureSwitch)
    featureSwitch.isWhiteListingEnabled match {
      case true =>
        Some(Access("PRIVATE", featureSwitch.whiteListedApplicationIds))
      case false => None
    }
  }
}

object VatApiDefinition extends VatApiDefinition
