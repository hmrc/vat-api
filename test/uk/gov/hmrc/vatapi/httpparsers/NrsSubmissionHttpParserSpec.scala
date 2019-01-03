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

package uk.gov.hmrc.vatapi.httpparsers

import org.scalatest.EitherValues
import play.api.http.Status._
import play.api.libs.json.Json
import uk.gov.hmrc.http.HttpResponse
import uk.gov.hmrc.vatapi.UnitSpec
import uk.gov.hmrc.vatapi.assets.TestConstants.NRSResponse._
import uk.gov.hmrc.vatapi.httpparsers.NrsSubmissionHttpParser.NrsSubmissionOutcomeReads.read


class NrsSubmissionHttpParserSpec extends UnitSpec with EitherValues {

    val successResponse = HttpResponse(ACCEPTED, responseJson = Some(nrsResponseJson))
    val successBadJsonResponse = HttpResponse(NOT_FOUND, responseJson = Some(Json.toJson("{}")))
    val failureResponse = HttpResponse(BAD_REQUEST, responseJson = Some(Json.toJson("{}")))

  "NrsSubmissionOutcome#read" when {
    "the response is OK" should {
      "return NrsData" when {
        "the Json returned is valid" in {
          read("", "", successResponse).right.value shouldBe nrsClientData.copy(timestamp = "")
        }

      }
      "return NrsError" when {
        "the Json returned is not valid" in {
          read("", "", successBadJsonResponse).right.value shouldBe EmptyNrsData
        }
      }
    }

    "the response status is not OK" should {
      "return NrsError" in {
        read("", "", failureResponse).left.value shouldBe NrsError
      }
    }
  }

}
