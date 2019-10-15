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

package uk.gov.hmrc.vatapi.mocks.auth

import org.mockito.{ArgumentMatchers => Matchers}
import org.scalatest.Suite
import play.api.libs.json.JsArray
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.auth.core.authorise.RawJsonPredicate
import uk.gov.hmrc.auth.core.retrieve._
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.test.UnitSpec
import uk.gov.hmrc.vatapi.assets.TestConstants.Auth._
import uk.gov.hmrc.vatapi.auth.APIAuthorisedFunctions
import uk.gov.hmrc.vatapi.mocks.Mock

import scala.concurrent.{ExecutionContext, Future}

trait MockAPIAuthorisedFunctions extends UnitSpec with Mock {

  self: Suite =>

  val mockAPIAuthorisedFunctions: APIAuthorisedFunctions = mock[APIAuthorisedFunctions]

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockAPIAuthorisedFunctions)
    setupMockAuthRetrievalSuccess(testAuthSuccessResponse.organisationResponse)
  }

  object testAuthSuccessResponseWithNrsData {

    import uk.gov.hmrc.vatapi.assets.TestConstants.Auth.vatEnrolment
    val organisationResponse =
      new ~(new ~(new ~(new ~(new ~(new ~(new ~(new ~(new ~(new ~(new ~(new ~(new ~(new ~(new ~(new ~(new ~(
        Option(AffinityGroup.Organisation),
        Enrolments(Set(vatEnrolment))),
        orgIdentityData.internalId),
        orgIdentityData.externalId),
        None),
        orgIdentityData.credentials),
        orgIdentityData.confidenceLevel),
        None),
        None),
        orgIdentityData.name)
        , None),
        orgIdentityData.email),
        orgIdentityData.agentInformation),
        orgIdentityData.groupIdentifier),
        orgIdentityData.credentialRole),
        None),
        orgIdentityData.credentialStrength),
        orgIdentityData.loginTimes
      )

    val individualResponse =
      new ~(new ~(new ~(new ~(new ~(new ~(new ~(new ~(new ~(new ~(new ~(new ~(new ~(new ~(new ~(new ~(new ~(
        Option(AffinityGroup.Individual),
        Enrolments(Set(vatEnrolment))),
        orgIdentityData.internalId),
        orgIdentityData.externalId),
        None),
        orgIdentityData.credentials),
        orgIdentityData.confidenceLevel),
        None),
        None),
        orgIdentityData.name)
        , None),
        orgIdentityData.email),
        orgIdentityData.agentInformation),
        orgIdentityData.groupIdentifier),
        orgIdentityData.credentialRole),
        None),
        orgIdentityData.credentialStrength),
        orgIdentityData.loginTimes
      )

    val agentResponse =
      new ~(new ~(new ~(new ~(new ~(new ~(new ~(new ~(new ~(new ~(new ~(new ~(new ~(new ~(new ~(new ~(new ~(
        Option(AffinityGroup.Agent),
        Enrolments(Set(
          vatEnrolment,
          agentEnrolment)
        )),
        agentIdentityData.internalId),
        agentIdentityData.externalId),
        None),
        agentIdentityData.credentials),
        agentIdentityData.confidenceLevel),
        None),
        None),
        agentIdentityData.name)
        , None),
        agentIdentityData.email),
        agentIdentityData.agentInformation),
        agentIdentityData.groupIdentifier),
        agentIdentityData.credentialRole),
        None),
        agentIdentityData.credentialStrength),
        agentIdentityData.loginTimes
      )
  }

  object testAuthSuccessResponse {
    val organisationResponse = new ~(new ~(
      Option(AffinityGroup.Organisation),
      Enrolments(Set(vatEnrolment))),
      orgIdentityData.agentInformation
    )

    val individualResponse = new ~(new ~(
      Option(AffinityGroup.Individual),
      Enrolments(Set(vatEnrolment))),
      orgIdentityData.agentInformation
    )

    val agentResponse = new ~(new ~(
      Option(AffinityGroup.Agent),
      Enrolments(Set(
        vatEnrolment,
        agentEnrolment))),
      agentIdentityData.agentInformation
    )

  }

  def setupMockAuthRetrievalSuccess[X, Y](retrievalValue: X ~ Y): Unit = {
    when(mockAPIAuthorisedFunctions.authorised(any()))
      .thenReturn(
        new mockAPIAuthorisedFunctions.AuthorisedFunction(RawJsonPredicate(JsArray())) {
          override def retrieve[A](retrieval: Retrieval[A]) = new mockAPIAuthorisedFunctions.AuthorisedFunctionWithResult[A](RawJsonPredicate(JsArray()), retrieval) {
            override def apply[B](body: A => Future[B])(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[B] = body.apply(retrievalValue.asInstanceOf[A])
          }
        })
  }

  def setupMockAuthorisationException(exception: Exception = InsufficientEnrolments()): Unit =
    when(mockAPIAuthorisedFunctions.authorised(any()))
      .thenReturn(
        new mockAPIAuthorisedFunctions.AuthorisedFunction(RawJsonPredicate(JsArray())) {
          override def apply[A](body: => Future[A])(implicit hc: HeaderCarrier, executionContext: ExecutionContext) = Future.failed(exception)

          override def retrieve[A](retrieval: Retrieval[A]) = new mockAPIAuthorisedFunctions.AuthorisedFunctionWithResult[A](RawJsonPredicate(JsArray()), retrieval) {
            override def apply[B](body: A => Future[B])(implicit hc: HeaderCarrier, executionContext: ExecutionContext): Future[B] = Future.failed(exception)
          }
        })

}
