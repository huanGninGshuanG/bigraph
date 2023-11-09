package controllers

import javax.inject._
import play.api.mvc._

/**
 * sho
 */
@Singleton
class IndexController @Inject()(cc: ControllerComponents) extends AbstractController(cc) {



  def index() = Action { implicit request: Request[AnyContent] =>
    Ok(views.html.aipIndex.index())
  }

  def login() = Action { implicit request: Request[AnyContent] =>
    Ok(views.html.login())
  }

}
