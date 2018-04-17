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

package uk.gov.hmrc.vatapi.auth
import uk.gov.hmrc.auth.core.AffinityGroup
import AuthConstants._

sealed trait AuthContext {
  val affinityGroup: String
  val agentCode: Option[String]
  val agentReference: Option[String]
  val userType: String
}

case object Organisation extends AuthContext {
  override val affinityGroup: String = ORGANISATION
  override val agentCode: Option[String] = None
  override val agentReference: Option[String] = None
  override val userType = "OrgVatPayer"
}

case object Individual extends AuthContext {
  override val affinityGroup: String = INDIVIDUAL
  override val agentCode: Option[String] = None
  override val agentReference: Option[String] = None
  override val userType = "IndVatPayer"
}

case class Agent(override val agentCode: Option[String], override val agentReference: Option[String]) extends AuthContext {
  override val affinityGroup: String = "agent"
  override val userType = "Agent"
}

case class VATAuthEnrolments(enrolmentToken: String, identifier: String, authRule: Option[String] = None)

object AffinityGroupToAuthContext {
  def authContext(affinityGroup: AffinityGroup) = {
    affinityGroup.getClass.getSimpleName.dropRight(1) match {
      case ORGANISATION => Organisation
      case INDIVIDUAL => Individual
    }
  }
}

object AuthConstants {
  val ORGANISATION = "Organisation"
  val INDIVIDUAL = "Individual"
}

