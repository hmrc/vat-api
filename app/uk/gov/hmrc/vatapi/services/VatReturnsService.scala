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

package uk.gov.hmrc.vatapi.services

import javax.inject.Inject
import play.api.Logger
import uk.gov.hmrc.domain.Vrn
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.vatapi.connectors.VatReturnsConnector
import uk.gov.hmrc.vatapi.models._
import uk.gov.hmrc.vatapi.resources.wrappers.VatReturnResponse

import scala.concurrent.{ExecutionContext, Future}

//object VatReturnsService extends VatReturnsService {
//
//  override val vatReturnsConnector: VatReturnsConnector = VatReturnsConnector
//
//}

class VatReturnsService @Inject()(vatReturnsConnector: VatReturnsConnector ) {

//trait VatReturnsService {

  val logger: Logger = Logger(this.getClass)
//  val vatReturnsConnector: VatReturnsConnector

  def submit(vrn: Vrn, vatReturn: des.VatReturnDeclaration)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[VatReturnResponse] = {
    logger.debug(s"[VatReturnsService][submit] - Submitting Vat Return")
    vatReturnsConnector.post(vrn, vatReturn)
  }
}