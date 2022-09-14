/*
 * Copyright 2022 HM Revenue & Customs
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

package v1.models.errors

import play.api.http.Status
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import support.UnitSpec
import v1.constants.PenaltiesConstants.userDetails
import v1.controllers.UserRequest

class ConnectorErrorSpec extends UnitSpec {

  implicit val userRequest: UserRequest[AnyContentAsEmpty.type] = UserRequest(userDetails,FakeRequest())

  implicit val correlationId: String = "abc123def456"

  "ConnectorError" when {

    "log" must {

      "return correct log message string" in {

        val actualResult: String = ConnectorError.log(
          logContext = "[TestClass][testMethod]",
          vrn = "123456789",
          status = Status.INTERNAL_SERVER_ERROR,
          details = "An error occurred"
        )

        val expectedResult =
          s"[TestClass][testMethod] " +
            s"VRN: 123456789, X-Request-Id: ${userRequest.id.toString}, " +
            s"X-Client-Id: ${userRequest.userDetails.clientId}, errorStatus: ${Status.INTERNAL_SERVER_ERROR.toString}, " +
            s"errorMessage: An error occurred, " +
            s"correlationId: $correlationId"

        actualResult shouldBe expectedResult
      }
    }
  }
}
