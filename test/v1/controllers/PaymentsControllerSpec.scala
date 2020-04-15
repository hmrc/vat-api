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

package v1.controllers

import play.api.libs.json.{JsValue, Json}
import uk.gov.hmrc.domain.Vrn
import uk.gov.hmrc.http.HeaderCarrier
import v1.mocks.requestParsers.MockPaymentsRequestParser
import v1.mocks.services.{MockEnrolmentsAuthService, MockPaymentsService}
import v1.models.request.payments.{PaymentsRawData, PaymentsRequest}
import v1.models.response.payments.PaymentsResponse

import scala.concurrent.ExecutionContext.Implicits.global

class PaymentsControllerSpec
 extends ControllerBaseSpec
   with MockEnrolmentsAuthService
   with MockPaymentsService
   with MockPaymentsRequestParser {

  trait Test{
    val hc: HeaderCarrier = HeaderCarrier()

    val controller: PaymentsController = new PaymentsController(
      authService = mockEnrolmentsAuthService,
      requestParser = mockPaymentsRequestParser,
      service = mockPaymentsService,
      cc = cc
    )

    MockEnrolmentsAuthService.authoriseUser()
  }

  val vrn: String = "123456789"
  val toDate: String = ""
  val fromDate: String = ""

  val rawData: PaymentsRawData =
    PaymentsRawData(
      vrn = vrn,
      from = Some(fromDate),
      to = Some(toDate)
    )

  val request: PaymentsRequest =
    PaymentsRequest(
      vrn = Vrn(vrn),
      from = fromDate,
      to = toDate
    )

  val paymentsResponse: PaymentsResponse =
    PaymentsResponse(
      payments = Seq(
        Payment(

        )
      )
    )

  val mtdJson: JsValue = Json.parse(
    """
      |{
      |   "payments":[
      |      {
      |         "amount":5,
      |         "received":"2017-02-11"
      |      },
      |      {
      |         "amount":50,
      |         "received":"2017-03-11"
      |      },
      |      {
      |         "amount":1000,
      |         "received":"2017-03-12"
      |      },
      |      {
      |         "amount":321,
      |         "received":"2017-08-05"
      |      },
      |      {
      |         "amount":91
      |      },
      |      {
      |         "amount":5,
      |         "received":"2017-09-12"
      |      }
      |   ]
      |}
    """.stripMargin
  )
}
