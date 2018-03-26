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

package uk.gov.hmrc.vatapi.resources

import play.api.libs.json.{JsValue, Json}

object FuncJsons {

  object NRS {
    def success(): JsValue =
      Json.parse(
        s"""
           |{
           |  "nrSubmissionId":"2dd537bc-4244-4ebf-bac9-96321be13cdc",
           |  "cadesTSignature":"30820b4f06092a864886f70111111111c0445c464",
           |  "timestamp":"2018-02-14T09:32:15Z"
           |}
        """.stripMargin)
  }

}