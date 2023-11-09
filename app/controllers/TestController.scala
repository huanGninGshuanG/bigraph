package controllers

import domain.TestData
import javax.inject.Inject
import org.slf4j.{Logger, LoggerFactory}
import play.api.mvc._
import repository.PkuResource
import services.{BigsimService, ResourceService, TestService}
import utils.JSONUtil

import scala.concurrent.ExecutionContext

class TestController @Inject()(testService: TestService, bigsimService: BigsimService,
                               resourceService: ResourceService, val controllerComponents: ControllerComponents)(implicit ec: ExecutionContext)
  extends BaseController {

  def logger: Logger = LoggerFactory.getLogger(this.getClass)

  def getValue(id: String = "70786628a53c11e98afbd0bf9c8f9b69"): Action[AnyContent]  = Action {  implicit request: Request[AnyContent] =>

    logger.debug("test id => " + id)

    bigsimService.handler(id, "strategyId",Map())

    //var outString = "Number is " + testService.getValue()
    Ok("test")
  }

  def execBigSim(resourceId:String, strategyId:String) : Action[AnyContent] = Action { implicit request: Request[AnyContent] =>

    logger.debug("test id => ${resourceId} , ${strategyId}")

    bigsimService.handler(resourceId, strategyId,Map())

    Ok("execBigSim")
  }

  def getResourceById =  Action {

    val resource = testService.getResource

    val strRes = JSONUtil.toJSONString(resource)


    Ok(strRes).as("application/json")
  }

  def getList = Action.async { implicit request =>

    testService.getList(0, 10).map { page =>

      val str = JSONUtil.listToJSONString(page)

      println(str)

      Ok(str).as("application/json")
    }
  }

  /**
    * 访问： /foo?p.index=0&p.size=10
    * @param p
    * @return
    */
  def foo(p: TestData) =  Action{

    logger.debug("TestData = " + p.toString)

    Ok(p.toString)
  }

  def postData = Action {implicit request =>

    val json = request.body.asJson

    println(json.get.toString)

    val pku = JSONUtil.GSON.fromJson(json.get.toString, classOf[PkuResource])

    println(json)

    Ok(pku.id)
  }
}

