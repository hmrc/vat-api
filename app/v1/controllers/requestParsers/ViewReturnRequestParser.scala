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

package v1.controllers.requestParsers

import javax.inject.Inject
import uk.gov.hmrc.domain.Vrn
import v1.controllers.requestParsers.validators.ViewReturnValidator
import v1.models.request.viewReturn.{ViewRawData, ViewRequest}

class ViewReturnRequestParser @Inject()(val validator: ViewReturnValidator)
  extends RequestParser[ViewRawData, ViewRequest] {

  override protected def requestFor(data: ViewRawData): ViewRequest = {
    ViewRequest(Vrn(data.vrn), data.periodKey)
  }

}