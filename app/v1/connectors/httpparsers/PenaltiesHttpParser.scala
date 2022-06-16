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
import play.api.libs.json.{JsError, JsSuccess}
import uk.gov.hmrc.http.{HttpReads, HttpResponse}
import utils.Logging
import v1.models.response.penalties.PenaltiesResponse

object PenaltiesHttpParser extends Logging {

  type PenaltiesHttpResponse = Either[ErrorResponse, PenaltiesResponse]

  implicit object PenaltiesHttpReads extends HttpReads[PenaltiesHttpResponse] {

    //TODO response may need to be wrapped in a ResponseWrapper if Payments team decide to pass the correlation id through
    def read(method: String, url: String, response: HttpResponse): PenaltiesHttpResponse = {
      response.status match {
        case OK => response.json.validate[PenaltiesResponse] match {
          case JsSuccess(model, _) => Right(model)
          case JsError(errors) =>
            logger.error(s"[PenaltiesHttpParser][read] invalid JSON errors: $errors")
            Left(InvalidJson)
        }
        case BAD_REQUEST =>
          logger.error(s"[PenaltiesHttpParser][read] Invalid VRN ${response.body}")
          Left(InvalidVrn)
        case NOT_FOUND =>
          logger.error(s"[PenaltiesHttpParser][read] VRN could not be found ${response.body}")
          Left(VrnNotFound)
        //TODO more error handling can be added once scenarios confirmed by Payments team
        case status =>
          logger.error(s"[PenaltiesHttpParser][read] unexpected response: status: $status")
          Left(UnexpectedFailure(status, s"unexpected response: status: $status"))
      }
    }
  }
}

trait ErrorResponse {
  val status: Int
  val body: String
}

case object InvalidJson extends ErrorResponse {
  override val status: Int = INTERNAL_SERVER_ERROR
  override val body = "Invalid JSON received"
}

case object InvalidVrn extends ErrorResponse {
  override val status: Int = BAD_REQUEST
  override val body = "VRN provided is invalid"
}

case object VrnNotFound extends ErrorResponse {
  override val status: Int = NOT_FOUND
  override val body = "VRN could not be found"
}

case class UnexpectedFailure(override val status: Int, override val body: String) extends ErrorResponse

