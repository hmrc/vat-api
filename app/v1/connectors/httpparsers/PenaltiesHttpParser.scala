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

import play.api.http.Status._
import play.api.libs.json.{JsError, JsSuccess, JsValue, Json}
import uk.gov.hmrc.http.{HttpReads, HttpResponse}
import utils.Logging
import v1.connectors.Outcome
import v1.models.errors._
import v1.models.outcomes.ResponseWrapper
import v1.models.response.penalties.{PenaltiesErrors, PenaltiesResponse}

object PenaltiesHttpParser extends Logging {

  implicit object PenaltiesHttpReads extends HttpReads[Outcome[PenaltiesResponse]] with HttpParser {

    //TODO change content after user research
    def errorHelper(jsonString: JsValue, status: Int): MtdError = {
      val penaltiesErrors = jsonString.as[PenaltiesErrors]
      val mtdErrorsConvert = penaltiesErrors.failures.map{ error =>
        (error.code, status) match {
          case ("INVALID_IDVALUE", BAD_REQUEST) => PenaltiesInvalidIdValue
          case ("NO_DATA_FOUND", NOT_FOUND) => PenaltiesNotDataFound
          case _ => MtdError(error.code, error.reason)
        }
      }

      val head = mtdErrorsConvert.head
      val error = if(mtdErrorsConvert.tail.isEmpty) {
        head
      } else {
        MtdError("INVALID_REQUEST", "Invalid request penalties", Some(Json.toJson(mtdErrorsConvert)))
      }
       error
    }

    def read(method: String, url: String, response: HttpResponse): Outcome[PenaltiesResponse] = {
      val responseCorrelationId = retrieveCorrelationId(response)
      response.status match {
        case OK => response.json.validate[PenaltiesResponse] match {
          case JsSuccess(model, _) => Right(ResponseWrapper(responseCorrelationId, model))
          case JsError(errors) =>
            logger.error(s"[PenaltiesHttpParser][read] invalid JSON errors: $errors")
            Left(ErrorWrapper(responseCorrelationId, InvalidJson))
        }
        case status =>
          val mtdErrors = errorHelper(response.json, status)
          logger.error(s"[PenaltiesHttpParser][read] status: ${status} with Error ${mtdErrors}")
          Left(ErrorWrapper(responseCorrelationId, mtdErrors))
      }
    }
  }

}
