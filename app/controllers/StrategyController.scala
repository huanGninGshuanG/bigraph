package controllers

import domain.enums.SimDataTypeEnum
import domain.enums.SimDataTypeEnum.SimDataTypeEnum
import javax.inject._
import org.slf4j.{Logger, LoggerFactory}
import play.api.mvc._
import services.BigsimService
import utils.JSONUtil

import scala.concurrent.ExecutionContext

@Singleton
class StrategyController @Inject()(bigsimService: BigsimService,cc: ControllerComponents)(implicit ec: ExecutionContext) extends AbstractController(cc) {

  def logger: Logger = LoggerFactory.getLogger(this.getClass)


  def form() = Action { implicit request: Request[AnyContent] =>
    Ok(views.html.aipStrategy.strategy_form())
  }

  def execSimulate = Action { implicit request =>
    println("what");
    println(request.body.asJson)

    val resourceId = request.body.asFormUrlEncoded.get("resourceId").head

    val strategyId =  request.body.asFormUrlEncoded.get("strategyId").head

    val  situationalData  =  request.body.asFormUrlEncoded.get("fileContent").head

    logger.debug("test id => ${resourceId} , ${strategyId}")

    var extMap:Map[SimDataTypeEnum,String] = Map()
    extMap += (SimDataTypeEnum.SITUATION_DATA->situationalData);
    println("Look");
    println(extMap);
    val logItemId = bigsimService.handler(resourceId, strategyId,extMap)

    val strRes =  JSONUtil.toJSONStr(logItemId)

    Ok(strRes).as("application/json")

  }

}
