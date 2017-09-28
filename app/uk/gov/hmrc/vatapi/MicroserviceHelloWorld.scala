package uk.gov.hmrc.vatapi.controllers

import uk.gov.hmrc.play.microservice.controller.BaseController
import uk.gov.hmrc.play.http.logging.MdcLoggingExecutionContext._
import play.api.mvc._
import scala.concurrent.Future

object MicroserviceHelloWorld extends MicroserviceHelloWorld

trait MicroserviceHelloWorld extends BaseController {

	def hello() = Action.async { implicit request =>
		Future.successful(Ok("Hello world"))
	}
}
