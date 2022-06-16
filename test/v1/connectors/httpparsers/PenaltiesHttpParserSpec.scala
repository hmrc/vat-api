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

package v1.connectors.httpparsers

import support.UnitSpec
import uk.gov.hmrc.http.HttpResponse
import v1.connectors.httpparsers.PenaltiesHttpParser.PenaltiesHttpReads
import play.api.http.Status
import play.api.libs.json.Json
import v1.constants.PenaltiesConstants

class PenaltiesHttpParserSpec extends UnitSpec {

  "PenaltiesHttpParser" when {

    "PenaltiesHttpReads" when {

      "response is OK (200)" when {

        "json is valid" must {

          "return Right(PenaltiesResponse)" in {

            val result = PenaltiesHttpReads.read("", "",
              HttpResponse(
                status = Status.OK,
                json = PenaltiesConstants.testPenaltiesResponseJson,
                headers = Map()
              )
            )

            result shouldBe Right(PenaltiesConstants.testPenaltiesResponse)
          }
        }

        "json is invalid" must {

          "return Left(InvalidJson)" in {

            val result = PenaltiesHttpReads.read("", "",
              HttpResponse(
                status = Status.OK,
                json = Json.obj(
                  "invalid" -> "json"
                ),
                headers = Map()
              )
            )

            result shouldBe Left(InvalidJson)
          }
        }
      }

      "response is BAD_REQUEST (400)" must {

        "return Left(InvalidVrn)" in {

          val result = PenaltiesHttpReads.read("", "",
            HttpResponse(
              status = Status.BAD_REQUEST,
              json = Json.obj(),
              headers = Map()
            )
          )
          result shouldBe Left(InvalidVrn)
        }
      }

      "response is NOT_FOUND (404)" must {

        "return Left(VrnNotFound)" in {

          val result = PenaltiesHttpReads.read("", "",
            HttpResponse(
              status = Status.NOT_FOUND,
              json = Json.obj(),
              headers = Map()
            )
          )
          result shouldBe Left(VrnNotFound)
        }
      }

      "response is INTERNAL_SERVER_ERROR (500)" must {

        "return Left(UnexpectedFailure)" in {

          val status = Status.INTERNAL_SERVER_ERROR

          val result = PenaltiesHttpReads.read("", "",
            HttpResponse(
              status = status,
              json = Json.obj(),
              headers = Map()
            )
          )
          result shouldBe Left(UnexpectedFailure(status, s"unexpected response: status: $status"))
        }
      }
    }
  }

}
