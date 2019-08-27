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

package uk.gov.hmrc.vatapi.controllers.definition

import uk.gov.hmrc.vatapi.controllers.definition.APIStatus.APIStatus

case class Definition(scopes: Seq[Scope],
                      api: APIDefinition)

case class APIDefinition(
                          name: String,
                          description: String,
                          context: String,
                          versions: Seq[APIVersion],
                          requiresTrust: Option[Boolean]) {

  require(name.nonEmpty, s"name is required")
  require(context.nonEmpty, s"context is required")
  require(description.nonEmpty, s"description is required")
  require(versions.nonEmpty, s"at least one version is required")
  require(uniqueVersions, s"version numbers must be unique")
  versions.foreach(version => {
    require(version.version.nonEmpty, s"version is required")
  })

  private def uniqueVersions = {
    !versions.map(_.version).groupBy(identity).mapValues(_.size).exists(_._2 > 1)
  }

}

case class Scope(key: String,
                 name: String,
                 description: String)

case class APIVersion(
                       version: String,
                       access: Option[Access] = None,
                       status: APIStatus,
                       endpointsEnabled: Boolean)


case class Access(`type`: String, whitelistedApplicationIds: Seq[String])

case class Parameter(name: String, required: Boolean = false)

object APIStatus extends Enumeration {
  type APIStatus = Value
  val ALPHA, BETA, STABLE, DEPRECATED, RETIRED = Value
}
