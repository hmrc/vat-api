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
    "uk.gov.hmrc"       %% "bootstrap-backend-play-28" % "5.3.0",
    "uk.gov.hmrc"       %% "domain"            % "5.10.0-play-26",
    "org.typelevel"     %% "cats-core"         % "2.6.1",
    "com.chuusai"       %% "shapeless"         % "2.4.0-M1",
    "com.typesafe.play" %% "play-json-joda"    % "2.7.3",
    "org.json4s"        %% "json4s-native"     % "3.7.0-M7",
    "org.json4s"        %% "json4s-ext"        % "3.7.0-M7",
    "com.github.ghik"   %  "silencer-lib"      % "1.6.0" % Provided cross CrossVersion.full,
    compilerPlugin("com.github.ghik" % "silencer-plugin" % "1.6.0" cross CrossVersion.full)
  )

  def test(scope: String = "test, func"): Seq[sbt.ModuleID] = Seq(
    "org.scalatest"          %% "scalatest"                % "3.2.9"              % scope,
    "com.typesafe.play"      %% "play-test"                % PlayVersion.current  % scope,
    "uk.gov.hmrc"            %% "hmrctest"                 % "3.10.0-play-26"     % scope,
    "com.vladsch.flexmark"   %  "flexmark-all"             % "0.36.8"             % scope,
    "org.scalamock"          %% "scalamock"                % "5.1.0"              % scope,
    "org.scalacheck"         %% "scalacheck"               % "1.15.4"             % scope,
    "org.scalatestplus.play" %% "scalatestplus-play"       % "5.1.0"              % scope,
    "org.scalatestplus"      %% "scalatestplus-mockito"    % "1.0.0-M2"           % scope,
    "com.github.tomakehurst" %  "wiremock-jre8"            % "2.27.2"             % scope,
    "org.skyscreamer"        %  "jsonassert"               % "1.5.0"              % scope,
    "com.jayway.restassured" %  "rest-assured"             % "2.9.0"              % scope,
    "org.mockito"            %  "mockito-core"             % "3.6.28"             % scope,
    "org.scoverage"          %% "scalac-scoverage-runtime" % "1.4.2"              % scope,
    "com.miguno.akka"        %% "akka-mock-scheduler"      % "0.5.5"              % scope
  )
}