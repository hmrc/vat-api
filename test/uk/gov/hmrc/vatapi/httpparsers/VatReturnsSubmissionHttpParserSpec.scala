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

///*
// * Copyright 2018 HM Revenue & Customs
// *
// * Licensed under the Apache License, Version 2.0 (the "License");
// * you may not use this file except in compliance with the License.
// * You may obtain a copy of the License at
// *
// *     http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// */
//
//package uk.gov.hmrc.vatapi.httpparsers
//
//import org.scalatest.EitherValues
//import play.api.http.Status._
//import play.api.libs.json.Json
//import uk.gov.hmrc.http.HttpResponse
//import uk.gov.hmrc.vatapi.UnitSpec
//import uk.gov.hmrc.vatapi.assets.TestConstants.VatReturn._
//import uk.gov.hmrc.vatapi.httpparsers.VatReturnsSubmissionHttpParser.VatReturnsSubmissionOutcomeReads.read
//
//
//class VatReturnsSubmissionHttpParserSpec extends UnitSpec with EitherValues {
//
//    val successResponse = HttpResponse(OK, responseJson = Some(Json.toJson(vatReturnsDes)))
//    val successBadJsonResponse = HttpResponse(OK, responseJson = Some(Json.toJson("{}")))
//    val failureResponse = HttpResponse(BAD_REQUEST, responseJson = Some(Json.toJson("{}")))
//
//  "NrsSubmissionOutcome#read" when {
//    "the response is OK" should {
//      "return NrsData" when {
//        "the Json returned is valid" in {
//          read("", "", successResponse).right.value shouldBe vatReturnsDes
//        }
//
//      }
//      "return NrsError" when {
//        "the Json returned is not valid" in {
//          read("", "", successBadJsonResponse).left.value shouldBe FailedVatReturnSubmission(INTERNAL_SERVER_ERROR)
//        }
//      }
//    }
//
//    "the response status is not OK" should {
//      "return NrsError" in {
//        read("", "", successBadJsonResponse).left.value shouldBe FailedVatReturnSubmission(BAD_REQUEST)
//      }
//    }
//  }
//
//}
