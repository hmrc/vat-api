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

package v1.controllers

import support.UnitSpec
import v1.constants.FinancialDataConstants._
import v1.controllers.requestParsers.FinancialDataRequestParser
import v1.mocks.validators.MockFinancialDataValidator
import v1.models.errors.{ErrorWrapper, VrnFormatError}

class FinancialDataRequestParserSpec extends UnitSpec {

  trait Setup extends MockFinancialDataValidator {
    lazy val parser = new FinancialDataRequestParser(mockValidator)
  }

  "PenaltiesRequestParser" when {

    "raw data is valid" must {

      "return PenaltiesRequest" in new Setup {

        MockVrnValidator.validate(rawData).returns(Nil)

        parser.parseRequest(rawData) shouldBe Right(financialRequest)
      }
    }

    "raw data is not valid" must {

      "return PenaltiesRequest" in new Setup {

        MockVrnValidator.validate(rawData).returns(List(VrnFormatError))

        parser.parseRequest(rawData) shouldBe Left(ErrorWrapper(correlationId, VrnFormatError))
      }
    }
  }
}
