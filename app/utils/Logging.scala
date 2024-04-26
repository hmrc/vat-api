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

package utils

import org.slf4j.{Logger, LoggerFactory}
import play.api.{LoggerLike, MarkerContext}
import play.api.mvc.Request
import uk.gov.hmrc.http.{HeaderNames, HttpResponse, SessionKeys}

trait Logging extends LoggerLike {

  lazy val trueClientIp: Request[_] => Option[String] = request => request.headers.get(HeaderNames.trueClientIp).map(trueClientIp => s"trueClientIp: $trueClientIp ")

  lazy val sessionId: Request[_] => Option[String] = request => request.session.get(SessionKeys.sessionId).map(sessionId => s"sessionId: $sessionId ")

  lazy val identifiers: Request[_] => String = request => Seq(trueClientIp(request), sessionId(request)).flatten.foldLeft("")(_ + _)

  lazy val trueClientIpFromHttpResponse: HttpResponse => Option[String] = httpResponse => httpResponse.headers.get(HeaderNames.trueClientIp).map(trueClientIp => s"trueClientIp: $trueClientIp")
  lazy val sessionIdFromHttpResponse: HttpResponse => Option[String] = httpResponse => httpResponse.headers.get(HeaderNames.xSessionId).map(sessionId => s"sessionId: $sessionId")
  lazy val identifiersFromHttpResponse: HttpResponse => String = request => Seq(trueClientIpFromHttpResponse(request), sessionIdFromHttpResponse(request)).flatten.foldLeft("")(_ + _)

  lazy val logger: Logger = LoggerFactory.getLogger(getClass)

  def infoLog(message: => String)(implicit mc: MarkerContext, request: Request[_]): Unit = this.info(s"$message (${identifiers(request)})")

  def infoConnectorLog(message: => String)(implicit mc: MarkerContext, httpResponse: HttpResponse): Unit = this.info(s"$message (${identifiersFromHttpResponse(httpResponse)})")

  def warnLog(message: => String)(implicit mc: MarkerContext, request: Request[_]): Unit = this.warn(s"$message (${identifiers(request)})")

  def warnLog(message: => String, throwable: Throwable)(implicit mc: MarkerContext, request: Request[_]): Unit = this.warn(s"$message (${identifiers(request)})", throwable)

  def warnConnectorLog(message: => String)(implicit httpResponse: HttpResponse): Unit = this.warn(s"$message (${identifiersFromHttpResponse(httpResponse)})")

  def errorLog(message: => String)(implicit mc: MarkerContext, request: Request[_]): Unit = this.error(s"$message (${identifiers(request)})")

  def errorLog(message: => String, throwable: Throwable)(implicit mc: MarkerContext, request: Request[_]): Unit = this.error(s"$message (${identifiers(request)})", throwable)

  def errorConnectorLog(message: => String)(implicit httpResponse: HttpResponse): Unit = this.error(s"$message (${identifiersFromHttpResponse(httpResponse)})")
}