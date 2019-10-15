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

package uk.gov.hmrc.vatapi.auth

import uk.gov.hmrc.auth.core.{AffinityGroup, Enrolments}
import AuthConstants._
import uk.gov.hmrc.auth.core.retrieve.AgentInformation
import uk.gov.hmrc.vatapi.models.IdentityData

sealed trait AuthContext {
  val affinityGroup: String
  val agentCode: Option[String]
  val agentReference: Option[String]
  val identityData: Option[IdentityData]
}

case class Organisation(override val identityData: Option[IdentityData] = None) extends AuthContext {
  override val affinityGroup: String = ORGANISATION
  override val agentCode: Option[String] = None
  override val agentReference: Option[String] = None
}

case class Individual(override val identityData: Option[IdentityData]) extends AuthContext {
  override val affinityGroup: String = INDIVIDUAL
  override val agentCode: Option[String] = None
  override val agentReference: Option[String] = None
}

case class Agent(override val agentCode: Option[String],
                 override val agentReference: Option[String],
                 override val identityData: Option[IdentityData] = None,
                 agentEnrolments: Enrolments
                ) extends AuthContext {
  override val affinityGroup: String = AGENT
}

case class VATAuthEnrolments(enrolmentToken: String, identifier: String, authRule: Option[String] = None)

object AffinityGroupToAuthContext {
  def authContext(enrolments: Enrolments,
                  affinityGroup: AffinityGroup,
                  identityData: Option[IdentityData],
                  agentInformation: Option[AgentInformation] = None): AuthContext = {
    affinityGroup.getClass.getSimpleName.dropRight(1) match {
      case ORGANISATION => Organisation(identityData)
      case INDIVIDUAL => Individual(identityData)
      case AGENT => agentInformation match {
        case Some(agent) => Agent(agentCode = agent.agentCode, agentReference = agent.agentId, identityData, enrolments)
        case None => Agent(None, None, identityData, enrolments)
      }
    }
  }
}

object AuthConstants {
  val ORGANISATION = "Organisation"
  val INDIVIDUAL = "Individual"
  val AGENT = "Agent"
}

