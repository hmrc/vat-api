/*
 * Copyright 2023 HM Revenue & Customs
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
import uk.gov.hmrc.auth.core.retrieve._
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals._
import v1.fixtures.auth.AuthFixture._
import v1.mocks.connectors.MockAuthConnector
import v1.models.auth.UserDetails
import v1.models.errors.{DownstreamError, ForbiddenDownstreamError, LegacyUnauthorisedError, MtdError}

import java.time.LocalDate
import scala.collection.Seq
import scala.concurrent.Future

class EnrolmentsAuthServiceSpec extends ServiceSpec {

  trait Test extends MockAuthConnector {

    val service: EnrolmentsAuthService = new EnrolmentsAuthService(mockAuthConnector)

    val authRetrievalsAffinity: Retrieval[Option[AffinityGroup] ~ Enrolments] = affinityGroup and allEnrolments
    val authRetrievalsAgentCode: Retrieval[Option[String] ~ Enrolments] = agentCode and allEnrolments

    val authRetrievalsAffinityWithNrs:
      Retrieval[Option[AffinityGroup] ~ Enrolments ~ Option[String] ~ Option[String] ~ Option[String] ~ Option[Credentials] ~ ConfidenceLevel ~ Option[String] ~ Option[String] ~ Option[Name] ~ Option[LocalDate] ~ Option[String] ~ AgentInformation ~ Option[String] ~ Option[CredentialRole] ~ Option[MdtpInformation] ~ Option[String] ~ LoginTimes ~ Option[ItmpName] ~ Option[LocalDate] ~ Option[ItmpAddress]] =
      affinityGroup and allEnrolments and internalId and externalId and agentCode and credentials and confidenceLevel and nino and saUtr and name and dateOfBirth and email and agentInformation and groupIdentifier and credentialRole and mdtpInformation and credentialStrength and loginTimes and itmpName and itmpDateOfBirth and itmpAddress

    val predicate: Enrolment = Enrolment("HMRC-MTD-VAT")
      .withIdentifier("VRN", "123456789")
      .withDelegatedAuthRule("mtd-vat-auth")
  }

  "authorised" when {

    "the user is an authorised individual and v1.nrs check is not required" should {
      "return the 'Individual' user type in the user details" in new Test {

        val retrievalsResultAffinity = new ~(Some(Individual), vatEnrolments)

        val expected = Right(UserDetails("Individual", None, ""))

        MockAuthConnector.authorised(predicate, authRetrievalsAffinity)
          .returns(Future.successful(retrievalsResultAffinity))

        private val result = await(service.authorised(predicate))

        result shouldBe expected
      }
    }

    "the user is an authorised individual and v1.nrs check is required" should {
      "return the 'Individual' user type in the user details" in new Test {

        private val retrievalsResultAffinity = authResponse(indIdentityData, vatEnrolments)

        val expected = Right(userDetails(Individual, AgentInformation(
          agentId = None,
          agentCode = None,
          agentFriendlyName = None)))

        MockAuthConnector.authorised(predicate, authRetrievalsAffinityWithNrs)
          .returns(Future.successful(retrievalsResultAffinity))

        private val result = await(service.authorised(predicate, nrsRequired = true))

        result shouldBe expected
      }
    }

    "the user is an authorised organisation and v1.nrs check is not required" should {
      "return the 'Organisation' user type in the user details" in new Test {

        val retrievalsResultAffinity = new ~(Some(Organisation), vatEnrolments)

        val expected = Right(UserDetails("Organisation", None, ""))

        MockAuthConnector.authorised(predicate, authRetrievalsAffinity)
          .returns(Future.successful(retrievalsResultAffinity))

        private val result = await(service.authorised(predicate))

        result shouldBe expected
      }
    }

    "the user is an authorised organisation and v1.nrs check is required" should {
      "return the 'Organisation' user type in the user details" in new Test {

        private val retrievalsResultAffinity = authResponse(orgIdentityData, vatEnrolments)

        val expected = Right(userDetails(Organisation, AgentInformation(
          agentId = None,
          agentCode = None,
          agentFriendlyName = None)))

        MockAuthConnector.authorised(predicate, authRetrievalsAffinityWithNrs)
          .returns(Future.successful(retrievalsResultAffinity))

        private val result = await(service.authorised(predicate, nrsRequired = true))

        result shouldBe expected
      }
    }

    "the user is an authorised agent and v1.nrs check is not required" should {
      "return the 'Agent' user type in the user details" in new Test {

        val retrievalsResultAffinity = new ~(Some(Agent), agentEnrolments)

        val expected = Right(UserDetails("Agent", Some("987654321"), ""))

        MockAuthConnector.authorised(predicate, authRetrievalsAffinity)
          .returns(Future.successful(retrievalsResultAffinity))

        private val result = await(service.authorised(predicate))

        result shouldBe expected
      }
    }

    "the user is an authorised agent and v1.nrs check is required" should {
      "return the 'Agent' user type in the user details" in new Test {

        private val retrievalsResultAffinity = authResponse(agentIdentityData, agentEnrolments)

        val expected = Right(userDetails(Agent, AgentInformation(
          agentCode = Some("AGENT007"),
          agentFriendlyName = Some("James"),
          agentId = Some("987654321"))))

        MockAuthConnector.authorised(predicate, authRetrievalsAffinityWithNrs)
          .returns(Future.successful(retrievalsResultAffinity))

        private val result = await(service.authorised(predicate, nrsRequired = true))

        result shouldBe expected
      }
    }

    "the user belongs to an unsupported affinity group and v1.nrs check is not required" should {
      "return an unauthorised error" in new Test {

        case object OtherAffinity extends AffinityGroup

        val retrievalsResultAffinity = new ~(Some(OtherAffinity), vatEnrolments)

        MockAuthConnector.authorised(predicate, authRetrievalsAffinity)
          .returns(Future.successful(retrievalsResultAffinity))

        private val result = await(service.authorised(predicate))

        result shouldBe Left(LegacyUnauthorisedError)
      }
    }

    "the user belongs to an unsupported affinity group and v1.nrs check is required" should {
      "return an unauthorised error" in new Test {

        case object OtherAffinity extends AffinityGroup

        private val retrievalsResultAffinity = authResponse(orgIdentityData.copy(affinityGroup = Some(OtherAffinity)), vatEnrolments)

        MockAuthConnector.authorised(predicate, authRetrievalsAffinityWithNrs)
          .returns(Future.successful(retrievalsResultAffinity))

        private val result = await(service.authorised(predicate, nrsRequired = true))

        result shouldBe Left(LegacyUnauthorisedError)
      }
    }

    "an exception occurs during enrolment authorisation and v1.nrs check is not required" must {
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

    "an exception occurs during enrolment authorisation and v1.nrs check is required" must {
      "map the exceptions correctly" when {

        def serviceException(exception: RuntimeException, mtdError: MtdError): Unit = {
          s"the exception '${exception.getClass.getSimpleName}' occurs" should {
            s"return the MtdError '${mtdError.getClass.getSimpleName}'" in new Test {

              MockAuthConnector.authorised(predicate, authRetrievalsAffinityWithNrs)
                .returns(Future.failed(exception))

              private val result = await(service.authorised(predicate, nrsRequired = true))

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

    "the arn and vrn are missing from the authorisation response and v1.nrs check is not required" should {
      "not throw an error" in new Test {

        val retrievalsResultAffinity = new ~(Some(Agent), Enrolments(Set.empty[Enrolment]))

        val expected = Right(UserDetails("Agent", None, ""))

        MockAuthConnector.authorised(predicate, authRetrievalsAffinity)
          .returns(Future.successful(retrievalsResultAffinity))

        private val result = await(service.authorised(predicate))

        result shouldBe expected
      }
    }

    "the arn and vrn are missing from the authorisation response and v1.nrs check is required" should {
      "not throw an error" in new Test {

        private val retrievalsResultAffinity = authResponse(orgIdentityData.copy(affinityGroup = Some(Agent)), vatEnrolments)

        val expected = Right(userDetails(Agent, AgentInformation(
          agentCode = None,
          agentFriendlyName = None,
          agentId = None)))

        MockAuthConnector.authorised(predicate, authRetrievalsAffinityWithNrs)
          .returns(Future.successful(retrievalsResultAffinity))

        private val result = await(service.authorised(predicate, nrsRequired = true))

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
