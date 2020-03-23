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

package utils.enums

import cats.Show
import org.scalacheck.{Arbitrary, Gen}
import org.scalatest.Inspectors
import play.api.libs.json._
import support.UnitSpec

sealed trait Enum

object Enum {
  case object `enum-one`   extends Enum
  case object `enum-two`   extends Enum
  case object `enum-three` extends Enum

  implicit val format: Format[Enum] = Enums.format[Enum]
}

case class Foo[A](someField: A)

object Foo {
  implicit def fmts[A: Format]: Format[Foo[A]] = Json.format[Foo[A]]
}

class EnumsSpec extends UnitSpec with Inspectors {

  import Enum._

  implicit val arbitraryEnumValue: Arbitrary[Enum] = Arbitrary[Enum](Gen.oneOf(`enum-one`, `enum-two`, `enum-three`))

  "SealedTraitEnumJson" must {

    "check toString assumption" in {
      `enum-two`.toString shouldBe "enum-two"
    }

    def json(value: Enum): JsValue = Json.parse(s"""
            |{
            | "someField": "$value"
            |}
          """.stripMargin)

    "generates reads" in {
      forAll (List(`enum-one`, `enum-two`, `enum-three`)) { value: Enum =>
        json(value).as[Foo[Enum]] shouldBe Foo(value)
      }
    }

    "generates writes" in {
      forAll (List(`enum-one`, `enum-two`, `enum-three`)) { value: Enum =>
        Json.toJson(Foo(value)) shouldBe json(value)
      }
    }

    "allow roundtrip" in {
      forAll (List(`enum-one`, `enum-two`, `enum-three`)) { value: Enum =>
        val foo = Foo(value)
        Json.toJson(foo).as[Foo[Enum]] shouldBe foo
      }
    }

    "allows external parse by name" in {
      Enums.parser[Enum].lift("enum-one") shouldBe Some(`enum-one`)
      Enums.parser[Enum].lift("unknown") shouldBe None
    }

    "allows alternative names (specified by method)" in {

      sealed trait Enum2 {
        def altName: String
      }

      object Enum2 {
        case object `enum-one` extends Enum2 {
          override def altName: String = "one"
        }
        case object `enum-two` extends Enum2 {
          override def altName: String = "two"
        }
        case object `enum-three` extends Enum2 {
          override def altName: String = "three"
        }

        implicit val show: Show[Enum2]     = Show.show[Enum2](_.altName)
        implicit val format: Format[Enum2] = Enums.format[Enum2]
      }

      val json = Json.parse("""
      |{
      | "someField": "one"
      |}""".stripMargin)

      json.as[Foo[Enum2]] shouldBe Foo(Enum2.`enum-one`)
      Json.toJson(Foo[Enum2](Enum2.`enum-one`)) shouldBe json
    }

    "detects badly formatted values" in {
      val badJson = Json.parse(s"""
      |{
      | "someField": "unknown"
      |}
      |""".stripMargin)

      badJson.validate[Foo[Enum]] shouldBe JsError(__ \ "someField", JsonValidationError("error.expected.Enum"))
    }

    "detects type errors" in {
      val badJson = Json.parse(s"""
                                  |{
                                  | "someField": 123
                                  |}
                                  |""".stripMargin)

      badJson.validate[Foo[Enum]] shouldBe JsError(__ \ "someField", JsonValidationError("error.expected.jsstring"))
    }

    "only work for sealed trait singletons (objects)" in {
      assertTypeError("""
        |      sealed trait NotEnum
        |
        |      case object ObjectOne                  extends NotEnum
        |      case class CaseClassTwo(value: String) extends NotEnum
        |
        |      Enums.format[NotEnum]
        """.stripMargin)
    }
  }
}
