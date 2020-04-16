package v1.mocks.services

import org.scalamock.handlers.CallHandler
import org.scalamock.scalatest.MockFactory
import uk.gov.hmrc.http.HeaderCarrier
import utils.EndpointLogContext
import v1.models.request.obligations.ObligationsRequest
import v1.models.response.obligations.ObligationsResponse
import v1.services.{ObligationsService, ServiceOutcome}

import scala.concurrent.{ExecutionContext, Future}

trait MockObligationService extends MockFactory{

  val mockObligationsService: ObligationsService = mock[ObligationsService]

  object MockObligationService {

    def receiveObligations(request: ObligationsRequest): CallHandler[Future[ServiceOutcome[ObligationsResponse]]] = {
      (mockObligationsService
        .retrieveObligations(_ : ObligationsRequest)(_: HeaderCarrier, _: ExecutionContext, _: EndpointLogContext))
        .expects(request, *, *, *)
    }
  }

}
