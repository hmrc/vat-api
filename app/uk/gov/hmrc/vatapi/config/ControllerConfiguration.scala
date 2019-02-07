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

import com.typesafe.config.Config
import javax.inject.Inject
import net.ceedubs.ficus.Ficus._
import net.ceedubs.ficus.readers.{StringReader, ValueReader}
import play.api.Configuration
import uk.gov.hmrc.play.config.ControllerConfig

import scala.util.matching.Regex

case class ControllerConfigParams(needsHeaderValidation: Boolean = true,
                                  needsLogging: Boolean = true,
                                  needsAuditing: Boolean = true,
                                  needsTaxYear: Boolean = true)

class ControllerConfiguration @Inject()(configuration: Configuration) extends ControllerConfig {
  private implicit val regexValueReader: ValueReader[Regex] =
    StringReader.stringValueReader.map(_.r)
  private implicit val controllerParamsReader: ValueReader[ControllerConfigParams] =
    ValueReader.relative[ControllerConfigParams] { config =>
      ControllerConfigParams(
        needsHeaderValidation =
          config.getAs[Boolean]("needsHeaderValidation").getOrElse(true),
        needsLogging = config.getAs[Boolean]("needsLogging").getOrElse(true),
        needsAuditing = config.getAs[Boolean]("needsAuditing").getOrElse(true),
        needsTaxYear = config.getAs[Boolean]("needsTaxYear").getOrElse(true)
      )
    }

  lazy val controllerConfigs: Config = configuration.underlying.as[Config]("controllers")

  def controllerParamsConfig(controllerName: String): ControllerConfigParams = {
    controllerConfigs
      .as[Option[ControllerConfigParams]](controllerName)
      .getOrElse(ControllerConfigParams())
  }
}