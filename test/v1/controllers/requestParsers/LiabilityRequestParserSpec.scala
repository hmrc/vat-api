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

package v1.controllers.requestParsers

import support.UnitSpec
import uk.gov.hmrc.domain.Vrn
import v1.mocks.validators.MockLiabilityValidator
import v1.models.errors.{ErrorWrapper, InvalidFromError, InvalidToError, VrnFormatError}
import v1.models.request.liability.{LiabilityRawData, LiabilityRequest}

class LiabilityRequestParserSpec extends UnitSpec {

  trait Test extends MockLiabilityValidator {
    lazy val parser = new LiabilitiesRequestParser(mockValidator)
  }

  private val validVrn = "123456789"
  private val validFromDate = "2020-01-01"
  private val validToDate = "2020-03-31"

  private val invalidVrn = "NotAValidVrn"
  private val invalidFrom = "2019-79-35"
  private val invalidTo = "2018-98-03"

  "parsing a retrieve Liability request" should {
    "return a retrieve Liability request" when {
      "valid data is provided" in new Test {

        MockVrnValidator.validate(LiabilityRawData(validVrn, Some(validFromDate), Some(validToDate))).returns(Nil)

        parser.parseRequest(LiabilityRawData(validVrn, Some(validFromDate), Some(validToDate))) shouldBe
          Right(LiabilityRequest(Vrn(validVrn), validFromDate, validToDate))
      }
    }

    "return an error" when {
      "invalid VRN is provided" in new Test {
        MockVrnValidator.validate(LiabilityRawData(invalidVrn, Some(validFromDate), Some(validToDate))).returns(List(VrnFormatError))

        parser.parseRequest(LiabilityRawData(invalidVrn, Some(validFromDate), Some(validToDate))) shouldBe
          Left(ErrorWrapper(None, VrnFormatError, None))
      }

      "invalid from date is provided" in new Test {
        MockVrnValidator.validate(LiabilityRawData(validVrn, Some(invalidFrom), Some(validToDate)))
          .returns(List(InvalidFromError))

        parser.parseRequest(LiabilityRawData(validVrn, Some(invalidFrom), Some(validToDate))) shouldBe
          Left(ErrorWrapper(None, InvalidFromError, None))
      }

      "invalid to date is provided" in new Test {
        MockVrnValidator.validate(LiabilityRawData(validVrn, Some(validFromDate), Some(invalidTo)))
          .returns(List(InvalidToError))

        parser.parseRequest(LiabilityRawData(validVrn, Some(validFromDate), Some(invalidTo))) shouldBe
          Left(ErrorWrapper(None, InvalidToError, None))
      }
    }
  }
}
