/*
 * Copyright 2023 HM Revenue & Customs
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

import play.api.test.FakeRequest
import support.{LogCapturing, UnitSpec}
import uk.gov.hmrc.http.{HeaderNames, SessionKeys}

class LoggingSpec extends UnitSpec with Logging with LogCapturing {

  val testTrueClientIp = "test-true-client-ip"
  val testSessionId = "test-session-id"

  implicit val request: FakeRequest[_] = FakeRequest()
    .withHeaders((HeaderNames.trueClientIp, testTrueClientIp))
    .withSession((SessionKeys.sessionId, testSessionId))

  val emptyRequest: FakeRequest[_] = FakeRequest()

  "trueClientIp" when {

    "trueClientIp is in request" must {

      "return message including trueClientIp" in {

        val request: FakeRequest[_] = FakeRequest().withHeaders((HeaderNames.trueClientIp, testTrueClientIp))

        trueClientIp(request) shouldBe Some(s"trueClientIp: $testTrueClientIp ")

      }
    }

    "trueClientIp is not in request" must {

      "return None" in {

        trueClientIp(emptyRequest) shouldBe None
      }
    }
  }

  "sessionId" when {

    "sessionId is in request" must {

      "return message including sessionId" in {

        val request: FakeRequest[_] = FakeRequest().withSession((SessionKeys.sessionId, testSessionId))

        sessionId(request) shouldBe Some(s"sessionId: $testSessionId ")

      }
    }

    "sessionId is not in request" must {

      "return None" in {

        sessionId(emptyRequest) shouldBe None
      }
    }
  }

  "identifiers" when {

    "trueClientIp is in request" must {

      "return message including trueClientIp" in {

        val request: FakeRequest[_] = FakeRequest().withHeaders((HeaderNames.trueClientIp, testTrueClientIp))

        identifiers(request) shouldBe s"trueClientIp: $testTrueClientIp "

      }
    }

    "sessionId is in request" must {

      "return message including sessionId" in {

        val request: FakeRequest[_] = FakeRequest().withSession((SessionKeys.sessionId, testSessionId))

        identifiers(request) shouldBe s"sessionId: $testSessionId "

      }
    }

    "all identifiers are in request" must {

      "return message including sessionId" in {

        identifiers(request) shouldBe s"trueClientIp: $testTrueClientIp sessionId: $testSessionId "

      }
    }

    "no identifiers are in request" must {

      "return None" in {

        identifiers(emptyRequest) shouldBe ""
      }
    }
  }

  "identifiers are added to logs" when {

    "log level is INFO" in {
      withCaptureOfLoggingFrom(logger) { capturedLogs =>

        infoLog("test INFO log message")

        capturedLogs.head.getMessage shouldBe s"test INFO log message (trueClientIp: $testTrueClientIp sessionId: $testSessionId )"
      }
    }

    "log level is WARN" in {
      withCaptureOfLoggingFrom(logger) { capturedLogs =>

        warnLog("test WARN log message")

        capturedLogs.head.getMessage shouldBe s"test WARN log message (trueClientIp: $testTrueClientIp sessionId: $testSessionId )"
      }
    }

    "log level is WARN and throwable is provided" in {
      withCaptureOfLoggingFrom(logger) { capturedLogs =>

        val exception = new Exception("ERROR")

        warnLog("test WARN log message", exception)

        capturedLogs.head.getMessage shouldBe s"test WARN log message (trueClientIp: $testTrueClientIp sessionId: $testSessionId )"
      }
    }

    "log level is ERROR" in {
      withCaptureOfLoggingFrom(logger) { capturedLogs =>

        errorLog("test ERROR log message")

        capturedLogs.head.getMessage shouldBe s"test ERROR log message (trueClientIp: $testTrueClientIp sessionId: $testSessionId )"
      }
    }

    "log level is ERROR and throwable is provided" in {
      withCaptureOfLoggingFrom(logger) { capturedLogs =>

        val exception = new Exception("ERROR")

        errorLog("test ERROR log message", exception)

        capturedLogs.head.getMessage shouldBe s"test ERROR log message (trueClientIp: $testTrueClientIp sessionId: $testSessionId )"
      }
    }
  }
}
