package v1.controllers

import uk.gov.hmrc.http.HeaderCarrier
import v1.mocks.requestParsers.MockObligationRequestParser
import v1.mocks.services.{MockEnrolmentsAuthService, MockObligationService}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class ObligationsControllerSpec extends ControllerBaseSpec
  with MockEnrolmentsAuthService
  with MockObligationService
  with MockObligationRequestParser{

  trait Test {
    val hc: HeaderCarrier = HeaderCarrier()

    val controller: ObligationsController = new ObligationsController (
      mockEnrolmentsAuthService,
      mockObligationRequestParser,
      mockObligationsService,
      cc
    )

    MockEnrolmentsAuthService.authoriseUser()
  }


}
