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

package v1.controllers

import mocks.MockAppConfig
import play.api.libs.json.Json
import play.api.mvc.Result
import uk.gov.hmrc.http.HeaderCarrier
import v1.audit.AuditEvents
import v1.constants.{CustomerInfoConstants}
import v1.mocks.MockIdGenerator
import v1.mocks.requestParsers.{MockCustomerInfoRequestParser}
import v1.mocks.services.{MockAuditService, MockCustomerInfoService, MockEnrolmentsAuthService}
import v1.models.audit.{AuditError}
import v1.models.audit.AuditResponse
import v1.models.errors._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class CustomerInfoControllerSpec extends ControllerBaseSpec with MockEnrolmentsAuthService
  with MockCustomerInfoService with MockCustomerInfoRequestParser with MockAuditService with MockIdGenerator with MockAppConfig {

  trait Test {
    val hc: HeaderCarrier = HeaderCarrier()

    val controller = new CustomerInfoController(
      authService = mockEnrolmentsAuthService,
      requestParser = mockCustomerInfoRequestParser,
      service = mockCustomerInfoService,
      auditService = stubAuditService,
      cc = cc,
      idGenerator = mockIdGenerator
    )

    MockIdGenerator.getUid.returns(CustomerInfoConstants.correlationId)
    MockEnrolmentsAuthService.authoriseUser()
  }

  "CustomerInfoController" when {

    "retrieveCustomerInfo" when {

      "valid request is supplied" when {

        "valid customerInfo data is returned" must {

          "return 200 and the customerInfo min data" in new Test {

            MockCustomerInfoRequestParser.parse(CustomerInfoConstants.rawData)(Right(CustomerInfoConstants.customerInfoRequest))

            MockCustomerInfoService.retrieveCustomerInfo(CustomerInfoConstants.customerInfoRequest)(Right(CustomerInfoConstants.wrappedCustomerInfoResponse()))

            val result: Future[Result] = controller.retrieveCustomerInfo(CustomerInfoConstants.vrn)(fakeGetRequest)

            status(result) shouldBe OK
            contentAsJson(result) shouldBe CustomerInfoConstants.emptyjson
            contentType(result) shouldBe Some("application/json")
            header("X-CorrelationId", result) shouldBe Some(CustomerInfoConstants.correlationId)

            MockedAuditService.verifyAuditEvent(AuditEvents.auditCustomerInfo(
              correlationId = CustomerInfoConstants.correlationId,
              userDetails = CustomerInfoConstants.userDetails,
              auditResponse = AuditResponse(OK, None, Some(CustomerInfoConstants.emptyjson))
            ))
          }
        }

        "errors are returned from CustomerInfo" when {

          val errors: Seq[(MtdError, Int)] = Seq(
            (CustomerInfoInvalidIdValue, BAD_REQUEST),
            (CustomerInfoNotDataFound, NOT_FOUND),
            (UnexpectedFailure.mtdError(INTERNAL_SERVER_ERROR, "error"), INTERNAL_SERVER_ERROR)
          )

          errors.foreach(errors => customerInfoErrors(errors._1, errors._2))

          def customerInfoErrors(mtdError: MtdError, expectedStatus: Int): Unit = {

            s"$mtdError error is returned" must {

              s"return $expectedStatus" in new Test {

                MockCustomerInfoRequestParser.parse(CustomerInfoConstants.rawData)(Right(CustomerInfoConstants.customerInfoRequest))

                MockCustomerInfoService.retrieveCustomerInfo(CustomerInfoConstants.customerInfoRequest)(Left(CustomerInfoConstants.errorWrapper(mtdError)))

                val result: Future[Result] = controller.retrieveCustomerInfo(CustomerInfoConstants.vrn)(fakeGetRequest)

                status(result) shouldBe expectedStatus
                contentAsJson(result) shouldBe Json.toJson(mtdError)
                contentType(result) shouldBe Some("application/json")
                header("X-CorrelationId", result) shouldBe Some(CustomerInfoConstants.correlationId)

                MockedAuditService.verifyAuditEvent(AuditEvents.auditCustomerInfo(
                  correlationId = CustomerInfoConstants.correlationId,
                  userDetails = CustomerInfoConstants.userDetails,
                  auditResponse = AuditResponse(expectedStatus, Some(Seq(AuditError(mtdError.code))), None)
                ))
              }
            }
          }
        }
      }

      "valid request is not supplied" must {

        "return a Bad Request" in new Test {

          MockCustomerInfoRequestParser.parse(CustomerInfoConstants.rawData)(Left(CustomerInfoConstants.errorWrapper(CustomerInfoInvalidIdValue)))

          val result: Future[Result] = controller.retrieveCustomerInfo(CustomerInfoConstants.vrn)(fakeGetRequest)

          status(result) shouldBe BAD_REQUEST
          contentAsJson(result) shouldBe Json.toJson(VrnFormatError)
          contentType(result) shouldBe Some("application/json")
          header("X-CorrelationId", result) shouldBe Some(CustomerInfoConstants.correlationId)

          MockedAuditService.verifyAuditEvent(AuditEvents.auditCustomerInfo(
            correlationId = CustomerInfoConstants.correlationId,
            userDetails = CustomerInfoConstants.userDetails,
            auditResponse = AuditResponse(BAD_REQUEST, Some(Seq(AuditError(VrnFormatError.code))), None)
          ))
        }
      }
    }
  }
}
