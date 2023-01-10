/*
 * Copyright 2023 HM Revenue & Customs
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

import config.FeatureSwitch.FeatureSwitch
import play.api.Logging

import scala.sys.SystemProperties

trait FeatureToggleSupport extends Logging {

  def getValueBool(key: String)(implicit appConfig: AppConfig): Option[Boolean] = {
    val prefix = "feature-switch."
    val keyString = prefix + key
    Some(appConfig.servicesConfig.getBoolean(keyString))
  }

  def getValue(key: String)(implicit appConfig: AppConfig): String = {
    sys.props.get(key).getOrElse(appConfig.servicesConfig.getString(key))
  }

  def getValue(featureSwitch: FeatureSwitch)(implicit appConfig: AppConfig): String = {
    getValue(featureSwitch.name)
  }

  private val versionRegex = """(\d)\.\d""".r

  def isVersionEnabled(version: String)(implicit appConfig: AppConfig): Boolean = {
    val versionNoIfPresent: Option[String] =
      version match {
        case versionRegex(v) => Some(v)
        case _               => None
      }

    val enabled = for {
      versionNo <- versionNoIfPresent
      enabled   <- getValueBool(s"version-$versionNo.enabled")
    } yield enabled

    enabled.getOrElse(false)
  }

  def isEnabled(featureSwitch: FeatureSwitch)(implicit appConfig: AppConfig): Boolean = {
    getValue(featureSwitch).toBoolean
  }

  def isDisabled(featureSwitch: FeatureSwitch)(implicit appConfig: AppConfig): Boolean = {
    !getValue(featureSwitch).toBoolean
  }

  def setValue(key: String, value: String): SystemProperties = {
    sys.props += key -> value
  }

  def setValue(featureSwitch: FeatureSwitch, value: String): SystemProperties = {
    setValue(featureSwitch.name, value)
  }

  def resetValue(key: String): SystemProperties = {
    sys.props -= key
  }

  def resetValue(featureSwitch: FeatureSwitch): SystemProperties = {
    resetValue(featureSwitch.name)
  }

  def enable(featureSwitch: FeatureSwitch): SystemProperties = {
    logger.debug(s"[FeatureToggleSupport][enable] ${featureSwitch.name} enabled")
    setValue(featureSwitch, true.toString)
  }

  def disable(featureSwitch: FeatureSwitch): SystemProperties = {
    logger.debug(s"[FeatureToggleSupport][enable] ${featureSwitch.name} disabled")
    setValue(featureSwitch, false.toString)
  }

}
object FeatureToggleSupport extends FeatureToggleSupport
