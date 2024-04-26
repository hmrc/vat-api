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

package v1.models.response.penalties

import play.api.libs.json
import utils.enums.Enums

sealed trait ExpiryReasonDownstream {
  def toUpstreamExpiryReason: ExpiryReasonUpstream
}

object ExpiryReasonDownstream {
  case object `APP` extends ExpiryReasonDownstream {
    override def toUpstreamExpiryReason: ExpiryReasonUpstream = ExpiryReasonUpstream.`appeal`
  }

  case object `FAP` extends ExpiryReasonDownstream {
    override def toUpstreamExpiryReason: ExpiryReasonUpstream = ExpiryReasonUpstream.`submission-frequency-change`
  }

  case object `ICR` extends ExpiryReasonDownstream {
    override def toUpstreamExpiryReason: ExpiryReasonUpstream = ExpiryReasonUpstream.`obligations-reversed`
  }

  case object `MAN` extends ExpiryReasonDownstream {
    override def toUpstreamExpiryReason: ExpiryReasonUpstream = ExpiryReasonUpstream.`HMRC-removed`
  }

  case object `NAT` extends ExpiryReasonDownstream {
    override def toUpstreamExpiryReason: ExpiryReasonUpstream = ExpiryReasonUpstream.`natural-expiry`
  }

  case object `NLT` extends ExpiryReasonDownstream {
    override def toUpstreamExpiryReason: ExpiryReasonUpstream = ExpiryReasonUpstream.`penalty-removed`
  }

  case object `POC` extends ExpiryReasonDownstream {
    override def toUpstreamExpiryReason: ExpiryReasonUpstream = ExpiryReasonUpstream.`expiry-conditions-met`
  }

  case object `RES` extends ExpiryReasonDownstream {
    override def toUpstreamExpiryReason: ExpiryReasonUpstream = ExpiryReasonUpstream.`HMRC-reset`
  }

  implicit val format: json.Format[ExpiryReasonDownstream] = Enums.format[ExpiryReasonDownstream]
}