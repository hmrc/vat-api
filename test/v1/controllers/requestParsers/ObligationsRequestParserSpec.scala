/*
 * Copyright 2021 HM Revenue & Customs
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
import v1.models.domain.Vrn
import v1.mocks.validators.MockObligationsValidator
import v1.models.errors._
import v1.models.request.obligations.{ObligationsRawData, ObligationsRequest}

class ObligationsRequestParserSpec extends UnitSpec {

  trait Test extends MockObligationsValidator {
    lazy val parser = new ObligationsRequestParser(mockValidator)
  }

  implicit val correlationId: String = "a1e8057e-fbbc-47a8-a8b4-78d9f015c253"
  private val validVrn = "123456789"
  private val validFromDate = "2020-01-01"
  private val validToDate = "2020-03-31"

  private val invalidVrn = "NotAValidVrn"
  private val invalidFrom = "2019-79-35"
  private val invalidTo = "2018-98-03"

  "parsing a retrieve obligations request" should {
    "return a retrieve obligations request" when {
      "valid data is provided" in new Test {

        MockVrnValidator.validate(ObligationsRawData(validVrn, Some(validFromDate), Some(validToDate), Some("F")))
          .returns(Nil)

        parser.parseRequest(ObligationsRawData(validVrn, Some(validFromDate), Some(validToDate), Some("F"))) shouldBe
          Right(ObligationsRequest(Vrn(validVrn), Some(validFromDate), Some(validToDate), Some("F")))
      }

      "valid data is provided with omissions" in new Test {

        MockVrnValidator.validate(ObligationsRawData(validVrn, None, None, Some("0")))
          .returns(Nil)

        parser.parseRequest(ObligationsRawData(validVrn, None, None, Some("0"))) shouldBe
          Right(ObligationsRequest(Vrn(validVrn), None, None, Some("0")))
      }
    }

    "return an error" when {
      "invalid VRN is provided" in new Test {
        MockVrnValidator.validate(ObligationsRawData(invalidVrn, Some(validFromDate), Some(validToDate), Some("F")))
          .returns(List(VrnFormatError))

        parser.parseRequest(ObligationsRawData(invalidVrn, Some(validFromDate), Some(validToDate), Some("F"))) shouldBe
          Left(ErrorWrapper(correlationId, VrnFormatError, None))
      }

      "invalid from date is provided" in new Test {
        MockVrnValidator.validate(ObligationsRawData(validVrn, Some(invalidFrom), Some(validToDate), Some("F")))
          .returns(List(InvalidFromError))

        parser.parseRequest(ObligationsRawData(validVrn, Some(invalidFrom), Some(validToDate), Some("F"))) shouldBe
          Left(ErrorWrapper(correlationId, InvalidFromError, None))
      }

      "invalid to date is provided" in new Test {
        MockVrnValidator.validate(ObligationsRawData(validVrn, Some(validFromDate), Some(invalidTo), Some("F")))
          .returns(List(InvalidToError))

        parser.parseRequest(ObligationsRawData(validVrn, Some(validFromDate), Some(invalidTo), Some("F"))) shouldBe
          Left(ErrorWrapper(correlationId, InvalidToError, None))
      }

      "invalid status is provided" in new Test {
        MockVrnValidator.validate(ObligationsRawData(validVrn, Some(validFromDate), Some(validToDate), Some("NotAStatus")))
          .returns(List(InvalidStatusError))

        parser.parseRequest(ObligationsRawData(validVrn, Some(validFromDate), Some(validToDate), Some("NotAStatus"))) shouldBe
          Left(ErrorWrapper(correlationId, InvalidStatusError, None))
      }

      "multiple request parameters supplied are invalid" in new Test {
        MockVrnValidator.validate(ObligationsRawData(invalidVrn, Some(invalidFrom), Some(invalidTo), Some("NotAStatus")))
          .returns(List(VrnFormatError))

        parser.parseRequest(ObligationsRawData(invalidVrn, Some(invalidFrom), Some(invalidTo), Some("NotAStatus"))) shouldBe
          Left(ErrorWrapper(correlationId, VrnFormatError, None))
      }

      "omissions are made without status('O')" in new Test {
        MockVrnValidator.validate(ObligationsRawData(validVrn, None, None, Some("NotAStatus")))
          .returns(List(InvalidStatusError))

        parser.parseRequest(ObligationsRawData(validVrn, None, None, Some("NotAStatus"))) shouldBe
        Left(ErrorWrapper(correlationId, InvalidStatusError, None))
      }
    }
  }
}
