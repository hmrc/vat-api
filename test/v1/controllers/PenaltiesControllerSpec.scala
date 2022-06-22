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

package v1.controllers

import com.typesafe.config.ConfigFactory
import mocks.MockAppConfig
import play.api.Configuration
import play.api.libs.json.Json
import play.api.mvc.Result
import uk.gov.hmrc.http.HeaderCarrier
import v1.audit.AuditEvents
import v1.constants.PenaltiesConstants
import v1.mocks.MockIdGenerator
import v1.mocks.requestParsers.MockPenaltiesRequestParser
import v1.mocks.services.{MockAuditService, MockEnrolmentsAuthService, MockPenaltiesService}
import v1.models.audit.{AuditError, AuditResponse}
import v1.models.errors.{BadRequestError, MtdError, UnexpectedFailure, VrnFormatError, VrnNotFound}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class PenaltiesControllerSpec extends ControllerBaseSpec with MockEnrolmentsAuthService
  with MockPenaltiesService with MockPenaltiesRequestParser with MockAuditService with MockIdGenerator with MockAppConfig {

  trait Test {
    val hc: HeaderCarrier = HeaderCarrier()

    val controller = new PenaltiesController(
      authService = mockEnrolmentsAuthService,
      requestParser = mockPenaltiesRequestParser,
      service = mockPenaltiesService,
      auditService = stubAuditService,
      cc = cc,
      idGenerator = mockIdGenerator,
      appConfig = mockAppConfig
    )
    MockedAppConfig.featureSwitch.returns(Some(Configuration(ConfigFactory.parseString(
      """
        |penaltiesEndpoints.enabled = true
      """.stripMargin
    ))))
    MockIdGenerator.getUid.returns(PenaltiesConstants.correlationId)
    MockEnrolmentsAuthService.authoriseUser()
  }

  "PenaltiesController" when {

    "retrievePenalties" when {

      "penaltiesEndpoints is enabled" when {

        "valid request is supplied" when {

          "valid penalties data is returned" must {

            "return 200 and the penalties data" in new Test {

              MockPenaltiesRequestParser.parse(PenaltiesConstants.rawData)(Right(PenaltiesConstants.penaltiesRequest))

              MockPenaltiesService.retrievePenalties(PenaltiesConstants.penaltiesRequest)(Right(PenaltiesConstants.wrappedPenaltiesResponse()))

              val result: Future[Result] = controller.retrievePenalties(PenaltiesConstants.vrn)(fakeGetRequest)

              status(result) shouldBe OK
              contentAsJson(result) shouldBe PenaltiesConstants.testPenaltiesResponseJson
              contentType(result) shouldBe Some("application/json")
              header("X-CorrelationId", result) shouldBe Some(PenaltiesConstants.correlationId)

              MockedAuditService.verifyAuditEvent(AuditEvents.auditPenalties(
                correlationId = PenaltiesConstants.correlationId,
                userDetails = PenaltiesConstants.userDetails,
                auditResponse = AuditResponse(OK, None, Some(PenaltiesConstants.testPenaltiesResponseJson))
              ))
            }
          }

          "errors are returned from Penalties" when {

            val errors: Seq[(MtdError, Int)] = Seq(
              (VrnFormatError, BAD_REQUEST),
              (VrnNotFound, NOT_FOUND),
              (UnexpectedFailure.mtdError(INTERNAL_SERVER_ERROR, "error"), INTERNAL_SERVER_ERROR)
            )

            errors.foreach(errors => penaltiesErrors(errors._1, errors._2))

            def penaltiesErrors(mtdError: MtdError, expectedStatus: Int): Unit = {

              s"$mtdError error is returned" must {

                s"return $expectedStatus" in new Test {

                  MockPenaltiesRequestParser.parse(PenaltiesConstants.rawData)(Right(PenaltiesConstants.penaltiesRequest))

                  MockPenaltiesService.retrievePenalties(PenaltiesConstants.penaltiesRequest)(Left(PenaltiesConstants.errorWrapper(mtdError)))

                  val result: Future[Result] = controller.retrievePenalties(PenaltiesConstants.vrn)(fakeGetRequest)

                  status(result) shouldBe expectedStatus
                  contentAsJson(result) shouldBe Json.toJson(mtdError)
                  contentType(result) shouldBe Some("application/json")
                  header("X-CorrelationId", result) shouldBe Some(PenaltiesConstants.correlationId)

                  MockedAuditService.verifyAuditEvent(AuditEvents.auditPenalties(
                    correlationId = PenaltiesConstants.correlationId,
                    userDetails = PenaltiesConstants.userDetails,
                    auditResponse = AuditResponse(expectedStatus, Some(Seq(AuditError(mtdError.code))), None)
                  ))
                }
              }
            }
          }
        }

        "valid request is not supplied" must {

          "return BadRequest" in new Test {

            MockPenaltiesRequestParser.parse(PenaltiesConstants.rawData)(Left(PenaltiesConstants.errorWrapper(VrnFormatError)))

            val result: Future[Result] = controller.retrievePenalties(PenaltiesConstants.vrn)(fakeGetRequest)

            status(result) shouldBe BAD_REQUEST
            contentAsJson(result) shouldBe Json.toJson(VrnFormatError)
            contentType(result) shouldBe Some("application/json")
            header("X-CorrelationId", result) shouldBe Some(PenaltiesConstants.correlationId)

            MockedAuditService.verifyAuditEvent(AuditEvents.auditPenalties(
              correlationId = PenaltiesConstants.correlationId,
              userDetails = PenaltiesConstants.userDetails,
              auditResponse = AuditResponse(BAD_REQUEST, Some(Seq(AuditError(VrnFormatError.code))), None)
            ))
          }
        }
      }

      "penaltiesEndpoint is disabled" must {

        "return BadRequest" in {

          val hc: HeaderCarrier = HeaderCarrier()

          val controller = new PenaltiesController(
            authService = mockEnrolmentsAuthService,
            requestParser = mockPenaltiesRequestParser,
            service = mockPenaltiesService,
            auditService = stubAuditService,
            cc = cc,
            idGenerator = mockIdGenerator,
            appConfig = mockAppConfig
          )

          MockEnrolmentsAuthService.authoriseUser()
          MockedAppConfig.featureSwitch.returns(Some(Configuration(ConfigFactory.parseString(
            """
              |penaltiesEndpoints.enabled = false
            """.stripMargin
          ))))

          val result: Future[Result] = controller.retrievePenalties(PenaltiesConstants.vrn)(fakeGetRequest)

          status(result) shouldBe BAD_REQUEST
          contentAsJson(result) shouldBe Json.toJson(BadRequestError)
          contentType(result) shouldBe Some("application/json")
        }
      }
    }
  }
}
