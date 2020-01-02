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

package uk.gov.hmrc.vatapi


import uk.gov.hmrc.domain.Vrn

import scala.util.Random

class VrnGenerator(random: Random) {
  def nextVrn(): Vrn = {
    val digits = (1 to 9).map(_ => random.nextInt(10)).foldLeft("")((acc, curr) => acc + curr.toString)
    Vrn(s"$digits")
  }
}

object VrnGenerator {
  def apply(): VrnGenerator = new VrnGenerator(new Random)

  def apply(seed: Long): VrnGenerator = new VrnGenerator(new Random(seed))
}
