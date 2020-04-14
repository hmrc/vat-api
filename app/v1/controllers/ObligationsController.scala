package v1.controllers


import javax.inject.Inject
import play.api.mvc.{Action, AnyContent, BaseController, ControllerComponents}
import uk.gov.hmrc.domain.Vrn
import uk.gov.hmrc.vatapi.services.AuditService
import utils.{EndpointLogContext, Logging}
import v1.controllers.requestParsers.ObligationsRequestParser
import v1.services.{EnrolmentsAuthService, ObligationsService}

import scala.concurrent.ExecutionContext

@Singleton
class ObligationsController @Inject()(val authService: EnrolmentsAuthService,
                                      requestParser: ObligationsRequestParser,
                                      service: ObligationsService,
                                      auditService: AuditService,
                                      cc: ControllerComponents)(implicit ec: ExecutionContext)
  extends AuthorisedController(cc) with BaseController with Logging {

  implicit val endpointLogContext: EndpointLogContext =
    EndpointLogContext(
      controllerName = "VatObligationsResource",
      endpointName = "retrieveVatObligations"
    )

  def Obligations(vrn: String, from: Option[String], to: Option[String], status: Option[String]): Action[AnyContent] =
    authorisedAction(vrn).async{ implicit request =>

    }
}
