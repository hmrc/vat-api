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

package uk.gov.hmrc.vatapi.httpparsers

import play.api.Logger
import play.api.http.Status._
import play.api.libs.json.{Json, Reads, Writes, _}
import uk.gov.hmrc.http.{HttpReads, HttpResponse}


object NrsSubmissionHttpParser {

  val logger: Logger = Logger(this.getClass)

  type NrsSubmissionOutcome = Either[NrsSubmissionFailure, NRSData]

  implicit object NrsSubmissionOutcomeReads extends HttpReads[NrsSubmissionOutcome] {
    override def read(method: String, url: String, response: HttpResponse): NrsSubmissionOutcome = {
      logger.debug(s"[NrsSubmissionHttpParser][#reads] - Reading NRS Response")
      response.status match {
        case BAD_REQUEST =>
          logger.warn(s"[NrsSubmissionHttpParser][#reads] - BAD_REQUEST status from NRS Response")
          Left(NrsError)
        case ACCEPTED =>
          response.json.validate[NRSData].fold(
            invalid => {
              logger.warn(s"[NrsSubmissionHttpParser][#reads] - Error reading NRS Response: $invalid")
              Left(NrsError)
            },
            valid =>{
              logger.debug(s"[NrsSubmissionHttpParser][#reads] - Retrieved NRS Data: $valid")
              Right(valid)
            }
          )
        case e =>
              logger.debug(s"[NrsSubmissionHttpParser][#reads] - Retrieved NRS status : $e")
              Right(EmptyNrsData)
      }
    }
  }
}

sealed trait NrsSubmissionFailure

case class NRSData(nrSubmissionId: String,
                   cadesTSignature: String,
                   timestamp: String
                  )
object EmptyNrsData extends NRSData("","This has been deprecated - DO NOT USE","")

object NRSData {
  implicit val writes: Writes[NRSData] = Json.writes[NRSData]
  implicit val reads: Reads[NRSData] = {
    (__ \ "nrSubmissionId").read[String].map { id =>
      NRSData.apply(id, "This has been deprecated - DO NOT USE", "")
    }
  }
}

case object NrsError extends NrsSubmissionFailure
