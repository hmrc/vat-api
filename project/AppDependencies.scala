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

  val compile: Seq[ModuleID] = Seq(
    ws exclude("org.apache.httpcomponents", "httpclient") exclude("org.apache.httpcomponents", "httpcore"),
    "uk.gov.hmrc"                  %% "bootstrap-backend-play-28" % "7.13.0",
    "org.typelevel"                %% "cats-core"                 % "2.9.0",
    "com.chuusai"                  %% "shapeless"                 % "2.4.0-M1",
    "com.fasterxml.jackson.module" %% "jackson-module-scala"      % "2.14.2"
  )

  def test(scope: String = "test, func"): Seq[sbt.ModuleID] = Seq(
    "org.scalatest"          %% "scalatest"                % "3.2.11"             % scope,
    "com.typesafe.play"      %% "play-test"                % PlayVersion.current  % scope,
    "com.vladsch.flexmark"   %  "flexmark-all"             % "0.62.2"             % scope,
    "org.scalamock"          %% "scalamock"                % "5.2.0"              % scope,
    "org.scalacheck"         %% "scalacheck"               % "1.17.0"             % scope,
    "org.scalatestplus.play" %% "scalatestplus-play"       % "5.1.0"              % scope,
    "com.github.tomakehurst" %  "wiremock-jre8"            % "2.35.0"             % scope,
    "com.miguno.akka"        %% "akka-mock-scheduler"      % "0.5.5"              % scope
  )
}