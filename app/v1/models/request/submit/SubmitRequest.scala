package v1.models.request.submit

import uk.gov.hmrc.domain.Vrn

case class SubmitRequest(vrn: Vrn, body: SubmitRequestBody)
