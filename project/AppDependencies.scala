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

import play.core.PlayVersion
import play.sbt.PlayImport._
import sbt._

object AppDependencies {

  val bootstrapPlayVersion = "1.1.0"
  val domainVersion = "5.6.0-play-26"
  val hmrcApiVersion = "3.4.0-play-26"
  val jsonJodaVersion = "2.6.13"
  val catsCoreVersion = "1.6.0"
  val json4SnativeVersion = "3.6.3"
  val json4SextVersion = "3.6.3"

  val hmrcTestVersion = "3.9.0-play-26"
  val scalaTestVersion = "3.0.5"
  val pegdownVersion = "1.6.0"
  val scalaTestPlusVerson = "3.1.2"
  val wiremockversion = "2.22.0"
  val scalacheckVersion = "1.14.0"
  val jsonAssertVersion = "1.5.0"
  val restAssuredVersion = "2.9.0"
  val mockitoVersion = "2.23.4"
  val scoverageVersion = "1.3.1"

  val compile = Seq(
    ws exclude("org.apache.httpcomponents", "httpclient") exclude("org.apache.httpcomponents", "httpcore"),
    "uk.gov.hmrc" %% "bootstrap-play-26" % bootstrapPlayVersion,
    "uk.gov.hmrc" %% "domain" % domainVersion,
    "uk.gov.hmrc" %% "play-hmrc-api" % hmrcApiVersion,
    "org.typelevel" %% "cats-core" % catsCoreVersion,
    "org.json4s" %% "json4s-native" % json4SnativeVersion,
    "org.json4s" %% "json4s-ext" % json4SextVersion
  )

  def test(scope: String = "test, func"): Seq[sbt.ModuleID] = Seq(
    "uk.gov.hmrc" %% "hmrctest" % hmrcTestVersion % scope,
    "org.scalatest" %% "scalatest" % scalaTestVersion % scope,
    "org.scalamock" %% "scalamock" % "4.2.0" % scope,
    "org.pegdown" % "pegdown" % pegdownVersion % scope,
    "com.typesafe.play" %% "play-test" % PlayVersion.current % scope,
    "org.scalatestplus.play" %% "scalatestplus-play" % scalaTestPlusVerson % scope,
    "com.github.tomakehurst" % "wiremock" % wiremockversion % scope,
    "org.scalacheck" %% "scalacheck" % scalacheckVersion % scope,
    "org.skyscreamer" % "jsonassert" % jsonAssertVersion % scope,
    "com.jayway.restassured" % "rest-assured" % restAssuredVersion % scope,
    "org.mockito" % "mockito-core" % mockitoVersion % scope,
    "org.scoverage" %% "scalac-scoverage-runtime" % scoverageVersion % scope
  )

  // Fixes a transitive dependency clash between wiremock and scalatestplus-play
  val overrides: Set[ModuleID] = {
    val jettyFromWiremockVersion = "9.2.24.v20180105"
    Set(
      "org.eclipse.jetty"           % "jetty-client"       % jettyFromWiremockVersion,
      "org.eclipse.jetty"           % "jetty-continuation" % jettyFromWiremockVersion,
      "org.eclipse.jetty"           % "jetty-http"         % jettyFromWiremockVersion,
      "org.eclipse.jetty"           % "jetty-io"           % jettyFromWiremockVersion,
      "org.eclipse.jetty"           % "jetty-security"     % jettyFromWiremockVersion,
      "org.eclipse.jetty"           % "jetty-server"       % jettyFromWiremockVersion,
      "org.eclipse.jetty"           % "jetty-servlet"      % jettyFromWiremockVersion,
      "org.eclipse.jetty"           % "jetty-servlets"     % jettyFromWiremockVersion,
      "org.eclipse.jetty"           % "jetty-util"         % jettyFromWiremockVersion,
      "org.eclipse.jetty"           % "jetty-webapp"       % jettyFromWiremockVersion,
      "org.eclipse.jetty"           % "jetty-xml"          % jettyFromWiremockVersion,
      "org.eclipse.jetty.websocket" % "websocket-api"      % jettyFromWiremockVersion,
      "org.eclipse.jetty.websocket" % "websocket-client"   % jettyFromWiremockVersion,
      "org.eclipse.jetty.websocket" % "websocket-common"   % jettyFromWiremockVersion
    )
  }
}
