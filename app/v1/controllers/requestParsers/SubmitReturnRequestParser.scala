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

package v1.controllers.requestParsers

import javax.inject.Inject
import v1.models.domain.Vrn
import v1.controllers.requestParsers.validators.SubmitReturnValidator
import v1.models.request.submit.{SubmitRawData, SubmitRequest, SubmitRequestBody}

class SubmitReturnRequestParser @Inject()(val validator: SubmitReturnValidator)
  extends RequestParser[SubmitRawData, SubmitRequest] {

  override protected def requestFor(data: SubmitRawData): SubmitRequest = {
    SubmitRequest(Vrn(data.vrn), data.body.asJson.get.as[SubmitRequestBody])
  }
}
