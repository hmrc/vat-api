/*
 * Copyright 2021 HM Revenue & Customs
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

import play.core.PlayVersion
import play.sbt.PlayImport._
import sbt._

object AppDependencies {

  val bootstrapVersion = "play-30"

  val compile: Seq[ModuleID] = Seq(
    "uk.gov.hmrc"                  %% s"bootstrap-backend-$bootstrapVersion" % "8.5.0",
    "org.typelevel"                %% "cats-core"                 % "2.10.0",
    "com.chuusai"                  %% "shapeless"                 % "2.4.0-M1",
    "com.fasterxml.jackson.module" %% "jackson-module-scala"      % "2.17.0"

  )

  def test(scope: String = "test, func"): Seq[sbt.ModuleID] = Seq(
    "uk.gov.hmrc"           %% s"bootstrap-test-$bootstrapVersion" % "8.5.0",
    "org.playframework"      %% "play-test"                % PlayVersion.current  % scope,
    "com.vladsch.flexmark"   %  "flexmark-all"             % "0.64.8"             % scope,
    "org.scalamock"          %% "scalamock"                % "5.2.0"              % scope,
    "org.scalacheck"         %% "scalacheck"               % "1.17.0"             % scope,
    "org.scalatestplus.play" %% "scalatestplus-play"       % "7.0.1"              % scope,
    "com.github.pjfanning"   %% "pekko-mock-scheduler"    % "0.6.0"              % scope
  )
}