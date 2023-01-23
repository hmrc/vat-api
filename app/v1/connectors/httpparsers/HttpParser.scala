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

package v1.connectors.httpparsers

import utils.Logging
import play.api.libs.json._
import play.api.mvc.Request
import uk.gov.hmrc.http.HttpResponse
import v1.models.errors._

import scala.util.{Success, Try}

trait HttpParser extends Logging {

  implicit class KnownJsonResponse(response: HttpResponse) {

    def validateJson[T](implicit reads: Reads[T], request: Request[_]): Option[T] = {
      Try(response.json) match {
        case Success(json: JsValue) => parseResult(json)
        case _ =>
          warnLog("[KnownJsonResponse][validateJson] No JSON was returned")
          None
      }
    }

    def parseResult[T](json: JsValue)(implicit reads: Reads[T], request: Request[_]): Option[T] = json.validate[T] match {

      case JsSuccess(value, _) => Some(value)
      case JsError(error) =>
        warnLog(s"[KnownJsonResponse][validateJson] Unable to parse JSON: $error")
        None
    }
  }

  def retrieveCorrelationId(response: HttpResponse): String = response.header("CorrelationId").getOrElse("No Correlation ID")


  private val bvrErrorReads: Reads[Seq[DesErrorCode]] = {
    implicit val errorIdReads: Reads[DesErrorCode] = (__ \ "id").read[String].map(DesErrorCode(_))
    (__ \ "bvrfailureResponseElement" \ "validationRuleFailures").read[Seq[DesErrorCode]]
  }

  def parseErrors(response: HttpResponse)(implicit request: Request[_]): DesError = {
    val singleError         = response.validateJson[DesErrorCode].map(err => DesErrors(List(err)))
    lazy val bvrErrors      = response.validateJson(bvrErrorReads, request).map(errs => OutboundError(BVRError, Some(errs.map(_.toMtd))))
    lazy val unableToParseJsonError = {
      warnLog(s"unable to parse errors from response: ${response.body}")
      OutboundError(DownstreamError)
    }

    singleError orElse bvrErrors getOrElse unableToParseJsonError
  }
}
