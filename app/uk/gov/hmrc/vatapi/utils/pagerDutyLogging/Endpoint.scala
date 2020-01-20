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

package uk.gov.hmrc.vatapi.utils.pagerDutyLogging

sealed trait Endpoint {
  def toLoggerMessage: LoggerMessages.Value
}

object Endpoint {
  case object RetrieveObligations extends Endpoint {
    override def toLoggerMessage: LoggerMessages.Value = LoggerMessages.RETRIEVE_OBLIGATIONS_500
  }
  case object SubmitReturn extends Endpoint {
    override def toLoggerMessage: LoggerMessages.Value = LoggerMessages.SUBMIT_RETURN_500
  }
  case object RetrieveReturns extends Endpoint {
    override def toLoggerMessage: LoggerMessages.Value = LoggerMessages.RETRIEVE_RETURN_500
  }
  case object RetrieveLiabilities extends Endpoint {
    override def toLoggerMessage: LoggerMessages.Value = LoggerMessages.RETRIEVE_LIABILITIES_500
  }
  case object RetrievePayments extends Endpoint {
    override def toLoggerMessage: LoggerMessages.Value = LoggerMessages.RETRIEVE_PAYMENTS_500
  }
}
