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

///*
// * Copyright 2020 HM Revenue & Customs
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
//package v1.connectors.httpparsers
//
//import play.api.Logger
//import play.api.http.Status._
//import play.api.libs.json.Reads
//import play.api.libs.ws.WSResponse
//import v1.nrs.NrsOutcome
//import v1.nrs.models.response.NrsFailure
//
//trait WsReads[A] {
//  def wsRead(response: WSResponse, defaultResult: A): A
//}
//
//object StandardNrsWsParser extends WsParser {
//
//  val logger: Logger = Logger(getClass)
//
//  case class SuccessCode(status: Int) extends AnyVal
//
//  implicit def nrsReads[A: Reads](implicit successCode: SuccessCode = SuccessCode(ACCEPTED)): WsReads[NrsOutcome[A]] =
//    (response: WSResponse, defaultResult: NrsOutcome[A]) => doRead(response, defaultResult) { response =>
//      response.validateJson[A] match {
//        case Some(ref) => Right(ref)
//        case None => Left(NrsFailure)
//      }
//    }
//
//  private def doRead[A](response: WSResponse, defaultResult: NrsOutcome[A])(successOutcomeFactory: WSResponse => NrsOutcome[A])(
//    implicit successCode: SuccessCode): NrsOutcome[A] = {
//
//    if (response.status!=successCode.status){
//      logger.info("[StandardNrsWsParser][read] - Error response received from NRS " +
//        s"with status: ${response.status} and body\n ${response.body}")
//    }
//    response.status match {
//      case successCode.status =>
//        logger.info("[StandardNrsWsParser][read] - Success response received from NRS")
//        successOutcomeFactory(response)
//      case BAD_REQUEST => Left(NrsFailure)
//      case _ => defaultResult
//    }
//  }
//}
