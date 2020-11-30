package v1.mocks.nrs

import org.scalamock.handlers.CallHandler
import org.scalamock.scalatest.MockFactory
import uk.gov.hmrc.http.HeaderCarrier
import v1.nrs.NrsConnector
import v1.nrs.models.request.NrsSubmission
import v1.nrs.models.response.NrsResponse

import scala.concurrent.{ExecutionContext, Future}

trait MockNrsConnector extends MockFactory {

  val mockNrsConnector: NrsConnector = mock[NrsConnector]

  object MockNrsConnector {

    def submitNrs(body: NrsSubmission): CallHandler[Future[NrsOutcome[NrsResponse]]] = {
      (mockNrsConnector
        .submitNrs(_: NrsSubmission, _: String)(_: HeaderCarrier, _: ExecutionContext))
        .expects(body, *, *, *)
    }
  }

}
