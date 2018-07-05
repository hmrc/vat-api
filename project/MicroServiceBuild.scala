import play.sbt.routes.RoutesKeys._
import sbt._
import uk.gov.hmrc.SbtAutoBuildPlugin
import uk.gov.hmrc.sbtdistributables.SbtDistributablesPlugin
import uk.gov.hmrc.versioning.SbtGitVersioning

object MicroServiceBuild extends Build with MicroService {

  val appName = "vat-api"

  override lazy val plugins: Seq[Plugins] = Seq(
    SbtAutoBuildPlugin, SbtGitVersioning, SbtDistributablesPlugin
  )

  override lazy val appDependencies: Seq[ModuleID] = AppDependencies()
  override lazy val playSettings: Seq[Setting[_]] = Seq(
    routesImport += "uk.gov.hmrc.vatapi.resources.Binders._"
  )
}

private object AppDependencies {

  import play.sbt.PlayImport._
  import play.core.PlayVersion

  val reactiveMongoVersion = "6.2.0"
  val microserviceBootstrapVersion = "6.18.0"
  val authClientVersion = "2.6.0"
  val domainVersion = "5.1.0"
  val hmrcApiVersion = "2.1.0"
  val jsonExtensionsVersion = "0.10.0"
  val jsonJodaVersion = "2.6.7"
  val catsCoreVersion = "1.0.1"
  val json4SnativeVersion = "3.5.3"
  val json4SextVersion = "3.5.3"

  val hmrcTestVersion = "3.0.0"
  val scalaTestVersion = "3.0.1"
  val pegdownVersion = "1.6.0"
  val scalaTestPlusVerson = "2.0.0"
  val wiremockversion = "2.12.0"
  val reactiveMongoTestVersion = "3.1.0"
  val scalacheckVersion = "1.13.4"
  val jsonAssertVersion = "1.4.0"
  val restAssuredVersion = "2.9.0"
  val mockitoVersion = "2.13.0"
  val scoverageVersion = "1.2.0"

  val compile = Seq(
    "uk.gov.hmrc" %% "play-reactivemongo" % reactiveMongoVersion,
    ws exclude("org.apache.httpcomponents", "httpclient") exclude("org.apache.httpcomponents", "httpcore"),
    "uk.gov.hmrc" %% "microservice-bootstrap" % microserviceBootstrapVersion,
    "uk.gov.hmrc" %% "auth-client" % authClientVersion,
    "uk.gov.hmrc" %% "domain" % domainVersion,
    "uk.gov.hmrc" %% "play-hmrc-api" % hmrcApiVersion,
    "ai.x" %% "play-json-extensions" % jsonExtensionsVersion,
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
