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

sealed trait LateSubmissionPenaltyCategoryUpstream {
  def toDownstreamPenaltyCategory: LateSubmissionPenaltyCategoryDownstream
}

object LateSubmissionPenaltyCategoryUpstream {
  case object `point` extends LateSubmissionPenaltyCategoryUpstream {
    override def toDownstreamPenaltyCategory: LateSubmissionPenaltyCategoryDownstream = LateSubmissionPenaltyCategoryDownstream.`P`
  }

  case object `charge` extends LateSubmissionPenaltyCategoryUpstream {
    override def toDownstreamPenaltyCategory: LateSubmissionPenaltyCategoryDownstream = LateSubmissionPenaltyCategoryDownstream.`C`
  }

  case object `threshold` extends LateSubmissionPenaltyCategoryUpstream {
    override def toDownstreamPenaltyCategory: LateSubmissionPenaltyCategoryDownstream = LateSubmissionPenaltyCategoryDownstream.`T`
  }

  implicit val format: json.Format[LateSubmissionPenaltyCategoryUpstream] = Enums.format[LateSubmissionPenaltyCategoryUpstream]
}