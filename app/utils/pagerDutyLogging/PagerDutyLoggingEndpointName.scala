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

object PagerDutyLoggingEndpointName extends Enumeration {
  val RETRIEVE_OBLIGATIONS_500,
  RETRIEVE_OBLIGATIONS_REQUEST_FAILED,
  SUBMIT_RETURN_500,
  SUBMIT_RETURNS_REQUEST_FAILED,
  RETRIEVE_RETURN_500,
  RETRIEVE_RETURN_REQUEST_FAILED,
  RETRIEVE_LIABILITIES_500,
  RETRIEVE_LIABILITIES_REQUEST_FAILED,
  RETRIEVE_PAYMENTS_500,
  RETRIEVE_PAYMENTS_REQUEST_FAILED,
  RETRIEVE_PENALTIES_500,
  RETRIEVE_PENALTIES_REQUEST_FAILED,
  RETRIEVE_FINANCIAL_DATA_500,
  RETRIEVE_FINANCIAL_DATA_REQUEST_FAILED
  = Value
}
