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

package utils.pagerDutyLogging

sealed trait Endpoint {
  def toLoggerMessage: PagerDutyLoggingEndpointName.Value
  def requestFailedMessage: PagerDutyLoggingEndpointName.Value
}

object Endpoint {

  case object RetrieveObligations extends Endpoint {
    override def toLoggerMessage: PagerDutyLoggingEndpointName.Value = PagerDutyLoggingEndpointName.RETRIEVE_OBLIGATIONS_500
    override def requestFailedMessage: PagerDutyLoggingEndpointName.Value = PagerDutyLoggingEndpointName.RETRIEVE_OBLIGATIONS_REQUEST_FAILED
  }
  case object SubmitReturn extends Endpoint {
    override def toLoggerMessage: PagerDutyLoggingEndpointName.Value = PagerDutyLoggingEndpointName.SUBMIT_RETURN_500
    override def requestFailedMessage: PagerDutyLoggingEndpointName.Value = PagerDutyLoggingEndpointName.SUBMIT_RETURNS_REQUEST_FAILED

  }
  case object RetrieveReturns extends Endpoint {
    override def toLoggerMessage: PagerDutyLoggingEndpointName.Value = PagerDutyLoggingEndpointName.RETRIEVE_RETURN_500
    override def requestFailedMessage: PagerDutyLoggingEndpointName.Value = PagerDutyLoggingEndpointName.RETRIEVE_RETURN_REQUEST_FAILED

  }
  case object RetrieveLiabilities extends Endpoint {
    override def toLoggerMessage: PagerDutyLoggingEndpointName.Value = PagerDutyLoggingEndpointName.RETRIEVE_LIABILITIES_500
    override def requestFailedMessage: PagerDutyLoggingEndpointName.Value = PagerDutyLoggingEndpointName.RETRIEVE_LIABILITIES_REQUEST_FAILED

  }
  case object RetrievePayments extends Endpoint {
    override def toLoggerMessage: PagerDutyLoggingEndpointName.Value = PagerDutyLoggingEndpointName.RETRIEVE_PAYMENTS_500
    override def requestFailedMessage: PagerDutyLoggingEndpointName.Value = PagerDutyLoggingEndpointName.RETRIEVE_PAYMENTS_REQUEST_FAILED

  }

  case object  RetrievePenalties extends Endpoint {
    override def toLoggerMessage: PagerDutyLoggingEndpointName.Value = PagerDutyLoggingEndpointName.RETRIEVE_PENALTIES_500
    override def requestFailedMessage: PagerDutyLoggingEndpointName.Value = PagerDutyLoggingEndpointName.RETRIEVE_PENALTIES_REQUEST_FAILED
  }

  case object  RetrieveFinancialData extends Endpoint {
    override def toLoggerMessage: PagerDutyLoggingEndpointName.Value = PagerDutyLoggingEndpointName.RETRIEVE_FINANCIAL_DATA_500
    override def requestFailedMessage: PagerDutyLoggingEndpointName.Value = PagerDutyLoggingEndpointName.RETRIEVE_FINANCIAL_DATA_REQUEST_FAILED
  }

  case object  RetrieveCustomerInfo extends Endpoint {
    override def toLoggerMessage: PagerDutyLoggingEndpointName.Value = PagerDutyLoggingEndpointName.RETRIEVE_CUSTOMER_INFO_500
    override def requestFailedMessage: PagerDutyLoggingEndpointName.Value = PagerDutyLoggingEndpointName.RETRIEVE_CUSTOMER_INFO_REQUEST_FAILED
  }
}