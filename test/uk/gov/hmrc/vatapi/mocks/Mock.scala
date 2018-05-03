/*
 * Copyright 2018 HM Revenue & Customs
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

package uk.gov.hmrc.vatapi.mocks

import org.mockito.{ArgumentMatchers => Matchers}
import org.mockito.Mockito
import org.mockito.stubbing.OngoingStubbing
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{BeforeAndAfterEach, Suite}

trait Mock extends MockitoSugar with BeforeAndAfterEach { _: Suite =>

  def any[T]() = Matchers.any[T]()
  def eqTo[T](t: T) = Matchers.eq[T](t)
  def when[T](t: T) = Mockito.when(t)
  def reset[T](t: T) = Mockito.reset(t)

  implicit class stubbingOps[T](stubbing: OngoingStubbing[T]){
    def returns(t: T) = stubbing.thenReturn(t)
  }
}