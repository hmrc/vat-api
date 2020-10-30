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

package mocks

import org.joda.time.DateTime
import org.scalamock.handlers.CallHandler
import org.scalamock.scalatest.MockFactory
import utils.{CurrentDateTime, IdGenerator}

trait MockIdGenerator extends MockFactory {

  val mockUuidGenerator: IdGenerator = mock[IdGenerator]

  object MockIdGenerator {
    val testUid = "a5894863-9cd7-4d0d-9eee-301ae79cbae6"
    def getUuid: CallHandler[String] = (mockUuidGenerator.getUid _).expects().returns(testUid)
  }
}
