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

package v1.models.response.penalties

import play.api.libs.json
import utils.enums.Enums

sealed trait ExpiryReasonUpstream {
  def toDownstreamExpiryReason: ExpiryReasonDownstream
}

object ExpiryReasonUpstream {
  case object `appeal` extends ExpiryReasonUpstream {
    override def toDownstreamExpiryReason: ExpiryReasonDownstream = ExpiryReasonDownstream.`APP`
  }

  case object `submission-frequency-change` extends ExpiryReasonUpstream {
    override def toDownstreamExpiryReason: ExpiryReasonDownstream = ExpiryReasonDownstream.`FAP`
  }

  case object `obligations-reversed` extends ExpiryReasonUpstream {
    override def toDownstreamExpiryReason: ExpiryReasonDownstream = ExpiryReasonDownstream.`ICR`
  }

  case object `HMRC-removed` extends ExpiryReasonUpstream {
    override def toDownstreamExpiryReason: ExpiryReasonDownstream = ExpiryReasonDownstream.`MAN`
  }

  case object `natural-expiry` extends ExpiryReasonUpstream {
    override def toDownstreamExpiryReason: ExpiryReasonDownstream = ExpiryReasonDownstream.`NAT`
  }

  case object `penalty-removed` extends ExpiryReasonUpstream {
    override def toDownstreamExpiryReason: ExpiryReasonDownstream = ExpiryReasonDownstream.`NLT`
  }

  case object `expiry-conditions-met` extends ExpiryReasonUpstream {
    override def toDownstreamExpiryReason: ExpiryReasonDownstream = ExpiryReasonDownstream.`POC`
  }

  case object `HMRC-reset` extends ExpiryReasonUpstream {
    override def toDownstreamExpiryReason: ExpiryReasonDownstream = ExpiryReasonDownstream.`RES`
  }

  implicit val format: json.Format[ExpiryReasonUpstream] = Enums.format[ExpiryReasonUpstream]
}
