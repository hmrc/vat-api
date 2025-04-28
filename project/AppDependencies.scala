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
import sbt._

object AppDependencies {

  val bootstrapPlayVersion = "play-30"

  val compile: Seq[ModuleID] = Seq(
    "uk.gov.hmrc"                  %% s"bootstrap-backend-$bootstrapPlayVersion" % "8.6.0",
    "org.typelevel"                %% "cats-core"                 % "2.10.0",
    "com.chuusai"                  %% "shapeless"                 % "2.4.0-M1",
    "com.fasterxml.jackson.module" %% "jackson-module-scala"      % "2.17.0"

  )

  def test: Seq[sbt.ModuleID] = Seq(
    "uk.gov.hmrc"           %% s"bootstrap-test-$bootstrapPlayVersion" % "8.6.0"  % Test,
    "org.playframework"      %% "play-test"                % PlayVersion.current  % Test,
    "com.vladsch.flexmark"   %  "flexmark-all"             % "0.64.8"             % Test,
    "org.scalamock"          %% "scalamock"                % "5.2.0"              % Test,
    "org.scalacheck"         %% "scalacheck"               % "1.17.0"             % Test,
    "org.scalatestplus.play" %% "scalatestplus-play"       % "7.0.1"              % Test,
    "com.github.pjfanning"   %% "pekko-mock-scheduler"    % "0.6.0"              % Test
  )
}