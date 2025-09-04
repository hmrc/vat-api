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

package support

import config.AppConfig
import org.scalatestplus.play.guice._
import play.api.inject.guice.{ GuiceApplicationBuilder, GuiceableModule }
import play.api.test.Injecting
import play.api.{ Application, Mode }

trait GuiceBox extends UnitSpec with GuiceOneAppPerSuite with Injecting {

  protected lazy val bindModules: Seq[GuiceableModule] = Seq.empty

  override implicit lazy val app: Application = new GuiceApplicationBuilder()
    .bindings(bindModules: _*)
    .overrides()
    .in(Mode.Test)
    .build()

  implicit lazy val appConfig: AppConfig = inject[AppConfig]
}
