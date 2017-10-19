/*
 * Copyright 2017 HM Revenue & Customs
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

import play.api.mvc.{PathBindable, QueryStringBindable}
import uk.gov.hmrc.domain.Vrn
import uk.gov.hmrc.vatapi.models.{ObligationsQueryParams, OptEither}

object Binders {

  implicit def vrnBinder(implicit stringBinder: PathBindable[String]) = new PathBindable[Vrn] {
    val vrnRegex = """^\d{9}$"""

    def unbind(key: String, vrn: Vrn): String = stringBinder.unbind(key, vrn.value)

    def bind(key: String, value: String): Either[String, Vrn] = {
      if (value.matches(vrnRegex)) {
        Right(Vrn(value))
      } else {
        Left("ERROR_VRN_INVALID")
      }
    }
  }


  implicit def obligationsQueryParamsBinder(implicit stringBinder: QueryStringBindable[String]) = new QueryStringBindable[ObligationsQueryParams] {

    override def bind(key: String, params: Map[String, Seq[String]]): OptEither[ObligationsQueryParams] = {
      val from = stringBinder.bind("from", params)
      val to = stringBinder.bind("to", params)
      val status = stringBinder.bind("status", params)

      val query = ObligationsQueryParams.from(from, to, status)
      if (query.isRight)
        Some(Right(query.right.get))
      else
        Some(Left(query.left.get))
    }

    override def unbind(key: String, value: ObligationsQueryParams): String = stringBinder.unbind(key, value.map(key).toString)

  }

}
