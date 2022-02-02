/*
 * Copyright 2022 HM Revenue & Customs
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

package utils.enums

import shapeless._

// Based on code in https://github.com/milessabin/shapeless/blob/master/examples/src/main/scala/shapeless/examples/enum.scala
object Values {

  trait MkValues[E] {
    def values: List[E]
  }

  object MkValues {
    implicit def values[E, Impls <: Coproduct](implicit gen: Generic.Aux[E, Impls], v: Aux[E, Impls]): MkValues[E] =
      new MkValues[E] {
        def values: List[E] = v.values
      }

    trait Aux[E, Impls] {
      def values: List[E]
    }

    object Aux {
      implicit def cnilAux[E]: Aux[E, CNil] =
        new Aux[E, CNil] {
          def values: List[E] = Nil
        }

      implicit def cconsAux[E, L <: E, R <: Coproduct](implicit l: Witness.Aux[L], r: Aux[E, R]): Aux[E, L :+: R] =
        new Aux[E, L :+: R] {
          def values: List[E] = l.value :: r.values
        }
    }
  }
}
