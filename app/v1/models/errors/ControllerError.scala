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

package v1.models.errors

import utils.EndpointLogContext
import v1.controllers.UserRequest

case class ControllerError[A](endpointLogContext: EndpointLogContext,
                              vrn: String,
                              request: UserRequest[A],
                              status: Int,
                              details: String,
                              correlationId: String)

object ControllerError {

  implicit def errorDetailsFormatter[A](errorDetails: ControllerError[A]): String = {
    s"[${errorDetails.endpointLogContext.controllerName}][${errorDetails.endpointLogContext.endpointName}] " +
    s"VRN: ${errorDetails.vrn}, X-Request-Id: ${errorDetails.request.id.toString}, " +
    s"X-Client-Id: ${errorDetails.request.userDetails.clientId}, errorStatus: ${errorDetails.status.toString}, " +
    s"errorMessage: ${errorDetails.details}, " +
      s"correlationId: ${errorDetails.correlationId}"
  }
}
