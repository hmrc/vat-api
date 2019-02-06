import play.sbt.routes.RoutesKeys._
import sbt._
import uk.gov.hmrc.SbtAutoBuildPlugin
import uk.gov.hmrc.sbtdistributables.SbtDistributablesPlugin
import uk.gov.hmrc.versioning.SbtGitVersioning

object MicroServiceBuild extends Build with MicroService {

  val appName = "vat-api"

  override lazy val appDependencies: Seq[ModuleID] = AppDependencies()
  override lazy val playSettings: Seq[Setting[_]] = Seq(
    routesImport += "uk.gov.hmrc.vatapi.resources.Binders._"
  )
}

private object AppDependencies {

  import play.sbt.PlayImport._
  import play.core.PlayVersion

  val microserviceBootstrapVersion = "8.7.0"
  val bootstrapPlayVersion = "4.8.0"
  val authClientVersion = "2.19.0-play-25"
  val domainVersion = "5.3.0"
  val hmrcApiVersion = "2.1.0"  // updating this is a breaking change to the project.....
  val jsonJodaVersion = "2.6.13"
  val catsCoreVersion = "1.5.0"
  val json4SnativeVersion = "3.6.3"
  val json4SextVersion = "3.6.3"

  val hmrcTestVersion = "3.2.0"
  val scalaTestVersion = "3.0.5"
  val pegdownVersion = "1.6.0"
  val scalaTestPlusVerson = "2.0.1"
  val wiremockversion = "2.20.0"
  val scalacheckVersion = "1.14.0"
  val jsonAssertVersion = "1.5.0"
  val restAssuredVersion = "2.9.0"
  val mockitoVersion = "2.23.4"
  val scoverageVersion = "1.3.1"

  val compile = Seq(
    ws exclude("org.apache.httpcomponents", "httpclient") exclude("org.apache.httpcomponents", "httpcore"),
    // TODO DELETE
//    "uk.gov.hmrc" %% "microservice-bootstrap" % microserviceBootstrapVersion,
    
    "uk.gov.hmrc" %% "bootstrap-play-25" % bootstrapPlayVersion,
    "uk.gov.hmrc" %% "auth-client" % authClientVersion,
    "uk.gov.hmrc" %% "domain" % domainVersion,
    "uk.gov.hmrc" %% "play-hmrc-api" % hmrcApiVersion,
    "com.typesafe.play" %% "play-json-joda" % jsonJodaVersion,
    "org.typelevel" %% "cats-core" % catsCoreVersion,
    "org.json4s" %% "json4s-native" % json4SnativeVersion,
    "org.json4s" %% "json4s-ext" % json4SextVersion
  )

  trait TestDependencies {
    lazy val scope: String = "test"
    lazy val test: Seq[ModuleID] = ???
  }

  object Test {
    def apply() = new TestDependencies {
      override lazy val test = Seq(
        "uk.gov.hmrc" %% "hmrctest" % hmrcTestVersion % scope,
        "org.scalatest" %% "scalatest" % scalaTestVersion % scope,
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

    }.test
  }
  
  def apply(): Seq[ModuleID] = compile ++ Test()
}
