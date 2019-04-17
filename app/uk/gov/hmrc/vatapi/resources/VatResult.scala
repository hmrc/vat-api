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

import play.api.libs.json.{Json, Writes}
import play.api.mvc.{Result, Results}
import uk.gov.hmrc.vatapi.models.audit.AuditResponse
import v2.models.audit.AuditError

trait VatResult {
  def result: Result

  def withHeaders(newHeaders: (String, String)*): VatResult

  def auditResponse: AuditResponse
}

object VatResult {

  case class Success[C: Writes](status: Int, body: C, headers: Seq[(String, String)] = Nil) extends VatResult {
    def withHeaders(newHeaders: (String, String)*): VatResult = copy(headers = headers ++ newHeaders)

    def result: Result =
      new Results.Status(status)(Json.toJson(body)).withHeaders(headers: _*)

    def auditResponse: AuditResponse =
      AuditResponse(status, None, Some(Json.toJson(body)))
  }

  case class Failure[E: Writes](status: Int, error: E, headers: Seq[(String, String)] = Nil)(implicit extractor: AuditErrorExtractor[E]) extends VatResult {
    def withHeaders(newHeaders: (String, String)*): VatResult = copy(headers = headers ++ newHeaders)

    def result: Result =
      new Results.Status(status)(Json.toJson(error)).withHeaders(headers: _*)

    def auditResponse: AuditResponse =
      AuditResponse(status, Some(extractor.auditErrors(error)), None)
  }

// TODO: remove this. Here only to handle existing inconsistent cases when Vat returns empty body with no error code
  case class FailureEmptyBody[E](status: Int, error: E, headers: Seq[(String, String)] = Nil)(implicit extractor: AuditErrorExtractor[E]) extends VatResult {
    def withHeaders(newHeaders: (String, String)*): VatResult = copy(headers = headers ++ newHeaders)

    def result: Result =
      new Results.Status(status).withHeaders(headers: _*)

    def auditResponse: AuditResponse =
      AuditResponse(status, Some(extractor.auditErrors(error)), None)
  }

}

trait AuditErrorExtractor[-E] {
  def auditErrors(e: E): Seq[AuditError]
}