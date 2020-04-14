/*
 * Copyright 2020 HM Revenue & Customs
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

package v1.services

import play.api.libs.json.JsResultException
import uk.gov.hmrc.auth.core.AffinityGroup.{Agent, Individual, Organisation}
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals.{affinityGroup, agentCode, allEnrolments}
import uk.gov.hmrc.auth.core.retrieve.{Retrieval, ~}
import v1.mocks.connectors.MockAuthConnector
import v1.models.auth.UserDetails
import v1.models.errors.{DownstreamError, ForbiddenDownstreamError, LegacyUnauthorisedError, MtdError}

import scala.collection.Seq
import scala.concurrent.Future

class EnrolmentsAuthServiceSpec extends ServiceSpec {

  trait Test extends MockAuthConnector {

    val service: EnrolmentsAuthService = new EnrolmentsAuthService(mockAuthConnector)

    val authRetrievalsAffinity: Retrieval[Option[AffinityGroup] ~ Enrolments] = affinityGroup and allEnrolments
    val authRetrievalsAgentCode: Retrieval[Option[String] ~ Enrolments] = agentCode and allEnrolments

    val predicate: Enrolment = Enrolment("HMRC-MTD-VAT")
      .withIdentifier("VRN", "123456789")
      .withDelegatedAuthRule("mtd-vat-auth")
  }

  "authorised" when {

    val enrolments: Enrolments =
      Enrolments(
        enrolments = Set(
          Enrolment(
            key = "HMRC-MTD-VAT",
            identifiers = Seq(
              EnrolmentIdentifier("VRN", "123456789")
            ),
            state = "Active"
          ),
          Enrolment(
            key = "HMRC-AS-AGENT",
            identifiers = Seq(
              EnrolmentIdentifier("AgentReferenceNumber", "987654321")
            ),
            state = "Active"
          )
        )
      )

    "the user is an authorised individual" should {
      "return the 'Individual' user type in the user details" in new Test {

        val retrievalsResultAffinity = new ~(Some(Individual), enrolments)
        val retrievalsResultAgentCode = new ~(Some("agentCode"), enrolments)

        val expected = Right(UserDetails("Individual", None, ""))

        MockAuthConnector.authorised(predicate, authRetrievalsAffinity)
          .returns(Future.successful(retrievalsResultAffinity))

        MockAuthConnector.authorised(AffinityGroup.Individual and Enrolment("HMRC-MTD-VAT"), authRetrievalsAgentCode)
          .returns(Future.successful(retrievalsResultAgentCode))

        private val result = await(service.authorised(predicate))

        result shouldBe expected
      }
    }

    "the user is an authorised organisation" should {
      "return the 'Organisation' user type in the user details" in new Test {

        val retrievalsResultAffinity = new ~(Some(Organisation), enrolments)
        val retrievalsResultAgentCode = new ~(Some("agentCode"), enrolments)

        val expected = Right(UserDetails("Organisation", None, ""))

        MockAuthConnector.authorised(predicate, authRetrievalsAffinity)
          .returns(Future.successful(retrievalsResultAffinity))

        MockAuthConnector.authorised(AffinityGroup.Organisation and Enrolment("HMRC-MTD-VAT"), authRetrievalsAgentCode)
          .returns(Future.successful(retrievalsResultAgentCode))

        private val result = await(service.authorised(predicate))

        result shouldBe expected
      }
    }

    "the user is an authorised agent" should {
      "return the 'Agent' user type in the user details" in new Test {

        val retrievalsResultAffinity = new ~(Some(Agent), enrolments)
        val retrievalsResultAgentCode = new ~(Some("agentCode"), enrolments)

        val expected = Right(UserDetails("Agent", Some("987654321"), ""))

        MockAuthConnector.authorised(predicate, authRetrievalsAffinity)
          .returns(Future.successful(retrievalsResultAffinity))

        MockAuthConnector.authorised(AffinityGroup.Agent and Enrolment("HMRC-MTD-VAT"), authRetrievalsAgentCode)
          .returns(Future.successful(retrievalsResultAgentCode))

        MockAuthConnector.authorised(AffinityGroup.Agent and Enrolment("HMRC-AS-AGENT"), authRetrievalsAgentCode)
          .returns(Future.successful(retrievalsResultAgentCode))

        private val result = await(service.authorised(predicate))

        result shouldBe expected
      }
    }

    "the user belongs to an unsupported affinity group" should {
      "return an unauthorised error" in new Test {

        case object OtherAffinity extends AffinityGroup

        val retrievalsResultAffinity = new ~(Some(OtherAffinity), enrolments)

        MockAuthConnector.authorised(predicate, authRetrievalsAffinity)
          .returns(Future.successful(retrievalsResultAffinity))

        private val result = await(service.authorised(predicate))

        result shouldBe Left(LegacyUnauthorisedError)
      }
    }

    "an exception occurs during enrolment authorisation" must {
      "map the exceptions correctly" when {

        def serviceException(exception: RuntimeException, mtdError: MtdError): Unit = {
          s"the exception '${exception.getClass.getSimpleName}' occurs" should {
            s"return the MtdError '${mtdError.getClass.getSimpleName}'" in new Test {

              MockAuthConnector.authorised(predicate, authRetrievalsAffinity)
                .returns(Future.failed(exception))

              private val result = await(service.authorised(predicate))

              result shouldBe Left(mtdError)
            }
          }
        }

        case class UnmappedException(msg: String = "Some text") extends AuthorisationException(msg)

        val authServiceErrorMap: Seq[(RuntimeException, MtdError)] =
          Seq(
            (InsufficientEnrolments(), LegacyUnauthorisedError),
            (InsufficientConfidenceLevel(), LegacyUnauthorisedError),
            (JsResultException(Seq.empty), ForbiddenDownstreamError),
            (UnmappedException(), DownstreamError)
          )

        authServiceErrorMap.foreach(args => (serviceException _).tupled(args))
      }
    }

    "the arn and vrn are missing from the authorisation response" should {
      "not throw an error" in new Test {

        val retrievalsResultAffinity = new ~(Some(Agent), Enrolments(Set.empty[Enrolment]))
        val retrievalsResultAgentCode = new ~(Some("agentCode"), Enrolments(Set.empty[Enrolment]))

        val expected = Right(UserDetails("Agent", None, ""))

        MockAuthConnector.authorised(predicate, authRetrievalsAffinity)
          .returns(Future.successful(retrievalsResultAffinity))

        MockAuthConnector.authorised(AffinityGroup.Agent and Enrolment("HMRC-MTD-VAT"), authRetrievalsAgentCode)
          .returns(Future.successful(retrievalsResultAgentCode))

        MockAuthConnector.authorised(AffinityGroup.Agent and Enrolment("HMRC-AS-AGENT"), authRetrievalsAgentCode)
          .returns(Future.successful(retrievalsResultAgentCode))

        private val result = await(service.authorised(predicate))

        result shouldBe expected
      }
    }
  }

  "getAgentReferenceFromEnrolments" when {
    "a valid enrolment with an arn exists" should {
      "return the expected client ARN" in new Test {

        val arn: String = "123456789"

        val enrolments: Enrolments =
          Enrolments(
            enrolments = Set(
              Enrolment(
                key = "HMRC-AS-AGENT",
                identifiers = Seq(EnrolmentIdentifier("AgentReferenceNumber", arn)),
                state = "Active"
              )
            )
          )

        service.getAgentReferenceFromEnrolments(enrolments) shouldBe Some(arn)
      }
    }

    "a valid enrolment with an arn does not exist" should {
      "return None" in new Test {

        val enrolments: Enrolments =
          Enrolments(
            enrolments = Set(
              Enrolment(
                key = "HMRC-AS-AGENT",
                identifiers = Seq(EnrolmentIdentifier("SomeOtherIdentifier", "id")),
                state = "Active"
              )
            )
          )

        service.getAgentReferenceFromEnrolments(enrolments) shouldBe None
      }
    }
  }

  "getClientReferenceFromEnrolments" when {
    "a valid enrolment with a VRN exists" should {
      "return the expected VRN" in new Test {

        val vrn: String = "987654321"
        val enrolments: Enrolments =
          Enrolments(
            enrolments = Set(
              Enrolment(
                key = "HMRC-MTD-VAT",
                identifiers = Seq(EnrolmentIdentifier("VRN", vrn)),
                state = "Active"
              )
            )
          )

        service.getClientReferenceFromEnrolments(enrolments) shouldBe Some(vrn)
      }
    }

    "a valid enrolment with a VRN does not exist" should {
      "return None" in new Test {

        val enrolments: Enrolments =
          Enrolments(
            enrolments = Set(
              Enrolment(
                key = "HMRC-MTD-VAT",
                identifiers = Seq(EnrolmentIdentifier("anIdentifier", "id")),
                state = "Active"
              )
            )
          )

        service.getClientReferenceFromEnrolments(enrolments) shouldBe None
      }
    }
  }
}
