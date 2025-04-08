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

package v1.controllers.requestParsers

import support.UnitSpec
import v1.constants.CustomerInfoConstants._
import v1.mocks.validators.{MockCustomerInfoValidator}
import v1.models.errors.{ErrorWrapper, VrnFormatError}

class CustomerInfoRequestParserSpec extends UnitSpec {

  trait Setup extends MockCustomerInfoValidator {
    lazy val parser = new CustomerInfoRequestParser(mockValidator)
  }

  "CustomerInfoRequestParser" when {

    "raw data is valid" must {

      "return CustomerInfoRequest" in new Setup {

        MockVrnValidator.validate(rawData).returns(Nil)

        parser.parseRequest(rawData) shouldBe Right(customerInfoRequest)
      }
    }

    "raw data is not valid" must {

      "return CustomerInfoRequest" in new Setup {

        MockVrnValidator.validate(rawData).returns(List(VrnFormatError))

        parser.parseRequest(rawData) shouldBe Left(ErrorWrapper(correlationId, VrnFormatError))
      }
    }
  }
}
