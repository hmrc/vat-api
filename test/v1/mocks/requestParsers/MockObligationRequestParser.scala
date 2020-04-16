package v1.mocks.requestParsers

import org.scalamock.handlers.CallHandler
import org.scalamock.scalatest.MockFactory
import v1.controllers.requestParsers.ObligationsRequestParser
import v1.models.errors.ErrorWrapper
import v1.models.request.obligations.{ObligationsRawData, ObligationsRequest}


trait MockObligationRequestParser extends MockFactory {

  val mockObligationRequestParser: ObligationsRequestParser = mock[ObligationsRequestParser]

  object MockObligationRequestParser {

    def parse(rawData: ObligationsRawData): CallHandler[Either[ErrorWrapper, ObligationsRequest]] = {
      (mockObligationRequestParser.parseRequest(_: ObligationsRawData)).expects(rawData)
    }

  }
}
