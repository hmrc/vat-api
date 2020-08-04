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

package v1.filters

import akka.stream.Materializer
import javax.inject.Inject
import play.api.libs.json.Json
import play.api.mvc.Results.Status
import play.api.mvc._
import uk.gov.hmrc.vatapi.models.ErrorClientNotAuthorizedToMTDVAT
import uk.gov.hmrc.vatapi.resources.GovTestScenarioHeader

import scala.concurrent.Future

class AgentSimulationFilter @Inject()(implicit val mat: Materializer) extends Filter {

  def apply(f: (RequestHeader) => Future[Result])(rh: RequestHeader): Future[Result] = {

    rh.headers.get(GovTestScenarioHeader) match {
      case Some("CLIENT_OR_AGENT_NOT_AUTHORISED") =>
        Future.successful(Status(ErrorClientNotAuthorizedToMTDVAT.httpStatusCode)(Json.toJson(ErrorClientNotAuthorizedToMTDVAT)))
      case _ => f(rh)
    }
  }

}
