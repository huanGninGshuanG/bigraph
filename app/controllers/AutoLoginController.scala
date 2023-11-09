package controllers

import domain.PkuResource
import domain.vo.ResourceVo
import javax.inject._
import org.slf4j.{Logger, LoggerFactory}
import play.api.mvc._
import services.{BigsimService, ResourceService}
import utils.JSONUtil
import utils.JSONUtil.GSON

import scala.concurrent.ExecutionContext

@Singleton
class AutoLoginController @Inject()(resourceService:ResourceService,bigsimService: BigsimService, cc: ControllerComponents)(implicit ec: ExecutionContext) extends AbstractController(cc) {

  def logger: Logger = LoggerFactory.getLogger(this.getClass)

  def autoExec = Action { implicit request =>
  //TODO 需增加token校验

    val json = request.body.asJson

    val resource = JSONUtil.GSON.fromJson(json.get.toString, classOf[ResourceVo])


    // 用于测试时随机生成 code. code 数据库中为唯一键，会产生冲突
    // resource.code = BigInt(1500, scala.util.Random).toString(36).substring(0, 10)

    resource.description =  java.net.URLDecoder.decode(resource.description,"utf-8")
    resource.fileContent = java.net.URLDecoder.decode(resource.fileContent,"utf-8")
    resource.extContent = java.net.URLDecoder.decode(resource.extContent,"utf-8")

    logger.debug("test id => ${resourceId} , ${strategyId}" + resource)
    var pkuResource = JSONUtil.GSON.fromJson( GSON.toJson(resource), classOf[PkuResource])

    if(resource.id!=null&&resource.id.trim.length>0){
      resourceService.updateById(pkuResource)
    }else{
      resourceService.save(pkuResource)
    }

    val resourceId = pkuResource.id
    var strategyId =  "default"
    if(resource.strategyId.nonEmpty){
      strategyId = resource.strategyId
    }



    val startTime=System.nanoTime
    val logItemId = bigsimService.handler(resourceId, strategyId,Map())   // 通过bigsimService来处理，模型和策略都是存放在数据库中，传入它们的Id。
    val endTime=System.nanoTime
    val delta=endTime-startTime
    logger.debug("bigsimService.handler(resourceId, strategyId)" + delta/1000000d)

    val strRes =  JSONUtil.toJSONStr(logItemId)
    logger.debug("kgq: ---------------------------strRes:" + strRes)
//    var url =  "http://localhost:9000/assets/html/log_form.scala.html?isUpdate=true&entityId="+logItemId
    Ok(strRes).as("application/json")
  }

}
