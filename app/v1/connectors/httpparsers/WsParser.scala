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

package v1.connectors.httpparsers

import play.api.Logger
import play.api.libs.json.{JsError, JsSuccess, JsValue, Reads}
import play.api.libs.ws.WSResponse

import scala.util.{Success, Try}

trait WsParser {

  implicit class KnownJsonResponse(response: WSResponse) {

    def validateJson[T](implicit reads: Reads[T]): Option[T] = {
      Try(response.json) match {
        case Success(json: JsValue) => parseResult(json)
        case _ =>
          Logger.warn("[KnownJsonResponse][validateJson - NRS] No JSON was returned")
          None
      }
    }

    def parseResult[T](json: JsValue)(implicit reads: Reads[T]): Option[T] = json.validate[T] match {

      case JsSuccess(value, _) => Some(value)
      case JsError(error) =>
        Logger.warn(s"[KnownJsonResponse][validateJson - NRS] Unable to parse JSON: $error")
        None
    }
  }
}
