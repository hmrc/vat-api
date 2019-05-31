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

package uk.gov.hmrc.vatapi.config

import javax.inject.Inject
import play.api.Configuration

case class FeatureSwitch @Inject()(value: Option[Configuration], env: String) {

  val DEFAULT_VALUE = true

  def isWhiteListingEnabled: Boolean = {
    value match {
      case Some(config) => config.getBoolean("white-list.enabled").getOrElse(false)
      case None => false
    }
  }

  def isAgentSimulationFilterEnabled: Boolean = value match {
    case Some(config) => config.getBoolean("client-agent-simulation.enabled").getOrElse(false)
    case None => false
  }

  def whiteListedApplicationIds: Seq[String] = {
    value match {
      case Some(config) =>
        config
          .getStringSeq("white-list.applicationIds")
          .getOrElse(throw new RuntimeException(s"$env.feature-switch.white-list.applicationIds is not configured"))
      case None => Seq()
    }
  }

  def isAuthEnabled: Boolean = {
    value match {
      case Some(config) => config.getBoolean("auth.enabled").getOrElse(true)
      case None => true
    }
  }

  def isOriginatorIdForSubmitEnabled: Boolean = {
    value match {
      case Some(config) => config.getBoolean("submit-with-originatorId.enabled").getOrElse(false)
      case None => false
    }
  }
}

