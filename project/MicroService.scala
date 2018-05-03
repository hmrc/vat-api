import play.routes.compiler.StaticRoutesGenerator
import sbt.Keys._
import sbt.Tests.{Group, SubProcess}
import sbt.{Def, _}
import scoverage.ScoverageKeys
import uk.gov.hmrc.sbtdistributables.SbtDistributablesPlugin._

trait MicroService {

  import uk.gov.hmrc._
  import DefaultBuildSettings._
  import TestPhases._
  import play.sbt.routes.RoutesKeys.routesGenerator

  val appName: String

  lazy val appDependencies: Seq[ModuleID] = ???
  lazy val plugins: Seq[Plugins] = Seq(play.sbt.PlayScala)
  lazy val playSettings: Seq[Setting[_]] = Seq.empty

  lazy val FuncTest = config("func") extend Test

  lazy val scoverageSettings: Seq[Def.Setting[_]] = {

    Seq(
      ScoverageKeys.coverageExcludedPackages := "<empty>;.*(Reverse|BuildInfo|Routes).*",
      ScoverageKeys.coverageMinimum := 80,
      ScoverageKeys.coverageFailOnMinimum := true,
      ScoverageKeys.coverageHighlighting := true
    )
  }

  lazy val microservice = Project(appName, file("."))
    .enablePlugins(Seq(play.sbt.PlayScala) ++ plugins: _*)
    .settings(playSettings: _*)
    .settings(scalaSettings: _*)
    .settings(publishingSettings: _*)
    .settings(scoverageSettings: _*)
    .settings(defaultSettings(): _*)
    .settings(
      targetJvm := "jvm-1.8",
      scalaVersion := "2.11.11",
      scalacOptions ++= Seq(
        // FIXME: Uncomment fatal-warnings will make compilation fail because of usage of Play.current instead of DI
        // Choose a DI strategy and uncomment
        //                            "-Xfatal-warnings",
        "-deprecation",
        "-encoding",
        "UTF-8",
        "-unchecked",
        "-language:postfixOps",
        "-language:implicitConversions",
        "-Ywarn-numeric-widen",
        "-Yno-adapted-args",
        "-Ypartial-unification"),
      libraryDependencies ++= appDependencies,
      parallelExecution in Test := false,
      fork in Test := false,
      javaOptions in Test += "-Dlogger.resource=logback-test.xml",
      retrieveManaged := true,
      evictionWarningOptions in update := EvictionWarningOptions.default.withWarnScalaVersionEviction(false)
    )
    .configs(FuncTest)
    .settings(inConfig(FuncTest)(Defaults.testSettings): _*)
    .settings(Keys.fork in FuncTest := false,
      unmanagedSourceDirectories in FuncTest := Seq((baseDirectory in FuncTest).value / "func"),
      unmanagedClasspath in FuncTest += baseDirectory.value / "resources",
      unmanagedClasspath in Runtime += baseDirectory.value / "resources",
      unmanagedResourceDirectories in FuncTest += baseDirectory.value,
      unmanagedResourceDirectories in Compile += baseDirectory.value / "resources",
      addTestReportOption(FuncTest, "int-test-reports"),
      testGrouping in FuncTest := oneForkedJvmPerTest((definedTests in FuncTest).value),
      parallelExecution in FuncTest := false,
      routesGenerator := StaticRoutesGenerator)
    .settings(resolvers += Resolver.bintrayRepo("hmrc", "releases"), resolvers += Resolver.jcenterRepo, resolvers += Resolver.sonatypeRepo("snapshots"))
}

private object TestPhases {

  def oneForkedJvmPerTest(tests: Seq[TestDefinition]): Seq[Group] =
    tests map { test =>
      Group(test.name, Seq(test), SubProcess(ForkOptions(runJVMOptions = Seq("-Dtest.name=" + test.name, "-Dlogger.resource=logback-test.xml"))))
    }
}
