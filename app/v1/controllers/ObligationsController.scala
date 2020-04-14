package v1.controllers


import javax.inject.Inject
import play.api.mvc.{BaseController, ControllerComponents}
import uk.gov.hmrc.vatapi.services.AuditService
import utils.Logging
import v1.controllers.requestParsers.ObligationsRequestParser
import v1.services.ObligationsService

import scala.concurrent.ExecutionContext

@Singleton
class ObligationsController @Inject()(val authService: EnrolmentsAuthService,
                                      val lookupService: MtdIdLookupService,
                                      requestParser: ObligationsRequestParser,
                                      service: ObligationsService,
                                      hateoasFactory: HateoasFactory,
                                      auditService: AuditService,
                                      cc: ControllerComponents)(implicit ec: ExecutionContext)
  extends AuthorisedController(cc) with BaseController with Logging {

}
