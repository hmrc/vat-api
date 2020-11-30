package v1.mocks.nrs

import org.joda.time.DateTime
import org.scalamock.handlers.CallHandler
import org.scalamock.scalatest.MockFactory
import uk.gov.hmrc.http.HeaderCarrier
import v1.controllers.UserRequest
import v1.models.errors.ErrorWrapper
import v1.models.request.submit.SubmitRequest
import v1.nrs.NrsService

import scala.concurrent.{ExecutionContext, Future}

trait MockNrsService extends MockFactory {

  val mockNrsService: NrsService = mock[NrsService]

  object MockNrsService {

    def submitNrs(request: SubmitRequest, nrsId: String, dateTime: DateTime): CallHandler[Future[Either[ErrorWrapper, NrsResponse]]] = {
      (mockNrsService
        .submitNrs(_: SubmitRequest, _: String, _: DateTime)(_: UserRequest[_], _: HeaderCarrier, _: ExecutionContext, _: String))
        .expects(request, *, *, *, *, *, *)
    }
  }

}
