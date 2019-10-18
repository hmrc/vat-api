/*
 * Copyright 2018 HM Revenue & Customs
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

import scoverage.ScoverageKeys
import uk.gov.hmrc.DefaultBuildSettings.{addTestReportOption, defaultSettings, scalaSettings}
import uk.gov.hmrc.sbtdistributables.SbtDistributablesPlugin.publishingSettings

val appName = "vat-api"

lazy val playSettings: Seq[Setting[_]] = Seq(
  routesImport += "uk.gov.hmrc.vatapi.resources.Binders._"
)

lazy val FuncTest = config("func") extend Test

lazy val scoverageSettings: Seq[Def.Setting[_]] = {

  Seq(
    ScoverageKeys.coverageExcludedPackages := "<empty>;.*(Reverse|BuildInfo|Routes).*",
    ScoverageKeys.coverageMinimum := 75.00,
    ScoverageKeys.coverageFailOnMinimum := true,
    ScoverageKeys.coverageHighlighting := true
  )
}

lazy val microservice = Project(appName, file("."))
  .enablePlugins(play.sbt.PlayScala, SbtAutoBuildPlugin, SbtGitVersioning, SbtDistributablesPlugin, SbtArtifactory)
  .settings(
    libraryDependencies ++= AppDependencies.compile ++ AppDependencies.test(),
    dependencyOverrides ++= AppDependencies.overrides,
    retrieveManaged := true,
    evictionWarningOptions in update := EvictionWarningOptions.default.withWarnScalaVersionEviction(false),
    scalaVersion := "2.11.12"
  )
  .settings(majorVersion := 1)
  .settings(playSettings: _*)
  .settings(scalaSettings: _*)
  .settings(publishingSettings: _*)
  .settings(scoverageSettings: _*)
  .settings(defaultSettings(): _*)
  .configs(FuncTest)
  .settings(inConfig(FuncTest)(Defaults.testSettings): _*)
  .settings(Keys.fork in FuncTest := false,
    unmanagedSourceDirectories in FuncTest := Seq((baseDirectory in FuncTest).value / "func"),
    unmanagedClasspath in FuncTest += baseDirectory.value / "resources",
    unmanagedClasspath in Runtime += baseDirectory.value / "resources",
    unmanagedResourceDirectories in FuncTest += baseDirectory.value,
    unmanagedResourceDirectories in Compile += baseDirectory.value / "resources",
    addTestReportOption(FuncTest, "int-test-reports"),
    testGrouping in FuncTest := FuncTestPhases.oneForkedJvmPerTest((definedTests in FuncTest).value),
    parallelExecution in FuncTest := false,
    routesGenerator := StaticRoutesGenerator)
  .settings(resolvers += Resolver.bintrayRepo("hmrc", "releases"), resolvers += Resolver.jcenterRepo, resolvers += Resolver.sonatypeRepo("snapshots"))
  .settings(PlayKeys.playDefaultPort := 9675)
