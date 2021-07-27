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

resolvers += "Typesafe Releases" at "https://repo.typesafe.com/typesafe/releases/"
resolvers += "HMRC-open-artefacts-maven" at "https://open.artefacts.tax.service.gov.uk/maven2"
resolvers += Resolver.url("HMRC-open-artefacts-ivy", url("https://open.artefacts.tax.service.gov.uk/ivy2"))(Resolver.ivyStylePatterns)

addSbtPlugin(dependency = "uk.gov.hmrc" % "sbt-auto-build" % "3.3.0")
addSbtPlugin(dependency = "uk.gov.hmrc" % "sbt-distributables" % "2.1.0")
addSbtPlugin(dependency = "com.typesafe.play" % "sbt-plugin" % "2.8.8")
addSbtPlugin(dependency = "org.scalastyle" %% "scalastyle-sbt-plugin" % "1.0.0")
addSbtPlugin(dependency = "org.scoverage" % "sbt-scoverage" % "1.8.2")
addSbtPlugin(dependency = "com.timushev.sbt" % "sbt-updates" % "0.5.3")