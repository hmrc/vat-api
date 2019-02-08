/*
 * Copyright 2019 HM Revenue & Customs
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

package uk.gov.hmrc.vatapi.resources

import play.api.mvc.Result
import uk.gov.hmrc.vatapi.resources.wrappers.Response

import scala.concurrent.{ExecutionContext, Future}

case class DesBusinessResult[R <: Response](businessResult: BusinessResult[R]) {

  def onSuccess(handleSuccess: R => Result)(implicit ec: ExecutionContext): Future[Result] =
    for {
      desResponseOrError <- businessResult.value
    } yield desResponseOrError match {
      case Left(errors) => handleErrors(errors)
      case Right(desResponse) => handleSuccess(desResponse)
    }

}
