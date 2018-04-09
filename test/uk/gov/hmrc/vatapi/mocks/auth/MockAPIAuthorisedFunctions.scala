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

package uk.gov.hmrc.vatapi.mocks.auth

import org.mockito.Matchers
import org.mockito.Mockito.{reset, when}
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{BeforeAndAfterEach, Suite}
import play.api.libs.json.{JsArray, Json}
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.auth.core.authorise.RawJsonPredicate
import uk.gov.hmrc.auth.core.retrieve.{Retrieval, ~}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.test.UnitSpec
import uk.gov.hmrc.vatapi.auth.APIAuthorisedFunctions

import scala.concurrent.{ExecutionContext, Future}

trait MockAPIAuthorisedFunctions extends UnitSpec with MockitoSugar with BeforeAndAfterEach {

  self: Suite =>

  val mockAPIAuthorisedFunctions = mock[APIAuthorisedFunctions]

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockAPIAuthorisedFunctions)
    setupMockAuthRetrievalSuccess(testAuthSuccessResponse)
  }

  val testAuthSuccessResponse = new ~(Option(AffinityGroup.Organisation), Enrolments(Set(
    Enrolment("HMRC-MTD-VAT", Seq(EnrolmentIdentifier("VRN", "123456789")), "activated")
  )))

  def setupMockAuthRetrievalSuccess[X,Y](retrievalValue: X~Y): Unit = {
    when(mockAPIAuthorisedFunctions.authorised(RawJsonPredicate(JsArray.apply(Seq(Json.toJson(Enrolment("HMRC-MTD-VAT").withIdentifier("VRN", "123456789")), Json.toJson(ConfidenceLevel.L200))))))
      .thenReturn(
        new mockAPIAuthorisedFunctions.AuthorisedFunction(RawJsonPredicate(JsArray.empty)) {
          override def retrieve[A](retrieval: Retrieval[A]) = new mockAPIAuthorisedFunctions.AuthorisedFunctionWithResult[A](RawJsonPredicate(JsArray.empty), retrieval) {
            override def apply[B](body: A => Future[B])(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[B] = body.apply(retrievalValue.asInstanceOf[A])
          }
        })
  }

  def setupMockAuthorisationException(exception: AuthorisationException = new InsufficientEnrolments()): Unit =
    when(mockAPIAuthorisedFunctions.authorised(Matchers.any()))
      .thenReturn(
        new mockAPIAuthorisedFunctions.AuthorisedFunction(RawJsonPredicate(JsArray.empty)) {
          override def apply[A](body: => Future[A])(implicit hc: HeaderCarrier, executionContext: ExecutionContext) = Future.failed(exception)
          override def retrieve[A](retrieval: Retrieval[A]) = new mockAPIAuthorisedFunctions.AuthorisedFunctionWithResult[A](RawJsonPredicate(JsArray.empty), retrieval) {
            override def apply[B](body: A => Future[B])(implicit hc: HeaderCarrier, executionContext: ExecutionContext): Future[B] = Future.failed(exception)
          }
        })

}
