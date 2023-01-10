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


object FeatureSwitch {

  val prefix = "feature-switch"

  val featureSwitches: Seq[FeatureSwitch] = Seq(
    AuthFeature,
    PenaltiesEndpointsFeature,
    FinancialDataRamlFeature
  )

  def apply(str: String): FeatureSwitch =
    featureSwitches find (_.name == str) match {
      case Some(switch) => switch
      case None => throw new IllegalArgumentException("Invalid feature switch: " + str)
    }

  def get(string: String): Option[FeatureSwitch] = featureSwitches find (_.name == string)


  sealed trait FeatureSwitch {
    val name: String
    val hint: Option[String] = None
  }

  case object Version1Feature extends FeatureSwitch {
    override val name: String = s"$prefix.version-1.enabled"
  }

  case object AuthFeature extends FeatureSwitch {
    override val name: String = s"$prefix.auth.enabled"
  }

  case object PenaltiesEndpointsFeature extends FeatureSwitch {
    override val name: String = s"$prefix.penaltiesEndpoints.enabled"
  }

  case object FinancialDataRamlFeature extends FeatureSwitch {
    override val name: String = s"$prefix.financialDataRamlFeature.enabled"
  }

}