/*
 * Copyright 2024 HM Revenue & Customs
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

package v1.models.errors

import play.api.http.Status
import v1.controllers.UserRequest

case class ConnectorError(vrn: String, requestId: String)

object ConnectorError {

  def log[A](logContext: String,
             vrn: String,
             status: Int = Status.INTERNAL_SERVER_ERROR,
             details: String,
            )(implicit request: UserRequest[A],
              correlationId: String): String = {
    s"$logContext " +
      s"VRN: $vrn, X-Request-Id: ${request.id.toString}, " +
      s"X-Client-Id: ${request.userDetails.clientId}, errorStatus: ${status.toString}, " +
      s"errorMessage: $details, " +
      s"correlationId: $correlationId"
  }
}
